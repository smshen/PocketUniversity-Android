package com.xyhui.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.mslibs.utils.VolleyLog;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.types.City;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;

public class CityList extends CListView {

	private PrefUtil mPrefUtil;

	public CityList(ListView lv, Activity activity) {
		super(lv, activity);
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		setListItemResource(R.layout.list_item_school);
	}

	@Override
	public void ensureUi() {
		mPrefUtil = new PrefUtil();

		mPerpage = 10000;
		super.setFooterResource(R.layout.list_item_school_bottom);
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

				City city = (City) mListItems.get(i);

				VolleyLog.d("onclick: %s", city.city);

				mPrefUtil.setPreference(Params.LOCAL.CITYID, city.id);
				mPrefUtil.setPreference(Params.LOCAL.CITYNAME, city.city);

				Intent intent = new Intent().setAction("android.user.CITY");
				intent.putExtra(Params.INTENT_EXTRA.CITY, city);
				mActivity.sendBroadcast(intent);

				// 选择城市并返回
				((Activity) mActivity).finish();
			}
		});

	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {
		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		City city = (City) obj;
		String title = city.city;
		LVP.add(new CListViewParam(R.id.school_title, title, true));

		return LVP;
	}

	@Override
	public void asyncData() {
		ArrayList<City> citys = PuApp.get().getLocalDataMgr().getCitys();

		if (citys != null && !citys.isEmpty()) {
			citys.remove(0);

			mListItems.clear();
			mDateList.clear();

			for (int i = 0; i < citys.size(); i++) {
				mListItems.add(citys.get(i));
			}
		}

		initListViewFinish();
		actionType = IDLE;
	}
}
