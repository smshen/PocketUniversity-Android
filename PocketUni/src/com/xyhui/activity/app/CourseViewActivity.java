package com.xyhui.activity.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.activity.more.PhoneBindingActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Course;
import com.xyhui.types.Response;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.widget.FLActivity;

public class CourseViewActivity extends FLActivity {
	private Button btn_back;
	private Button btn_join;

	private Course course;

	private TextView text_notice;
	private ImageView course_icon;

	private String courseid;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.COURSEID)) {
			courseid = getIntent().getStringExtra(Params.INTENT_EXTRA.COURSEID);
		} else {
			finish();
			return;
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_course_view);

		btn_back = (Button) findViewById(R.id.btn_back);

		btn_join = (Button) findViewById(R.id.btn_join);
		text_notice = (TextView) findViewById(R.id.text_notice);
		course_icon = (ImageView) findViewById(R.id.course_icon);
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

		btn_join.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mobile = new PrefUtil().getPreference(Params.LOCAL.MOBILE);
				if (course.need_tel == 1 && TextUtils.isEmpty(mobile)) {
					new AlertDialog.Builder(mActivity).setTitle("您还没有绑定手机, 现在去绑定吗?")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									Intent intent = new Intent();
									intent.setClass(mActivity, PhoneBindingActivity.class);
									mActivity.startActivity(intent);
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

								}
							}).show();
				} else {
					showProgress();
					new Api(joinCallback, mActivity).joinCourse(PuApp.get().getToken(), course.id);
				}
			}
		});
	}

	CallBack joinCallback = new CallBack() {

		@Override
		public void onSuccess(String response) {
			dismissProgress();

			Response joinInfo = JSONUtils.fromJson(response, Response.class);

			if (null != joinInfo && 1 == joinInfo.status) {
				NotificationsUtil.ToastBottomMsg(getBaseContext(), joinInfo.message);
				btn_join.setText("已报名");
				btn_join.setBackgroundColor(Color.DKGRAY);
				btn_join.setEnabled(false);
			} else {
				NotificationsUtil.ToastBottomMsg(getBaseContext(), joinInfo.message);
			}
		}
	};

	@Override
	public void ensureUi() {
		showProgress();
		new Api(callback, mActivity).getCourse(PuApp.get().getToken(), courseid);
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			course = JSONUtils.fromJson(response, Course.class);

			if (course != null) {
				text_notice.setText(course.getNotice());

				String logo = course.logoId;
				if (!TextUtils.isEmpty(course.logoId) && !"default.gif".equalsIgnoreCase(logo)) {
					UrlImageViewHelper.setUrlDrawable(course_icon, logo, R.drawable.img_default);
				}

				btn_join.setEnabled(false);

				if (course.isFinished) {
					btn_join.setText("课程已结束");
					btn_join.setBackgroundColor(Color.RED);
				} else if (course.hasJoined) {
					btn_join.setText("已报名");
					btn_join.setBackgroundColor(Color.DKGRAY);
				} else if (course.isAvailable) {
					btn_join.setText("我要报名");
					btn_join.setEnabled(true);
				} else {
					btn_join.setText("课程进行中");
					btn_join.setBackgroundColor(Color.YELLOW);
				}
			}
		}
	};
}
