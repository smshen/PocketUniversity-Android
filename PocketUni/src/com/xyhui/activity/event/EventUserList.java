package com.xyhui.activity.event;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.activity.WebViewActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.EventUser;
import com.xyhui.utils.Params;

public class EventUserList extends CListView {
	private String eventID;
	private String mKey;

	public EventUserList(PullToRefreshListView lv, Activity activity, String eid, String key) {
		super(lv, activity);
		eventID = eid;
		mKey = key;

		// 在父类将执行 initListItemResource();ensureUi();
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		this.setListItemResource(R.layout.list_item_event_user);
	}

	public void refresh() {
		refreshListViewStart();
	}

	public void search(String key) {
		mKey = key;
		refreshListViewStart();
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;
		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多人气排行");

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
				EventUser item = (EventUser) mListItems.get(i);

				// 打开选手详情
				Intent intent = new Intent();
				intent.setClass(mActivity, WebViewActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TITLE, item.realname);
				intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL,
						EventViewActivity.PLAY_PAGE_PREFIX + "id=" + eventID + "&pid=" + item.id);
				mActivity.startActivity(intent);
			}
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		final EventUser item = (EventUser) obj;

		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		CListViewParam imgAvatarLVP = new CListViewParam(R.id.img_avatar, R.drawable.img_default,
				true);
		imgAvatarLVP.setImgAsync(true);
		imgAvatarLVP.setItemTag(item.path);
		LVP.add(imgAvatarLVP);

		LVP.add(new CListViewParam(R.id.text_nickname, item.realname, true));
		LVP.add(new CListViewParam(R.id.text_params, item.getInfo(), true));
		LVP.add(new CListViewParam(R.id.text_vote, item.ticket + " 票", true));

		if (item.canVote) {
			final CListViewParam btnVoteLVP = new CListViewParam(R.id.btn_vote, "投TA一票", true);
			btnVoteLVP.setItemTag(item.id);
			btnVoteLVP.setOnclickLinstener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					final String id = v.getTag().toString();
					final Button btn_vote = (Button) v;

					new AlertDialog.Builder(mActivity)
							.setTitle("您是否确定把今天的票投给" + item.realname + "?")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									btn_vote.setText("正在投票");
									btn_vote.setEnabled(false);
									Api api = new Api(votecallback, mActivity);
									api.setExtra(btn_vote);
									api.vote(PuApp.get().getToken(), id, eventID);
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									dialog.cancel();
								}
							}).show();

				}
			});

			LVP.add(btnVoteLVP);
		} else {
			LVP.add(new CListViewParam(R.id.btn_vote, "", false));
		}
		return LVP;
	}

	ListCallBack<ArrayList<EventUser>> callback = new ListCallBack<ArrayList<EventUser>>(
			EventUserList.this) {
		@Override
		public void setType() {
			myType = new TypeToken<ArrayList<EventUser>>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (mT != null) {
				for (int i = 0; i < mT.size(); i++) {
					mListItems.add(mT.get(i));
				}
			}

		}
	};

	@Override
	public void asyncData() {
		super.asyncData();
		new Api(callback, mActivity).getPlayerList(PuApp.get().getToken(), eventID, mKey,
				mPerpage, page);
	}

	CallBack votecallback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			Button btn = (Button) getExtra();

			if (response.contains("true")) {
				btn.setText("己投票");
			} else {
				btn.setText("票已投完");
			}
		}
	};

}
