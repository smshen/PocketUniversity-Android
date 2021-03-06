package com.xyhui.utils;

import java.io.File;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.mslibs.utils.VolleyLog;
import com.xyhui.activity.PuApp;

public class PuAppConfig {
	private BroadcastReceiver OnDownloadDonereceiver;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void registReceiver() {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			OnDownloadDonereceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

						VolleyLog.d("ACTION_DOWNLOAD_COMPLETE");
						Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
						VolleyLog.d("got download id: %d", dwnId);
						DownloadManager manager = (DownloadManager) PuApp.get().getSystemService(
								Context.DOWNLOAD_SERVICE);

						Query query = new Query();
						query.setFilterById(dwnId);
						Cursor cur = manager.query(query);

						if (cur.moveToFirst()) {
							int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
							int status = cur.getInt(columnIndex);

							VolleyLog.d("got cur.getInt(%d): %d", columnIndex, status);
							if (DownloadManager.STATUS_SUCCESSFUL == status) {
								String localUriString = cur.getString(cur
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

								File mFile = new File(Uri.parse(localUriString).getPath());

								if (!mFile.exists()) {
									return;
								}

								String filename = mFile.getName().toString();

								VolleyLog.d("got file: %s", mFile.getAbsolutePath());

								if (filename.endsWith(".apk")) {
									PuApp.get().installPackage(mFile);
								}
							}
						}
					}
				}
			};

			PuApp.get().registerReceiver(OnDownloadDonereceiver,
					new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		}
	}

	public void unregistReceiver() {
		if (OnDownloadDonereceiver != null) {
			PuApp.get().unregisterReceiver(OnDownloadDonereceiver);
		}
	}
}
