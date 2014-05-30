package jp.co.spookies.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicPlayerReceiver extends BroadcastReceiver {

	private MusicPlayerActivity musicPlayerActivity;

	public MusicPlayerReceiver(MusicPlayerActivity activity) {
		musicPlayerActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		musicPlayerActivity.onReceiveChanged();
	}

}
