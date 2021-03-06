package com.xyhui.types;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;

public class User implements Parcelable {
	public String uid;
	public String uname;
	public String province;
	public String city;
	public String school;
	public String location;
	public String face;
	public String sex;
	public int weibo_count;
	public int favorite_count;
	public int followers_count;
	public int followed_count;
	public String is_followed;
	public int is_verified;
	public int school_event_score_used;
	public int school_event_credit;
	public String money;
	public String grad;
	public String sign;

	// 抓取用户的第一条微博,不为空就抓取整个微博列表,显示在用户的个人主页
	public JsonElement status;

	public String getInfo() {
		String result = "";
		result += "动态:" + weibo_count + "   ";
		result += "关注:" + followed_count + "   ";
		result += "粉丝:" + followers_count;
		return result;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(uname);
		out.writeString(sex);
		out.writeString(face);
	}

	public User() {

	}

	public User(Parcel in) {
		uname = in.readString();
		sex = in.readString();
		face = in.readString();
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};
}
