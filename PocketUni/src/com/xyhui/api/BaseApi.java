package com.xyhui.api;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mslibs.utils.VolleyLog;
import com.xyhui.activity.PuApp;

public class BaseApi {

	public static final String EXCEPTION_UNKNOWNHOST = "unknownhost";
	public static final String EXCEPTION_CONNECT = "connect";
	public static final String EXCEPTION_SOCKET = "socket";
	public static final String EXCEPTION_SOCKET_TIMEOUT = "socket_timeout";

	private static final String INVALID_TOKEN = "{\"message\":\"认证失败\",\"code\":\"00001\"}";

	protected CallBack mCallBack;
	protected Context mContext;
	protected Object extra;

	private static boolean mDialogShowing;

	public BaseApi(CallBack callback, Context context) {
		mCallBack = callback;
		mContext = context;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	protected AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
		@Override
		public void onSuccess(String response) {
			VolleyLog.v(decodeUnicode(response));

			if (null == mCallBack || null == mContext) {
				return;
			}

			if (INVALID_TOKEN.equals(decodeUnicode(response))) {
				if (PuApp.get().isLogon()) {
					new AlertDialog.Builder(mContext).setTitle("登陆失效").setMessage("本次登陆已失效,请重新登陆")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									// 注销账号
									PuApp.get().logout(mContext);
								}
							}).setCancelable(false).show();
				}

				return;
			}

			if (extra != null) {
				mCallBack.setExtra(extra);
			}

			mCallBack.onSuccess(response);
		}

		@Override
		public void onFailure(Throwable e) {
			VolleyLog.e(e, e.toString());

			if (e instanceof UnknownHostException) {
				VolleyLog.e(e, EXCEPTION_UNKNOWNHOST);
			} else if (e instanceof ConnectException) {
				VolleyLog.e(e, EXCEPTION_CONNECT);
			} else if (e instanceof SocketException) {
				VolleyLog.e(e, EXCEPTION_SOCKET);
			} else if (e instanceof SocketTimeoutException) {
				VolleyLog.e(e, EXCEPTION_SOCKET_TIMEOUT);
			}

			if (null == mCallBack || null == mContext) {
				return;
			}

			mCallBack.onFailure(e.getClass().getSimpleName());

			if (!((Activity) mContext).isFinishing()) {
				if (!mDialogShowing) {
					mDialogShowing = true;
					new AlertDialog.Builder(mContext).setTitle("网络故障")
							.setMessage("1.检查您的网络设置\n2.当前服务器不稳定")
							.setPositiveButton("网络设置", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mDialogShowing = false;
									Intent intent = null;
									// 判断手机系统的版本 即API大于10 就是3.0或以上版本
									if (android.os.Build.VERSION.SDK_INT > 10) {
										intent = new Intent(
												android.provider.Settings.ACTION_SETTINGS);
									} else {
										intent = new Intent();
										ComponentName component = new ComponentName(
												"com.android.settings",
												"com.android.settings.WirelessSettings");
										intent.setComponent(component);
										intent.setAction("android.intent.action.VIEW");
									}
									mContext.startActivity(intent);
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mDialogShowing = false;
									dialog.dismiss();
								}
							}).setCancelable(false).show();
				}
			}
		}
	};

	/**
	 * 把unicode模式的字符串,转成正常的字符串
	 */
	public static String decodeUnicode(String theString) {
		if (null == theString) {
			return "null";
		}

		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}
}
