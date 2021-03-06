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

public class GroupMemberManageActivity extends FLActivity {
	private Button btn_back;
	private Button btn_member_manage;
	private Button btn_apply_manage;

	public static final int SELECT_TYPE_USER = 0;
	public static final int SELECT_TYPE_MEMBER = 1;
	public static final int SELECT_TYPE_APPLY = 2;

	private int selectType = SELECT_TYPE_MEMBER;

	private PullToRefreshListView member_listview;
	private GroupMemberList mMemberListView;

	private PullToRefreshListView apply_listview;
	private GroupMemberList mApplyListView;

	private String group_id;
	private boolean isCreator;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.GROUP_ID)) {
			group_id = getIntent().getStringExtra(Params.INTENT_EXTRA.GROUP_ID);
			isCreator = getIntent().getBooleanExtra(GroupViewActivity.IS_GROUP_CREATOR, false);
			VolleyLog.d("got groupid: %s", group_id);
		}

		if (TextUtils.isEmpty(group_id)) {
			VolleyLog.d("no groupid");
			finish();
			return;
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_group_membermanage);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_member_manage = (Button) findViewById(R.id.btn_member_manage);
		btn_apply_manage = (Button) findViewById(R.id.btn_apply_manage);

		member_listview = (PullToRefreshListView) findViewById(R.id.member_listview);
		apply_listview = (PullToRefreshListView) findViewById(R.id.apply_listview);
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

		btn_member_manage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectType(SELECT_TYPE_MEMBER);
			}
		});

		btn_apply_manage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectType(SELECT_TYPE_APPLY);
				if (mApplyListView == null) {
					mApplyListView = new GroupMemberList(apply_listview, mActivity, group_id,
							selectType, 1);
				}
			}
		});
	}

	@Override
	public void ensureUi() {
		setSelectType(SELECT_TYPE_MEMBER);
		if (null == mMemberListView) {
			mMemberListView = new GroupMemberList(member_listview, mActivity, group_id,
					selectType, 1);
		}
	}

	public boolean isCreator() {
		return isCreator;
	}

	public void setSelectType(int selectType) {
		this.selectType = selectType;
		if (selectType == SELECT_TYPE_MEMBER) {
			btn_member_manage.setSelected(true);
			btn_apply_manage.setSelected(false);

			member_listview.setVisibility(View.VISIBLE);
			apply_listview.setVisibility(View.GONE);
		} else {
			btn_member_manage.setSelected(false);
			btn_apply_manage.setSelected(true);

			member_listview.setVisibility(View.GONE);
			apply_listview.setVisibility(View.VISIBLE);
		}
	}
}
