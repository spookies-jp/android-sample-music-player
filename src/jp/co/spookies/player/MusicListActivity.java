package jp.co.spookies.player;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MusicListActivity extends ListActivity {

	private IMusicPlayerService musicPlayerService;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		/**
		 * サービスに接続したときに叩かれるメソッド.
		 */
		public void onServiceConnected(ComponentName name, IBinder service) {
			musicPlayerService = IMusicPlayerService.Stub.asInterface(service);
		}

		/**
		 * サービスから切断したときに叩かれるメソッド.
		 */
		public void onServiceDisconnected(ComponentName name) {
			musicPlayerService = null;
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.list);

		Intent searcher = getIntent();
		String s = searcher.getStringExtra("s");
		s = s.replaceAll("%", "\\%");
		s = s.replaceAll("_", "\\_");
		s = "%" + s + "%";

		Intent intent = new Intent(IMusicPlayerService.class.getName());
		// startService( intent );
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ?",
				new String[] { s }, null);

		ListView listView = getListView();
		listView.setScrollingCacheEnabled(false);
		List<Song> songs = new ArrayList<Song>();
		while (cursor.moveToNext()) {
			Song song = new Song();

			song.setTitle(cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			song.setArtist(cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
			song.setAlbum(cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));

			song.setDataUri(cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			song.setAlbumId(cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));

			songs.add(song);
		}

		SongListAdapter adapter = new SongListAdapter(this, R.layout.song,
				songs);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new SongClickListener());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	class SongListAdapter extends ArrayAdapter<Song> {
		private List<Song> songs = null;
		private LayoutInflater inflater = null;

		public SongListAdapter(Context context, int textViwResourceId,
				List<Song> items) {
			super(context, textViwResourceId);
			this.songs = items;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int i = 0; i < items.size(); i++) {
				add(items.get(i));
			}
		}

		public List<Song> getSongs() {
			return songs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			// ビューにレイアウトを設定
			if (view == null) {
				view = inflater.inflate(R.layout.song, null);
			}

			if (songs.size() <= position)
				return view;

			Song song = songs.get(position);

			if (song != null) {
				TextView textTitle = (TextView) view.findViewById(R.id.title);
				textTitle.setText(song.getTitle());
				TextView textArtist = (TextView) view.findViewById(R.id.artist);
				textArtist.setText(song.getArtist());
				TextView textAlbum = (TextView) view.findViewById(R.id.album);
				textAlbum.setText(song.getAlbum());

				Drawable d = song.getImage(MusicListActivity.this);
				ImageView artWork = (ImageView) view
						.findViewById(R.id.art_work);
				if (d != null) {
					artWork.setImageDrawable(d);
				} else {
					artWork.setImageResource(R.drawable.icon_list);
				}
			}
			return view;
		}
	}

	class SongClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> adepter, View view,
				int position, long id) {

			SongListAdapter songAdapter = (SongListAdapter) adepter
					.getAdapter();
			List<Song> songs = songAdapter.getSongs();
			try {
				musicPlayerService.playList(songs, position);
			} catch (RemoteException e) {
			}

			finish();
		}
	}
}
