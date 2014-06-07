package com.xyhui.activity.event;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.activity.Tab3AppActivity;
import com.xyhui.activity.WebViewActivity;
import com.xyhui.activity.more.PhoneBindingActivity;
import com.xyhui.activity.more.QRCardViewActivity;
import com.xyhui.activity.more.QRCodeScanActivity;
import com.xyhui.activity.weibo.UserHomePageActivity;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.api.Client;
import com.xyhui.types.EventBanner;
import com.xyhui.types.EventUser;
import com.xyhui.types.Response;
import com.xyhui.utils.DownloadImpl;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.utils.ShareSDKUtil;
import com.xyhui.widget.FLActivity;

public class EventViewActivity extends FLActivity {
	private final String MP_ID_1 = "25150";
	private final String MP_ID_2 = "25139";
	private final String MP_ID_3 = "25134";

	private PrefUtil mPrefUtil;

	private Button btn_back;
	private Button btn_more;

	private Button btn_join;
	private Button btn_cancel;
	private Button btn_checkin;
	private Button btn_share;
	private Button btn_favor;

	private LinearLayout event_detail_layout;

	private EventBanner event;
	private ImageView img_event_banner;
	private TextView text_event_title;
	private ImageView img_avatar_mask;
	private TextView text_nickname;
	private TextView text_notice;
	private RelativeLayout user_layout;

	private LinearLayout userlist_layout;
	private Button btn_more_user;

	private String eventid;
	private final int RECOMMEND_VOTE_COUNT = 3;

	public final static String PLAY_PAGE_PREFIX = "http://pocketuni.net/index.php?app=event&mod=Front&act=playerDetails&";

	@Override
	public void init() {
		mPrefUtil = new PrefUtil();

		if (getIntent().hasExtra(Params.INTENT_EXTRA.EVENTID)) {
			eventid = getIntent().getStringExtra(Params.INTENT_EXTRA.EVENTID);

			if (TextUtils.isEmpty(eventid)) {
				finish();
			}
		}
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_event_view);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_more = (Button) findViewById(R.id.btn_more);
		btn_join = (Button) findViewById(R.id.btn_join);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_checkin = (Button) findViewById(R.id.btn_checkin);
		btn_share = (Button) findViewById(R.id.btn_share);
		btn_favor = (Button) findViewById(R.id.btn_favor);
		img_event_banner = (ImageView) findViewById(R.id.img_event_banner);
		img_avatar_mask = (ImageView) findViewById(R.id.img_avatar_mask);
		text_event_title = (TextView) findViewById(R.id.text_event_title);
		text_nickname = (TextView) findViewById(R.id.text_nickname);
		text_notice = (TextView) findViewById(R.id.text_notice);
		event_detail_layout = (LinearLayout) findViewById(R.id.event_detail_layout);
		user_layout = (RelativeLayout) findViewById(R.id.user_layout);
		userlist_layout = (LinearLayout) findViewById(R.id.userlist_layout);
		btn_more_user = (Button) findViewById(R.id.btn_more_user);
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

		btn_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle("选择操作");

				builder.setItems(R.array.event_lucky_admin, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							// 抽签
							Intent intent = new Intent(mActivity, EventLuckyActivity.class);
							intent.putExtra(Params.INTENT_EXTRA.EVENTID, eventid);
							startActivity(intent);
							break;
						case 1:
							// 查看签到码
							new AlertDialog.Builder(mActivity)
									.setTitle(event.adminCode)
									.setPositiveButton("确定",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
														int whichButton) {
												}
											}).show();
							break;
						}
						dialog.cancel();
					}
				}).show();
			}
		});

		img_event_banner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMiaoPiaoDiglog();
			}
		});

		btn_join.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mobile = mPrefUtil.getPreference(Params.LOCAL.MOBILE);
				if (event.need_tel == 1 && TextUtils.isEmpty(mobile)) {
					new AlertDialog.Builder(mActivity).setTitle("您还没有绑定手机, 现在去绑定吗?")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									Intent intent = new Intent(mActivity,
											PhoneBindingActivity.class);
									startActivity(intent);
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
								}
							}).show();
				} else {
					showProgress("正在提交请求", "请稍候...");
					new Api(joinCallback, mActivity).joinEvent(PuApp.get().getToken(), eventid);
				}
			}
		});

		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgress("正在提交请求", "请稍候...");
				new Api(cancelJoinCB, mActivity).cancelJoinEvent(PuApp.get().getToken(), eventid);
			}
		});

		event_detail_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开活动详情
				Intent intent = new Intent();
				intent.setClass(mActivity, EventDetailActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.EVENTID, eventid);
				startActivity(intent);
			}
		});

		user_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mActivity, UserHomePageActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.USER_ID, event.uid);
				startActivity(intent);
			}
		});

		btn_more_user.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开人气排行
				Intent intent = new Intent(mActivity, EventUserListActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.EVENTID, eventid);
				startActivity(intent);
			}
		});

		btn_checkin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (event.uid.equals(mPrefUtil.getPreference(Params.LOCAL.UID))) {
					Intent intent = new Intent(mActivity, QRCodeScanActivity.class);
					intent.putExtra(Params.INTENT_EXTRA.ATTEND_CODE, event.adminCode);
					startActivity(intent);
				} else if (event.showAttend == 1) {
					Intent intent = new Intent(mActivity, QRCardViewActivity.class);
					startActivity(intent);
				}
			}
		});

		btn_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = "分享活动:【" + event.title + "】http://pocketuni.net/eventf/"
						+ eventid;
				ShareSDKUtil.showShare(mActivity, false, content, null, null);
			}
		});

		btn_favor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (btn_favor == null)
					return;

				int hasfav = (Integer) btn_favor.getTag();
				btn_favor.setEnabled(false);
				if (hasfav == 1) {
					new Api(favcallback, mActivity).eventFav(PuApp.get().getToken(), eventid,
							"cancel");
				} else {
					new Api(favcallback, mActivity).eventFav(PuApp.get().getToken(), eventid,
							"add");
				}
			}
		});
	}

	@Override
	public void ensureUi() {
		showProgress();
		showProgress();
		new Api(callback, mActivity).getEvent(PuApp.get().getToken(), eventid);
		new Api(useristcallback, mActivity).getPlayerList(PuApp.get().getToken(), eventid, null,
				3, 1);

		showMiaoPiaoDiglog();
	}

	@Override
	protected void onResume() {
		super.onResume();
	};

	private void showMiaoPiaoDiglog() {
		if (MP_ID_1.equals(eventid) || MP_ID_2.equals(eventid) || MP_ID_3.equals(eventid)) {
			new AlertDialog.Builder(mActivity).setTitle("下载秒拍？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							DownloadImpl download = new DownloadImpl(mActivity,
									Tab3AppActivity.MIAOPAI_DOWNLOAD_URL, "秒拍",
									Tab3AppActivity.MIAOPAI_PKG_NAME + ".apk");
							download.startDownload();
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).show();
		}
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			event = JSONUtils.fromJson(response, EventBanner.class);

			if (null == event) {
				NotificationsUtil.ToastBottomMsg(mActivity, "数据异常");
				finish();
			} else if (event.status == 0) {
				NotificationsUtil.ToastBottomMsg(mActivity, event.msg);
				finish();
			} else {
				if (!TextUtils.isEmpty(event.banner)) {
					String img = Client.BANNER_URL_PREFIX + event.banner;
					UrlImageViewHelper.setUrlDrawable(img_event_banner, img,
							R.drawable.img_default);
				} else {
					img_event_banner.setImageResource(R.drawable.none);
				}

				text_event_title.setText(event.title);

				UrlImageViewHelper.setUrlDrawable(img_avatar_mask, event.uface,
						R.drawable.img_default);

				text_nickname.setText(event.uname);

				text_notice.setText(event.getNotice());

				if (event.hasJoin != 1 && event.canJoin == 1) {
					btn_join.setVisibility(View.VISIBLE);
				} else {
					btn_join.setVisibility(View.GONE);
				}

				// uid表示发起者id 和 本地uid作比较
				if ("1".equals(event.isStart) && event.hasJoin == 1
						&& !event.uid.equals(mPrefUtil.getPreference(Params.LOCAL.UID))) {
					btn_cancel.setVisibility(View.VISIBLE);
				} else {
					btn_cancel.setVisibility(View.GONE);
				}

				if (event.showAttend == 1
						|| event.uid.equals(mPrefUtil.getPreference(Params.LOCAL.UID))) {
					btn_checkin.setVisibility(View.VISIBLE);
				} else {
					btn_checkin.setVisibility(View.GONE);
				}

				if (event.hasFav) {
					btn_favor.setText("取消收藏");
					btn_favor.setTag(1);
				} else {
					btn_favor.setText("收藏活动");
					btn_favor.setTag(0);
				}

				if (event.uid.equals(mPrefUtil.getPreference(Params.LOCAL.UID))) {
					btn_more.setVisibility(View.VISIBLE);
				} else {
					btn_more.setVisibility(View.GONE);
				}
				btn_favor.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();
			NotificationsUtil.ToastBottomMsg(mActivity, message);
			finish();
		}
	};

	CallBack useristcallback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			ArrayList<EventUser> items = JSONUtils.fromJson(response,
					new TypeToken<ArrayList<EventUser>>() {
					});

			LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			userlist_layout.removeAllViews();
			int count;
			if (null != items && (count = items.size()) > 0) {
				if (count > RECOMMEND_VOTE_COUNT) {
					count = RECOMMEND_VOTE_COUNT;
				}
				for (int i = 0; i < count; i++) {
					final EventUser item = (EventUser) items.get(i);

					View userItemView = mInflater.inflate(R.layout.list_item_event_user, null);

					if (userItemView != null) {
						ImageView img_avatar = (ImageView) userItemView
								.findViewById(R.id.img_avatar);
						// 需要异步图像，默认图像：img_default

						UrlImageViewHelper.setUrlDrawable(img_avatar, item.path,
								R.drawable.img_default);

						TextView text_nickname = (TextView) userItemView
								.findViewById(R.id.text_nickname);
						text_nickname.setText(item.realname);

						TextView text_params = (TextView) userItemView
								.findViewById(R.id.text_params);
						text_params.setText(item.getInfo());

						TextView text_vote = (TextView) userItemView.findViewById(R.id.text_vote);
						text_vote.setText(item.ticket + " 票");

						final Button btn_vote = (Button) userItemView.findViewById(R.id.btn_vote);

						if (item.canVote) {
							btn_vote.setVisibility(View.VISIBLE);
							btn_vote.setTag(item.id);
							btn_vote.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {

									final String realname = item.realname;
									final String id = v.getTag().toString();
									final Button btn_vote = (Button) v;

									new AlertDialog.Builder(mActivity)
											.setTitle("您是否确定把今天的票投给" + realname + "?")
											.setPositiveButton("确定",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {

															btn_vote.setText("正在投票");
															btn_vote.setEnabled(false);
															Api api = new Api(votecallback,
																	mActivity);
															api.setExtra(btn_vote);
															api.vote(PuApp.get().getToken(), id,
																	eventid);
														}
													})
											.setNegativeButton("取消",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {
														}
													}).show();
								}
							});
						} else {
							btn_vote.setVisibility(View.GONE);
						}

						userItemView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// 选手详情
								Intent intent = new Intent(mActivity, WebViewActivity.class);
								intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TITLE, item.realname);
								intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL, PLAY_PAGE_PREFIX
										+ "id=" + event.id + "&pid=" + item.id);
								intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_MECHANISM,
										WebViewActivity.POST);
								startActivity(intent);
							}
						});

					}
					userlist_layout.addView(userItemView);
				}
			}
		}
	};

	CallBack votecallback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			Button btn = (Button) getExtra();
			if (response.contains("true")) {
				btn.setText("己投票");
				new Api(useristcallback, mActivity).getPlayerList(PuApp.get().getToken(), eventid,
						null, RECOMMEND_VOTE_COUNT, 1);
			} else {
				btn.setText("票已投完");
			}
		}
	};

	CallBack favcallback = new CallBack() {
		@Override
		public void onSuccess(String response) {

			if (btn_favor == null)
				return;

			int hasfav = (Integer) btn_favor.getTag();

			if (hasfav == 0) {
				btn_favor.setText("取消收藏");
				btn_favor.setTag(1);
				NotificationsUtil.ToastBottomMsg(mActivity, "己收藏成功");
			} else {
				btn_favor.setText("收藏活动");
				btn_favor.setTag(0);
				NotificationsUtil.ToastBottomMsg(mActivity, "己取消收藏");
			}

			btn_favor.setEnabled(true);
		}
	};

	CallBack joinCallback = new CallBack() {

		@Override
		public void onSuccess(String response) {
			dismissProgress();

			if ("true".equalsIgnoreCase(response)) {
				NotificationsUtil.ToastBottomMsg(getBaseContext(), "加入成功");
				showProgress();
				new Api(callback, mActivity).getEvent(PuApp.get().getToken(), eventid);
			} else {
				NotificationsUtil.ToastBottomMsg(getBaseContext(), "加入失败");
			}
		}
	};

	CallBack cancelJoinCB = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			Response r = JSONUtils.fromJson(response, Response.class);

			if (1 == r.status) {
				NotificationsUtil.ToastBottomMsg(mActivity, r.msg);
			} else {
				NotificationsUtil.ToastBottomMsg(mActivity, "取消失败");
			}

			showProgress();
			new Api(callback, mActivity).getEvent(PuApp.get().getToken(), eventid);
		}
	};
}