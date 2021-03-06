package com.xyhui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.xyhui.R;
import com.xyhui.activity.weibo.WeiboEditActivity;
import com.xyhui.activity.weibo.WeiboList;
import com.xyhui.activity.weibo.WeiboTopicList;
import com.xyhui.types.Banner;
import com.xyhui.utils.Params;
import com.xyhui.widget.AdBannerLayout;
import com.xyhui.widget.FLActivity;

public class TabWeiboActivity extends FLActivity {

	private PullToRefreshListView weibo_listview;
	private PullToRefreshListView all_weibo_listview;
	private PullToRefreshListView weibotopic_listview;

	private WeiboList mWeiboListView;
	private WeiboList mAllWeiboListView;
	private WeiboTopicList mWeiboTopicListView;

	private AdBannerLayout ad_banner;

	private Button btn_newweibo;
	private Button btn_topic;

	private Button btn_my_weibo;
	private Button btn_all_weibo;

	public final int WEIBO_MY = 0;
	public final int WEIBO_ALL = 1;
	public final int TOPIC = 2;

	public int showType = WEIBO_ALL;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_tab_weibo);

		weibo_listview = (PullToRefreshListView) findViewById(R.id.weibo_listview);
		all_weibo_listview = (PullToRefreshListView) findViewById(R.id.all_weibo_listview);
		weibotopic_listview = (PullToRefreshListView) findViewById(R.id.weibotopic_listview);
		btn_newweibo = (Button) findViewById(R.id.btn_newweibo);
		btn_topic = (Button) findViewById(R.id.btn_topic);

		ad_banner = (AdBannerLayout) findViewById(R.id.ad_banner);

		btn_my_weibo = (Button) findViewById(R.id.btn_my_weibo);
		btn_all_weibo = (Button) findViewById(R.id.btn_all_weibo);
	}

	@Override
	public void bindListener() {

		btn_newweibo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开发微博窗体
				Intent intent = new Intent();
				intent.setClass(mActivity, WeiboEditActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.WEIBO_EDIT, Params.INTENT_VALUE.WEIBO_NEW);
				mActivity.startActivity(intent);
			}
		});

		btn_topic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 专题列表
				showType = TOPIC;
				selectWeiboByType();
			}
		});

		btn_my_weibo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 我的微博
				showType = WEIBO_MY;
				selectWeiboByType();
			}
		});

		btn_all_weibo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 微博广场
				showType = WEIBO_ALL;
				selectWeiboByType();
			}
		});
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

	@Override
	public void ensureUi() {
		ad_banner.init(Banner.TYPE_WEIBO);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Params.INTENT_ACTION.WEIBOLIST);
		registerReceiver(mEvtReceiver, filter);
		selectWeiboByType();
	}

	public void selectWeiboByType() {
		if (showType == WEIBO_MY) {
			btn_all_weibo.setSelected(false);
			btn_my_weibo.setSelected(true);
			btn_topic.setSelected(false);

			all_weibo_listview.setVisibility(View.GONE);
			weibo_listview.setVisibility(View.VISIBLE);
			weibotopic_listview.setVisibility(View.GONE);

			if (mWeiboListView == null) {
				mWeiboListView = new WeiboList(weibo_listview, mActivity, WeiboList.TIMELINE);
			} else {
				mWeiboListView.refreshListViewStart();
			}
		} else if (showType == WEIBO_ALL) {

			btn_all_weibo.setSelected(true);
			btn_my_weibo.setSelected(false);
			btn_topic.setSelected(false);

			all_weibo_listview.setVisibility(View.VISIBLE);
			weibo_listview.setVisibility(View.GONE);
			weibotopic_listview.setVisibility(View.GONE);

			if (mAllWeiboListView == null) {
				mAllWeiboListView = new WeiboList(all_weibo_listview, mActivity, WeiboList.ALL);
			} else {
				mAllWeiboListView.refreshListViewStart();
			}
		} else if (showType == TOPIC) {

			btn_all_weibo.setSelected(false);
			btn_my_weibo.setSelected(false);
			btn_topic.setSelected(true);

			all_weibo_listview.setVisibility(View.GONE);
			weibo_listview.setVisibility(View.GONE);
			weibotopic_listview.setVisibility(View.VISIBLE);

			if (mWeiboTopicListView == null) {
				mWeiboTopicListView = new WeiboTopicList(weibotopic_listview, mActivity);
			} else {
				mWeiboTopicListView.refreshListViewStart();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mEvtReceiver);
	}

	public BroadcastReceiver mEvtReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Params.INTENT_ACTION.WEIBOLIST)) {
				if (mWeiboListView != null) {
					mWeiboListView.refreshListViewStart();
				}

				if (mAllWeiboListView != null) {
					mAllWeiboListView.refreshListViewStart();
				}
			}
		}
	};
}
