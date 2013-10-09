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

import org.webrtc.videoengine.ViERenderer;

import android.app.AlertDialog;
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
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.CCP.phone.CameraInfo;
import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.Device.CallType;
import com.hisun.phone.core.voice.Device.Rotate;
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
 * Voip incoming interface, called for display and operation process of the call。
 * 
 * @version 1.0.0
 */
public class CallInActivity extends CCPBaseActivity implements OnClickListener {
	
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
	// 名称显示区
	private TextView mVtalkName;
	// 号码显示区
	private TextView mVtalkNumber;
	
	private TextView mCallStatus;
	// 号码
	private String mPhoneNumber;
	// 名称
	private String mNickName;
	// 通话 ID
	private String mCurrentCallId;
	// voip 账号
	private String mVoipAccount;
	// 状态栏
	private NotificationManager mNotificationManager;
	// 透传号码参数
	private static final String KEY_TEL = "tel";
	// 透传名称参数
	private static final String KEY_NAME = "nickname";
	// 是否静音
	private boolean isMute = false;
	// 是否免提
	private boolean isHandsfree = false;
	private boolean  isDialerShow = false;
	// 是否接通
	private boolean isConnect = false;
	
	
	// video 
	private CallType mCallType;
	private Button mVideoStop;
	private Button mVideoBegin;
	private ImageView mVideoIcon;
	private RelativeLayout mVideoTipsLy;
	
	private TextView mVideoTopTips;
	//private TextView mVideoCallTips;
	private SurfaceView mVideoView;
	// Local Video
	private RelativeLayout mLoaclVideoView;
	// Remote Video
	private FrameLayout mVideoLayout;
	
	private ImageButton mCameraSwitch;
	
	CameraInfo[] cameraInfos;
	// The first rear facing camera
    int defaultCameraId;
    Intent currIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (VoiceHelper.getInstance() == null || mHandler == null) {
			finish();
			return;
		}
		// 设置回调handler
		VoiceHelper.getInstance().setHandler(mHandler);
		currIntent = getIntent();
		initialize(currIntent);
		initResourceRefs();
		registerReceiver(new String[]{SettingsActivity.INTENT_P2P_ENABLED});
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(mHandler != null ) {
			mHandler.removeCallbacks(finish);
		}
		releaseCurrCall(false);
		currIntent = intent;
		initialize(currIntent);
		initResourceRefs();
		
	}
	
	/**
	 * Initialize all UI elements from resources.
	 * 
	 */
	private void initResourceRefs() {
		isConnect = false;
		if(mCallType == Device.CallType.VIDEO) {
			// video ..
			setContentView(R.layout.layout_video_activity);
			
			//head  titile init...
			((TextView)findViewById(R.id.voice_title)).setText(R.string.app_title_video);
			
			Button lButton = (Button) findViewById(R.id.voice_btn_back);
			lButton.setText(R.string.btn_title_back);
			lButton.setOnClickListener(this);
			lButton.setVisibility(View.VISIBLE);
			
			
			findViewById(R.id.video_botton_cancle).setVisibility(View.GONE);
			
			mVideoTipsLy = (RelativeLayout) findViewById(R.id.video_call_in_ly);
			mVideoTipsLy.setVisibility(View.VISIBLE);
			
			mVideoIcon = (ImageView) findViewById(R.id.video_icon);
			
			mVideoTopTips = (TextView) findViewById(R.id.notice_tips);
			// Top tips view invited ...
			mVideoTopTips.setText(getString(R.string.str_video_invited_recivie, mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
			//底部时间
			mCallStateTips = (TextView) findViewById(R.id.video_call_tips);
			//接受
			mVideoBegin = (Button) findViewById(R.id.video_botton_begin);
			mVideoBegin.setVisibility(View.VISIBLE);
			//拒绝
			mVideoStop = (Button) findViewById(R.id.video_stop);
			mVideoStop.setEnabled(true);
			mVideoBegin.setOnClickListener(this);
			mVideoStop.setOnClickListener(this);
			
			
			mVideoView = (SurfaceView) findViewById(R.id.video_view);
			mVideoView.getHolder().setFixedSize(240, 320);
			mLoaclVideoView = (RelativeLayout) findViewById(R.id.localvideo_view);
			mVideoLayout = (FrameLayout) findViewById(R.id.Video_layout);
			mCameraSwitch= (ImageButton) findViewById(R.id.camera_switch);
			mCameraSwitch.setOnClickListener(this);
			
			mCallStatus = (TextView) findViewById(R.id.call_status);
			mCallStatus.setVisibility(View.GONE);
			cameraInfos = VoiceHelper.getInstance().getDevice().getCameraInfo();
			
			// Find the ID of the default camera
			if(cameraInfos != null && cameraInfos.length != 0) {
				defaultCameraId = cameraInfos[cameraInfos.length - 1].index;
			}
			VoiceHelper.getInstance().getDevice().selectCamera(defaultCameraId, 0, 15, Rotate.Rotate_Auto);
			
			VoiceHelper.getInstance().getDevice().setVideoView(mVideoView, null);
			// Create a RelativeLayout container that will hold a SurfaceView,
	        // and set it as the content of our activity.
			SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
			mLoaclVideoView.addView(localView);
			
		} else {
			setContentView(R.layout.layout_callin);
			
			mVtalkName = (TextView) findViewById(R.id.layout_callin_name);
			mVtalkNumber = (TextView) findViewById(R.id.layout_callin_number);
			((ImageButton) findViewById(R.id.layout_callin_cancel)).setOnClickListener(this);
			((ImageButton) findViewById(R.id.layout_callin_accept)).setOnClickListener(this);
			mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(
					Context.NOTIFICATION_SERVICE);
			showNotification(getString(R.string.notification_calling_title),
					getString(R.string.notification_calling_content));
			
			setDisplayNameNumber();
			
		}
	}

	private void setDisplayNameNumber() {
		if(mCallType == Device.CallType.VOICEP2P) {
			// viop call ...
			if (!TextUtils.isEmpty(mVoipAccount)) {
				mVtalkNumber.setText(mVoipAccount);
			}
		} else {
			// viop call ...
			if (!TextUtils.isEmpty(mPhoneNumber)) {
				mVtalkNumber.setText(mPhoneNumber);
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] mPhoneNumber " + mPhoneNumber);
			}
			if (!TextUtils.isEmpty(mNickName)) {
				mVtalkName.setText(mNickName);
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] VtalkName" + mVtalkName);
			} else {
				mVtalkName.setText(R.string.voip_unknown_user);
			}
		}
	}
	
	
	/**
	 * Read parameters or previously saved state of this activity.
	 * 
	 */
	private void initialize(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras == null) {
			finish();
			return;
		}
		mVoipAccount = extras.getString(Device.CALLER);
		mCurrentCallId = extras.getString(Device.CALLID);
		mCallType = (CallType) extras.get(Device.CALLTYPE);
		// 传入数据是否有误
		if (mVoipAccount == null || mCurrentCallId == null) { 
			finish();
			return;
		}
		// 透传信息
		String[] infos = extras.getStringArray(Device.REMOTE);
		if (infos != null && infos.length > 0) {
			for (String str : infos) {
				if (str.startsWith(KEY_TEL)) {
					mPhoneNumber = VoiceUtil.getLastwords(str, "=");
				} else if (str.startsWith(KEY_NAME)) {
					mNickName = VoiceUtil.getLastwords(str, "=");
				}
			}
		}
		
		
	}

	/**
	 * 呼叫建立后,初始化通话中界面
	 * 
	 */
	public void initCallHold() {
		Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] initCallHold.收到呼叫连接，初始化通话界面.");
		isConnect = true;
		setContentView(R.layout.layout_call_interface);
		mCallStateTips = (TextView) findViewById(R.id.layout_callin_duration);
		mCallMute = (ImageView) findViewById(R.id.layout_callin_mute);
		mCallHandFree = (ImageView) findViewById(R.id.layout_callin_handfree);
		mVHangUp = (ImageView) findViewById(R.id.layout_call_reject);
		mVtalkName = (TextView) findViewById(R.id.layout_callin_name);
		mVtalkName.setVisibility(View.VISIBLE);
		mVtalkNumber = (TextView) findViewById(R.id.layout_callin_number);
		
		mCallStatus = (TextView) findViewById(R.id.call_status);
		mCallStatus.setVisibility(View.VISIBLE);
		
		// 显示时间，隐藏状态
		mCallStateTips.setVisibility(View.GONE);
		
		// 键盘
		mDiaerpadBtn = (ImageView) findViewById(R.id.layout_callin_diaerpad);
		mDiaerpad = (LinearLayout) findViewById(R.id.layout_diaerpad);
		
		setupKeypad();
		mDmfInput = (EditText) findViewById(R.id.dial_input_numer_TXT);
		
		mDiaerpadBtn.setOnClickListener(this);
		mCallMute.setOnClickListener(this);
		mCallHandFree.setOnClickListener(this);
		mVHangUp.setOnClickListener(this);
		
		setDisplayNameNumber();
		try {
			isMute = VoiceHelper.getInstance().getDevice().getMuteStatus();
			isHandsfree = VoiceHelper.getInstance().getDevice().getLoudsSpeakerStatus();
		} catch (Exception e) {
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
		
		case R.id.layout_call_reject:
			// 通话中挂断
			try {
				if (VoiceHelper.getInstance().getDevice() != null && mCurrentCallId != null) {
					VoiceHelper.getInstance().getDevice().releaseCall(mCurrentCallId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.layout_callin_accept:
		case R.id.video_botton_begin: // video ..
			// 接受呼叫
			//mTime = 0;
			try {
				if (VoiceHelper.getInstance().getDevice() != null && mCurrentCallId != null) {
					VoiceHelper.getInstance().getDevice().acceptCall(mCurrentCallId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] acceptCall...");
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
		case R.id.layout_callin_cancel:
			// 呼入拒绝
			try {
				if (VoiceHelper.getInstance().getDevice() != null && mCurrentCallId != null) {
					VoiceHelper.getInstance().getDevice().rejectCall(mCurrentCallId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] rejectCall...");
			finish();
			break;
			
			// video back ...
		case R.id.voice_btn_back :
		case R.id.video_stop :
			mVideoStop.setEnabled(false);
			try {
				if(isConnect) {
					// 通话中挂断
					if (VoiceHelper.getInstance().getDevice() != null && mCurrentCallId != null) {
						VoiceHelper.getInstance().getDevice().releaseCall(mCurrentCallId);
					}
				} else {
					// 呼入拒绝
					if (VoiceHelper.getInstance().getDevice() != null && mCurrentCallId != null) {
						VoiceHelper.getInstance().getDevice().rejectCall(mCurrentCallId);
					}
					finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
	
		case R.id.camera_switch :
			mCameraSwitch.setEnabled(false);
			// check for availability of multiple cameras
            if (cameraInfos.length == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getString(R.string.camera_alert))
                       .setNeutralButton(R.string.dialog_alert_close, null);
                AlertDialog alert = builder.create();
                alert.show();
                return ;
            }

            // OK, we have multiple cameras.
            // Release this camera -> cameraCurrentlyLocked
            if(cameraInfos != null ) {
            	for (int i = 0; i < cameraInfos.length; i++) {
            		if(defaultCameraId != cameraInfos[i].index){
            			defaultCameraId = cameraInfos[i].index;
            			break;
            		}
                }
            }
            if(defaultCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            	Toast.makeText(CallInActivity.this, R.string.camera_switch_front, Toast.LENGTH_SHORT).show();
            } else {
            	Toast.makeText(CallInActivity.this,  R.string.camera_switch_back, Toast.LENGTH_SHORT).show();
            	
            }
            VoiceHelper.getInstance().getDevice().selectCamera(defaultCameraId, 0, 15, Rotate.Rotate_Auto);
    		mCameraSwitch.setEnabled(true);
			break;
		default:
			break;
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
		releaseCurrCall(true);
		super.onDestroy();
	}

	private void releaseCurrCall(boolean releaseAll) {
		/*if (mTimer != null  && releaseAll) {
			mTimer.cancel();
			mTimer = null;
		}*/
		currIntent = null;
		if(mHandler!=null && releaseAll){
			mHandler = null;
		}
		mCallMute = null;
		mCallHandFree = null;
		mVHangUp = null;
		mCallStateTips = null;
		mVtalkName = null;
		mVtalkNumber = null;
		if (isMute && VoiceHelper.getInstance().getDevice() != null) {
			VoiceHelper.getInstance().getDevice().setMute(isMute);
		}
		if (isHandsfree && VoiceHelper.getInstance().getDevice() != null) {
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(isMute);
		}
		if (mNotificationManager != null) {
			mNotificationManager.cancel(R.drawable.icon_incoming_samll);
			mNotificationManager = null;
		}
		mPhoneNumber = null;
		BaseApplication.getInstance().setAudioMode(AudioManager.MODE_NORMAL);
	}

	/**
	 * 用于挂断时修改按钮属性及关闭操作
	 */
	private void finishCalling() {
		try {
			if (isConnect) {
				mChronometer.stop();
				mCallStateTips.setVisibility(View.VISIBLE);
				
				
				isConnect = false;
				if(mCallType == Device.CallType.VIDEO) {
					mVideoLayout.setVisibility(View.GONE);
					mVideoIcon.setVisibility(View.VISIBLE);
					mVideoTopTips.setVisibility(View.VISIBLE);
					mCameraSwitch.setVisibility(View.GONE);
					if(mVideoStop.isEnabled()) {
						mVideoTopTips.setText(getString(R.string.str_video_call_end, CCPConfig.VoIP_ID.substring(CCPConfig.VoIP_ID.length() - 3, CCPConfig.VoIP_ID.length())));
					} else {
						mVideoTopTips.setText(R.string.voip_calling_finish);
					}
					// bottom can't click ...
				} else {
					mChronometer.setVisibility(View.GONE);
					mCallStateTips.setText(R.string.voip_calling_finish);
					mCallHandFree.setClickable(false);
					mCallMute.setClickable(false);
					mVHangUp.setClickable(false);
					mDiaerpadBtn.setClickable(false);
					mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
					mCallHandFree.setImageResource(R.drawable.call_interface_hands_free);
					mCallMute.setImageResource(R.drawable.call_interface_mute);
					mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
				}
				
				// 延时关闭
				mHandler.postDelayed(finish, 3000);
			} else {
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * 回调handler，根据Voip通话状态，更新界面显示
	 */
	private android.os.Handler mHandler = new android.os.Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String callid = null;
			Bundle b = null;
			// 获取通话ID
			if(msg.obj instanceof String){
				callid = (String)msg.obj;
			}else if (msg.obj instanceof Bundle){
				b = (Bundle) msg.obj;
				callid = b.getString(Device.CALLID);
			}
			switch (msg.what) {
			case VoiceHelper.WHAT_ON_CALL_ANSWERED:
				
				// answer  
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] voip on call answered!!");
				if (callid != null && mCurrentCallId.equals(callid)) {
					if(mCallType == Device.CallType.VIDEO) {
						initResVideoSuccess();
						
					} else {
						initialize(currIntent);
						// voip other ..
						initCallHold();
					}
					
					mChronometer = (Chronometer) findViewById(R.id.chronometer);	
					mChronometer.setBase(SystemClock.elapsedRealtime());
					mChronometer.setVisibility(View.VISIBLE);
					mChronometer.start();
					if(mHandler != null && mCallType != Device.CallType.VIDEO) {
						mHandler.sendMessage(mHandler.obtainMessage(VideoActivity.WHAT_ON_CODE_CALL_STATUS));
					}
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_RELEASED:
				
				// 挂断
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[CallInActivity] voip on call released!!");
				try {
					if (callid != null && mCurrentCallId.equals(callid)) {
						finishCalling();
						//finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case VideoActivity.WHAT_ON_CODE_CALL_STATUS:
				CallStatisticsInfo callStatistics = VoiceHelper.getInstance().getDevice().getCallStatistics(mCallType);
				if(callStatistics != null) {
					int fractionLost = callStatistics.getFractionLost();
					int rttMs = callStatistics.getRttMs();
					mCallStatus.setText(getString(R.string.str_call_status, (fractionLost/255),rttMs));
					mCallStatus.setVisibility(View.VISIBLE);
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

	/**
	 * 显示状态栏
	 * @param topic
	 * @param text
	 */
	private void showNotification(String topic, String text) {
		try {
			Notification notification = new Notification(R.drawable.icon_incoming_samll, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
			Intent intent = new Intent("ACTION_VOIP_INCALL");
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(this, topic, text, contentIntent);
			mNotificationManager.notify(R.drawable.icon_incoming_samll, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 屏蔽返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onResume() {
		super.onResume();
		lockScreen();
		
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
		releaseLockScreen();
	}
	// video ..
	private void initResVideoSuccess() {
		
		mVideoLayout.setVisibility(View.VISIBLE);
		mVideoIcon.setVisibility(View.GONE);
		mCallStateTips.setText(getString(R.string.str_video_bottom_time,mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
		mCallStateTips.setVisibility(View.VISIBLE);
		mVideoTopTips.setVisibility(View.GONE);
		mCameraSwitch.setVisibility(View.VISIBLE);
		
		mVideoBegin.setVisibility(View.GONE);
		isConnect = true;
	}
	
	
	/***********************************  KeyPad    *************************************************/
	
	private EditText mDmfInput;
    
	void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDmfInput.getText().clear();
		mDmfInput.onKeyDown(keyCode, event);
		VoiceHelper.getInstance().getDevice().sendDTMF(mCurrentCallId, mDmfInput.getText().toString().toCharArray()[0]);
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
	
}
