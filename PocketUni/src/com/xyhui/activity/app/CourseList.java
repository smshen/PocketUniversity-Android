package com.xyhui.activity.app;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.Course;
import com.xyhui.utils.Params;

public class CourseList extends CListView {
	public static final int COURSE_ALL = 0;
	public static final int COURSE_SEARCH = 1;
	public static final int COURSE_MY_JOIN = 2;

	private int course_type = COURSE_ALL;
	private String sortID;
	private String schoolID;
	private String keyword;

	public CourseList(PullToRefreshListView lv, Activity activity, int type) {
		super(lv, activity);
		course_type = type;
		initListViewStart();
	}

	public CourseList(PullToRefreshListView lv, Activity activity, int type, String sid,
			String cid, String key) {
		// 在父类将执行 initListItemResource();ensureUi();
		super(lv, activity);

		course_type = type;
		schoolID = sid;
		sortID = cid;
		keyword = key;

		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		setListItemResource(R.layout.list_item_course);
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;
		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多课程");
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
				Course item = (Course) mListItems.get(i);

				// 打开课程详情页
				Intent intent = new Intent();
				intent.setClass(mActivity, CourseViewActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.COURSEID, item.id);

				mActivity.startActivity(intent);
			}
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		Course item = (Course) obj;
		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		CListViewParam avatarLVP = new CListViewParam(R.id.img_avatar, R.drawable.img_default,
				true);
		avatarLVP.setImgAsync(true);
		avatarLVP.setItemTag(item.logoId);
		LVP.add(avatarLVP);

		LVP.add(new CListViewParam(R.id.text_title, item.title, true));
		LVP.add(new CListViewParam(R.id.text_tips, item.getTips(), true));
		return LVP;
	}

	@Override
	public void asyncData() {
		super.asyncData();
		if (COURSE_MY_JOIN == course_type) {
			new Api(callback, mActivity).getMyCourseList(PuApp.get().getToken(), "join", mPerpage,
					page);
		} else {
			new Api(callback, mActivity).getCourseList(PuApp.get().getToken(), "", schoolID,
					sortID, keyword, mPerpage, page);
		}
	}

	ListCallBack<ArrayList<Course>> callback = new ListCallBack<ArrayList<Course>>(CourseList.this) {
		@Override
		public void setType() {
			myType = new TypeToken<ArrayList<Course>>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (mT != null && !mT.isEmpty()) {
				for (int i = 0; i < mT.size(); i++) {
					mListItems.add(mT.get(i));
				}
			} else {
				NotificationsUtil.ToastTopMsg(mActivity, "没有搜索到相关信息");
				return;
			}
		}
	};

	public void search(String schoolId, String sortId, String key) {
		course_type = COURSE_SEARCH;
		schoolID = schoolId;
		sortID = sortId;
		keyword = key;
		initListViewStart();
	}
}
