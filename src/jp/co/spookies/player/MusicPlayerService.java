package jp.co.spookies.player;

import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

public class MusicPlayerService extends Service {

	public static final String ACTION = "Music Player Service";

	private NotificationManager notificationManager;
	private MediaPlayer player = new MediaPlayer();
	private List<Song> songs = null;
	private int cursor = 0;

	public MediaPlayer getPlayer() {
		return player;
	}

	public void setPlayer(MediaPlayer player) {
		this.player = player;
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public IMusicPlayerService.Stub getMusicPlayerSercice() {
		return musicPlayerSercice;
	}

	public void setMusicPlayerSercice(
			IMusicPlayerService.Stub musicPlayerSercice) {
		this.musicPlayerSercice = musicPlayerSercice;
	}

	private IMusicPlayerService.Stub musicPlayerSercice = new IMusicPlayerService.Stub() {

		/**
		 * 曲を指定して再生
		 * 
		 * @param song
		 */
		public void play(Song song) {

			try {

				// 曲の再生
				MediaPlayer player = getPlayer();
				if (player.isPlaying()) {
					player.stop();
				}
				player.reset();
				player.setDataSource(getApplicationContext(),
						Uri.parse(song.getDataUri()));
				player.prepare();
				player.start();
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						next();
					}
				});

				// notify android
				notifyPlaying();

				// Broadcast
				Intent intent = new Intent(ACTION);
				intent.putExtra("start_song", true);
				sendBroadcast(intent);
			} catch (IOException e) {
			}
		}

		public Song playList(List<Song> songs, int index) {
			setSongs(songs);
			return playAt(index);
		}

		public Song playAt(int index) {
			setCursor(index);
			List<Song> songs = getSongs();
			Song song = songs.get(index);
			play(song);
			return song;
		}

		public Song previous() {
			int cursor = getCursor();
			cursor -= 1;
			if (cursor < 0)
				cursor = songs.size() - 1;
			return playAt(cursor);
		}

		public Song playOrPause() {
			MediaPlayer player = getPlayer();
			if (player.isPlaying()) {
				player.pause();
				notifyCancel();

			} else {
				player.start();
				notifyPlaying();
			}

			return playing();
		}

		public Song next() {
			int cursor = getCursor();
			cursor += 1;
			if (cursor >= songs.size()) {
				cursor = 0;
			}
			return playAt(cursor);
		}

		public Song playing() {
			List<Song> songs = getSongs();
			if (songs == null || songs.size() == 0)
				return null;
			return songs.get(getCursor());
		}

		public void stop() {
			stopSelf();
		}

		public boolean isPlaying() {
			MediaPlayer player = getPlayer();
			return player.isPlaying();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		if (IMusicPlayerService.class.getName().equals(intent.getAction())) {
			return musicPlayerSercice;
		}

		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (player != null) {
			player.stop();
			player.release();
			player = null;
		}
		notifyCancel();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	/**
	 * notify playing
	 */
	public void notifyPlaying() {

		CharSequence songTitle = getText(R.string.app_name);
		try {
			Song song = musicPlayerSercice.playing();
			songTitle = song.getTitle();
		} catch (Exception e) {
		}

		Notification n = new Notification();
		n.icon = R.drawable.icon;
		n.tickerText = "Relax Player";
		n.when = System.currentTimeMillis();
		Intent i = new Intent(getApplicationContext(),
				MusicPlayerActivity.class);
		PendingIntent pend = PendingIntent.getActivity(getApplicationContext(),
				0, i, 0);
		n.setLatestEventInfo(getApplicationContext(), songTitle,
				getText(R.string.app_name), pend);
		n.flags = Notification.FLAG_ONGOING_EVENT;
		setForeground(true);
		notificationManager.notify(R.string.app_name, n);
	}

	public void notifyCancel() {
		notificationManager.cancel(R.string.app_name);
	}
}
