package com.xyhui.activity.app;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.types.City;
import com.xyhui.types.School;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.widget.FLActivity;

public class NavMapListActivity extends FLActivity {
	private Button btn_back;

	private ListView nav_map_listview;
	private NavMapList mNavMapListView;

	private Spinner spinner_city;
	private ArrayAdapter<City> cityAdapter;
	private ArrayList<City> citys;
	private String cityId;
	private int citySelectionId;

	private Spinner spinner_school;
	private ArrayAdapter<School> schoolAdapter;
	private ArrayList<School> schools;
	private String schoolId;
	private int schoolSelectionId;

	private boolean isInitSchoolSpinner = true;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_nav_map_list);

		btn_back = (Button) findViewById(R.id.btn_back);
		nav_map_listview = (ListView) findViewById(R.id.nav_map_listview);
		spinner_school = (Spinner) findViewById(R.id.spinner_school);
		spinner_city = (Spinner) findViewById(R.id.spinner_city);
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
				search();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

	}

	@Override
	public void ensureUi() {
		setCitySpinner();
		openGPSSettings();

	}

	private void setCitySpinner() {
		citys = PuApp.get().getLocalDataMgr().getCitys();
		if (citys != null) {
			// 将可选内容与ArrayAdapter连接起来
			cityAdapter = new ArrayAdapter<City>(this, R.layout.spinner_item_layout, citys);
			cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			spinner_city.setAdapter(cityAdapter);

			// 默认为用户所在城市
			cityId = new PrefUtil().getPreference(Params.LOCAL.CITYID);

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
		schoolId = new PrefUtil().getPreference(Params.LOCAL.SCHOOLID);

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

	void search() {

		String sid = "0";

		int i = spinner_school.getSelectedItemPosition();
		if (schools != null && i < schools.size()) {
			sid = schools.get(i).school;
		}
		if (mNavMapListView != null) {
			mNavMapListView.search(sid);
		} else {
			mNavMapListView = new NavMapList(nav_map_listview, mActivity, sid);
		}
	}

	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		} else {

			new AlertDialog.Builder(mActivity).setTitle("GPS尚未打开! \n 现在设置吗?")
					.setPositiveButton("设置", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(myIntent);
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						}
					}).show();
		}
	}
}
