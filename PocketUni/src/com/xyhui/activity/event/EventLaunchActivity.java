package com.xyhui.activity.event;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.reflect.TypeToken;
import com.mslibs.utils.JSONUtils;
import com.mslibs.utils.NotificationsUtil;
import com.mslibs.utils.VolleyLog;
import com.xyhui.R;
import com.xyhui.activity.PuApp;
import com.xyhui.api.Api;
import com.xyhui.api.CallBack;
import com.xyhui.types.EventCat;
import com.xyhui.types.Response;
import com.xyhui.utils.ImageUtil;
import com.xyhui.utils.Storage;
import com.xyhui.widget.FLActivity;

public class EventLaunchActivity extends FLActivity {
	private final static String EVENT_START_TIME = "starttime";
	private final static String EVENT_END_TIME = "endtime";
	private final static String EVENT_DEAD_TIME = "deadtime";

	private final static String EVENT_START_DATE = "startdate";
	private final static String EVENT_END_DATE = "enddate";
	private final static String EVENT_DEAD_DATE = "deaddate";

	private final String PIC_NAME = "event_logo.jpg";
	private final String PIC_NAME_TEMP = "event_logo_temp.jpg";

	private final int PIC_WIDTH = 125;
	private final int PIC_HEIGHT = 125;

	private ImageUtil mImageUtil;

	private Button btn_back;
	private Button btn_submit;

	private ImageView img_event_logo;

	private EditText edit_event_name;
	private EditText edit_event_intro;
	private EditText edit_event_maxmember;
	private EditText edit_event_loc;

	private Spinner spinner_event_org;
	private Spinner spinner_event_verifier;
	private Spinner spinner_event_type;
	private Spinner spinner_event_group;

	private TextView text_event_group;
	private TextView text_event_group_tag;

	private EditText edit_event_startdate;
	private EditText edit_event_starttime;
	private EditText edit_event_enddate;
	private EditText edit_event_endtime;
	private EditText edit_event_deaddate;
	private EditText edit_event_deadtime;

	public CheckBox chk_no;
	
	private String mEventName;
	private String mEventIntro;
	private String mEventMaxmember;
	private String mEventLoc;
	private String mEventOrgId;
	private String mEventVerifierId;
	private String mEventTypeId;
	private String mEventGroupId;

	private String mEventStartdate;
	private String mEventStarttime;
	private String mEventEnddate;
	private String mEventEndtime;
	private String mEventDeaddate;
	private String mEventDeadtime;

	private EventLaunchData mEventLaunchData;

	private ArrayList<EventOption> mOrgList;
	private ArrayList<EventOption> mGroupList;
	private ArrayList<EventVerifier> mVerifierList;

	private boolean mDialogShowing = false;

	@Override
	public void init() {
		mImageUtil = new ImageUtil(this, PIC_WIDTH, PIC_HEIGHT, PIC_NAME, PIC_NAME_TEMP);
	}

	@Override
	public void linkUiVar() {
		setContentView(R.layout.activity_event_launch);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_submit = (Button) findViewById(R.id.btn_submit);

		img_event_logo = (ImageView) findViewById(R.id.img_event_logo);

		edit_event_name = (EditText) findViewById(R.id.edit_event_name);
		edit_event_intro = (EditText) findViewById(R.id.edit_event_intro);
		edit_event_maxmember = (EditText) findViewById(R.id.edit_event_maxmember);
		edit_event_loc = (EditText) findViewById(R.id.edit_event_loc);

		spinner_event_org = (Spinner) findViewById(R.id.spinner_event_org);
		spinner_event_verifier = (Spinner) findViewById(R.id.spinner_event_verifier);
		spinner_event_type = (Spinner) findViewById(R.id.spinner_event_type);
		spinner_event_group = (Spinner) findViewById(R.id.spinner_event_group);
		text_event_group = (TextView) findViewById(R.id.text_event_group);
		text_event_group_tag = (TextView) findViewById(R.id.text_event_group_tag);

		edit_event_startdate = (EditText) findViewById(R.id.edit_event_startdate);
		edit_event_starttime = (EditText) findViewById(R.id.edit_event_starttime);
		edit_event_enddate = (EditText) findViewById(R.id.edit_event_enddate);
		edit_event_endtime = (EditText) findViewById(R.id.edit_event_endtime);
		edit_event_deaddate = (EditText) findViewById(R.id.edit_event_deaddate);
		edit_event_deadtime = (EditText) findViewById(R.id.edit_event_deadtime);
		
		chk_no = (CheckBox) findViewById(R.id.chk_no);
	}

	@Override
	public void bindListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mActivity).setTitle("退出")
						.setMessage("您确定要放弃本次编辑吗?(退出后数据不会保存)")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}).show();
			}
		});

		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkWidgetData()) {
					showProgress("正在提交", "请稍候...");
					new Api(doAddEventCB, mActivity).doAddEvent(PuApp.get().getToken(),
							mImageUtil.getPicFile(), mEventName, mEventVerifierId, mEventLoc,
							mEventTypeId, mEventStartdate + " " + mEventStarttime, mEventEnddate
									+ " " + mEventEndtime, mEventDeaddate + " " + mEventDeadtime,
							mEventGroupId, mEventMaxmember, mEventIntro, mEventOrgId, chk_no.isChecked() ? "1" : "0");
				}
			}
		});

		img_event_logo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowPickDialog();
			}
		});

		spinner_event_org.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEventOrgId = ((EventOption) spinner_event_org.getItemAtPosition(position)).id;

				if (!TextUtils.isEmpty(mEventOrgId)) {
					showProgress();
					new Api(csOrgaCB, mActivity).csOrga(PuApp.get().getToken(), mEventOrgId);
					spinner_event_verifier.setSelection(0);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner_event_verifier.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEventVerifierId = ((EventVerifier) spinner_event_verifier
						.getItemAtPosition(position)).uid;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner_event_verifier.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (TextUtils.isEmpty(mEventOrgId) && !mDialogShowing) {
					mDialogShowing = true;
					new AlertDialog.Builder(EventLaunchActivity.this).setTitle("请先选择归属组织")
							.setPositiveButton("确认", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mDialogShowing = false;
								}
							}).setCancelable(false).show();
					return true;
				}
				return false;
			}
		});

		spinner_event_type.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEventTypeId = ((EventCat) spinner_event_type.getItemAtPosition(position)).id;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner_event_group.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mEventGroupId = ((EventOption) spinner_event_group.getItemAtPosition(position)).gid;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		edit_event_startdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(EVENT_START_DATE);
			}
		});

		edit_event_starttime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(EVENT_START_TIME);
			}
		});

		edit_event_enddate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(EVENT_END_DATE);
			}
		});

		edit_event_endtime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(EVENT_END_TIME);
			}
		});

		edit_event_deaddate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog(EVENT_DEAD_DATE);
			}
		});

		edit_event_deadtime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog(EVENT_DEAD_TIME);
			}
		});
	}

	@Override
	public void ensureUi() {
		setTypeSpinner();
		mVerifierList = new ArrayList<EventVerifier>();
		setVerifierSpinner();

		File img = mImageUtil.getPicFile();
		if (img.exists() && img.delete()) {
			VolleyLog.d("删除图标成功");
		}

		showProgress();
		new Api(addEventCB, mActivity).addEvent(PuApp.get().getToken());
	}

	/**
	 * 选择提示对话框
	 */
	private void ShowPickDialog() {
		if (!Storage.externalStorageAvailable()) {
			NotificationsUtil.ToastBottomMsg(mActivity, "sd卡不可用");
			return;
		}

		new AlertDialog.Builder(this).setTitle("设置活动图标")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mImageUtil.getAndCropPhoto();
					}
				}).setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						// 下面这句指定调用相机拍照后的照片存储的路径
						intent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(mImageUtil.getPicFileTemp()));
						startActivityForResult(intent, ImageUtil.CAPTURE_PIC);
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			VolleyLog.d("requestCode = %d ", requestCode);
			VolleyLog.d("resultCode = %d", resultCode);
			VolleyLog.d("data = %s", null == data ? "null" : data.toString());
			return;
		}

		switch (requestCode) {
		case ImageUtil.CAPTURE_PIC:
			mImageUtil.cropPhoto(Uri.fromFile(mImageUtil.getPicFileTemp()));
			break;
		case ImageUtil.HANDLE_PIC:
			mImageUtil.setPicToView(this, img_event_logo, Uri.fromFile(mImageUtil.getPicFile()));
			break;
		default:
			break;
		}
	}

	CallBack addEventCB = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			mEventLaunchData = JSONUtils.fromJson(response, EventLaunchData.class);

			if (null != mEventLaunchData) {
				if (mEventLaunchData.status == 0) {
					NotificationsUtil.ToastBottomMsg(mActivity, mEventLaunchData.msg);
					finish();
				} else {
					setOrgSpinner();
					showGroupWidget();
				}
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();
			NotificationsUtil.ToastBottomMsg(mActivity, message);
		}
	};

	CallBack csOrgaCB = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();

			mVerifierList = JSONUtils.fromJson(response,
					new TypeToken<ArrayList<EventVerifier>>() {
					});

			if (null != mVerifierList && mVerifierList.size() > 0) {
				setVerifierSpinner();
			} else {
				NotificationsUtil.ToastBottomMsg(mActivity, "该组织没有审核人,请选择其他组织");
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();

		}
	};

	CallBack doAddEventCB = new CallBack() {
		@Override
		public void onSuccess(String response) {
			dismissProgress();
			Response r = JSONUtils.fromJson(response, Response.class);

			if (null != r) {
				NotificationsUtil.ToastBottomMsg(mActivity, r.msg);
				if (r.status == 1) {
					finish();
				}
			}
		}

		@Override
		public void onFailure(String message) {
			dismissProgress();

		}
	};

	public static class DatePickerFragment extends DialogFragment implements OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, monthOfYear, dayOfMonth);

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(calendar.getTime());

			String tag = getTag().toString();
			EditText edit;

			if (tag.equals(EVENT_START_DATE)) {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_startdate);
			} else if (tag.equals(EVENT_END_DATE)) {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_enddate);
			} else {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_deaddate);
			}

			edit.setText(date);
		}
	}

	public void showDatePickerDialog(String which) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), which);
	}

	public static class TimePickerFragment extends DialogFragment implements OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar calendar = Calendar.getInstance();
			calendar.clear();

			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);

			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String date = format.format(calendar.getTime());

			String tag = getTag().toString();
			EditText edit;

			if (tag.equals(EVENT_START_TIME)) {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_starttime);
			} else if (tag.equals(EVENT_END_TIME)) {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_endtime);
			} else {
				edit = (EditText) getActivity().findViewById(R.id.edit_event_deadtime);
			}

			edit.setText(date);
		}
	}

	public void showTimePickerDialog(String which) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), which);
	}

	private boolean checkWidgetData() {
		if (!mImageUtil.getPicFile().exists()) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动图标不能为空");
			return false;
		}

		mEventName = edit_event_name.getText().toString();
		if (TextUtils.isEmpty(mEventName)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动名称不能为空");
			return false;
		}

		mEventIntro = edit_event_intro.getText().toString();
		if (TextUtils.isEmpty(mEventIntro)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动简介不能为空");
			return false;
		}

		mEventMaxmember = edit_event_maxmember.getText().toString();
		if (TextUtils.isEmpty(mEventMaxmember)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动人数不能为空");
			return false;
		}

		mEventLoc = edit_event_loc.getText().toString();
		if (TextUtils.isEmpty(mEventLoc)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动地点不能为空");
			return false;
		}

		if (TextUtils.isEmpty(mEventVerifierId)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "审核人不能为空");
			return false;
		}

		if (TextUtils.isEmpty(mEventTypeId) || "0".equals(mEventTypeId)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动类型不能为空");
			return false;
		}

		if (null != mGroupList && mGroupList.size() > 0 && TextUtils.isEmpty(mEventGroupId)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动归属部落不能为空");
			return false;
		}

		mEventStartdate = edit_event_startdate.getText().toString();
		mEventStarttime = edit_event_starttime.getText().toString();
		if (TextUtils.isEmpty(mEventStartdate) || TextUtils.isEmpty(mEventStarttime)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动开始时间不能为空");
			return false;
		}

		mEventEnddate = edit_event_enddate.getText().toString();
		mEventEndtime = edit_event_endtime.getText().toString();
		if (TextUtils.isEmpty(mEventEnddate) || TextUtils.isEmpty(mEventEndtime)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "活动结束时间不能为空");
			return false;
		}

		mEventDeaddate = edit_event_deaddate.getText().toString();
		mEventDeadtime = edit_event_deadtime.getText().toString();
		if (TextUtils.isEmpty(mEventDeaddate) || TextUtils.isEmpty(mEventDeadtime)) {
			NotificationsUtil.ToastBottomMsg(mActivity, "报名截止时间不能为空");
			return false;
		}

		return true;
	}

	private void setOrgSpinner() {
		mOrgList = new ArrayList<EventOption>();
		mOrgList.addAll(mEventLaunchData.schoolOrga);

		for (EventOption schoolOrga : mEventLaunchData.schoolOrga) {
			schoolOrga.id = "-" + schoolOrga.id;
		}

		mOrgList.addAll(mEventLaunchData.school);

		EventOption eventOption = new EventOption();
		eventOption.id = "";
		eventOption.title = "请选择";
		mOrgList.add(0, eventOption);

		ArrayAdapter<EventOption> orgAdapter = new ArrayAdapter<EventOption>(this,
				R.layout.spinner_item_layout, mOrgList);
		orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_event_org.setAdapter(orgAdapter);
	}

	private void setVerifierSpinner() {
		EventVerifier eventVerifier = new EventVerifier();
		eventVerifier.uid = "";
		eventVerifier.realname = "请选择";
		mVerifierList.add(0, eventVerifier);

		ArrayAdapter<EventVerifier> verifierAdapter = new ArrayAdapter<EventVerifier>(this,
				R.layout.spinner_item_layout, mVerifierList);
		verifierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_event_verifier.setAdapter(verifierAdapter);
	}

	private void setTypeSpinner() {
		ArrayAdapter<EventCat> typeAdapter = new ArrayAdapter<EventCat>(this,
				R.layout.spinner_item_layout, PuApp.get().getLocalDataMgr().getEventCats());
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_event_type.setAdapter(typeAdapter);
	}

	private void showGroupWidget() {
		mGroupList = mEventLaunchData.group;
		int groupSize;

		if (null != mGroupList) {
			groupSize = mGroupList.size();
		} else {
			groupSize = 0;
		}

		if (groupSize == 0) {
			text_event_group_tag.setVisibility(View.GONE);
			text_event_group.setVisibility(View.GONE);
			spinner_event_group.setVisibility(View.GONE);
			mEventGroupId = null;
		} else if (groupSize == 1) {
			text_event_group_tag.setVisibility(View.VISIBLE);
			text_event_group.setVisibility(View.VISIBLE);
			spinner_event_group.setVisibility(View.GONE);
			text_event_group.setText(mGroupList.get(0).title);
			mEventGroupId = mGroupList.get(0).gid;
		} else if (groupSize > 1) {
			text_event_group_tag.setVisibility(View.VISIBLE);
			text_event_group.setVisibility(View.GONE);
			spinner_event_group.setVisibility(View.VISIBLE);

			EventOption eventOption = new EventOption();
			eventOption.id = "";
			eventOption.title = "请选择";
			mGroupList.add(0, eventOption);

			ArrayAdapter<EventOption> groupAdapter = new ArrayAdapter<EventOption>(this,
					R.layout.spinner_item_layout, mGroupList);
			groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner_event_group.setAdapter(groupAdapter);
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(mActivity).setTitle("退出").setMessage("您确定要放弃本次编辑吗?(退出后数据不会保存)")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).show();
	}

	class EventLaunchData {
		int status;
		String msg;
		ArrayList<EventOption> group;
		ArrayList<EventOption> schoolOrga;
		ArrayList<EventOption> school;
	}

	class EventOption {
		String id;
		String gid;
		String title;

		@Override
		public String toString() {
			return title;
		}
	}

	class EventVerifier {
		String uid;
		String realname;

		@Override
		public String toString() {
			return realname;
		}
	}
}
