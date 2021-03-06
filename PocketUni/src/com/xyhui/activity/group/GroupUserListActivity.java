package com.xyhui.activity.group;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class GroupUserListActivity extends FLActivity {

	private Button btn_back;
	private PullToRefreshListView user_listview;

	private String group_id;
	private int mIsMember;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUP_ID)) {
			group_id = getIntent().getStringExtra(Params.INTENT_EXTRA.GROUP_ID);
			VolleyLog.d("got groupid:%s", group_id);
		}

		if (TextUtils.isEmpty(group_id)) {
			VolleyLog.d("no groupid");
			finish();
			return;
		}

		if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUPMEMBER)) {
			mIsMember = getIntent().getIntExtra(Params.INTENT_EXTRA.GROUPMEMBER, 0);
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_group_user_list);

		btn_back = (Button) findViewById(R.id.btn_back);
		user_listview = (PullToRefreshListView) findViewById(R.id.user_listview);
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
	}

	@Override
	public void ensureUi() {
		new GroupMemberList(user_listview, mActivity, group_id,
				GroupMemberManageActivity.SELECT_TYPE_USER, mIsMember);
	}
}
