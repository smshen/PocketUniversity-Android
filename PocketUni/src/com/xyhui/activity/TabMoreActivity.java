package com.xyhui.activity;

import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.activity.more.AboutActivity;
import com.xyhui.activity.more.AccountManageActivity;
import com.xyhui.activity.more.QRCardViewActivity;
import com.xyhui.activity.more.QRCodeScanActivity;
import com.xyhui.activity.weibo.UserHomePageActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Banner;
import com.xyhui.types.Response;
import com.xyhui.utils.DownloadImpl;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.widget.AdBannerLayout;
import com.xyhui.widget.FLTabActivity;

public class TabMoreActivity extends FLTabActivity {

	private final String HELP_URL = "http://pocketuni.net/puhelp.html";

	private LinearLayout account_layout;
	private LinearLayout homepage_layout;
	private LinearLayout qrcard_layout;
	private LinearLayout qrscan_layout;
	private LinearLayout question_layout;
	private LinearLayout about_layout;
	private LinearLayout update_layout;
	private Button btn_signout;
	private TextView text_version;
	private AdBannerLayout ad_banner;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_tab_more);

		account_layout = (LinearLayout) findViewById(R.id.account_layout);
		homepage_layout = (LinearLayout) findViewById(R.id.homepage_layout);
		qrcard_layout = (LinearLayout) findViewById(R.id.qrcard_layout);
		qrscan_layout = (LinearLayout) findViewById(R.id.qrscan_layout);
		question_layout = (LinearLayout) findViewById(R.id.question_layout);
		about_layout = (LinearLayout) findViewById(R.id.about_layout);
		update_layout = (LinearLayout) findViewById(R.id.update_layout);
		text_version = (TextView) findViewById(R.id.text_version);
		btn_signout = (Button) findViewById(R.id.btn_signout);

		ad_banner = (AdBannerLayout) findViewById(R.id.ad_banner);
	}

	@Override
	public void bindListener() {

		account_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 账号管理
				Intent intent = new Intent(mActivity, AccountManageActivity.class);
				startActivity(intent);
			}
		});
		homepage_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 个人首页
				String user_id = new PrefUtil().getPreference(Params.LOCAL.UID);

				Intent intent = new Intent(mActivity, UserHomePageActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.USER_ID, user_id);
				startActivity(intent);
			}
		});
		qrcard_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 二维码名片
				Intent intent = new Intent(mActivity, QRCardViewActivity.class);
				startActivity(intent);
			}
		});
		qrscan_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 二维码扫描
				Intent intent = new Intent(mActivity, QRCodeScanActivity.class);
				startActivity(intent);
			}
		});
		question_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 常见问题
				Intent intent = new Intent(mActivity, WebViewActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TITLE, "常见问题");
				intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL, HELP_URL);
				startActivity(intent);
			}
		});
		about_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 关于PocketUni
				Intent intent = new Intent(mActivity, AboutActivity.class);
				startActivity(intent);
			}
		});
		update_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 检测新版本
				showProgress();
				new Api(callback, mActivity).getVersion();
			}
		});
		btn_signout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 注销账号
				new AlertDialog.Builder(mActivity).setTitle("确定要注销吗?")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								NotificationsUtil.ToastBottomMsg(mActivity, "正在注销...");
								setAliasAndTags("", new HashSet<String>());
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

							}
						}).show();
			}
		});
	}

	@Override
	public void ensureUi() {
		ad_banner.init(Banner.TYPE_MORE);

		// 显示当前版本号
		String v = getVersion();
		text_version.setText("当前版本：" + v);
	}

	protected void onResume() {
		super.onResume();
		ad_banner.reload();
	}

	protected void onPause() {
		super.onPause();
		ad_banner.pause();
	}

	private void setAliasAndTags(final String alias, final Set<String> tags) {
		JPushInterface.setAliasAndTags(PuApp.get(), alias, tags, new TagAliasCallback() {
			@Override
			public void gotResult(int arg0, String arg1, Set<String> arg2) {
				VolleyLog.d("ResultCode: %d Alias: %s Tags: %s", arg0, arg1, arg2.toString());
				if (Params.JPush.STATE_OK == arg0) {
					PuApp.get().logout(mActivity);
				} else {
					NotificationsUtil.ToastBottomMsg(mActivity, "暂时无法注销，请检查您的网络连接");
				}
			}
		});
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			Response r = JSONUtils.fromJson(response, Response.class);

			if (r == null) {
				return;
			}

			final String version = r.version;
			final String url = r.response;
			String current_version = getVersion();

			VolleyLog.d("version: %s \n current_version: %s", version, current_version);

			if (TextUtils.isEmpty(version) || TextUtils.isEmpty(url)
					|| current_version.compareTo(version) >= 0) {
				NotificationsUtil.ToastBottomMsg(mActivity, "当前是最新版本");
			} else {
				new AlertDialog.Builder(TabMoreActivity.this).setTitle("升级应用程序")
						.setMessage(r.msg)
						.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

								DownloadImpl download = new DownloadImpl(mActivity, url,
										"PocketUni客户端", String.format("PocketUni_%s.apk", version));
								download.startDownload();
							}
						}).setNeutralButton("暂不升级", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();
							}
						}).show();
			}
		}
	};
}
