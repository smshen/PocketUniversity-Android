package com.xyhui.activity.more;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mslibs.utils.JSONUtils;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.User;
import com.xyhui.utils.Params;
import com.xyhui.utils.PrefUtil;
import com.xyhui.utils.QRcodeUtils;
import com.xyhui.widget.FLActivity;

public class QRCardViewActivity extends FLActivity {
	private Button btn_back;
	private ImageView img_qrcode;
	private ImageView img_avatar_mask;
	private TextView text_nickname;
	private TextView text_params;

	private User user;

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_qrcard_view);

		btn_back = (Button) findViewById(R.id.btn_back);
		img_qrcode = (ImageView) findViewById(R.id.user_qrcode);
		img_avatar_mask = (ImageView) findViewById(R.id.img_avatar_mask);
		text_nickname = (TextView) findViewById(R.id.text_nickname);
		text_params = (TextView) findViewById(R.id.text_params);
	}

	@Override
	public void bindListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void ensureUi() {
		img_qrcode.setVisibility(View.GONE);
		new Api(callback, mActivity).myinfo(PuApp.get().getToken());
		String uid = new PrefUtil().getPreference(Params.LOCAL.UID);
		String code = "xyhui://user/" + uid;
		// String code = "http://pocketuni.net/eventf/" + 13115;

		QRcodeUtils.setupQRcode(code, img_qrcode);
		img_qrcode.setVisibility(View.VISIBLE);
	}

	CallBack callback = new CallBack() {
		@Override
		public void onSuccess(String response) {
			user = JSONUtils.fromJson(response, User.class);

			if (user != null) {
				text_nickname.setText(user.uname);
				text_params.setText("动态:" + user.weibo_count + "   粉丝:" + user.followers_count
						+ "   关注:" + user.followed_count);
				UrlImageViewHelper.setUrlDrawable(img_avatar_mask, user.face, R.drawable.none);
			}
		}
	};
}
