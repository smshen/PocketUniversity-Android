package com.xyhui.activity.more;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.QrCode;
import com.xyhui.types.Response;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class QRCodeResultActivity extends FLActivity {
	private Button btn_back;
	private TextView text_result;
	private LinearLayout btn_addfriend;
	private LinearLayout btn_eventcheckin;
	private ImageView img_scan_result;

	private int mCodeType;
	private QrCode mQrCode;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.QRCODE)) {
			mQrCode = getIntent().getParcelableExtra(Params.INTENT_EXTRA.QRCODE);
			mCodeType = mQrCode.type;
		} else {
			finish();
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_qrcode_result);

		btn_back = (Button) findViewById(R.id.btn_back);
		text_result = (TextView) findViewById(R.id.text_result);
		btn_addfriend = (LinearLayout) findViewById(R.id.btn_addfriend);
		btn_eventcheckin = (LinearLayout) findViewById(R.id.btn_eventcheckin);
		img_scan_result = (ImageView) findViewById(R.id.img_scan_result);
	}

	@Override
	public void bindListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 关闭返回
				finish();
			}
		});

		btn_addfriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 加关注
				btn_addfriend.setEnabled(false);
				showProgress();
				new Api(callback, mActivity).follow(PuApp.get().getToken(), mQrCode.id);

			}
		});

		btn_eventcheckin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 活动签到
				showProgress();
				btn_eventcheckin.setEnabled(false);
				if (mCodeType == QrCode.EVENTCHECKIN) {
					new Api(callback, mActivity).userAttend(PuApp.get().getToken(), mQrCode.id);
				} else if (mCodeType == QrCode.ADMINEVENTCHECKIN) {
					new Api(callback, mActivity).adminAttend(PuApp.get().getToken(), mQrCode.code,
							mQrCode.id);
				}
			}
		});
	}

	@Override
	public void ensureUi() {
		String title = "";
		switch (mCodeType) {
		case QrCode.ADDFRIEND:
			btn_addfriend.setVisibility(View.VISIBLE);
			btn_eventcheckin.setVisibility(View.GONE);
			title = "关注好友";
			break;
		case QrCode.EVENTCHECKIN:
			btn_addfriend.setVisibility(View.GONE);
			btn_eventcheckin.setVisibility(View.VISIBLE);
			title = "签到活动";
			break;
		case QrCode.ADMINEVENTCHECKIN:
			showProgress();
			new Api(checkedCallback, mActivity).isChecked(PuApp.get().getToken(), mQrCode.code,
					mQrCode.id);
			break;
		default:
			btn_addfriend.setVisibility(View.GONE);
			btn_eventcheckin.setVisibility(View.GONE);
			title = mQrCode.code;
		}

		if (!TextUtils.isEmpty(mQrCode.title)) {
			title += mQrCode.title;
		}

		text_result.setText(title);
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			if (mCodeType == QrCode.EVENTCHECKIN || mCodeType == QrCode.ADMINEVENTCHECKIN) {
				Response result = JSONUtils.fromJson(response, Response.class);
				if (null != result) {
					if (result.status == 1) {
						text_result.setText("签到成功");
						img_scan_result.setVisibility(View.VISIBLE);
						btn_eventcheckin.setVisibility(View.GONE);
						img_scan_result.setImageResource(R.drawable.scan_result_ok);
						finish();
					} else {
						String message;
						if (!TextUtils.isEmpty(result.msg)) {
							message = result.msg;
						} else {
							message = "签到失败，请联系管理员";
						}

						new AlertDialog.Builder(QRCodeResultActivity.this)
								.setIcon(R.drawable.scan_result_error).setTitle(message)
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										finish();
									}
								}).create().show();
					}
				}
			} else {
				NotificationsUtil.ToastBottomMsg(mActivity, "操作成功");
				finish();
			}
		}
	};

	CallBack checkedCallback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			Response result = JSONUtils.fromJson(response, Response.class);
			if (null != result) {
				if (result.status == 0) {
					btn_addfriend.setVisibility(View.GONE);
					btn_eventcheckin.setVisibility(View.GONE);
					text_result.setText(result.msg);
				} else {
					btn_addfriend.setVisibility(View.GONE);
					btn_eventcheckin.setVisibility(View.VISIBLE);
					text_result.setText("管理员签到活动");
				}
			}
		}
	};
}
