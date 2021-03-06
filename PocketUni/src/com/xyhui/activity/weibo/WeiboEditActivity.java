package com.xyhui.activity.weibo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.mslibs.utils.ImageUtils;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Response;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class WeiboEditActivity extends FLActivity {
	private int edit_type = Params.INTENT_VALUE.WEIBO_NEW;

	private Button btn_back;
	private Button btn_send;
	private TextView navbar_TitleText;
	private EditText edit_title;
	private ImageView img_split;
	private EditText edit_content;
	private TextView text_counter;

	private ImageView img_photo;
	private Button toolbar_photo;
	private Button toolbar_topic;
	private Button toolbar_at;
	private Button toolbar_swf;
	private Button toolbar_link;
	private Button toolbar_face;

	private LinearLayout layout_faces;
	// private String[] face_names = new String[] { "tongue", "smile", "lol", "loveliness",
	// "titter",
	// "biggrin", "shy", "sweat", "yun", "ku", "yiwen", "mad", "fendou", "funk", "cry",
	// "sad", "ha", "huffy", "pig", "kiss", "victory", "ok", "handshake", "cake", "hug",
	// "beer", "call", "time", "moon" };
	private String[] face_names = new String[] { "tongue", "smile", "lol", "loveliness", "titter",
			"biggrin", "shy", "sweat", "yun", "ku", "88", "mad", "fendou", "funk", "cry", "sad",
			"ha", "huffy", "pig", "guzhang", "victory", "ok", "tu", "cake", "hug", "beer", "call",
			"time", "moon", "hei" };
	private boolean face_is_open = false;

	private String weibo_id;
	private String group_id;
	private String blog_id;
	private boolean hasImage;

	private String flash;

	private String topicName;
	private String mUserName;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.WEIBO_TOPIC_ID)) {
			topicName = getIntent().getStringExtra(Params.INTENT_EXTRA.WEIBO_TOPIC_ID);
		}

		if (getIntent().hasExtra(Params.INTENT_EXTRA.USERNAME)) {
			mUserName = getIntent().getStringExtra(Params.INTENT_EXTRA.USERNAME);
		}

		if (getIntent().hasExtra(Params.INTENT_EXTRA.WEIBO_EDIT)) {
			edit_type = getIntent().getIntExtra(Params.INTENT_EXTRA.WEIBO_EDIT, 0);
		} else {
			finish();
			return;
		}
		if (edit_type == Params.INTENT_VALUE.WEIBO_BLOG) {
			if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUP_ID)) {
				group_id = getIntent().getStringExtra(Params.INTENT_EXTRA.GROUP_ID);
			} else {
				finish();
			}

		} else if (edit_type == Params.INTENT_VALUE.WEIBO_BLOGREPLY) {
			if (getIntent().hasExtra(Params.INTENT_EXTRA.BLOG_ID)) {
				blog_id = getIntent().getStringExtra(Params.INTENT_EXTRA.BLOG_ID);
			} else {
				finish();
			}

			if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUP_ID)) {
				group_id = getIntent().getStringExtra(Params.INTENT_EXTRA.GROUP_ID);
			} else {
				finish();
			}

		} else if (edit_type != Params.INTENT_VALUE.WEIBO_NEW) {
			if (getIntent().hasExtra(Params.INTENT_EXTRA.WEIBO_ID)) {
				weibo_id = getIntent().getStringExtra(Params.INTENT_EXTRA.WEIBO_ID);
			} else {
				finish();
			}
		}

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_weibo_edit);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_send = (Button) findViewById(R.id.btn_send);
		navbar_TitleText = (TextView) findViewById(R.id.navbar_TitleText);
		edit_title = (EditText) findViewById(R.id.edit_title);
		img_split = (ImageView) findViewById(R.id.img_split);
		edit_content = (EditText) findViewById(R.id.edit_content);

		text_counter = (TextView) findViewById(R.id.text_counter);

		img_photo = (ImageView) findViewById(R.id.img_photo);
		toolbar_photo = (Button) findViewById(R.id.toolbar_photo);
		toolbar_topic = (Button) findViewById(R.id.toolbar_topic);
		toolbar_at = (Button) findViewById(R.id.toolbar_at);
		toolbar_swf = (Button) findViewById(R.id.toolbar_swf);
		toolbar_link = (Button) findViewById(R.id.toolbar_link);
		toolbar_face = (Button) findViewById(R.id.toolbar_face);

		layout_faces = (LinearLayout) findViewById(R.id.layout_faces);
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
		btn_send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_send.setEnabled(false);

				// 提交
				String content = edit_content.getText().toString().trim();

				if (TextUtils.isEmpty(content) && edit_type == Params.INTENT_VALUE.WEIBO_FORWARD) {
					content = "转发";
				}

				if (TextUtils.isEmpty(content) && edit_type != Params.INTENT_VALUE.WEIBO_FORWARD) {
					NotificationsUtil.ToastBottomMsg(mActivity, "请输入内容");
					return;
				}

				if (content.length() > 140) {
					NotificationsUtil.ToastBottomMsg(mActivity, "内容超过140个字符");
					return;
				}

				if (Params.INTENT_VALUE.WEIBO_BLOG == edit_type && content.length() < 5) {
					NotificationsUtil.ToastBottomMsg(mActivity, "内容不能少于5个字符");
					return;
				}

				hideSoftInput(edit_content);
				showProgress("正在提交请求", "请稍候...");

				RequestParams params = PuApp.get().getToken();
				switch (edit_type) {
				case Params.INTENT_VALUE.WEIBO_NEW:
					// 微博新建
					if (hasImage) {
						String pic = Environment.getExternalStorageDirectory() + "/tmp_upload.jpg";
						new Api(callback, mActivity).upload(params, content, pic);
					} else {
						new Api(callback, mActivity).update(params, content);
					}
					break;
				case Params.INTENT_VALUE.WEIBO_REPLY:
					// 微博评论
					String transpond = "0";
					String reply_comment_id = "0";
					// weibo_id = "100000";
					new Api(callback, mActivity).comment(params, reply_comment_id, weibo_id,
							content, transpond);
					break;
				case Params.INTENT_VALUE.WEIBO_FORWARD:
					// 微博转发
					String reply_weibo_id = "0";
					new Api(callback, mActivity).repost(params, reply_weibo_id, content, weibo_id);
					break;
				case Params.INTENT_VALUE.WEIBO_BLOG:
					String title = edit_title.getText().toString();
					String pic = null;
					if (hasImage) {
						pic = Environment.getExternalStorageDirectory() + "/tmp_upload.jpg";
					}
					new Api(callback, mActivity).newtopic(params, group_id, title, content, flash,
							pic);
					break;
				case Params.INTENT_VALUE.WEIBO_BLOGREPLY:
					// 文章新建
					// group_id 部落id
					VolleyLog.d("reply:%s", blog_id);
					new Api(callback, mActivity).replytopic(params, group_id, blog_id, content);
					break;
				default:
					break;
				}
			}
		});
		img_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 清除图片，打开相机或者相册
				img_photo.setVisibility(View.GONE);
				toolbar_photo.setVisibility(View.VISIBLE);
				hasImage = false;
			}
		});
		toolbar_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开相机或者相册
				// 获取图片后显示在img_photo

				hideSoftInput(edit_content);
				BuildImageDialog(mActivity);
			}
		});
		toolbar_topic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 插入#话题#
				String CurrentStr = edit_content.getText().toString();
				int Currentlen = CurrentStr.length();
				String SharpStr = "#请插入话题名称#";
				edit_content.setText(CurrentStr + SharpStr);
				edit_content.setSelection(Currentlen + 1, Currentlen + SharpStr.length() - 1);
			}
		});
		toolbar_at.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 插入@
				String CurrentStr = edit_content.getText().toString();
				String SharpStr = CurrentStr + "@";
				edit_content.setText(SharpStr);
				edit_content.setSelection(SharpStr.length(), SharpStr.length());
			}
		});
		toolbar_swf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input_swf = new EditText(WeiboEditActivity.this);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				input_swf.setLayoutParams(lp);
				input_swf.setSingleLine(true);
				input_swf.setTextSize(14);
				input_swf.setHint("支持优酷swf视频链接");

				// 插入视频
				new AlertDialog.Builder(WeiboEditActivity.this).setTitle("插入视频链接")
						.setView(input_swf)
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								flash = input_swf.getText().toString();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

							}
						}).show();
			}
		});
		toolbar_link.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);

				LinearLayout content_layout = new LinearLayout(WeiboEditActivity.this);
				content_layout.setLayoutParams(lp);
				content_layout.setOrientation(LinearLayout.VERTICAL);

				final EditText input_text = new EditText(WeiboEditActivity.this);
				input_text.setLayoutParams(lp);
				input_text.setSingleLine(true);
				input_text.setTextSize(14);
				input_text.setHint("超链接文字");
				content_layout.addView(input_text);

				final EditText input_url = new EditText(WeiboEditActivity.this);
				input_url.setLayoutParams(lp);
				input_url.setSingleLine(true);
				input_url.setTextSize(14);
				input_url.setHint("超链接地址");
				content_layout.addView(input_url);

				// 插入链接
				new AlertDialog.Builder(WeiboEditActivity.this).setTitle("插入网址超链接")
						.setView(content_layout)
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								if (input_text.getTextSize() > 0 && input_url.getTextSize() > 0) {
									String linkStr = "<a href=\"" + input_url.getText() + "\">"
											+ input_text.getText() + "</a>";
									String CurrentStr = edit_content.getText().toString();
									String SharpStr = CurrentStr + linkStr;
									edit_content.setText(SharpStr);
									edit_content.setSelection(SharpStr.length(), SharpStr.length());
								}
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

							}
						}).show();

			}
		});
		toolbar_face.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (face_is_open) {
					toolbar_face.setBackgroundResource(R.drawable.btn_selector_edit_face);

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					layout_faces.setVisibility(View.GONE);

					face_is_open = false;
				} else {
					toolbar_face.setBackgroundResource(R.drawable.btn_selector_edit_keyboard);

					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					layout_faces.setVisibility(View.VISIBLE);
					WeiboEditActivity.super.hideSoftInput(edit_content);

					face_is_open = true;
				}
			}
		});

		edit_content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (face_is_open) {
					toolbar_face.setBackgroundResource(R.drawable.btn_selector_edit_face);
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					layout_faces.setVisibility(View.GONE);

					face_is_open = false;
				}
			}
		});

		edit_content.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int length = edit_content.getText().length();

				text_counter.setText("字数：" + length + "/140");
				if (length > 140) {
					btn_send.setEnabled(false);
					text_counter.setTextColor(Color.rgb(255, 0, 0));
				} else if (length > 0) {
					btn_send.setEnabled(true);
					text_counter.setTextColor(Color.rgb(153, 153, 153));
				} else {
					btn_send.setEnabled(false);
					text_counter.setTextColor(Color.rgb(153, 153, 153));
				}
			}

		});

		OnClickListener faceOCL = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageView img_face = (ImageView) v;
				String face_name = img_face.getTag().toString();
				String CurrentStr = edit_content.getText().toString();
				String SharpStr = CurrentStr + "[" + face_name + "]";
				edit_content.setText(SharpStr);
				edit_content.setSelection(SharpStr.length(), SharpStr.length());
			}
		};
		for (int i = 0; i < face_names.length; i++) {
			Resources resources = getResources();
			int imageResource = resources.getIdentifier(getPackageName() + ":id/img_fece"
					+ (i + 1), null, null);
			ImageView img_face = (ImageView) findViewById(imageResource);
			if (img_face != null) {
				img_face.setTag(face_names[i]);
				img_face.setOnClickListener(faceOCL);
			}
		}

	}

	@Override
	public void ensureUi() {
		layout_faces.setVisibility(View.GONE);
		btn_send.setEnabled(false);
		super.openKeyboard();

		toolbar_face.setTag("");
		switch (edit_type) {
		case Params.INTENT_VALUE.WEIBO_NEW:
			edit_title.setVisibility(View.GONE);
			img_split.setVisibility(View.GONE);
			edit_content.setHint("说说新鲜事");
			toolbar_swf.setVisibility(View.GONE);
			toolbar_link.setVisibility(View.GONE);

			if (null != topicName) {
				edit_content.setText("#" + topicName + "#");
				edit_content.setSelection(edit_content.getText().toString().length());
			}
			break;
		case Params.INTENT_VALUE.WEIBO_REPLY:

			if (null != mUserName) {
				navbar_TitleText.setText("回复评论");
				edit_content.setText("回复@" + mUserName + " : ");
				edit_content.setSelection(edit_content.getText().toString().length());
			} else {
				navbar_TitleText.setText("发表评论");
				edit_content.setHint("发表你的看法");
			}

			edit_title.setVisibility(View.GONE);
			img_split.setVisibility(View.GONE);

			toolbar_swf.setVisibility(View.GONE);
			toolbar_link.setVisibility(View.GONE);

			toolbar_photo.setVisibility(View.GONE);
			toolbar_topic.setVisibility(View.GONE);
			toolbar_at.setVisibility(View.GONE);
			break;
		case Params.INTENT_VALUE.WEIBO_FORWARD:
			navbar_TitleText.setText("转发");

			edit_title.setVisibility(View.GONE);
			img_split.setVisibility(View.GONE);
			toolbar_swf.setVisibility(View.GONE);
			toolbar_link.setVisibility(View.GONE);

			edit_content.setText("转发");
			edit_content.setHint("");
			btn_send.setEnabled(true);
			toolbar_photo.setVisibility(View.GONE);
			toolbar_topic.setVisibility(View.GONE);
			toolbar_at.setVisibility(View.GONE);
			break;
		case Params.INTENT_VALUE.WEIBO_BLOG:
			navbar_TitleText.setText("新建帖子");

			edit_content.setHint("帖子正文");

			toolbar_topic.setVisibility(View.GONE);
			toolbar_at.setVisibility(View.GONE);
			break;
		case Params.INTENT_VALUE.WEIBO_BLOGREPLY:
			navbar_TitleText.setText("回复帖子");

			edit_content.setHint("说点什么吧");

			edit_title.setVisibility(View.GONE);
			img_split.setVisibility(View.GONE);
			toolbar_swf.setVisibility(View.GONE);
			toolbar_link.setVisibility(View.GONE);

			toolbar_photo.setVisibility(View.GONE);
			toolbar_topic.setVisibility(View.GONE);
			toolbar_at.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	CallBack callback = new CallBack() {

		@Override
		public void onSuccess(String response) {
			dismissProgress();
			btn_send.setEnabled(true);
			int reponse_id = 0;
			try {
				reponse_id = Integer.parseInt(response);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			if (reponse_id > 0) {
				NotificationsUtil.ToastBottomMsg(mActivity, "操作成功");
				String action = null;
				switch (edit_type) {
				case Params.INTENT_VALUE.WEIBO_NEW:
					action = Params.INTENT_ACTION.WEIBOLIST;
					break;
				case Params.INTENT_VALUE.WEIBO_REPLY:
					action = Params.INTENT_ACTION.WEIBOVIEW;
					break;
				case Params.INTENT_VALUE.WEIBO_BLOG:
					action = Params.INTENT_ACTION.BLOGLIST;
					break;
				case Params.INTENT_VALUE.WEIBO_BLOGREPLY:
					action = Params.INTENT_ACTION.BLOGVIEW;
					break;
				}
				if (!TextUtils.isEmpty(action)) {
					Intent intent = new Intent().setAction(action);
					sendBroadcast(intent);
				}

				finish();
			} else if (0 == reponse_id) {
				NotificationsUtil.ToastBottomMsg(mActivity, "您操作太频繁，请稍后再发！");
			} else {
				Response r = JSONUtils.fromJson(response, Response.class);

				if (null != r) {
					if (!TextUtils.isEmpty(r.message)) {
						NotificationsUtil.ToastBottomMsg(mActivity, r.message);
					} else {
						NotificationsUtil.ToastBottomMsg(mActivity, "操作失败");
					}
				}
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();
			btn_send.setEnabled(true);
			NotificationsUtil.ToastBottomMsg(mActivity, "操作失败");
		}
	};

	CallBack uploadcallback = new CallBack() {

		@Override
		public void onSuccess(String response) {
			int reponse_id = 0;
			try {
				reponse_id = Integer.parseInt(response);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			if (reponse_id > 0) {
				VolleyLog.d("success:%d", reponse_id);
			} else {
				VolleyLog.e("error");
				NotificationsUtil.ToastBottomMsg(mActivity, "操作失败");
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();
			NotificationsUtil.ToastBottomMsg(mActivity, "操作失败");
		}
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

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

				img_photo.setImageBitmap(bmp);
				img_photo.setVisibility(View.VISIBLE);
				String content = edit_content.getText().toString().trim();
				if (TextUtils.isEmpty(content)) {
					edit_content.setText("分享图片");
				}
				toolbar_photo.setVisibility(View.GONE);
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

					img_photo.setImageBitmap(bmp);
					img_photo.setVisibility(View.VISIBLE);
					String content = edit_content.getText().toString().trim();
					if (TextUtils.isEmpty(content)) {
						edit_content.setText("分享图片");
					}
					toolbar_photo.setVisibility(View.GONE);
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
