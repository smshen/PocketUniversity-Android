package com.xyhui.activity.group;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.activity.weibo.UserHomePageActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.GroupMember;
import com.xyhui.types.GroupMembers;
import com.xyhui.types.Response;
import com.xyhui.utils.Params;

public class GroupMemberList extends CListView {
	private String mGroupId;
	private int mListType;
	private int mIsMember;

	public GroupMemberList(PullToRefreshListView lv, Activity activity, String group_id,
			int listType, int isMember) {
		super(lv, activity);

		mGroupId = group_id;
		mListType = listType;
		mIsMember = isMember;

		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		this.setListItemResource(R.layout.list_item_user);
	}

	public void refresh() {
		refreshListViewStart();
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;

		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多成员");

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
				switch (mListType) {
				case GroupMemberManageActivity.SELECT_TYPE_USER:
					toHomepage(v);
					break;
				case GroupMemberManageActivity.SELECT_TYPE_MEMBER:
				case GroupMemberManageActivity.SELECT_TYPE_APPLY:
					showListDialog(v, mListType);
					break;
				}
			}
		});
	}

	private void toHomepage(View v) {
		// 打开个人主页
		int i = (Integer) v.getTag();
		GroupMember item = (GroupMember) mListItems.get(i);

		Intent intent = new Intent();
		intent.setClass(mActivity, UserHomePageActivity.class);
		intent.putExtra(Params.INTENT_EXTRA.USER_ID, item.uid);
		mActivity.startActivity(intent);
	}

	private void showListDialog(View v, int listType) {
		final int i = (Integer) v.getTag();
		final GroupMember item = (GroupMember) mListItems.get(i);
		final String uid = item.uid;

		final boolean isCreator = ((GroupMemberManageActivity) mActivity).isCreator();

		switch (listType) {
		case GroupMemberManageActivity.SELECT_TYPE_MEMBER:

			if (item.isManager()) {
				new AlertDialog.Builder(mActivity)
						.setTitle("成员管理")
						.setItems(R.array.member_manage_demote,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case 0:
											if (isCreator) {
												showConfirmActionDialog("normal",
														R.array.member_manage_demote, which, uid);
											} else {
												NotificationsUtil.ToastBottomMsg(mActivity,
														"你没有相关权限");
											}
											break;
										case 1:
											if (isCreator) {
												showConfirmActionDialog("out",
														R.array.member_manage_demote, which, uid);
											} else {
												NotificationsUtil.ToastBottomMsg(mActivity,
														"你没有相关权限");
											}

											break;
										}

										dialog.cancel();
									}
								}).show();
			} else if (!item.isCreator()) {
				new AlertDialog.Builder(mActivity)
						.setTitle("成员管理")
						.setItems(R.array.member_manage_promote,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case 0:
											if (isCreator) {
												showConfirmActionDialog("admin",
														R.array.member_manage_promote, which, uid);
											} else {
												NotificationsUtil.ToastBottomMsg(mActivity,
														"你没有相关权限");
											}

											break;
										case 1:
											showConfirmActionDialog("out",
													R.array.member_manage_promote, which, uid);
											break;
										}

										dialog.cancel();
									}
								}).show();
			}

			break;
		case GroupMemberManageActivity.SELECT_TYPE_APPLY:
			new AlertDialog.Builder(mActivity).setTitle("申请管理")
					.setItems(R.array.apply_manage, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								showConfirmActionDialog("allow", R.array.apply_manage, which, uid);
								break;
							case 1:
								showConfirmActionDialog("out", R.array.apply_manage, which, uid);
								break;
							}

							dialog.cancel();
						}
					}).show();
			break;
		}
	}

	private void showConfirmActionDialog(final String action, int arrayRes, int which,
			final String uid) {
		String[] items = mActivity.getResources().getStringArray(arrayRes);
		new AlertDialog.Builder(mActivity).setTitle("确定" + items[which] + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						new Api(memberActionCB, mActivity).memberAction(PuApp.get().getToken(),
								mGroupId, uid, action);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {
		GroupMember item = (GroupMember) obj;

		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		CListViewParam avatarLVP = new CListViewParam(R.id.img_avatar, R.drawable.avatar00, true);
		if (!TextUtils.isEmpty(item.face)) {
			avatarLVP.setImgAsync(true);
			avatarLVP.setItemTag(item.face);
		}
		LVP.add(avatarLVP);

		String name;

		if (0 == mIsMember || TextUtils.isEmpty(item.remark)) {
			name = item.name;
		} else {
			name = String.format("%s(%s)", item.name, item.remark);
		}

		LVP.add(new CListViewParam(R.id.text_nickname, name, true));
		LVP.add(new CListViewParam(R.id.text_school, item.name, false));
		LVP.add(new CListViewParam(R.id.text_params, item.getLevel() + " " + item.getInfo(), true));
		return LVP;
	}

	CallBack memberActionCB = new CallBack() {

		@Override
		public void onSuccess(String response) {

			Response result = JSONUtils.fromJson(response, Response.class);

			if (result.status == 1) {
				NotificationsUtil.ToastBottomMsg(mActivity, "成功");
				refreshListViewStart();
			} else if (!TextUtils.isEmpty(result.msg)) {
				NotificationsUtil.ToastBottomMsg(mActivity, result.msg);
			}
		}
	};

	ListCallBack<GroupMembers> callback = new ListCallBack<GroupMembers>(GroupMemberList.this) {
		@Override
		public void setType() {
			myType = new TypeToken<GroupMembers>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (mT != null && mT.data != null && !mT.data.isEmpty()) {
				for (int i = 0; i < mT.data.size(); i++) {
					mListItems.add(mT.data.get(i));
				}
			}
		}
	};

	@Override
	public void asyncData() {
		super.asyncData();
		switch (mListType) {
		case GroupMemberManageActivity.SELECT_TYPE_USER:
		case GroupMemberManageActivity.SELECT_TYPE_MEMBER:
			new Api(callback, mActivity).groupmember(PuApp.get().getToken(), mGroupId, "0",
					mPerpage, page);
			break;
		case GroupMemberManageActivity.SELECT_TYPE_APPLY:
			new Api(callback, mActivity).groupmember(PuApp.get().getToken(), mGroupId, "1",
					mPerpage, page);
			break;
		}
	}

}
