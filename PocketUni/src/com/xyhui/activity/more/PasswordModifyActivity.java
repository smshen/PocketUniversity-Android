package com.xyhui.activity.more;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.Response;
import com.xyhui.widget.FLActivity;

public class PasswordModifyActivity extends FLActivity {
	private Button btn_back;
	private Button btn_submit;

	private EditText edit_old_passsword;
	private EditText edit_new_password;
	private EditText edit_repeat_password;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_account_password);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_submit = (Button) findViewById(R.id.btn_submit);

		edit_old_passsword = (EditText) findViewById(R.id.edit_old_passsword);
		edit_new_password = (EditText) findViewById(R.id.edit_new_password);
		edit_repeat_password = (EditText) findViewById(R.id.edit_repeat_password);
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
		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 提交修改信息
				String oldpassword = edit_old_passsword.getText().toString();
				oldpassword = oldpassword.trim();
				String password = edit_new_password.getText().toString();
				password = password.trim();
				String repassword = edit_repeat_password.getText().toString();
				repassword = repassword.trim();

				if (oldpassword.length() < 6 || oldpassword.length() > 15) {
					NotificationsUtil.ToastBottomMsg(mActivity, "原密码格式有误, 合法的为6-15位字符");
					return;
				}

				if (password.length() < 6 || password.length() > 15) {
					NotificationsUtil.ToastBottomMsg(mActivity, "新密码格式有误, 合法的为6-15位字符");
					return;
				}

				if (!password.equals(repassword)) {
					NotificationsUtil.ToastBottomMsg(mActivity, "两次密码不同");
					return;
				}

				new Api(callback, mActivity).profilepassword(PuApp.get().getToken(), oldpassword,
						password, repassword);
			}
		});
	}

	CallBack callback = new CallBack() {

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
}
