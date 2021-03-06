package com.xyhui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.xyhui.R;
import com.xyhui.activity.weibo.UserList;
import com.xyhui.activity.weibo.UserSearchActivity;
import com.xyhui.types.Banner;
import com.xyhui.utils.Params;
import com.xyhui.widget.AdBannerLayout;
import com.xyhui.widget.FLActivity;

public class TabFriendActivity extends FLActivity {

	private Button btn_nav_follows;
	private Button btn_nav_followed;
	private Button btn_search;
	private Button btn_back;

	private PullToRefreshListView followed_listview;
	private PullToRefreshListView follows_listview;

	private AdBannerLayout ad_banner;

	private UserList mUserListView_followed;
	private UserList mUserListView_follows;
	private int user_type = UserList.USER_FOLLOWED;

	private String UID;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.FRIENDLIST_TYPE)) {
			user_type = getIntent().getIntExtra(Params.INTENT_EXTRA.FRIENDLIST_TYPE,
					UserList.USER_FOLLOWED);
		}

		if (getIntent().hasExtra(Params.INTENT_EXTRA.FRIENDLIST_USERID)) {
			UID = getIntent().getStringExtra(Params.INTENT_EXTRA.FRIENDLIST_USERID);
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_tab_friend);

		btn_nav_follows = (Button) findViewById(R.id.btn_nav_follows);
		btn_nav_followed = (Button) findViewById(R.id.btn_nav_followed);
		btn_search = (Button) findViewById(R.id.btn_search);
		btn_back = (Button) findViewById(R.id.btn_back);

		ad_banner = (AdBannerLayout) findViewById(R.id.ad_banner);

		followed_listview = (PullToRefreshListView) findViewById(R.id.followed_listview);
		follows_listview = (PullToRefreshListView) findViewById(R.id.follows_listview);
	}

	@Override
	public void bindListener() {
		btn_nav_follows.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 粉丝列表
				user_type = UserList.USER_FOLLOWS;
				selectFriendByType();
			}
		});
		btn_nav_followed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 关注列表
				user_type = UserList.USER_FOLLOWED;
				selectFriendByType();
			}
		});

		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 关闭返回
				finish();
			}
		});

		btn_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开寻找好友
				Intent intent = new Intent();
				intent.setClass(mActivity, UserSearchActivity.class);
				mActivity.startActivity(intent);
			}
		});
	}

	@Override
	public void ensureUi() {
		ad_banner.init(Banner.TYPE_FRIEND);

		if (UID == null) {
//			btn_back.setVisibility(View.GONE);
//			btn_search.setVisibility(View.VISIBLE);
		} else {
//			btn_back.setVisibility(View.VISIBLE);
//			btn_search.setVisibility(View.GONE);
			btn_nav_followed.setText("我的关注");
			btn_nav_follows.setText("我的粉丝");
		}

		selectFriendByType();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ad_banner.reload();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ad_banner.pause();
	}

	public void selectFriendByType() {
		if (user_type == UserList.USER_FOLLOWED) {
			btn_nav_followed.setSelected(true);
			btn_nav_follows.setSelected(false);

			follows_listview.setVisibility(View.GONE);
			followed_listview.setVisibility(View.VISIBLE);

			if (mUserListView_followed == null) {
				mUserListView_followed = new UserList(followed_listview, mActivity,
						UserList.USER_FOLLOWED, null, UID);
			} else {
				mUserListView_followed.refreshListViewStart();
			}
		} else {
			btn_nav_follows.setSelected(true);
			btn_nav_followed.setSelected(false);

			followed_listview.setVisibility(View.GONE);
			follows_listview.setVisibility(View.VISIBLE);

			if (mUserListView_follows == null) {
				mUserListView_follows = new UserList(follows_listview, mActivity,
						UserList.USER_FOLLOWS, null, UID);
			} else {
				mUserListView_follows.refreshListViewStart();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (TextUtils.isEmpty(UID)) {
			return super.onKeyDown(keyCode, event);
		}
		finish();
		return true;
	}
}
