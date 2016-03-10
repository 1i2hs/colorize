package edu.skku.inho.colorize.CroppingBackgroundPage;

import com.bumptech.glide.load.Key;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Created by XEiN on 3/10/16.
 */
public class BackgroundImageFileChangedDateSignature implements Key {
	public long mCurrentChangedDate;

	public BackgroundImageFileChangedDateSignature(long currentChangedDate) {
		mCurrentChangedDate = currentChangedDate;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BackgroundImageFileChangedDateSignature) {
			BackgroundImageFileChangedDateSignature other = (BackgroundImageFileChangedDateSignature) o;
			return mCurrentChangedDate == other.mCurrentChangedDate;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) mCurrentChangedDate;
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
		messageDigest.update(ByteBuffer.allocate(Integer.SIZE).putLong(mCurrentChangedDate).array());
	}
}
