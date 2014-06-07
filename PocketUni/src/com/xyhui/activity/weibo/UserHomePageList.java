package com.xyhui.activity.weibo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.JSONUtils;
import com.mslibs.widget.CActivity;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.ImageZoomActivity;
import com.xyhui.activity.PuApp;
import com.xyhui.activity.Tab4FriendActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.User;
import com.xyhui.types.Weibo;
import com.xyhui.types.WeiboTypeData;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;

public class UserHomePageList extends CListView {

	private String mUserID;
	private String mUserName;

	private User mUser;

	public UserHomePageList(PullToRefreshListView lv, Activity activity, String user_id,
			String user_name) {
		super(lv, activity);
		mUserID = user_id;
		mUserName = user_name;

		// 在父类将执行 initListItemResource();ensureUi();
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		setListItemResource(R.layout.list_item_user_view_weibo);
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;
		super.setHeaderResource(R.layout.list_item_user_view_header);
		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多微博");
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

				if (i > 0 && mListItems.get(i) != null) {

					Weibo weibo = (Weibo) mListItems.get(i);
					// 打开查看微博正文
					Intent intent = new Intent();
					intent.setClass(mActivity, WeiboViewListActivity.class);

					// VolleyLog.d("WEIBO:%s", weibo.content);
					intent.putExtra(Params.INTENT_EXTRA.WEIBO, weibo);

					WeiboTypeData img = weibo.type_data;
					if (img != null) {
						img.fix();
						intent.putExtra(Params.INTENT_EXTRA.WEIBO_DATA, weibo.type_data);
					}

					if (weibo.transpond_data instanceof JsonObject) {
						Weibo transpond_data = JSONUtils.fromJson(weibo.transpond_data,
								Weibo.class);
						// VolleyLog.d("WEIBO_FORWARD:%s", null == transpond_data ? "null"
						// : transpond_data.content);
						intent.putExtra(Params.INTENT_EXTRA.WEIBO_FORWARD, transpond_data);

						WeiboTypeData trans_img = transpond_data.type_data;
						if (trans_img != null) {
							trans_img.fix();
							intent.putExtra(Params.INTENT_EXTRA.WEIBO_FORWARD_DATA, trans_img);
						}
					}
					mActivity.startActivity(intent);
				}
			}
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();
		if (index == 0 && mUser != null) {
			// 个人信息数据

			// 头像
			CListViewParam avatarLVP = new CListViewParam(R.id.img_avatar, R.drawable.avatar00,
					true);
			avatarLVP.setImgAsync(true);
			avatarLVP.setItemTag(mUser.face);
			avatarLVP.setImgRoundCorner(6);
			LVP.add(avatarLVP);

			// 昵称
			LVP.add(new CListViewParam(R.id.text_nickname, mUser.uname, true));

			String info = mUser.sex;

			if (!TextUtils.isEmpty(mUser.school)) {
				info = info + " " + mUser.school;
			}
			// 信息
			LVP.add(new CListViewParam(R.id.text_info, info, true));

			String uid = new PrefUtil().getPreference(Params.LOCAL.UID);
			// VolleyLog.d("got uid:%s  user.uid:%s  isfollowed:%s", uid, mUser.uid,
			// mUser.is_followed);

			if (!TextUtils.isEmpty(uid) && !uid.equalsIgnoreCase(mUser.uid)) {
				// 关注按钮

				boolean isfollow = !"unfollow".equalsIgnoreCase(mUser.is_followed);
				CListViewParam btn_follow;
				if (isfollow) {
					btn_follow = new CListViewParam(R.id.btn_follow,
							R.drawable.btn_selector_follow_release, true);
				} else {
					btn_follow = new CListViewParam(R.id.btn_follow,
							R.drawable.btn_selector_follow_add, true);
				}

				btn_follow.setItemTag(isfollow);
				btn_follow.setOnclickLinstener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Button btn = (Button) v;

						btn.setEnabled(false);

						boolean isfollow = (Boolean) btn.getTag();

						Api api = new Api(followcallback, mActivity);
						api.setExtra(btn);

						if (isfollow) {
							// 解除关注
							api.unfollow(PuApp.get().getToken(), mUser.uid);
						} else {
							api.follow(PuApp.get().getToken(), mUser.uid);
						}
					}
				});
				LVP.add(btn_follow);
			}

			if (!TextUtils.isEmpty(uid) && !uid.equalsIgnoreCase(mUser.uid)) {
				// 私信按钮
				CListViewParam btn_message = new CListViewParam(R.id.btn_message,
						R.drawable.btn_selector_message, true);

				btn_message.setOnclickLinstener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 打开私信回复
						Intent intent = new Intent();
						intent.setClass(mActivity, ChatDetailListActivity.class);
						intent.putExtra(Params.INTENT_EXTRA.MESSAGE_UID, mUser.uid);
						intent.putExtra(Params.INTENT_EXTRA.USERNAME, mUser.uname);
						mActivity.startActivity(intent);
					}
				});
				LVP.add(btn_message);
			}

			// 微博按钮
			CListViewParam btn_params_weibo = new CListViewParam(R.id.btn_params_weibo, null, true);
			btn_params_weibo.setOnclickLinstener(new OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
			LVP.add(btn_params_weibo);
			// 微博数量
			LVP.add(new CListViewParam(R.id.text_params_weibo, "" + mUser.weibo_count, true));
			// 粉丝按钮
			CListViewParam btn_params_follows = new CListViewParam(R.id.btn_params_follows, null,
					true);
			btn_params_follows.setOnclickLinstener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 打开粉丝列表
					Intent intent = new Intent();
					intent.setClass(mActivity, Tab4FriendActivity.class);
					intent.putExtra(Params.INTENT_EXTRA.FRIENDLIST_USERID, mUser.uid);
					intent.putExtra(Params.INTENT_EXTRA.FRIENDLIST_TYPE,
							Params.INTENT_VALUE.FRIENDLIST_FOLLOWS);
					mActivity.startActivity(intent);
				}
			});
			LVP.add(btn_params_follows);
			// 粉丝数量
			LVP.add(new CListViewParam(R.id.text_params_follows, "" + mUser.followers_count, true));
			// 关注按钮
			CListViewParam btn_params_followed = new CListViewParam(R.id.btn_params_followed,
					null, true);
			btn_params_followed.setOnclickLinstener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 打开关注列表
					Intent intent = new Intent();
					intent.setClass(mActivity, Tab4FriendActivity.class);
					intent.putExtra(Params.INTENT_EXTRA.FRIENDLIST_USERID, mUser.uid);
					intent.putExtra(Params.INTENT_EXTRA.FRIENDLIST_TYPE,
							Params.INTENT_VALUE.FRIENDLIST_FOLLOWED);
					mActivity.startActivity(intent);
				}
			});
			LVP.add(btn_params_followed);
			// 关注数量
			LVP.add(new CListViewParam(R.id.text_params_followed, "" + mUser.followed_count, true));

		} else {
			if (obj == null) {
				LVP.add(new CListViewParam(R.id.text_empty_tips, null, true));
				LVP.add(new CListViewParam(R.id.text_nickname, null, false));
				LVP.add(new CListViewParam(R.id.text_content, null, false));
				LVP.add(new CListViewParam(R.id.img_weibo, null, false));
				LVP.add(new CListViewParam(R.id.text_forward, null, false));
				LVP.add(new CListViewParam(R.id.text_datefrom, null, false));
				LVP.add(new CListViewParam(R.id.text_params, null, false));
			} else if (obj instanceof Weibo) {

				LVP.add(new CListViewParam(R.id.text_empty_tips, null, false));

				// 个人微博数据适配
				Weibo weibo = (Weibo) obj;

				LVP.add(new CListViewParam(R.id.text_nickname, weibo.uname, true));

				// VolleyLog.d("content:%s", weibo.content);

				LVP.add(new CListViewParam(R.id.text_content, weibo.content, true));

				final WeiboTypeData img = weibo.type_data;
				if (img == null) {
					LVP.add(new CListViewParam(R.id.img_weibo, null, false));
				} else {
					img.fix();

					LVP.add(new CListViewParam(R.id.img_weibo, R.drawable.img_default, true));

					CListViewParam imgweiboLVP = new CListViewParam(R.id.img_weibo,
							R.drawable.img_default, true);
					imgweiboLVP.setImgAsync(true);
					imgweiboLVP.setItemTag(img.thumburl);
					// VolleyLog.d("img.thumburl:%s", img.thumburl);
					imgweiboLVP.setOnclickLinstener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// 放大图片浏览
							// Intent intent = new Intent();
							// intent.setClass(mActivity, WebViewActivity.class);
							// intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TITLE, "图片浏览");
							// intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL,
							// img.thumbmiddleurl);
							// intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TYPE,
							// WebViewActivity.TYPE_IMAGE);
							// mActivity.startActivity(intent);

							Intent intent = new Intent(mActivity, ImageZoomActivity.class);
							intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL, img.thumbmiddleurl);
							mActivity.startActivity(intent);
						}
					});
					LVP.add(imgweiboLVP);
				}

				if (weibo.transpond_data instanceof JsonObject) {
					Weibo transpond_data = JSONUtils.fromJson(weibo.transpond_data, Weibo.class);
					// VolleyLog.d("jsonobject,transpond_data=%s", null == transpond_data ? "null"
					// : transpond_data.content);
					LVP.add(new CListViewParam(R.id.text_forward, transpond_data, true));
				} else {
					LVP.add(new CListViewParam(R.id.text_forward, null, false));
				}

				LVP.add(new CListViewParam(R.id.text_datefrom, weibo.ctime, true));
				String zan = weibo.is_hearted == 1 ? "已赞" : "赞";
				LVP.add(new CListViewParam(R.id.text_params, String.format(
						"转发(%s) |评论(%s) | %s(%s) ", weibo.transpond, weibo.comment, zan,
						weibo.heart), true));

			}
		}
		return LVP;
	}

	ListCallBack<ArrayList<Weibo>> callback = new ListCallBack<ArrayList<Weibo>>(
			UserHomePageList.this) {
		@Override
		public void setType() {
			myType = new TypeToken<ArrayList<Weibo>>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (GETMORE != actionType) {
				mListItems.add("weiboheader");
			}

			if (mT != null && !mT.isEmpty()) {
				for (int i = 0; i < mT.size(); i++) {
					mListItems.add(mT.get(i));
				}
			} else if (GETMORE != actionType) {
				// reply empty
				mListItems.add(null);
			}
		}
	};

	CallBack usercallback = new CallBack() {

		@Override
		public void onSuccess(String response) {
			((CActivity) mActivity).dismissProgress();
			mUser = JSONUtils.fromJson(response, User.class);

			if (null == mUser) {
				new AlertDialog.Builder(mActivity).setCancelable(false).setTitle("用户不存在")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								((Activity) mActivity).finish();
							}
						}).show();
			} else if (mUser.status instanceof JsonObject) {
				((CActivity) mActivity).showProgress();
				mUserID = mUser.uid;
				new Api(callback, mActivity).user_timeline(PuApp.get().getToken(), mUserID,
						mPerpage, page);
			} else if (GETMORE == actionType) {
				getmoreListViewFinish();
				actionType = IDLE;
			} else if (INIT == actionType) {
				mListItems.clear();
				mDateList.clear();
				mListItems.add("weiboheader");
				// reply empty
				mListItems.add(null);

				initListViewFinish();
				actionType = IDLE;
			} else if (REFRESH == actionType) {
				mListItems.clear();
				mDateList.clear();
				mListItems.add("weiboheader");
				// reply empty
				mListItems.add(null);

				refreshListViewFinish();
				actionType = IDLE;
			}
		}

		@Override
		public void onFailure(String message) {
			actionType = IDLE;
			((CActivity) mActivity).dismissProgress();
		};
	};

	@Override
	public void asyncData() {

		super.asyncData();

		if (mUserID != null) {
			new Api(usercallback, mActivity).showuser(PuApp.get().getToken(), mUserID);
		} else if (mUserName != null) {
			new Api(usercallback, mActivity).showuserbyname(PuApp.get().getToken(), mUserName);
		}
	}

	CallBack followcallback = new CallBack() {
		@Override
		public void onSuccess(String response) {

			Button btn = (Button) this.getExtra();
			boolean isfollow = (Boolean) btn.getTag();
			if (isfollow) {
				btn.setBackgroundResource(R.drawable.btn_selector_follow_add);
				btn.setTag(false);
			} else {
				btn.setBackgroundResource(R.drawable.btn_selector_follow_release);
				btn.setTag(true);
			}
			btn.setEnabled(true);
		}
	};
}