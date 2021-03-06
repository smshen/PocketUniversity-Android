package com.xyhui.utils;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;

public class DownloadImpl {
	private String mUrl;
	private Context mContext;
	private String mFileDescription;
	private String mFileName;

	public DownloadImpl(Context context, String url, String fileDescription, String fileName) {
		mContext = context;
		mUrl = url;
		mFileDescription = fileDescription;
		mFileName = fileName;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void startDownload() {
		if (!storageAvailableForDownload()) {
			NotificationsUtil.ToastTopMsg(mContext, "sd卡当前不可用或者空间不足");
			return;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			downloadAsDefault();
		} else {
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));
			request.setDescription(String.format("正在下载%s,请稍候", mFileDescription));
			request.setTitle(mFileDescription);

			String downloaddir = Environment.DIRECTORY_DOWNLOADS;
			if (downloaddir.contains("://")) {
				downloaddir = "download";
			}

			VolleyLog.d("url=%s dir=%s filename=%s", mUrl, downloaddir, mFileName);

			Environment.getExternalStoragePublicDirectory(downloaddir).mkdir();
			request.setDestinationInExternalPublicDir(downloaddir, mFileName);
			DownloadManager manager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
			try {
				manager.enqueue(request);
				NotificationsUtil.ToastLongMessage(mContext, "开始下载");
			} catch (Exception e) {
				VolleyLog.e(e, "");
				downloadAsDefault();
			}
		}
	}

	private void downloadAsDefault() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(mUrl));
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	/**
	 * Checks whether the space is enough for download.
	 * 
	 * @param current
	 *            the download needs to check whether the space is enough
	 * @return
	 */
	private boolean storageAvailableForDownload() {
		// Make sure the space can satisfy the current download, so
		// we will add the size of running downloads.
		long neededspace = Storage.APK_SIZE + Storage.RESERVED_SPACE;

		// Calculate the needed space for current download.
		long cacheSpace = Storage.cachePartitionAvailableSpace();
		long dataSpace = Storage.dataPartitionAvailableSpace();
		long externalStorageSpace = Storage.externalStorageAvailableSpace();

		VolleyLog.d("[Needed space : %s], Cache partition space : %s, "
				+ "Data partition space : %s, External storage space : %s",
				Storage.readableSize(neededspace), Storage.readableSize(cacheSpace),
				Storage.readableSize(dataSpace), Storage.readableSize(externalStorageSpace));

		if (Storage.externalStorageAvailable()) {
			if (externalStorageSpace < neededspace) {
				return false;
			}
		} else if (dataSpace < neededspace) {
			return false;
		}

		return true;
	}
}
