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
package com.voice.demo.video;

import org.webrtc.videoengine.ViERenderer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.CCP.phone.CameraInfo;
import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.Device.Rotate;
import com.hisun.phone.core.voice.DeviceListener.Reason;
import com.hisun.phone.core.voice.model.CallStatisticsInfo;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.setting.SettingsActivity;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;
/**
 * 
 * @author Jorstin Chan
 * @version Time: 2013-9-6
 */
public class VideoActivity extends CCPBaseActivity implements View.OnClickListener {

	public static final int WHAT_ON_CODE_CALL_STATUS = 11;
	
	private Button mVideoStop;
	private Button mVideoBegin;
	private Button mVideoCancle;
	private ImageView mVideoIcon;
	private RelativeLayout mVideoTipsLy;
	
	private TextView mVideoTopTips;
	private TextView mVideoCallTips;
	private TextView mCallStatus;
	private SurfaceView mVideoView;
	// Local Video
	private RelativeLayout mLoaclVideoView;
	// Remote Video
	private FrameLayout mVideoLayout;
	private Chronometer mChronometer;
	// voip 账号
	private String mVoipAccount;
	// 通话 ID
	private String mCurrentCallId;
	// 通话状态
	private boolean isConnect = false;
	// 手动挂断
	private boolean isSelfReject = false;
	
	private ImageButton mCameraSwitch;
	
	CameraInfo[] cameraInfos;
	// The first rear facing camera
    int defaultCameraId;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.layout_video_activity);
		
		handleTitleDisplay(getString(R.string.btn_title_back), getString(R.string.app_title_video), null);
		VoiceHelper.getInstance().setHandler(mHandler);
		initResourceRefs();
		initialize();
		
		cameraInfos = VoiceHelper.getInstance().getDevice().getCameraInfo();
		
		// Find the ID of the default camera
		if(cameraInfos != null && cameraInfos.length != 0) {
			defaultCameraId = cameraInfos[cameraInfos.length - 1].index;
		}
		VoiceHelper.getInstance().getDevice().selectCamera(defaultCameraId, 0, 15, Rotate.Rotate_Auto);
		mCurrentCallId = VoiceHelper.getInstance().getDevice().makeCall(Device.CallType.VIDEO, mVoipAccount);
		
		registerReceiver(new String[]{SettingsActivity.INTENT_P2P_ENABLED});
	}

	private void initResourceRefs() {
		mVideoTipsLy = (RelativeLayout) findViewById(R.id.video_call_in_ly);
		mVideoIcon = (ImageView) findViewById(R.id.video_icon);
		
		mVideoTopTips = (TextView) findViewById(R.id.notice_tips);
		mVideoCallTips = (TextView) findViewById(R.id.video_call_tips);
		
		mVideoCancle = (Button) findViewById(R.id.video_botton_cancle);
		mVideoBegin = (Button) findViewById(R.id.video_botton_begin);
		mVideoStop = (Button) findViewById(R.id.video_stop);
		mVideoStop.setEnabled(false);
		mVideoCancle.setOnClickListener(this);
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
		//mVideoView.getHolder().setFixedSize(width, height);
		VoiceHelper.getInstance().getDevice().setVideoView(mVideoView, null);
		
		// Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
		SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
		mLoaclVideoView.addView(localView);
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
			
			// Video...
			mVoipAccount = bundle.getString(BaseApplication.VALUE_DIAL_VOIP_INPUT);
			if (mVoipAccount == null) {
				finish();
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(mLoaclVideoView != null && mLoaclVideoView.getVisibility() == View.VISIBLE) {
			SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
			mLoaclVideoView.addView(localView);
		}
		
		lockScreen();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseLockScreen();
	}
	
	@Override
	protected void handleTitleAction(int direction) {
		if(direction == TITLE_LEFT_ACTION) {
			// Hang up the video call...
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] onClick: Voip talk hand up, CurrentCallId " + mCurrentCallId);
			isSelfReject = true;
			try {
				if (mCurrentCallId != null) {
					VoiceHelper.getInstance().getDevice().releaseCall(mCurrentCallId);
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
		} else {
			
			super.handleTitleAction(direction);
		}
	}
	
	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if(intent.getAction().equals(SettingsActivity.INTENT_P2P_ENABLED)){
			addNotificatoinToView(getString(R.string.str_p2p_enable), Gravity.TOP);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_botton_begin:

			break;
			
		case R.id.video_stop:
		case R.id.video_botton_cancle:
			
			handleTitleAction(TITLE_LEFT_ACTION);
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
            	defaultCameraId = (defaultCameraId + 1) % cameraInfos.length;
            	/*for (int i = 0; i < cameraInfos.length; i++) {
            		if(defaultCameraId != cameraInfos[i].index){
            			defaultCameraId = cameraInfos[i].index;
            			break;
            		}
                }*/
            }
           /* if(defaultCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            	Toast.makeText(VideoActivity.this, R.string.camera_switch_front, Toast.LENGTH_SHORT).show();
            } else {
            	Toast.makeText(VideoActivity.this,  R.string.camera_switch_back, Toast.LENGTH_SHORT).show();
            	
            }*/
            VoiceHelper.getInstance().getDevice().selectCamera(defaultCameraId, 0, 15, Rotate.Rotate_Auto);
    		mCameraSwitch.setEnabled(true);
			break;
		default:
			break;
		}
	}
	
	
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
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] handleMessage: voip alerting!!");
				if (callid != null && mCurrentCallId.equals(callid)) {//等待对方接受邀请...
					mVideoCallTips.setText(getString(R.string.str_tips_wait_invited));
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_PROCEEDING:
				 // 连接到服务器
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] handleMessage: voip on call proceeding!!");
				if (callid != null && mCurrentCallId.equals(callid)) {
					mVideoCallTips.setText(getString(R.string.voip_call_connect));
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_ANSWERED:
				// 对端应答
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] handleMessage: voip on call answered!!");
				if (callid != null && mCurrentCallId.equals(callid) && !isConnect) {
					initResVideoSuccess();
					if(mHandler != null ) {
						// 
						//mHandler.sendMessage(mHandler.obtainMessage(WHAT_ON_CODE_CALL_STATUS));
					}
				}
				break;
				
			case VoiceHelper.WHAT_ON_CALL_RELEASED:
				// 远端挂断，本地挂断在onClick中处理
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] handleMessage: voip on call released!!");
				if (callid != null && mCurrentCallId.equals(callid) && !isSelfReject) {
					finishCalling();
				}
				break;
			case VoiceHelper.WHAT_ON_CALL_MAKECALL_FAILED:
				// 发起通话失败
				Log4Util.d(VoiceHelper.DEMO_TAG ,"[VideoActivity] handleMessage: voip on call makecall failed!!");
				if (b != null && b.get(Device.REASON) != null) {
					reason = (Reason) b.get(Device.REASON);
					if (callid != null && mCurrentCallId.equals(callid) && !isSelfReject) {
						finishCalling(reason);
					}
				}
				break;
			case WHAT_ON_CODE_CALL_STATUS:
				CallStatisticsInfo callStatistics = VoiceHelper.getInstance().getDevice().getCallStatistics(Device.CallType.VIDEO);
				if(callStatistics != null) {
					int fractionLost = callStatistics.getFractionLost();
					int rttMs = callStatistics.getRttMs();
					mCallStatus.setText(getString(R.string.str_call_status, fractionLost,rttMs));
				}
				if(isConnect && mHandler!= null) {
					Message callMessage = mHandler.obtainMessage(WHAT_ON_CODE_CALL_STATUS);
					mHandler.sendMessageDelayed(callMessage,4000);
				}
				break;
			default:
				break;
			}
		}

	};
	
	//
	private void initResVideoSuccess() {
		isConnect = true;
		mVideoLayout.setVisibility(View.VISIBLE);
		mVideoIcon.setVisibility(View.GONE);
		mVideoTopTips.setVisibility(View.GONE);
		mCameraSwitch.setVisibility(View.VISIBLE);
		mVideoTipsLy.setVisibility(View.VISIBLE);
		
		// bottom ...
		mVideoCancle.setVisibility(View.GONE);
		mVideoCallTips.setVisibility(View.VISIBLE); 
		mVideoCallTips.setText(getString(R.string.str_video_bottom_time,mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
		mVideoStop.setVisibility(View.VISIBLE);
		mVideoStop.setEnabled(true);
		
		
		mChronometer = (Chronometer) findViewById(R.id.chronometer);	
		mChronometer.setBase(SystemClock.elapsedRealtime());
		mChronometer.setVisibility(View.VISIBLE);
		mChronometer.start();
	}
	
	/**
	 * 根据状态,修改按钮属性及关闭操作
	 * 
	 * @param reason
	 */
	private void finishCalling() {
		try {
			// set Chronometer view gone..
			mChronometer.stop();
			//mChronometer.setVisibility(View.GONE);
			// 3 second mis , finsh this activit ...
			mHandler.postDelayed(finish, 3000);
			
			mVideoTopTips.setVisibility(View.VISIBLE);
			mCameraSwitch.setVisibility(View.GONE);
			mVideoTopTips.setText(R.string.voip_calling_finish);
			if(isConnect) {
				mVideoLayout.setVisibility(View.GONE);
				mVideoIcon.setVisibility(View.VISIBLE);
				
				// bottom can't click ...
				mVideoStop.setEnabled(false);
			} else {
				mVideoCancle.setEnabled(false);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void finishCalling(Reason reason) {
		try {
			//mChronometer.setVisibility(View.GONE);

			mVideoTopTips.setVisibility(View.VISIBLE);
			mCameraSwitch.setVisibility(View.GONE);
			if(isConnect) {
				mChronometer.stop();
				mVideoLayout.setVisibility(View.GONE);
				mVideoIcon.setVisibility(View.VISIBLE);
				isConnect = false;
				// bottom can't click ...
				mVideoStop.setEnabled(false);
			} else {
				mVideoCancle.setEnabled(false);
			}
			mHandler.postDelayed(finish, 3000);
			
			// 处理通话结束状态
			if (reason == Reason.DECLINED) {
				mVideoTopTips.setText(getString(R.string.str_video_call_end, mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
			} else if (reason == Reason.CALLMISSED) {
				mVideoTopTips.setText(getString(R.string.voip_calling_timeout));
			} else if (reason == Reason.PAYMENT) {
				mVideoTopTips.setText(getString(R.string.voip_call_fail_no_cash));
			} else if (reason == Reason.UNKNOWN) {
				mVideoTopTips.setText(getString(R.string.voip_calling_finish));
			} else if (reason == Reason.NOTRESPONSE) {
				mVideoTopTips.setText(getString(R.string.voip_call_fail));
			} else if (reason == Reason.VERSIONNOTSUPPORT) {
				mVideoTopTips.setText(getString(R.string.str_video_not_support));
			} else if (reason == Reason.OTHERVERSIONNOTSUPPORT) {
				mVideoTopTips.setText(getString(R.string.str_other_voip_not_support));
			}  else {
				mVideoTopTips.setText(getString(R.string.voip_calling_network_instability));
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	
	
}
