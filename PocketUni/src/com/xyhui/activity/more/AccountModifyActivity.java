package com.xyhui.activity.more;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Response;
import com.xyhui.types.User;
import com.xyhui.utils.ImageUtil;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.utils.Storage;
import com.xyhui.widget.FLActivity;

public class AccountModifyActivity extends FLActivity {
	private final String PIC_NAME = "pu_avatar.jpg";
	private final String PIC_NAME_TEMP = "pu_avatar_temp.jpg";
	private final int PIC_WIDTH = 150;
	private final int PIC_HEIGHT = 150;

	private Button btn_back;
	private Button btn_submit;
	private ImageView img_avatar;
	private EditText edit_nickname;
	private RadioButton radio_boy;
	private RadioButton radio_girl;

	private ImageUtil mImageUtil;
	private User mUser;
	private String mNickName;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.USER)) {
			mUser = getIntent().getParcelableExtra(Params.INTENT_EXTRA.USER);
		}

		mImageUtil = new ImageUtil(this, PIC_WIDTH, PIC_HEIGHT, PIC_NAME, PIC_NAME_TEMP);
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_account_baseinfo);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_submit = (Button) findViewById(R.id.btn_submit);

		img_avatar = (ImageView) findViewById(R.id.img_avatar);
		edit_nickname = (EditText) findViewById(R.id.edit_nickname);

		radio_boy = (RadioButton) findViewById(R.id.radio_boy);
		radio_girl = (RadioButton) findViewById(R.id.radio_girl);
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

		img_avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowPickDialog();
			}
		});

		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mNickName = edit_nickname.getText().toString().trim();
				String sex = "0";
				if (radio_boy.isChecked()) {
					sex = "1";
				}
				showProgress();
				new Api(callback, mActivity).profileupdate(PuApp.get().getToken(), mNickName, sex,
						mImageUtil.getPicFile());
			}
		});
	}

	@Override
	public void ensureUi() {
		// 初始化信息
		UrlImageViewHelper.setUrlDrawable((ImageView) img_avatar, mUser.face, R.drawable.avatar00);
		edit_nickname.setText(mUser.uname);
		boolean isboy = false;
		if ("男".equalsIgnoreCase(mUser.sex)) {
			isboy = true;
		}
		radio_boy.setChecked(isboy);
		radio_girl.setChecked(!isboy);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageUtil.deleteFile();
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			Response r = JSONUtils.fromJson(response, Response.class);

			if (null != r) {
				if (!TextUtils.isEmpty(r.response)) {
					// 清除头像图片缓存
					UrlImageViewHelper.remove_(mActivity, mUser.face);

					UrlImageViewHelper.setUrlDrawable((ImageView) img_avatar, mUser.face,
							R.drawable.avatar00);
					new PrefUtil().setPreference(Params.LOCAL.NICKNAME, mNickName);
					NotificationsUtil.ToastBottomMsg(mActivity, r.response);
				} else if (!TextUtils.isEmpty(r.message)) {
					NotificationsUtil.ToastBottomMsg(mActivity, r.message);
				}
			}
		}
	};

	/**
	 * 选择提示对话框
	 */
	private void ShowPickDialog() {
		if (!Storage.externalStorageAvailable()) {
			NotificationsUtil.ToastBottomMsg(mActivity, "sd卡不可用");
			return;
		}

		new AlertDialog.Builder(this).setTitle("设置头像...")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mImageUtil.getAndCropPhoto();
					}
				}).setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						// 下面这句指定调用相机拍照后的照片存储的路径
						intent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(mImageUtil.getPicFileTemp()));
						startActivityForResult(intent, ImageUtil.CAPTURE_PIC);
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			VolleyLog.d("requestCode = %d ", requestCode);
			VolleyLog.d("resultCode = %d", resultCode);
			VolleyLog.d("data = %s", null == data ? "null" : data.toString());
			return;
		}

		switch (requestCode) {
		case ImageUtil.CAPTURE_PIC:
			mImageUtil.cropPhoto(Uri.fromFile(mImageUtil.getPicFileTemp()));
			break;
		case ImageUtil.HANDLE_PIC:
			mImageUtil.setPicToView(this, img_avatar, Uri.fromFile(mImageUtil.getPicFile()));
			break;
		default:
			break;
		}
	}
}
