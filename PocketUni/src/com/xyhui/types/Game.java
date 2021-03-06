package com.xyhui.types;

public class Game {

	public Game() {

	}

	public Game(String uid, String name, String pkgName, String launchPath, String installed,
			String apkPath, String downloadUrl, String iconUrl, String versionName) {
		super();
		setUid(uid);
		setName(name);
		setPkgName(pkgName);
		setLaunchPath(launchPath);
		setInstalled(installed);
		setApkPath(apkPath);
		setDownloadUrl(downloadUrl);
		setIconUrl(iconUrl);
		setVersionName(versionName);
	}

	@Override
	public String toString() {
		// 游戏名称#图标路径#是否安装#游戏id
		return String.format("%s#%s#%s#%s#%s|", mName, mIconUrl, mInstalled, mUid, mVersionName);
	};

	// 游戏id
	private String mUid;

	// 游戏的名称
	private String mName;

	// 游戏包名
	private String mPkgName;

	// 游戏启动路径
	private String mLaunchPath;

	// 游戏是否安装
	private String mInstalled;

	// 游戏安装包的存储路径
	private String mApkPath;

	// 游戏的下载地址
	private String mDownloadUrl;

	// 游戏图标url
	private String mIconUrl;

	// 游戏版本名
	private String mVersionName;

	public String getUid() {
		return mUid;
	}

	public void setUid(String uid) {
		mUid = uid;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getPkgName() {
		return mPkgName;
	}

	public void setPkgName(String pkgName) {
		mPkgName = pkgName;
	}

	public String getInstalled() {
		return mInstalled;
	}

	public void setInstalled(String installed) {
		mInstalled = installed;
	}

	public String getApkPath() {
		return mApkPath;
	}

	public void setApkPath(String apkPath) {
		mApkPath = apkPath;
	}

	public String getDownloadUrl() {
		return mDownloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		mDownloadUrl = downloadUrl;
	}

	public String getLaunchPath() {
		return mLaunchPath;
	}

	public void setLaunchPath(String mLaunchPath) {
		this.mLaunchPath = mLaunchPath;
	}

	public String getIconUrl() {
		return mIconUrl;
	}

	public void setIconUrl(String mIconUrl) {
		this.mIconUrl = mIconUrl;
	}

	public String getVersionName() {
		return mVersionName;
	}

	public void setVersionName(String mVersionName) {
		this.mVersionName = mVersionName;
	}
}
