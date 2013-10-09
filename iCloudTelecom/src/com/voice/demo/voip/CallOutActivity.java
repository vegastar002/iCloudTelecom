/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.voice.demo.voip;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.DeviceListener.CBState;
import com.hisun.phone.core.voice.DeviceListener.Reason;
import com.hisun.phone.core.voice.model.CallStatisticsInfo;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.setting.SettingsActivity;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.video.VideoActivity;

/**
 * 
 *  Voip呼出界面，呼出方用于显示和操作通话过程。
 * 
 * @version 1.0.0
 */
public class CallOutActivity extends CCPBaseActivity implements OnClickListener {
	// 话筒调节控制区
	private LinearLayout mCallAudio;
	// 静音按钮
	private ImageView mCallMute;
	// 免提按钮
	private ImageView mCallHandFree;
	// 键盘
	private ImageView mDiaerpadBtn;
	//键盘区
	private LinearLayout mDiaerpad;
	
	// 挂机按钮
	private ImageView mVHangUp;
	// 动态状态显示区
	private TextView mCallStateTips;
	private Chronometer mChronometer;
	// 号码显示区
	private TextView mVtalkNumber;
	private TextView mCallStatus;
	// 号码
	private String mPhoneNumber;
	// 通话 ID
	private String mCurrentCallId;
	// voip 账号
	private String mVoipAccount;
	// 通话类型，直拨，落地, 回拨
	private String mType = "";
	// 状态栏
	private NotificationManager mNotificationManager;
	// activity 标签
	private static final String TAG = "CallOutActivity";
	// 通话状态
	private boolean isConnect = false;
	// 是否静音
	private boolean isMute = false;
	// 是否免提
	private boolean isHandsfree = false;
	// 是否键盘显示
	private boolean isDialerShow = false;
	// 手动挂断
	private boolean isSelfReject = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_call_interface);
		
		initResourceRefs();
		initialize();
		initCall();
		registerReceiver(new String[]{SettingsActivity.INTENT_P2P_ENABLED});
	}

	/**
	 * Initialize all UI elements from resources.
	 * 
	 */
	private void initResourceRefs() {
		mCallAudio = (LinearLayout) findViewById(R.id.layout_call_audio);
		mCallMute = (ImageView) findViewById(R.id.layout_callin_mute);
		mCallHandFree = (ImageView) findViewById(R.id.layout_callin_handfree);
		mVHangUp = (ImageButton) findViewById(R.id.layout_call_reject);
		mCallStateTips = (TextView) findViewById(R.id.layout_callin_duration);
		
		// call time
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mVtalkNumber = (TextView) findViewById(R.id.layout_callin_number);
		// 键盘按钮
		mDiaerpadBtn = (ImageView) findViewById(R.id.layout_callin_diaerpad);
		mDiaerpad = (LinearLayout) findViewById(R.id.layout_diaerpad);
		
		mDiaerpadBtn.setOnClickListener(this);
		mCallMute.setOnClickListener(this);
		mCallHandFree.setOnClickListener(this);
		mVHangUp.setOnClickListener(this);
		mCallMute.setEnabled(false);
		mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(
				Context.NOTIFICATION_SERVICE);
		showNotification(getString(R.string.notification_calling_title),
				getString(R.string.notification_calling_content));
		
		
		setupKeypad();
		mDmfInput = (EditText) findViewById(R.id.dial_input_numer_TXT);
		
		mCallStatus = (TextView) findViewById(R.id.call_status);
	}

	/**
	 * Read parameters or previously saved state of this activity.
	 */
	private void initialize() {
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				finish();
				return;
			}
			mType = bundle.getString(BaseApplication.VALUE_DIAL_MODE);
			
			if (mType.equals(BaseApplication.VALUE_DIAL_MODE_FREE)) {
				// voip免费通话时显示voip账号
				mVoipAccount = bundle.getString(BaseApplication.VALUE_DIAL_VOIP_INPUT);
				if (mVoipAccount == null) {
					finish();
					return;
				}
				mVtalkNumber.setText(mVoipAccount);
			} else {
				// 直拨及回拨显示号码
				mPhoneNumber = bundle.getString(BaseApplication.VALUE_DIAL_VOIP_INPUT);
				mVtalkNumber.setText(mPhoneNumber);
			}
		}
	}

	private void setupKeypad() {
		/**Setup the listeners for the buttons*/
		findViewById(R.id.zero).setOnClickListener(this);
		findViewById(R.id.one).setOnClickListener(this);
		findViewById(R.id.two).setOnClickListener(this);
		findViewById(R.id.three).setOnClickListener(this);
		findViewById(R.id.four).setOnClickListener(this);
		findViewById(R.id.five).setOnClickListener(this);
		findViewById(R.id.six).setOnClickListener(this);
		findViewById(R.id.seven).setOnClickListener(this);
		findViewById(R.id.eight).setOnClickListener(this);
		findViewById(R.id.nine).setOnClickListener(this);
		findViewById(R.id.star).setOnClickListener(this);
		findViewById(R.id.pound).setOnClickListener(this);
	}
	
	/**
	 * Initialize mode
	 * 
	 */
	private void initCall() {
		try {
			VoiceHelper.getInstance().setHandler(mHandler);
			if (mType.equals(BaseApplication.VALUE_DIAL_MODE_FREE)) {
				// voip免费通话
				if (mVoipAccount != null && !TextUtils.isEmpty(mVoipAccount)) {
					mCurrentCallId = VoiceHelper.getInstance().getDevice().makeCall(Device.CallType.VOICEP2P, mVoipAccount);
					Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] VoIP calll, mVoipAccount " + mVoipAccount + " currentCallId " +  mCurrentCallId);
				}
			} else if (mType.equals(BaseApplication.VALUE_DIAL_MODE_DIRECT)) {
				// 直拨
				mCurrentCallId = VoiceHelper.getInstance().getDevice().makeCall(Device.CallType.VOICEP2L, VoiceUtil.getStandardMDN(mPhoneNumber));
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] Direct dial, mPhoneNumber " + mPhoneNumber + " currentCallId " +  mCurrentCallId);
			}  else if (mType.equals(BaseApplication.VALUE_DIAL_MODE_BACK)) {
				// 回拨
				VoiceHelper.getInstance().getDevice().makeCallback(CCPConfig.Src_phone, mPhoneNumber);
				mCallAudio.setVisibility(View.GONE);
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] call back, mPhoneNumber " + mPhoneNumber);
				return;
			} else {
				finish();
				return;
			}

			if (mCurrentCallId == null || mCurrentCallId.length() < 1) {
				BaseApplication.getInstance().showToast(R.string.no_support_voip);
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] Sorry, "+ getString(R.string.no_support_voip)+" , Call failed. ");
				finish();
				return;
			}
			isMute = VoiceHelper.getInstance().getDevice().getMuteStatus();
			isHandsfree = VoiceHelper.getInstance().getDevice().getLoudsSpeakerStatus();
		} catch (Exception e) {
			finish();
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] Sorry, call failure leads to an unknown exception, please try again. ");
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		// keypad
		case R.id.zero:{
			keyPressed(KeyEvent.KEYCODE_0);
			return ;
		}
		case R.id.one:{
			keyPressed(KeyEvent.KEYCODE_1);
			return ;
		}
		case R.id.two:{
			keyPressed(KeyEvent.KEYCODE_2);
			return ;
		}
		case R.id.three:{
			keyPressed(KeyEvent.KEYCODE_3);
			return ;
		}
		case R.id.four:{
			keyPressed(KeyEvent.KEYCODE_4);
			return ;
		}
		case R.id.five:{
			keyPressed(KeyEvent.KEYCODE_5);
			return ;
		}
		case R.id.six:{
			keyPressed(KeyEvent.KEYCODE_6);
			return ;
		}
		case R.id.seven:{
			keyPressed(KeyEvent.KEYCODE_7);
			return ;
		}
		case R.id.eight:{
			keyPressed(KeyEvent.KEYCODE_8);
			return ;
		}
		case R.id.nine:{
			keyPressed(KeyEvent.KEYCODE_9);
			return ;
		}
		case R.id.star:{
			keyPressed(KeyEvent.KEYCODE_STAR);
			return ;
		}
		case R.id.pound:{
			keyPressed(KeyEvent.KEYCODE_POUND);
			return ;
		}
		
		// keybad end ...
		
		case R.id.layout_call_reject:
			// 挂断电话
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] Voip talk hand up, CurrentCallId " + mCurrentCallId);
			isSelfReject = true;
			try {
				if (mCurrentCallId != null) {
					VoiceHelper.getInstance().getDevice().releaseCall(mCurrentCallId);
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
			break;
		case R.id.layout_callin_mute:
			// 设置静音
			setMuteUI();
			break;
		case R.id.layout_callin_handfree:
			// 设置免提
			sethandfreeUI();
			break;
			
		case R.id.layout_callin_diaerpad:
			
			// 设置键盘
			setDialerpadUI();
			break;
		default:
			break;
		}
	}
	/**
	 * 设置静音
	 */
	private void setMuteUI() {
		try {
			if (isMute) {
				mCallMute.setImageResource(R.drawable.call_interface_mute);
			} else {
				mCallMute.setImageResource(R.drawable.call_interface_mute_on);
			}
			VoiceHelper.getInstance().getDevice().setMute(isMute);
			isMute = VoiceHelper.getInstance().getDevice().getMuteStatus();
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] 设置通话静音");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置免提
	 */
	private void sethandfreeUI() {
		try {
			if (isHandsfree) {
				mCallHandFree.setImageResource(R.drawable.call_interface_hands_free);
			} else {
				mCallHandFree.setImageResource(R.drawable.call_interface_hands_free_on);
			}
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(isHandsfree);
			isHandsfree = VoiceHelper.getInstance().getDevice().getLoudsSpeakerStatus();
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] 设置通话免提");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setDialerpadUI() {
		if(isDialerShow) {
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mDiaerpad.setVisibility(View.GONE);
			isDialerShow = false;
		} else {
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad_on);
			mDiaerpad.setVisibility(View.VISIBLE);
			isDialerShow = true;
		}
		
	}
	/**
	 * 延时关闭界面
	 */
	final Runnable finish = new Runnable() {
		public void run() {
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		if (isMute && VoiceHelper.getInstance().getDevice() != null) {
			VoiceHelper.getInstance().getDevice().setMute(isMute);
		}
		if (isHandsfree && VoiceHelper.getInstance().getDevice() != null) {
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(isHandsfree);
		}
		if (mVHangUp != null) {
			mVHangUp = null;
		}
		if (mCallAudio != null) {
			mCallAudio = null;
		}
		if (mCallStateTips != null) {
			mCallStateTips = null;
		}
		if (mVtalkNumber != null) {
			mVtalkNumber = null;
		}
		if (mCallMute != null) {
			mCallMute = null;
		}
		if (mCallHandFree != null) {
			mCallHandFree = null;
		}
		if (mDiaerpadBtn != null) {
			mDiaerpadBtn = null;
		}
		if (mNotificationManager != null) {
			mNotificationManager.cancel(R.drawable.icon_call_small);
			mNotificationManager = null;
		}
		mPhoneNumber = null;
		mVoipAccount = null;
		mCurrentCallId = null;
		if(mHandler!=null){
			mHandler = null;
		}
		BaseApplication.getInstance().setAudioMode(AudioManager.MODE_NORMAL);
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 屏蔽返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 根据状态,修改按钮属性及关闭操作
	 * 
	 * @param reason
	 */
	private void finishCalling(Reason reason) {
		try {
			mChronometer.stop();
			mChronometer.setVisibility(View.GONE);
			mCallStateTips.setVisibility(View.VISIBLE);
			
			mHandler.postDelayed(finish, 5000);
			mCallHandFree.setClickable(false);
			mCallMute.setClickable(false);
			mVHangUp.setClickable(false);
			mDiaerpadBtn.setClickable(false);
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mCallHandFree.setImageResource(R.drawable.call_interface_hands_free);
			mCallMute.setImageResource(R.drawable.call_interface_mute);
			mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
			// 处理通话结束状态
			if (reason == Reason.DECLINED) {
				mCallStateTips.setText(getString(R.string.voip_calling_refuse));
			} else if (reason == Reason.CALLMISSED) {
				mCallStateTips.setText(getString(R.string.voip_calling_timeout));
			} else if (reason == Reason.PAYMENT) {
				mCallStateTips.setText(getString(R.string.voip_call_fail_no_cash));
			} else if (reason == Reason.UNKNOWN) {
				mCallStateTips.setText(getString(R.string.voip_calling_finish));
			} else if (reason == Reason.NOTRESPONSE) {
				mCallStateTips.setText(getString(R.string.voip_call_fail));
			} else if (reason == Reason.VERSIONNOTSUPPORT) {
				mCallStateTips.setText(getString(R.string.str_voip_not_support));
			} else if (reason == Reason.OTHERVERSIONNOTSUPPORT) {
				mCallStateTips.setText(getString(R.string.str_other_voip_not_support));
			} else {
				mCallStateTips.setText(getString(R.string.voip_calling_network_instability));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用于挂断时修改按钮属性及关闭操作
	 */
	private void finishCalling() {
		try {
			if (isConnect) {
				// set Chronometer view gone..
				mChronometer.stop();
				mChronometer.setVisibility(View.GONE);
				// 接通后关闭
				mCallStateTips.setVisibility(View.VISIBLE);
				mCallStateTips.setText(R.string.voip_calling_finish);
				mHandler.postDelayed(finish, 3000);
			} else {
				// 未接通，直接关闭
				finish();
			}
			mCallHandFree.setClickable(false);
			mCallMute.setClickable(false);
			mVHangUp.setClickable(false);
			mDiaerpadBtn.setClickable(false);
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mCallHandFree.setImageResource(R.drawable.call_interface_hands_free);
			mCallMute.setImageResource(R.drawable.call_interface_mute);
			mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示状态栏
	 * @param topic
	 * @param text
	 */
	private void showNotification(String topic, String text) {
		try {
			Notification notification = new Notification(R.drawable.icon_call_small, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
			Intent intent = new Intent("ACTION_VOIP_OUTCALL");
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(this, topic, text, contentIntent);
			mNotificationManager.notify(R.drawable.icon_call_small, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 回调handler，根据Voip通话状态，更新界面显示
	 */
	private android.os.Handler mHandler = new android.os.Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String callid = null;
			Reason reason;
			Bundle b = null;
			//获取通话ID
			if(msg.obj instanceof String ){
				callid = (String)msg.obj;
			}else if (msg.obj instanceof Bundle){
				b = (Bundle) msg.obj;
				callid = b.getString(Device.CALLID);
			}
			switch (msg.what) {
			case VoiceHelper.WHAT_ON_CALL_ALERTING:
				// 连接到对端用户，播放铃音
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip alerting!!");
				if (callid != null && mCurrentCallId.equals(callid)) {
					mCallStateTips.setText(getString(R.string.voip_calling_wait));
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_PROCEEDING:
				 // 连接到服务器
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip on call proceeding!!");
				if (callid != null && mCurrentCallId.equals(callid)) {
					mCallStateTips.setText(getString(R.string.voip_call_connect));
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_ANSWERED:
				// 对端应答
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip on call answered!!");
				if (callid != null && mCurrentCallId.equals(callid)) {
					isConnect = true;
					mCallMute.setEnabled(true);
					mCallStateTips.setVisibility(View.GONE);
					mChronometer.setBase(SystemClock.elapsedRealtime());
					mChronometer.setVisibility(View.VISIBLE);
					mChronometer.start();
					
					if(mHandler != null ) {
						mHandler.sendMessage(mHandler.obtainMessage(VideoActivity.WHAT_ON_CODE_CALL_STATUS));
//						mCallStatus.setVisibility(View.VISIBLE);
					}
				}
				break;
				
			case VoiceHelper.WHAT_ON_CALL_RELEASED:
				// 远端挂断，本地挂断在onClick中处理
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip on call released!!");
				if (callid != null && mCurrentCallId.equals(callid) && !isSelfReject) {
					finishCalling();
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_MAKECALL_FAILED:
				// 发起通话失败
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip on call makecall failed!!");
				if (b != null && b.get(Device.REASON) != null) {
					reason = (Reason) b.get(Device.REASON);
					Log.d(TAG, "reason:"+reason);
					if (callid != null && mCurrentCallId.equals(callid) && !isSelfReject) {
						finishCalling(reason);
					}
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_BACKING:
				// 回拨通话回调	
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] voip on callback");
				if (b != null) {
					CBState status = (CBState) b.get(Device.CBSTATE);
					Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallOutActivity] status:"+status);
					if (status != null) {
						if (status.equals(CBState.CONNECTING)) {
							mCallStateTips.setText(getString(R.string.voip_call_back_connect));
						} else {
							mHandler.postDelayed(finish, 5000);
							if (status.equals(CBState.SUCCESS)) {
								mCallStateTips.setText(getString(R.string.voip_call_back_success));
							} else if (status.equals(CBState.FAILED)) {
								mCallStateTips.setText(getString(R.string.voip_call_fail));
							} else if (status.equals(CBState.PAYMENT)) {
								mCallStateTips.setText(getString(R.string.voip_call_fail_no_cash));
							} else {
								mCallStateTips.setText(getString(R.string.voip_calling_timeout));
							}
							mVHangUp.setClickable(false);
							mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
						}
					}
				}
				break;
				
			case VideoActivity.WHAT_ON_CODE_CALL_STATUS:
				
				CallStatisticsInfo callStatistics = VoiceHelper.getInstance().getDevice().getCallStatistics(Device.CallType.VOICEP2P);
				if(callStatistics != null) {
					int fractionLost = callStatistics.getFractionLost();
					int rttMs = callStatistics.getRttMs();
					mCallStatus.setText(getString(R.string.str_call_status, (fractionLost/255),rttMs));
//					mCallStatus.setVisibility(View.VISIBLE);
				} else {
					mCallStatus.setVisibility(View.GONE);
				}
				if(isConnect && mHandler!= null) {
					Message callMessage = mHandler.obtainMessage(VideoActivity.WHAT_ON_CODE_CALL_STATUS);
					mHandler.sendMessageDelayed(callMessage,4000);
				}
				break;
			default:
				break;
			}
		}
	};
	
	
	/***********************************  KeyPad    *************************************************/
	
	private EditText mDmfInput;
    
	void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDmfInput.getText().clear();
		mDmfInput.onKeyDown(keyCode, event);
		VoiceHelper.getInstance().getDevice().sendDTMF(mCurrentCallId, mDmfInput.getText().toString().toCharArray()[0]);
    }
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
	}
    
    @Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if(intent.getAction().equals(SettingsActivity.INTENT_P2P_ENABLED)){
			addNotificatoinToView(getString(R.string.str_p2p_enable)/*, Gravity.TOP*/);
		}
	}
    
    @Override
	protected void onPause() {
		super.onPause();
	}
}
