package com.comic.seexian.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.comic.seexian.Loge;

public class BitmapUtils {

	public static final int TARGET_PHOTO_WIDTH = 1080;

	public static Bitmap compressBitmap(Bitmap originBitmap) {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		originBitmap.compress(Bitmap.CompressFormat.JPEG, 30, ostream);
		byte[] byteArray = ostream.toByteArray();

		Bitmap compressedBitmap = null;

		if (byteArray.length > 0) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;

			BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, opts);

			int oriW = opts.outWidth;
			Loge.i("origin Width = " + oriW);

			int sampleSize = (int) (oriW / TARGET_PHOTO_WIDTH);

			Loge.i("sampleSize = " + sampleSize);

			if (sampleSize < 1) {
				sampleSize = 1;
			}

			opts.inSampleSize = sampleSize;
			opts.inJustDecodeBounds = false;

			compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0,
					byteArray.length, opts);

		}

		return compressedBitmap;
	}

	public static Bitmap getBitmapFromUri(Context ctx, Uri uri) {
		ContentResolver contentResolver = ctx.getContentResolver();
		Bitmap bitmap = null;
		if (contentResolver != null) {
			try {
				bitmap = MediaStore.Images.Media
						.getBitmap(contentResolver, uri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

}
