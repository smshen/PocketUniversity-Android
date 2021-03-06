package com.xyhui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Response;
import com.xyhui.utils.DownloadImpl;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.widget.FLActivity;

public class LoadingActivity extends FLActivity {
	/**
	 * 界面跳转的重试时间（防止要用到的数据未加载完全）
	 */
	private final int RETRY_DURATION = 500;

	private ImageView img_loading_bg;
	private TextView tv_versioninfo;

	private String mNewVersionUrl;
	private String mCurrent_version;
	private boolean mIsFirstLaunch;
	private PrefUtil mPrefUtil;
	private Response mVersionInfo;

	@Override
	public void init() {
		mPrefUtil = new PrefUtil();
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_loading);

		img_loading_bg = (ImageView) findViewById(R.id.img_loading_bg);
		tv_versioninfo = (TextView) findViewById(R.id.tv_versioninfo);
	}

	@Override
	public void ensureUi() {
		mCurrent_version = getVersion();
		String oldVersion = mPrefUtil.getPreference(Params.LOCAL.VERSION);

		if (null == oldVersion || mCurrent_version.compareTo(oldVersion) != 0) {
			mIsFirstLaunch = true;
			mPrefUtil.setPreference(Params.LOCAL.VERSION, mCurrent_version);
		} else {
			mIsFirstLaunch = false;
		}

		tv_versioninfo.setText(String.format("V %s", mCurrent_version));

		// 检测是否首次启动 默认 isFirstLaunch = true
		if (mIsFirstLaunch) {
			img_loading_bg.setVisibility(View.VISIBLE);
			img_loading_bg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new Api(callback, mActivity).getVersion();
				}
			});
		} else {
			img_loading_bg.setVisibility(View.GONE);
			new Api(callback, mActivity).getVersion();
		}

		PuApp.get().getLocalDataMgr().setCitys(mActivity);
		PuApp.get().getLocalDataMgr().setSchools(mActivity);

		if (PuApp.get().isLogon()) {
			PuApp.get().getLocalDataMgr().setEventCats(mActivity);
			PuApp.get().getLocalDataMgr().setGroupCats(mActivity);
			PuApp.get().getLocalDataMgr().setUserData(mActivity);
		}
	}

	public void signinOrMain() {
		img_loading_bg.postDelayed(new Runnable() {
			public void run() {
				VolleyLog.d(System.currentTimeMillis() / 1000 + "");
				if (PuApp.get().isLogon()) {
					// TODO 强制用户从新登陆，添加客户端信息(推送),未来的版本去掉
					if (mIsFirstLaunch) {
						PuApp.get().logout(mActivity);
					} else if (PuApp.get().getLocalDataMgr().allDataArrived()) {
						Intent intent = new Intent();
						if (!PuApp.get().getLocalDataMgr().isVerified()) {
							intent.setClass(mActivity, AccountBindingActivity.class);
						} else if (!PuApp.get().getLocalDataMgr().isInited()) {
							intent.setClass(mActivity, UserInitActivity.class);
						} else {
							intent.setClass(mActivity, MainActivity.class);
						}
						startActivity(intent);
						finish();
					}
				} else if (PuApp.get().getLocalDataMgr().citySchoolArrived()) {
					Intent intent = new Intent(mActivity, SigninActivity.class);
					startActivity(intent);
					finish();
				} else {
					signinOrMain();
				}
			}
		}, RETRY_DURATION);
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			mVersionInfo = JSONUtils.fromJson(response, Response.class);

			if (null != mVersionInfo) {
				String version = mVersionInfo.version;
				mNewVersionUrl = mVersionInfo.response;

				if (TextUtils.isEmpty(version) || TextUtils.isEmpty(mNewVersionUrl)
						|| mCurrent_version.compareTo(version) >= 0) {
					signinOrMain();
				} else {
					new AlertDialog.Builder(LoadingActivity.this).setTitle("升级应用程序")
							.setMessage(mVersionInfo.msg)
							.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									signinOrMain();
									DownloadImpl download = new DownloadImpl(mActivity,
											mNewVersionUrl, "PocketUni客户端", String.format(
													"PocketUni_%s.apk", mVersionInfo.version));
									download.startDownload();
								}
							}).setNeutralButton("暂不升级", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									signinOrMain();
								}
							}).setCancelable(false).show();
				}
			} else {
				signinOrMain();
			}
		}
	};
}
