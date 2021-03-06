package com.xyhui.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.mslibs.utils.VolleyLog;

public class ImageUtil {
	private Activity mActivity;

	private int mPicWidth;
	private int mPicHeight;

	private File mPicFile;
	private File mPicFileTemp;

	public static final int CAPTURE_PIC = 0;
	public static final int HANDLE_PIC = 1;

	public static final String DIR_NAME = "pu";

	public ImageUtil(Activity activity, int picWidth, int picHeight, String picName,
			String picNameTemp) {
		setActivity(activity);
		setPicWidth(picWidth);
		setPicHeight(picHeight);
		setPicFile(createImageFile(DIR_NAME, picName));
		setPicFileTemp(createImageFile(DIR_NAME, picNameTemp));
	}

	public Activity getActivity() {
		return mActivity;
	}

	public void setActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	public int getPicWidth() {
		return mPicWidth;
	}

	public void setPicWidth(int mPicWidth) {
		this.mPicWidth = mPicWidth;
	}

	public int getPicHeight() {
		return mPicHeight;
	}

	public void setPicHeight(int mPicHeight) {
		this.mPicHeight = mPicHeight;
	}

	public File getPicFile() {
		return mPicFile;
	}

	public void setPicFile(File mPicFile) {
		this.mPicFile = mPicFile;
	}

	public File getPicFileTemp() {
		return mPicFileTemp;
	}

	public void setPicFileTemp(File mPicFileTemp) {
		this.mPicFileTemp = mPicFileTemp;
	}

	public void deleteFile() {
		if (mPicFile.exists()) {
			mPicFile.delete();
		}
	}

	private File createImageFile(String path, String fileName) {
		File file = new File(Environment.getExternalStorageDirectory().getPath(), path);

		if (!file.exists()) {
			file.mkdirs();
		}

		VolleyLog.d(file.getAbsolutePath());
		File f = new File(file, fileName);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	/**
	 * 拍照后,裁剪图片
	 * 
	 * @param uri
	 */
	public void cropPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", mPicWidth);
		intent.putExtra("outputY", mPicHeight);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPicFile));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false);
		mActivity.startActivityForResult(intent, HANDLE_PIC);
	}

	/**
	 * 直接从图库裁剪图片
	 */
	public void getAndCropPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", mPicWidth);
		intent.putExtra("outputY", mPicHeight);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPicFile));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false); // no face detection
		mActivity.startActivityForResult(intent, HANDLE_PIC);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	public void setPicToView(Context context, ImageView view, Uri uri) {
		Bitmap photo = decodeUriAsBitmap(context, uri);

		if (null != photo) {
			view.setImageBitmap(photo);
		}
	}

	private Bitmap decodeUriAsBitmap(Context context, Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}
}
