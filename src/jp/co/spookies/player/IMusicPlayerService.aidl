package jp.co.spookies.player;

import jp.co.spookies.player.Song;

interface IMusicPlayerService {

	Song next();
	Song playOrPause();
	Song previous();
	Song playAt(int index);
	void play(in Song song);
	Song playList(in List<Song> song, int index);
	void stop();
	Song playing();
	boolean isPlaying();
}
