package com.xyhui.activity.group;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mslibs.utils.ImageUtils;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.api.Client;
import com.xyhui.types.DetailGroup;
import com.xyhui.types.Response;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class GroupEditActivity extends FLActivity {
	private Button btn_back;
	private Button btn_submit;

	private ImageView img_group_avatar;
	private Button btn_group_avatar;
	private EditText edit_group_intro;
	private EditText edit_group_label;

	private DetailGroup group;

	private boolean isEdit;

	private boolean hasImage;

	private TextView navbar_TitleText;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUP)) {
			group = getIntent().getParcelableExtra(Params.INTENT_EXTRA.GROUP);
			String group_id = group.id;
			if (group != null) {
				isEdit = true;
			}
			VolleyLog.d("got groupid: %s", group_id);
		} else {
			VolleyLog.d("no groupid");
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_group_edit);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		navbar_TitleText = (TextView) findViewById(R.id.navbar_TitleText);
		img_group_avatar = (ImageView) findViewById(R.id.img_group_avatar);
		btn_group_avatar = (Button) findViewById(R.id.btn_group_avatar);
		edit_group_intro = (EditText) findViewById(R.id.edit_group_intro);
		edit_group_label = (EditText) findViewById(R.id.edit_group_label);
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

		btn_group_avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BuildImageDialog(mActivity);
			}
		});

		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 提交创建部落
				showProgress("正在提交", ",请稍候...");

				String info = edit_group_intro.getText().toString();
				String tag = edit_group_label.getText().toString();
				String pic = null;

				if (hasImage) {
					pic = Environment.getExternalStorageDirectory() + "/tmp_upload.jpg";
				}

				if (isEdit) {
					new Api(createcallback, mActivity).modifyGroup(PuApp.get().getToken(),
							group.id, null, null, null, info, tag, pic);
				}
			}
		});
	}

	@Override
	public void ensureUi() {

		if (isEdit) {
			navbar_TitleText.setText("修改部落");
		}

		setupUI();
	}

	private void setupUI() {
		if (isEdit) {
			edit_group_intro.setText(group.intro);
			edit_group_label.setText(group.tagstring);

			if (!TextUtils.isEmpty(group.logo) && !"default.gif".equalsIgnoreCase(group.logo)) {
				String logo = Client.UPLOAD_URL + group.logo;
				UrlImageViewHelper.setUrlDrawable(img_group_avatar, logo, R.drawable.group_avatar);
			}
		}
	}

	CallBack createcallback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();

			Response r = JSONUtils.fromJson(response, Response.class);

			if (null != r) {
				if (!TextUtils.isEmpty(r.response)) {
					NotificationsUtil.ToastBottomMsg(mActivity, r.response);
					finish();
				} else if (!TextUtils.isEmpty(r.message)) {
					NotificationsUtil.ToastBottomMsg(mActivity, r.message);
				}
			}
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String pathInput = null;
		hasImage = false;
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {

		case Params.REQUEST_CODE.TAKE_PHOTO:

			try {
				String mImagePath = Environment.getExternalStorageDirectory() + "/tmp_upload.jpg";
				ImageUtils.resampleImageAndSaveToNewLocation(mImagePath, mImagePath);
				Bitmap bmp = BitmapFactory.decodeFile(mImagePath);

				img_group_avatar.setImageBitmap(bmp);

				hasImage = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case Params.REQUEST_CODE.GET_PHOTO:

			try {
				Uri uri = data.getData();
				String urlScheme = uri.getScheme();
				pathInput = uri.getPath();

				if (urlScheme.equals("file")) {
					pathInput = uri.getPath();
				} else if (urlScheme.equals("content")) {
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					pathInput = cursor.getString(column_index);
					cursor.close();
				}

				if (!TextUtils.isEmpty(pathInput)) {
					String mImagePath = Environment.getExternalStorageDirectory()
							+ "/tmp_upload.jpg";
					ImageUtils.resampleImageAndSaveToNewLocation(pathInput, mImagePath);
					Bitmap bmp = BitmapFactory.decodeFile(mImagePath);

					img_group_avatar.setImageBitmap(bmp);

					hasImage = true;
				} else {
					NotificationsUtil.ToastBottomMsg(getBaseContext(), "无法读入图片");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}
}
