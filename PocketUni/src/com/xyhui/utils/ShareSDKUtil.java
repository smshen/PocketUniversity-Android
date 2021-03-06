/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package com.xyhui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.mslibs.utils.NotificationsUtil;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;

/**
 * ShareSDK 官网地址 ： http://www.sharesdk.cn </br> 1、ShareSDK接口演示页面</br> 包括演示使用快捷分享完成图文分享、</br>
 * 无页面直接分享、授权、关注和不同平台的分享等等功能。</br>
 * 
 * 2、如果要咨询客服，请加企业QQ 4006852216 </br> 3、咨询客服时，请把问题描述清楚，最好附带错误信息截图 </br>
 * 4、一般问题，集成文档中都有，请先看看集成文档；减少客服压力，多谢合作 ^_^ 5、由于客服压力巨大，每个月难免有那么几天，请见谅
 */
public class ShareSDKUtil {
	private static final String POCKETUNI_PAGE = "http://pocketuni.net";

	// 使用快捷分享完成分享（请务必仔细阅读位于SDK解压目录下Docs文件夹中OnekeyShare类的JavaDoc）
	/**
	 * ShareSDK集成方法有两种</br> 1、第一种是引用方式，例如引用onekeyshare项目，onekeyshare项目再引用mainlibs库</br>
	 * 2、第二种是把onekeyshare和mainlibs集成到项目中，本例子就是用第二种方式</br> 请看“ShareSDK 使用说明文档”，SDK下载目录中 </br>
	 * 或者看网络集成文档
	 * http://wiki.sharesdk.cn/Android_%E5%BF%AB%E9%80%9F%E9%9B%86%E6%88%90%E6%8C%87%E5%8D%97
	 * 3、混淆时，把sample或者本例子的混淆代码copy过去，在proguard-project.txt文件中
	 * 
	 * 
	 * 平台配置信息有三种方式： 1、在我们后台配置各个微博平台的key
	 * 2、在代码中配置各个微博平台的key，http://sharesdk.cn/androidDoc/cn/sharesdk/framework/ShareSDK.html
	 * 3、在配置文件中配置，本例子里面的assets/ShareSDK.conf,
	 */
	public static void showShare(final Context context, boolean silent, final String content,
			String imagePath, String imageUrl) {
		final OnekeyShare oks = new OnekeyShare();

		// 分享时Notification的图标和文字
		oks.setNotification(R.drawable.share_notify_icon, context.getString(R.string.app_name));
		// address是接收人地址，仅在信息和邮件使用
		oks.setAddress("请选择联系人");
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(context.getString(R.string.share));
		// for人人,QQ空间
		oks.setTitleUrl(POCKETUNI_PAGE);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(content);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		if (imagePath != null) {
			oks.setImagePath(imagePath);
		}
		// imageUrl是图片的网络路径，新浪微博、人人网、QQ空间、微信的两个平台、Linked-In支持此字段
		if (imageUrl != null) {
			oks.setImageUrl(imageUrl);
		}
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment(context.getString(R.string.share));
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(context.getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(POCKETUNI_PAGE);
		// 是否直接分享（true则直接分享）
		oks.setSilent(silent);

		// 构造一个图标
		Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.pu_share);
		// 定义图标的标签
		String label = context.getResources().getString(R.string.app_name);

		final CallBack sharecallback = new CallBack() {
			@Override
			public void onSuccess(String response) {
				NotificationsUtil.ToastBottomMsg(context, "分享成功");
			}

			@Override
			public void onFailure(String message) {
				NotificationsUtil.ToastBottomMsg(context, "分享失败");
			}
		};

		// 图标点击后会通过Toast提示消息
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Api(sharecallback, context).update(PuApp.get().getToken(), content);
				oks.finish();
			}
		};

		oks.setCustomerLogo(logo, label, listener);
		oks.show(context);
	}
}
