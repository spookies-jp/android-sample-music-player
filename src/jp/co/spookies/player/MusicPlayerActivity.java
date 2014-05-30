package jp.co.spookies.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MusicPlayerActivity extends Activity {

	private IMusicPlayerService musicPlayerService;
	private final MusicPlayerReceiver receiver = new MusicPlayerReceiver(this);

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			musicPlayerService = IMusicPlayerService.Stub.asInterface(service);
			onReceiveChanged();
		}

		public void onServiceDisconnected(ComponentName name) {
			musicPlayerService = null;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// サービスを開始
		Intent intent = new Intent(IMusicPlayerService.class.getName());
		startService(intent);

		IntentFilter filter = new IntentFilter(MusicPlayerService.ACTION);
		registerReceiver(receiver, filter);

		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
		unregisterReceiver(receiver);
	}

	/**
	 * アートワークのクリック
	 * 
	 * @param view
	 */
	public void onArtworkClicked(View view) {

		LayoutInflater inflater = LayoutInflater.from(MusicPlayerActivity.this);
		View dialogView = inflater.inflate(R.layout.dialog, null);
		final EditText editText = (EditText) dialogView
				.findViewById(R.id.editText1);

		AlertDialog.Builder dialog = new AlertDialog.Builder(
				MusicPlayerActivity.this);
		dialog.setTitle("Search Title");
		dialog.setIcon(R.drawable.icon);
		dialog.setView(dialogView);
		dialog.setPositiveButton("Search",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent();
						i.setClass(getApplicationContext(),
								MusicListActivity.class);
						i.putExtra("s", editText.getText().toString());
						startActivity(i);
					}
				});
		dialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	public void onButtonPrevClicked(View view) {
		try {
			if (musicPlayerService.playing() == null) {
				onArtworkClicked(view);
				return;
			}
			musicPlayerService.previous();
		} catch (RemoteException e) {
		}
	}

	public void onButtonPlayClicked(View view) {

		try {
			if (musicPlayerService.playing() == null) {
				onArtworkClicked(view);
				return;
			}
			musicPlayerService.playOrPause();

			ImageView playButton = (ImageView) findViewById(R.id.button_play);
			if (musicPlayerService.isPlaying()) {
				playButton.setImageResource(R.drawable.button_pause);
			} else {
				playButton.setImageResource(R.drawable.button_play);
			}
		} catch (RemoteException e) {
		}
	}

	public void onButtonNextClicked(View view) {
		try {
			if (musicPlayerService.playing() == null) {
				onArtworkClicked(view);
				return;
			}
			musicPlayerService.next();
		} catch (RemoteException e) {
		}
	}

	public void onReceiveChanged() {
		if (musicPlayerService == null)
			return;

		try {

			Song song = musicPlayerService.playing();

			if (song == null)
				return;

			// アートワーク変更
			Drawable d = song.getImage(MusicPlayerActivity.this);
			ImageView artwork = (ImageView) findViewById(R.id.artwork);
			if (d != null) {
				artwork.setImageDrawable(d);
			} else {
				artwork.setImageResource(R.drawable.no_image);
			}
			ImageView playButton = (ImageView) findViewById(R.id.button_play);
			playButton.setImageResource(R.drawable.button_pause);

			// タイトル表示
			Toast toast = Toast.makeText(getApplicationContext(),
					song.getTitle(), Toast.LENGTH_LONG);
			toast.show();
		} catch (Exception e) {
		}
	}

}