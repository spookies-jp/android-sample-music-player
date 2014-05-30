package jp.co.spookies.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class Song implements Parcelable {

	private String title = null;
	private String album = null;
	private String artist = null;

	private String albumId = null;
	private String dataUri = null;

	public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
		public Song createFromParcel(Parcel in) {
			return new Song(in);
		}

		public Song[] newArray(int size) {
			return new Song[size];
		}
	};

	public Song() {
		super();
	}

	private Song(Parcel in) {
		title = in.readString();
		album = in.readString();
		artist = in.readString();
		albumId = in.readString();
		dataUri = in.readString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDataUri() {
		return dataUri;
	}

	public void setDataUri(String dataUri) {
		this.dataUri = dataUri;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
	}

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public Drawable getImage(Activity activity) {

		// アルバムIDと紐付くアルバム情報を検索
		Cursor albumCursor = activity.managedQuery(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Albums._ID + "=?",
				new String[] { getAlbumId() }, null);

		if (albumCursor.moveToFirst()) {
			// アルバム画像ファイル
			int albumArtIndex = albumCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
			String albumArt = albumCursor.getString(albumArtIndex);
			if (albumArt != null) {
				try {

					File artWorkFile = new File(albumArt);
					InputStream in = new FileInputStream(artWorkFile);
					Drawable d = Drawable.createFromStream(in, null);

					return d;
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

}
