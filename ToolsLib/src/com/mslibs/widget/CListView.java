package com.mslibs.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class CListView {
	protected int page = 1;
	// 基础变量
	public ListView mListView; // 不带下啦刷新
	public PullToRefreshListView mListViewPTR; // 带下啦刷新
	public CListViewAdapter mAdapter; // 适配器
	public ArrayList<ArrayList<CListViewParam>> mDateList; // 适配数据
	public ArrayList<Object> mListItems; // 原型数据
	public Context mActivity; // 主窗体

	// 页面控制变量
	public int mOffset = 0;
	public int mPerpage = 20;

	public int actionType = IDLE;
	public static final int IDLE = 0;
	public static final int INIT = 1;
	public static final int REFRESH = 2;
	public static final int GETMORE = 3;

	// 资源变量
	private int mListItemResource = 0;
	private int mGetMoreResource = 0;
	private int mGetMoreTitleRID = 0;
	private String mGetMoreTitle = "";
	private int mHeaderResource = 0;
	private int mFooterResource = 0;
	private int mSingleResource = 0;

	public CListView(PullToRefreshListView lv, Activity activity) {
		mListViewPTR = lv;
		mActivity = activity;
		mListItems = new ArrayList<Object>();
		initListItemResource();
		ensureUi();
	}

	public CListView(ListView lv, Activity activity) {
		mListView = lv;
		mActivity = activity;
		mListItems = new ArrayList<Object>();
		initListItemResource();
		ensureUi();
	}

	public abstract void initListItemResource();

	public abstract ArrayList<CListViewParam> matchListItem(Object obj, int index);

	public void setSelectedResourcem(int res) {
		mAdapter.setSelectedResourcem(res);
	}

	public void setSelectedIndex(int i) {
		mAdapter.setSelectedIndex(i);
	}

	public void setListItemResource(int listItemResource) {
		mListItemResource = listItemResource;
	}

	public void setGetMoreResource(int getMoreResource, int titleRID, String title) {
		mGetMoreResource = getMoreResource;
		mGetMoreTitleRID = titleRID;
		mGetMoreTitle = title;
	}

	public void setHeaderResource(int headerResource) {
		mHeaderResource = headerResource;
	}

	public void setFooterResource(int footerResource) {
		mFooterResource = footerResource;
	}

	public void setSingleResource(int singleResource) {
		mSingleResource = singleResource;
	}

	public void setItemOnclickLinstener(OnClickListener onClickListener) {
		mAdapter.setItemOnclickLinstener(onClickListener);
	}

	public void setGetMoreClickListener(OnClickListener onClickListener) {
		mAdapter.setGetMoreClickListener(onClickListener);
	}

	public void ensureUi() {

		mDateList = new ArrayList<ArrayList<CListViewParam>>();
		mAdapter = new CListViewAdapter(mActivity, mListItemResource);

		mAdapter.setGetMoreResource(mGetMoreResource);
		mAdapter.setHeaderResource(mHeaderResource);
		mAdapter.setFooterResource(mFooterResource);
		mAdapter.setSingleResource(mSingleResource);

		mAdapter.setData(mDateList);

		mAdapter.ItemViewEmptyInvisible = true; // 隐藏空值的控件

		if (mListView != null) {

			mListView.setDividerHeight(0);
			mListView.setAdapter(mAdapter);
		} else {
			mListViewPTR.setAdapter(mAdapter);
			mListViewPTR.setOnRefreshListener(new OnRefreshListener<ListView>() {
				public void onRefresh(PullToRefreshBase<ListView> refreshView) {
					refreshListViewStart();
				}
			});
		}
	}

	public void initListViewStart() {
		if (actionType != IDLE) {
			return;
		}

		actionType = INIT;
		mOffset = 0;

		// 异步获取数据
		asyncData();
	}

	public void initListViewFinish() {
		// 控制是否需要显示更多
		isMorePage();

		for (int i = 0; i < mListItems.size(); i++) {
			mDateList.add(matchListItem(mListItems.get(i), i));
		}

		if (mAdapter.isNotMore == false) {
			setMoreLVP();
		}

		mAdapter.notifyDataSetChanged();
	}

	public void refreshListViewStart() {
		if (actionType != IDLE)
			return;
		actionType = REFRESH;
		mOffset = 0;
		// 异步获取数据
		asyncData();
	}

	public void refreshListViewFinish() {
		// 控制是否需要显示更多
		isMorePage();

		for (int i = 0; i < mListItems.size(); i++) {
			mDateList.add(matchListItem(mListItems.get(i), i));
		}

		if (mAdapter.isNotMore == false) {
			setMoreLVP();
		}

		mAdapter.notifyDataSetChanged();

		// 回到顶部
		if (mListView != null) {
			mListView.setSelection(0);
		} else {
			mListViewPTR.onRefreshComplete();
		}
	}

	public void getmoreListViewStart() {
		if (actionType != IDLE)
			return;
		actionType = GETMORE;
		mOffset += mPerpage;

		// 异步获取数据
		asyncData();
	}

	public void getmoreListViewFinish() {
		// 控制是否需要显示更多
		isMorePage();

		for (int i = mDateList.size() - 1, m = mListItems.size(); i < m; i++) {
			mDateList.add(i, matchListItem(mListItems.get(i), i));
		}
		if (!mDateList.isEmpty() && mAdapter.isNotMore) {
			mDateList.remove(mDateList.size() - 1);
		}
		mAdapter.notifyDataSetChanged();
	}

	public void setMoreLVP() {
		if (mGetMoreResource == 0) {
			return;
		}
		if (mListItems.size() >= mPerpage) {
			ArrayList<CListViewParam> getMoreLVP = new ArrayList<CListViewParam>();
			getMoreLVP.add(new CListViewParam(mGetMoreTitleRID, mGetMoreTitle));
			mDateList.add(getMoreLVP);
		}
	}

	public void asyncData() {
		((CActivity) mActivity).showProgress();
		if (GETMORE == actionType) {
			page++;
		} else {
			page = 1;
		}
	}

	private void isMorePage() {
		if (mListItems.size() < mOffset + mPerpage) {
			mAdapter.isNotMore = true;
		} else {
			mAdapter.isNotMore = false;
		}
	}
}
