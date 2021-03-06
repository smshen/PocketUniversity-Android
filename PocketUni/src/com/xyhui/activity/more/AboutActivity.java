package com.xyhui.activity.more;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.xyhui.R;
import com.xyhui.activity.group.GroupViewActivity;
import com.xyhui.types.Group;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class AboutActivity extends FLActivity {
	private Button btn_back;
	private LinearLayout btn_feedback;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_about);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_feedback = (LinearLayout) findViewById(R.id.btn_feedback);
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

		btn_feedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Group item = new Group();
				item.id = "25";

				// 打开部落主页
				Intent intent = new Intent();
				intent.setClass(mActivity, GroupViewActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.GROUP_ID, item.id);
				mActivity.startActivity(intent);
			}
		});
	}

	@Override
	public void ensureUi() {

	}
}
