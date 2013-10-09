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


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.hisun.phone.core.voice.Device.AudioMode;
import com.hisun.phone.core.voice.Device.AudioType;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.ExConsultation.ExpertMainActivity;
import com.voice.demo.chatroom.ChatRoomConversation;
import com.voice.demo.group.GroupMessageListActivity;
import com.voice.demo.interphone.InterPhoneActivity;
import com.voice.demo.interphone.InviteInterPhoneActivity;
import com.voice.demo.outboundmarketing.MarketActivity;
import com.voice.demo.setting.SettingsActivity;
import com.voice.demo.setting.SettingsActivity.SettingSwitch;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.tools.SDKVersion;
import com.voice.demo.video.VideoActivity;
import com.voice.demo.voicecode.VoiceVerificationCodeActivity;
/**
 * Starting the program list
 * 1, Network telephone
 * 2, Delay voice
 * 3, inter phone 
 * 4, video
 * 5, Marketing outbound
 * 6, Voice verification code
 * 7, Voice group chat
 *
 */
public class SelectVoiceActivity extends CCPBaseActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_select_voice);
		
		// Network telephone
		findViewById(R.id.voip_select).setOnClickListener(this);
		// inter phone 
		findViewById(R.id.intercom_select).setOnClickListener(this);
		// video
		findViewById(R.id.video_call).setOnClickListener(this);
				// Marketing outbound
		findViewById(R.id.market_outside_call).setOnClickListener(this);
		// Voice verification code
		findViewById(R.id.voice_verification_code).setOnClickListener(this);
		
		// Voice group chat
		findViewById(R.id.voice_group_chat_selector).setOnClickListener(this);
		
		// im sms
		findViewById(R.id.im_sms_selector).setOnClickListener(this);
		
		// setting
		findViewById(R.id.setting_selector).setOnClickListener(this);
		
		
		findViewById(R.id.ex_consultation).setOnClickListener(this);
		findViewById(R.id.phonebook_backup).setVisibility(View.INVISIBLE);
		//findViewById(R.id.phonebook_backup).setOnClickListener(this);
		
		
		
		if(VoiceHelper.getInstance().getDevice() == null) {
			CCPUtil.clearActivityTask(this);
		} else {
			initAudioConfig();
			// 
			boolean p2pSwitch = getSharedPreferences().getBoolean(SettingSwitch.SETTING_SWITCH_P2P, false);
			if(p2pSwitch) {
				//getDevice().setFirewallPolicy(1);
			}
			// Check the SDK version
			checkSDKversion();
		}
		
	}


	private static final int REQUEST_CODE_VIDEO_CALL = 11;
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.voip_select:
			startActivity(new Intent(SelectVoiceActivity.this, NetPhoneCallActivity.class));
			break;
		case R.id.intercom_select:
			startActivity(new Intent(SelectVoiceActivity.this, InterPhoneActivity.class));
			break;
		
		
		case R.id.video_call:
			Intent intent = new Intent(SelectVoiceActivity.this, InviteInterPhoneActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			intent.putExtra("create_to", InviteInterPhoneActivity.CREATE_TO_VIDEO_CALL);
			startActivityForResult(intent, REQUEST_CODE_VIDEO_CALL);
			break;
			
		case R.id.market_outside_call:
			startActivity(new Intent(SelectVoiceActivity.this, MarketActivity.class));
			break;
			
		case R.id.voice_verification_code:
			//alertTipsDialog();
			startActivity(new Intent(SelectVoiceActivity.this, VoiceVerificationCodeActivity.class));
			break;
			
		case R.id.voice_group_chat_selector:
			//alertTipsDialog();
			startActivity(new Intent(SelectVoiceActivity.this, ChatRoomConversation.class));
			break;
			
		case R.id.im_sms_selector:
			startActivity(new Intent(SelectVoiceActivity.this, GroupMessageListActivity.class));
			break;
			
		case R.id.setting_selector:
			startActivity(new Intent(SelectVoiceActivity.this, SettingsActivity.class));
			break;
		case R.id.ex_consultation:
			startActivity(new Intent(SelectVoiceActivity.this, ExpertMainActivity.class));
			break;
		case R.id.phonebook_backup:
			//startActivity(new Intent(SelectVoiceActivity.this, SettingsActivity.class));
			//startActivity(new Intent(SelectVoiceActivity.this, XHmainActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	private void initAudioConfig(){
		Context ctx = SelectVoiceActivity.this;
		SharedPreferences sp = ctx.getSharedPreferences(CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);

		//设置AUDIO_AGC
		Boolean flag = sp.getBoolean("AUTOMANAGE_SWITCH_KEY", false);
		AudioMode audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_AGC,	sp.getInt("AUTOMANAGE_INDEX_KEY", 3));
		VoiceHelper.getInstance().getDevice().setAudioConfigEnabled(AudioType.AUDIO_AGC, flag, audioMode);
		
		//设置AUDIO_EC
		flag = sp.getBoolean("ECHOCANCELLED_SWITCH_KEY", true);
		audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_EC, sp.getInt("ECHOCANCELLED_INDEX_KEY", 4));
		VoiceHelper.getInstance().getDevice().setAudioConfigEnabled(AudioType.AUDIO_EC, flag, audioMode);

		//设置AUDIO_NS
		flag = sp.getBoolean("SILENCERESTRAIN_SWITCH_KEY", true);
		audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_NS, sp.getInt("SILENCERESTRAIN_INDEX_KEY", 6));
		VoiceHelper.getInstance().getDevice().setAudioConfigEnabled(AudioType.AUDIO_NS, flag, audioMode);

		//设置码流
		flag = sp.getBoolean("VIDEOSTREAM_SWITCH_KEY", false);
		if(flag){
			String bitString = sp.getString("VIDEOSTREAM_CONTENT_KEY", "150");
			int bitrates = Integer.valueOf(bitString).intValue();
			VoiceHelper.getInstance().getDevice().setVideoBitRates(bitrates);
		}

	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	//  Video ...
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log4Util.d(VoiceHelper.DEMO_TAG ,"[SelectVoiceActivity] onActivityResult: requestCode=" + requestCode
				+ ", resultCode=" + resultCode + ", data=" + data);

		// If there's no data (because the user didn't select a number and
		// just hit BACK, for example), there's nothing to do.
		if (requestCode != REQUEST_CODE_VIDEO_CALL ) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[SelectVoiceActivity] onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}
		
		String phoneStr = "" ;
		if(data.hasExtra("VOIP_CALL_NUMNBER")) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				phoneStr = extras.getString("VOIP_CALL_NUMNBER");
			}
		}
		
		if(TextUtils.isEmpty(phoneStr)) {
			BaseApplication.getInstance().showToast(R.string.edit_input_empty);
			return ;
		}

		// VOIP免费电话
		if (!phoneStr.startsWith("8") || phoneStr.length() != 14) {
			// 判断输入合法性
			BaseApplication.getInstance().showToast(
					getString(R.string.voip_number_format));
			return;
		}
		
		Intent intent = new Intent(SelectVoiceActivity.this, VideoActivity.class);
		intent.putExtra(BaseApplication.VALUE_DIAL_VOIP_INPUT, phoneStr);
		startActivity(intent);
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// TODO: 处理退出
			BaseApplication.getInstance().quitApp();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void checkSDKversion() {
		SDKVersion sdkVersion = CCPUtil.getSDKVersion(VoiceHelper.getInstance().getDevice().getVersion());
		if(sdkVersion != null) {
			String tips = getString(R.string.str_sdk_support_tips, sdkVersion.getVersion(),String.valueOf(sdkVersion.isAudioSwitch())
					,String.valueOf(sdkVersion.isVideoSwitch()));
			addNotificatoinToView(tips);
			Log4Util.i(VoiceHelper.DEMO_TAG, "The current SDK version number :" + sdkVersion.toString());
		}
	}
}
