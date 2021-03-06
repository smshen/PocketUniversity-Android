package com.xyhui.activity.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.MobileApp;
import com.xyhui.utils.DownloadImpl;

public class MobileZoneList extends CListView {

	public MobileZoneList(PullToRefreshListView lv, Activity activity) {
		super(lv, activity);
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		this.setListItemResource(R.layout.list_item_mobileapp);
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;
		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多应用");
		super.ensureUi();
		super.setGetMoreClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getmoreListViewStart();
			}
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		final MobileApp item = (MobileApp) obj;
		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		LVP.add(new CListViewParam(R.id.text_title, item.mAppName, true));

		CListViewParam avatarLVP = new CListViewParam(R.id.img_app, R.drawable.img_default, true);
		avatarLVP.setImgAsync(true);
		avatarLVP.setItemTag(item.mIconUrl);
		LVP.add(avatarLVP);

		final boolean installed = PuApp.get().isInstalled(item);
		String isInstalled;
		if (installed) {
			isInstalled = "打开";
		} else {
			isInstalled = "下载";

		}

		CListViewParam downloadLVP = new CListViewParam(R.id.btn_download, isInstalled, true);

		downloadLVP.setOnclickLinstener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (installed) {
					launchApp(item);
				} else {
					new AlertDialog.Builder(mActivity).setTitle("下载").setMessage("点击确定开始下载")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									downloadApp(item);
								}
							}).setNeutralButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									dialog.dismiss();
								}
							}).setCancelable(false).show();
				}
			}
		});

		LVP.add(downloadLVP);

		return LVP;
	}

	@Override
	public void asyncData() {
		super.asyncData();
		new Api(callback, mActivity).partner(PuApp.get().getToken(), mPerpage, page);
	}

	ListCallBack<ArrayList<MobileApp>> callback = new ListCallBack<ArrayList<MobileApp>>(
			MobileZoneList.this) {
		@Override
		public void setType() {
			myType = new TypeToken<ArrayList<MobileApp>>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (mT != null && !mT.isEmpty()) {
				for (int i = 0; i < mT.size(); i++) {
					mListItems.add(mT.get(i));
				}
			} else {
				NotificationsUtil.ToastTopMsg(mActivity, "没有相关信息");
				return;
			}
		}
	};

	private void launchApp(MobileApp app) {
		String packageName = app.mPkgName;
		Intent mIntent = mActivity.getPackageManager().getLaunchIntentForPackage(packageName);

		if (mIntent != null) {
			try {
				mActivity.startActivity(mIntent);
			} catch (ActivityNotFoundException err) {

			}
		}
	}

	private void downloadApp(MobileApp app) {
		String appName = app.mAppName;
		String filename = "139" + appName + ".apk";

		DownloadImpl download = new DownloadImpl(mActivity, app.mDownloadUrl, appName, filename);
		download.startDownload();
	}
}
