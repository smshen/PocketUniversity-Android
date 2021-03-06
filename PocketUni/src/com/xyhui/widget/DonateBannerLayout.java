package com.xyhui.widget;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mslibs.widget.CRelativeLayout;
import com.mslibs.widget.PagerIndicator;
import com.xyhui.R;

public class DonateBannerLayout extends CRelativeLayout {

	private ViewPager mViewPager;
	private BannerPagerAdapter mBannerPagerAdapter;
	private PagerIndicator mPagerIndicator;
	private ArrayList<String> mDataList;
	private Timer mTimer;
	private TimerTask mTask;

	public DonateBannerLayout(Context context) {
		super(context);
	}

	public DonateBannerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setContentView(R.layout.donate_banner_layout);
	}

	public DonateBannerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void linkUiVar() {
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pagerIndicator);
	}

	private void bindListener() {
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageSelected(int arg0) {
				mPagerIndicator.setPagerIndex(arg0);
				// VolleyLog.d("setPagerIndex: %d", arg0);
			}
		});

		mViewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mViewPager.getParent().requestDisallowInterceptTouchEvent(true);

				if (MotionEvent.ACTION_UP == event.getAction()) {
					reload();
				}

				if (MotionEvent.ACTION_MOVE == event.getAction()) {
					pause();
				}

				return false;
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (null != mDataList && mDataList.size() > 0) {
					int index = mViewPager.getCurrentItem();
					// VolleyLog.d("getCurrentItem(index: %d)", index);
					if (++index >= mDataList.size()) {
						index = 0;
					}
					mViewPager.setCurrentItem(index, true);
				}
				break;
			}
		}
	};

	public void init(ArrayList<String> imgs) {
		linkUiVar();
		bindListener();

		mDataList = imgs;

		// 初始化适配器
		mBannerPagerAdapter = new BannerPagerAdapter(getContext());
		mBannerPagerAdapter.setDataList(mDataList);
		mViewPager.setAdapter(mBannerPagerAdapter);
		mPagerIndicator.setPagerCount(mBannerPagerAdapter.getCount());
	}

	public void reload() {
		if (mTimer == null && mTask == null) {
			mTimer = new Timer();

			mTask = new TimerTask() {
				public void run() {
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				}
			};

			mTimer.schedule(mTask, 5000, 5000);
		}
	}

	public void pause() {
		if (mTimer != null && mTask != null) {
			mTask.cancel();
			mTimer.cancel();
			mTask = null;
			mTimer = null;
		}
	}
}
