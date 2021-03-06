package com.xyhui.activity.app;

import java.util.ArrayList;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.xyhui.R;
import com.xyhui.types.Maplist;
import com.xyhui.utils.Params;
import com.xyhui.widget.FLActivity;

public class NavAddressListActivity extends FLActivity {
	private Button btn_back;
	private Button btn_clear;

	private EditText text_search;

	private ListView nav_address_listview;
	private NavAddressList mNavAddressListView;

	private ArrayList<Maplist> maps;

	@Override
	public void init() {
		if (getIntent().hasExtra(Params.INTENT_EXTRA.MAP)) {
			maps = getIntent().getParcelableArrayListExtra(Params.INTENT_EXTRA.MAP);
		} else {
			finish();
			return;
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_nav_address_list);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_clear = (Button) findViewById(R.id.btn_clear_nav);
		text_search = (EditText) findViewById(R.id.nav_search_text);
		nav_address_listview = (ListView) findViewById(R.id.nav_address_listview);
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

		btn_clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				text_search.setText("");
			}
		});

		text_search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				refreshList();
				if (TextUtils.isEmpty(text_search.getText())) {
					// 不显示清除按钮
					btn_clear.setVisibility(View.GONE);
				} else {
					btn_clear.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void refreshList() {
		if (null == mNavAddressListView) {
			mNavAddressListView = new NavAddressList(nav_address_listview, mActivity, maps);
		} else {
			mNavAddressListView.setKey(text_search.getText().toString());
			mNavAddressListView.refreshListViewStart();
		}
	}

	@Override
	public void ensureUi() {
		mNavAddressListView = new NavAddressList(nav_address_listview, mActivity, maps);
	}
}
