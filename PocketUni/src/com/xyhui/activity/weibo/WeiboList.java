package com.xyhui.activity.weibo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mslibs.utils.JSONUtils;
import com.mslibs.widget.CListView;
import com.mslibs.widget.CListViewParam;
import com.xyhui.R;
import com.xyhui.activity.ImageZoomActivity;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.ListCallBack;
import com.xyhui.types.Weibo;
import com.xyhui.types.WeiboTopicDetail;
import com.xyhui.types.WeiboTypeData;
import com.xyhui.utils.Params;

public class WeiboList extends CListView {
	public static final int TIMELINE = 1;
	public static final int METIONS = 2;
	public static final int TOPIC = 3;

	public static final int ALL = 4;
	public static final int MY_TOPIC = 5;
	public static final int ZAN = 6;

	private int list_type = 0;

	private String mTopicName;

	public WeiboList(PullToRefreshListView lv, Activity activity, int type) {
		// 在父类将执行 initListItemResource();ensureUi();
		super(lv, activity);

		list_type = type;

		if (TOPIC != list_type && MY_TOPIC != list_type) {
			initListViewStart();
		}
	}

	public void setTopicName(String topicName) {
		mTopicName = topicName;
		initListViewStart();
	}

	@Override
	public void initListItemResource() {
		setListItemResource(R.layout.list_item_weibo);
	}

	@Override
	public void ensureUi() {
		mPerpage = 10;
		super.setGetMoreResource(R.layout.list_item_getmore, R.id.list_item_getmore_title,
				"查看更多");
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
					Weibo transpond_data = JSONUtils.fromJson(weibo.transpond_data, Weibo.class);
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
		});
	}

	@Override
	public ArrayList<CListViewParam> matchListItem(Object obj, int index) {

		final Weibo weibo = (Weibo) obj;
		ArrayList<CListViewParam> LVP = new ArrayList<CListViewParam>();

		CListViewParam avatarLVP = new CListViewParam(R.id.img_avatar, R.drawable.avatar00, true);
		avatarLVP.setImgAsync(true);

		avatarLVP.setItemTag(weibo.face);
		avatarLVP.setOnclickLinstener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开个人主页
				Intent intent = new Intent();
				intent.setClass(mActivity, UserHomePageActivity.class);
				intent.putExtra(Params.INTENT_EXTRA.USER_ID, weibo.uid);
				mActivity.startActivity(intent);
			}
		});
		LVP.add(avatarLVP);

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
			imgweiboLVP.setOnclickLinstener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 放大图片浏览
					// Intent intent = new Intent();
					// intent.setClass(mActivity, WebViewActivity.class);
					// intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_TITLE, "图片浏览");
					// intent.putExtra(Params.INTENT_EXTRA.WEBVIEW_URL, img.thumbmiddleurl);
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
		LVP.add(new CListViewParam(R.id.text_params, String.format("转发(%s) |评论(%s) | %s(%s) ",
				weibo.transpond, weibo.comment, zan, weibo.heart), true));

		return LVP;
	}

	ListCallBack<ArrayList<Weibo>> callback = new ListCallBack<ArrayList<Weibo>>(WeiboList.this) {

		@Override
		public void setType() {
			myType = new TypeToken<ArrayList<Weibo>>() {
			}.getType();
		}

		@Override
		public void addItems() {
			if (mT != null && !mT.isEmpty()) {
				for (int i = 0; i < mT.size(); i++) {
					mListItems.add(mT.get(i));
				}
			}
		}
	};

	ListCallBack<WeiboTopicDetail> topicCallback = new ListCallBack<WeiboTopicDetail>(
			WeiboList.this) {

		@Override
		public void setType() {
			myType = WeiboTopicDetail.class;
		}

		@Override
		public String preProcess(String response) {
			// 由于服务端返回数据的格式无法直接通过Gson解析，需要做一些字符串处理
			// 典型的错误数据的情况："list":{"20019":{"weibo_id"...
			StringBuilder sb = new StringBuilder(response);

			// 该位置的字符需要替换为数组符号 [ 或者 ]
			int arrayIndex;
			arrayIndex = sb.indexOf("list");

			if (sb.indexOf("list\":null") != -1) {
				sb.replace(arrayIndex + 6, arrayIndex + 10, "[]");
			} else {
				// 替换掉"list":{"20019"中的{为[，后面查找":{"时就不会有list后面的匹配了
				sb.replace(arrayIndex + 6, arrayIndex + 7, "[");
				sb.replace(sb.length() - 2, sb.length() - 1, "]");
			}

			int start = 0, end = 0;
			// 从end开始查找，防止死循环
			while ((end = sb.indexOf("\":{\"", end)) != -1) {
				start = end - 1;
				end += 2;

				// 如果是图片数据"type_data":{"0"，跳过不处理
				if (sb.charAt(start) == 'a') {
					continue;
				}
				while ((sb.charAt(start)) != '\"') {
					start--;
				}

				// 如果是多张图片{"0":{"thumburl，不处理
				if (end - start > 4) {
					sb.delete(start, end);
				}
			}

			// VolleyLog.d(sb.toString());
			return sb.toString();
		}

		@Override
		public void addItems() {
			if (mT != null && mT.list != null && !mT.list.isEmpty()) {
				for (int i = 0; i < mT.list.size(); i++) {
					mListItems.add(mT.list.get(i));
				}
			}
		}
	};

	@Override
	public void asyncData() {
		super.asyncData();
		switch (list_type) {
		case TIMELINE:
			new Api(callback, mActivity).friends_timeline(PuApp.get().getToken(), mPerpage, page);
			break;
		case METIONS:
			new Api(callback, mActivity).mentions(PuApp.get().getToken(), mPerpage, page);
			break;
		case TOPIC:
			new Api(topicCallback, mActivity).topic(PuApp.get().getToken(), mTopicName, mPerpage,
					page);
			break;
		case MY_TOPIC:
			new Api(topicCallback, mActivity).mytopic(PuApp.get().getToken(), mTopicName,
					mPerpage, page);
			break;
		case ALL:
			new Api(callback, mActivity).weibo(PuApp.get().getToken(), mPerpage, page);
			break;
		case ZAN:
			new Api(callback, mActivity).indexZan(PuApp.get().getToken(), mPerpage, page);
			break;
		default:
			break;
		}
	}
}
