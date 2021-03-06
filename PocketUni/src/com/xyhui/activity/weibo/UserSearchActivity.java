package com.xyhui.activity.weibo;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.City;
import com.xyhui.types.KeyValuePair;
import com.xyhui.types.School;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.widget.FLActivity;

public class UserSearchActivity extends FLActivity {
	private Button btn_back;
	private PullToRefreshListView user_listview;
	private UserList mUserListView;
	private EditText edit_keyword;
	private Button btn_search;

	private Spinner spinner_city;
	private ArrayAdapter<City> cityAdapter;
	private String cityId = "0";
	private int citySelectionId;
	private ArrayList<City> citys;

	private Spinner spinner_school;
	private ArrayAdapter<School> schoolAdapter;
	private String schoolId = "0";
	private int schoolSelectionId;
	private ArrayList<School> schools;

	private Spinner spinner_dpart;
	private ArrayAdapter<KeyValuePair> dpartAdapter;
	private String dpartId = "0";
	private ArrayList<KeyValuePair> dparts;

	private final String CUPID = "13";

	private boolean isInitSchoolSpinner = true;

	private PrefUtil mPrefUtil;

	@Override
	public void init() {
		mPrefUtil = new PrefUtil();
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_user_search);

		btn_back = (Button) findViewById(R.id.btn_back);
		user_listview = (PullToRefreshListView) findViewById(R.id.user_listview);
		edit_keyword = (EditText) findViewById(R.id.edit_keyword);
		btn_search = (Button) findViewById(R.id.btn_search);

		spinner_school = (Spinner) findViewById(R.id.spinner_school);
		spinner_city = (Spinner) findViewById(R.id.spinner_city);
		spinner_dpart = (Spinner) findViewById(R.id.spinner_dpart);
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

		btn_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 查询部落
				String key = edit_keyword.getText().toString();

				if (TextUtils.isEmpty(key.trim())) {
					NotificationsUtil.ToastTopMsg(getBaseContext(), "输入字段不能为空");
					return;
				}

				hideSoftInput(edit_keyword);

				int i = spinner_school.getSelectedItemPosition();
				if (i < schools.size()) {
					schoolId = schools.get(i).school;
				}

				int j = spinner_dpart.getSelectedItemPosition();
				if (j < dparts.size()) {
					dpartId = dparts.get(j).id;
				}

				if (mUserListView == null) {
					mUserListView = new UserList(user_listview, mActivity, UserList.USER_SEARCH,
							key, null);
				}

				mUserListView.search(schoolId, dpartId, key);
			}
		});

		spinner_city.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setSchoolSpinner();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner_school.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (schools.get(position).school.equals(mPrefUtil
						.getPreference(Params.LOCAL.SCHOOLID))) {
					spinner_dpart.setVisibility(View.VISIBLE);
				} else {
					spinner_dpart.setVisibility(View.INVISIBLE);
				}

				schoolId = schools.get(position).school;

				if (TextUtils.isEmpty(edit_keyword.getText().toString())) {
					if (mUserListView == null) {
						mUserListView = new UserList(user_listview, mActivity,
								UserList.USER_RECOMMEND, null, null);
					}

					if (!"0".equals(schoolId)) {
						mUserListView.recommend(schoolId, dpartId);
					}
				} else {
					btn_search.performClick();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner_dpart.setOnItemSelectedListener(new OnItemSelectedListener() {
			int mCount;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCount++;
				dpartId = dparts.get(position).id;
				if (mCount >= 2) {
					if (TextUtils.isEmpty(edit_keyword.getText().toString())
							&& !"0".equals(schoolId)) {
						mUserListView.recommend(schoolId, dpartId);
					} else {
						btn_search.performClick();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override
	public void ensureUi() {
		setCitySpinner();
	}

	private void setCitySpinner() {
		citys = PuApp.get().getLocalDataMgr().getCitys();

		if (null != citys) {
			// 将可选内容与ArrayAdapter连接起来
			cityAdapter = new ArrayAdapter<City>(this, R.layout.spinner_item_layout, citys);
			cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			spinner_city.setAdapter(cityAdapter);

			// 默认为用户所在城市
			cityId = mPrefUtil.getPreference(Params.LOCAL.CITYID);

			int i = 1;
			for (; i < citys.size(); i++) {
				if (citys.get(i).id.equals(cityId)) {
					citySelectionId = i;
					break;
				}
			}
			if (i >= citys.size()) {
				citySelectionId = 0;
			}

			spinner_city.setSelection(citySelectionId);
		}
	}

	private void initialSchoolSpinner() {
		// 默认为用户所在学校
		schoolId = mPrefUtil.getPreference(Params.LOCAL.SCHOOLID);

		if (null != schools) {
			int selection = 1;
			for (; selection < schools.size(); selection++) {
				if (schools.get(selection).school.equals(schoolId)) {
					schoolSelectionId = selection;
					break;
				}
			}
			if (selection >= schools.size()) {
				schoolSelectionId = 0;
			}

			spinner_school.setSelection(schoolSelectionId);

			new Api(dpartListCB, mActivity).getDepart(PuApp.get().getToken(), schoolId);
		}
	}

	private void setSchoolSpinner() {

		schools = PuApp.get().getLocalDataMgr().getSchools();

		if (null != schools) {
			int i = spinner_city.getSelectedItemPosition();

			if (i > 0 && i < citys.size()) {
				cityId = citys.get(i).id;

				for (int j = schools.size() - 1; j >= 0; j--) {
					School school = schools.get(j);
					String schoolCityId = school.cityId;

					if (!cityId.equals(schoolCityId)) {
						schools.remove(school);
					}
				}
			} else if (i != 0) {
				return;
			}

			// 将可选内容与ArrayAdapter连接起来
			schoolAdapter = new ArrayAdapter<School>(this, R.layout.spinner_item_layout, schools);
			schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			spinner_school.setAdapter(schoolAdapter);

			if (isInitSchoolSpinner) {
				initialSchoolSpinner();
				isInitSchoolSpinner = false;
			} else {
				spinner_school.setSelection(0);
			}
		}
	}

	CallBack dpartListCB = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dparts = JSONUtils.fromJson(response, new TypeToken<ArrayList<KeyValuePair>>() {
			});

			if (null != dparts) {
				if (cityId.equals(CUPID)) {
					dparts.add(0, new KeyValuePair("0", "选择分类"));
				} else {
					dparts.add(0, new KeyValuePair("0", "选择院系"));
				}

				dpartAdapter = new ArrayAdapter<KeyValuePair>(UserSearchActivity.this,
						R.layout.spinner_item_layout, dparts);
				dpartAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 将adapter 添加到spinner中
				spinner_dpart.setAdapter(dpartAdapter);
			}
		}
	};
}
