package com.xyhui.activity.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.Maplist;
import com.xyhui.types.Maplists;
import com.xyhui.utils.Params;

public class NavMapList extends CListView {
	private String mSchoolId;

	public NavMapList(ListView lv, Activity activity, String schoolId) {
		super(lv, activity);

		mSchoolId = schoolId;

		// 在父类将执行 initListItemResource();ensureUi();
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		this.setListItemResource(R.layout.list_item_nav_map);
	}

	public void search(String sid) {
		mSchoolId = sid;
		refreshListViewStart();
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;

		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多地图");
		super.ensureUi();

		super.setGetMoreClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getmoreListViewStart();
			}
		});

		super.setItemOnclickLinstener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int i = (Integer) v.getTag();

				Maplist item = (Maplist) mListItems.get(i);

				// 打开地图查看
				Intent intent = new Intent();
				intent.setClass(mActivity, NavMapViewActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.MAP, item);
				mActivity.startActivity(intent);
			}
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		Maplist item = (Maplist) obj;
		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();
		LVP.add(new CListViewParam(R.id.text_title, item.title, true));
		return LVP;
	}

	ListCallBack<Maplists> callback = new ListCallBack<Maplists>(NavMapList.this) {

		@Override
		public void setType() {
			myType = new TypeToken<Maplists>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (null != mT && mT.data != null) {
				for (int i = 0; i < mT.data.size(); i++) {
					mListItems.add(mT.data.get(i));
				}
			}
		}
	};

	@Override
	public void asyncData() {
		super.asyncData();
		new Api(callback, mActivity).ditus(PuApp.get().getToken(), mSchoolId, mPerpage, page);
	}
}
