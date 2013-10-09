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
package com.voice.demo.interphone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.interphone.InterphoneControlMicMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneExitMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneInviteMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneJoinMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneMember;
import com.hisun.phone.core.voice.model.interphone.InterphoneMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneOverMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneReleaseMicMsg;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;

public class InterPhoneRoomActivity extends CCPBaseActivity implements View.OnTouchListener{
	
	private static final long INTER_PHONE_TIME_INTERVAL = 500;
	
	private static final String TYPE_UNONLINE = "0";
	private static final String TYPE_ONLINE = "1";
	
	private static final String TYPE_MIC_CONTROLER = "1";
	private static final String TYPE_MIC_UNCONTROL = "0";
	
	private static final String TYPE_SPEAK_INITIATOR = "1";
	private static final String TYPE_SPEAK_UNINITIATOR = "0";
	
	
	public static final int MESSAGE_TYPE_JOIN = 101;
	public static final int MESSAGE_TYPE_EXIT = 102;
	public static final int MESSAGE_TYPE_CONTROL_MIC = 103;
	public static final int MESSAGE_TYPE_RELEASE_MIC = 104;
	
	private ListView mInterphoneList;
	private TextView mNoticeTips;
	private Button mInterSpeak;
	
	
	private InterPhoneMemberAdapter mIPoneMemAdapter;
	
	private ArrayList<InterphoneMember> mJoinMembers;
	
	private Chronometer mChronometer;
	private TextView mPersonCount;
	private int onLineNum;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_interphone_room_activity);
		
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		

		
		handleTitleDisplay(null
				, null
				, getString(R.string.str_quit));
		
		mInterphoneList = (ListView) findViewById(R.id.interphone_list);
		mNoticeTips = (TextView) findViewById(R.id.notice_tips);
		mInterSpeak = (Button) findViewById(R.id.interphone_speak);
		
		mInterSpeak.setEnabled(false);
		mInterSpeak.setOnTouchListener(this) ;
		
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mPersonCount = (TextView) findViewById(R.id.count_tv);
		
		if(VoiceHelper.getInstance().getDevice() == null) {
			CCPUtil.registDialog(this) ;
			//finish();
		} else {
			VoiceHelper.getInstance().setHandler(mHandler);
			initialize(savedInstanceState);
			
		}
	}

	
	private void InitIntercom(ArrayList<String> mInviterMember) {
		// Initiated intercom ...
		mJoinMembers = new ArrayList<InterphoneMember>();
		mInviterMember.add(CCPConfig.VoIP_ID);
		for (String member : mInviterMember) {
			InterphoneMember interMember = new InterphoneMember();
			interMember.mic = TYPE_MIC_UNCONTROL;
			interMember.online = TYPE_UNONLINE;
			interMember.voipId = member;
			if(CCPConfig.VoIP_ID.equals(member)) {
				interMember.type = TYPE_SPEAK_INITIATOR;
				mJoinMembers.add(0, interMember);
			} else {
				interMember.type = TYPE_SPEAK_UNINITIATOR ;
				mJoinMembers.add(interMember);
			}
		}
		mPersonCount.setText("0/" + mJoinMembers.size());
		mInviterMember.remove(CCPConfig.VoIP_ID);
		VoiceHelper.getInstance().getDevice().startInterphone(mInviterMember.toArray(new String[]{}), CCPConfig.App_ID);
		
		mNoticeTips.setText(getString(R.string.str_top_notice_tips_invite ,mInviterMember.size()));
		mIPoneMemAdapter = new InterPhoneMemberAdapter(getApplicationContext(), mJoinMembers);
		mInterphoneList.setAdapter(mIPoneMemAdapter);
	}


	private void initialize(Bundle savedInstanceState) {
		// read parameters or previously saved state of this activity.
		Intent intent = getIntent();
		if(intent.hasExtra("confNo")) {
			// join to invite an intercom
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String confNo = extras.getString("confNo"); 
				VoiceHelper.getInstance().getDevice().joinInterphone(confNo);
				mNoticeTips.setText(R.string.top_tips_connecting_wait);
				setActivityTitle(getString(R.string.str_inter_phone_room_title, confNo.substring(confNo.length() - 3, confNo.length())));
			}
			
		} else if (intent.hasExtra("InviterMember")) {
			Bundle extras = intent.getExtras();
			ArrayList<String> mInviterMember = null ;
			if (extras != null) {
				mInviterMember =  extras.getStringArrayList("InviterMember");
				
			}
			if (mInviterMember == null || mInviterMember.isEmpty()) {
				throw new IllegalStateException(
						"Invalid inviter phone  member error :" + " " + mInviterMember);
			} else {
				//
			}
			
			InitIntercom(mInviterMember);
		}
	
	}
	
	boolean isDownEvent;
	long downTime = 0;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Message obtainMessage = null;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lockScreen();
			if (mHandler != null ) {
				obtainMessage = mHandler.obtainMessage(VoiceHelper.WHAT_ON_PLAY_MUSIC);
				mHandler.sendMessage(obtainMessage);
			}
			isDownEvent = true;
			downTime = event.getDownTime();
			mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_pressed);
			if (mHandler != null ) {
				obtainMessage = mHandler.obtainMessage(VoiceHelper.WHAT_ON_REQUEST_MIC_CONTROL);
				mHandler.sendMessageDelayed(obtainMessage, INTER_PHONE_TIME_INTERVAL);
			}
			break;
		case MotionEvent.ACTION_UP:
			releaseLockScreen();
			isDownEvent = false;
			if (mHandler != null ) {
				mHandler.removeMessages(VoiceHelper.WHAT_ON_REQUEST_MIC_CONTROL);
				obtainMessage = mHandler.obtainMessage(VoiceHelper.WHAT_ON_STOP_MUSIC);
				mHandler.sendMessage(obtainMessage);
			}
			mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_normal);
			if((event.getEventTime() - downTime) >= INTER_PHONE_TIME_INTERVAL) {
				VoiceHelper.getInstance().getDevice().releaseMic(confNo);
				downTime = 0;
			}
			mNoticeTips.setText(R.string.top_tips_intercom_ing);
			break;
		}
		return false;
	}
	
	@Override
	protected void handleTitleAction(int direction) {
		if(direction == TITLE_RIGHT_ACTION) {
			// quit interphone ...
			if(VoiceHelper.getInstance().getDevice() != null) {
				VoiceHelper.getInstance().getDevice().exitInterphone();
				VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(true);
			}
			finish();
		} else {
			super.handleTitleAction(direction);
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(VoiceHelper.getInstance().getDevice() != null ) {
			VoiceHelper.getInstance().getDevice().exitInterphone();
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(false);
		}
	}
	
	class InterPhoneMemberAdapter extends ArrayAdapter<InterphoneMember> {

		LayoutInflater mInflater;
		
		public InterPhoneMemberAdapter(Context context,List<InterphoneMember> objects) {
			super(context, 0, objects);
			
			mInflater = getLayoutInflater();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			InterJoinMemberHolder holder;
			if (convertView == null|| convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.into_interphone_member_list_item, null);
				holder = new InterJoinMemberHolder();
				
				holder.statuIcon = (ImageView) convertView.findViewById(R.id.interphone_join_statu);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.actionTips = (TextView) convertView.findViewById(R.id.action_tips);
			} else {
				holder = (InterJoinMemberHolder) convertView.getTag();
			}
			
			// do ..
			InterphoneMember phoneMember = mJoinMembers.get(position);
			if( phoneMember != null ) {
				holder.name.setText(phoneMember.voipId);
				
				String control = phoneMember.mic;
				String online = phoneMember.online;
				
				int resoureId = 0 ;
				int tipsId = 0;
				if(TYPE_ONLINE.equals(online)) {
					if(TYPE_SPEAK_INITIATOR.equals(control)) {
						resoureId = R.drawable.status_speaking ;
						tipsId = R.string.str_join_speaking ;
					} else {
						resoureId = R.drawable.status_join ;
						tipsId = R.string.str_join_success ;
					}
				} else {
					resoureId = R.drawable.status_wait ;
					tipsId = R.string.str_join_wait ;
				}
				
				if(CCPConfig.VoIP_ID.equals(phoneMember.voipId)) {
					resoureId = R.drawable.inter_person_icon;
				}
				holder.statuIcon.setImageResource(resoureId);
				holder.actionTips.setText(tipsId);
			}
			
			return convertView;
		}
		
		class InterJoinMemberHolder {
			ImageView statuIcon;
			TextView name;
			TextView actionTips;
		}
	}
	
	private String confNo;
	
	// Callback handler, according to the interphone state, 
	// update the display interface
	private android.os.Handler mHandler = new android.os.Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = 0;
			InterphoneMsg ipmsg = null;
			ArrayList<InterphoneMember> members = null;
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(Device.REASON);
				if (b.getString(Device.CONFNO) != null)
					confNo = b.getString(Device.CONFNO);
				if (b.getSerializable(Device.INTERPHONEMSG) != null)
					ipmsg = (InterphoneMsg) b.getSerializable(Device.INTERPHONEMSG);
				if (b.getSerializable(Device.MEMBERS) != null)
					members = (ArrayList<InterphoneMember>) b.getSerializable(Device.MEMBERS);
			}

			switch (msg.what) {
			case VoiceHelper.WHAT_ON_INTERPHONE:
				reason = b.getInt(Device.REASON);
				if(reason == 0){
					// join interphone success ..
					VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(true);
					VoiceHelper.getInstance().getDevice().queryMembersWithInterphone(confNo);
					mNoticeTips.setText(CCPConfig.VoIP_ID + getString(R.string.str_join_inter_phone_success));
					setActivityTitle(getString(R.string.str_inter_phone_room_title ,confNo.substring(confNo.length() - 3, confNo.length())));
					
				}else{ 
					//  failed ..
					Log4Util.d(VoiceHelper.DEMO_TAG , "[InterPhoneRoomActivity] handleMessage: Sorry ,invite member inter phone failed ...");
					VoiceHelper.getInstance().getDevice().exitInterphone();
					BaseApplication.getInstance().showToast(getString(R.string.str_join_inter_phone_failed, reason));
					finish();	
				}
				break;
			case VoiceHelper.WHAT_ON_INTERPHONE_MEMBERS:
				mInterSpeak.setEnabled(true);
				if(members!=null){
					if(mJoinMembers == null ) {
						mJoinMembers = new ArrayList<InterphoneMember>();
					}
					mJoinMembers.clear();
					for (InterphoneMember i :members) {
						if(i.voipId.equals(CCPConfig.VoIP_ID)) {
							mJoinMembers.add(0, i);
						} else {
							mJoinMembers.add(i);
						}
						if(TYPE_ONLINE.equals(i.online)) {
							increase(1);
						}
					}
					mPersonCount.setText(onLineNum + "/" + members.size());
					mIPoneMemAdapter = new InterPhoneMemberAdapter(getApplicationContext(), mJoinMembers);
					mInterphoneList.setAdapter(mIPoneMemAdapter);
				} 
				break;
			case VoiceHelper.WHAT_ON_CONTROL_MIC:
				reason = b.getInt(Device.REASON);
				if(reason==0){
					// control mic success ..
					try {
						CCPUtil.playNotifycationMusic(getApplicationContext(), "inter_phone_connect.mp3");
					} catch (IOException e) {
						e.printStackTrace();
					}
					mChronometer.setBase(SystemClock.elapsedRealtime());
					mChronometer.setVisibility(View.VISIBLE);
					mChronometer.start();
					mNoticeTips.setText(R.string.str_control_mic_success);
					mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_connect) ;
					UpdateViewUI(new String[] {CCPConfig.VoIP_ID}, MESSAGE_TYPE_CONTROL_MIC);
					
				}else{
					// failed ..
					if(isDownEvent) {
						mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_failed) ;
					} else {
						mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_normal) ;
					}
					mNoticeTips.setText(R.string.str_control_mic_failed);
				}	
				break;
			case VoiceHelper.WHAT_ON_RELEASE_MIC:
				
				// Regardless of whether the successful release of wheat, wheat
				// UI pictures of local changes
				mInterSpeak.setBackgroundResource(R.drawable.voice_intephone_normal);
				UpdateViewUI(new String[] {CCPConfig.VoIP_ID}, MESSAGE_TYPE_RELEASE_MIC);
				break;
			case VoiceHelper.WHAT_ON_INTERPHONE_SIP_MESSAGE:
				
				try {
					if(ipmsg!=null){
						if (ipmsg instanceof InterphoneInviteMsg) {
							InterphoneInviteMsg ipi= (InterphoneInviteMsg)ipmsg;
							mNoticeTips.setText(getString(R.string.str_invite_join_inter, ipi.from));
						} else if (ipmsg instanceof InterphoneJoinMsg) {
							InterphoneJoinMsg ipj = (InterphoneJoinMsg)ipmsg;
							increase(ipj.whos.length);
							UpdateViewUI(ipj.whos, MESSAGE_TYPE_JOIN) ;
						} else if (ipmsg instanceof InterphoneExitMsg) {
							InterphoneExitMsg ipe = (InterphoneExitMsg)ipmsg;
							decrease(ipe.whos.length);
							UpdateViewUI(ipe.whos, MESSAGE_TYPE_EXIT);
						} else if (ipmsg instanceof InterphoneOverMsg){
							// interphone over .
							
						} else if (ipmsg instanceof InterphoneControlMicMsg){
							
							InterphoneControlMicMsg mic = (InterphoneControlMicMsg)ipmsg;
							UpdateViewUI(new String[] {mic.who}, MESSAGE_TYPE_CONTROL_MIC);
							mChronometer.setBase(SystemClock.elapsedRealtime());
							mChronometer.setVisibility(View.VISIBLE);
							mChronometer.start();
						} else if (ipmsg instanceof InterphoneReleaseMicMsg){
							InterphoneReleaseMicMsg mic = (InterphoneReleaseMicMsg)ipmsg;
							UpdateViewUI(new String[] {mic.who}, MESSAGE_TYPE_RELEASE_MIC);
							mChronometer.stop();
							mChronometer.setVisibility(View.GONE);
							
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case VoiceHelper.WHAT_ON_REQUEST_MIC_CONTROL:
				if(isDownEvent) {
					VoiceHelper.getInstance().getDevice().controlMic(confNo);
					mNoticeTips.setText(R.string.top_tips_connecting_wait);
				}
				
				break;
			case VoiceHelper.WHAT_ON_PLAY_MUSIC:
				try {
					CCPUtil.playNotifycationMusic(getApplicationContext(), "inter_phone_pressed.mp3");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case VoiceHelper.WHAT_ON_STOP_MUSIC:
				try {
					mChronometer.stop();
					mChronometer.setVisibility(View.GONE);
					CCPUtil.playNotifycationMusic(getApplicationContext(), "inter_phone_up.mp3");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	// According to handle and type to update the UI callback interface.
	void UpdateViewUI(String[] whos , int type) {
		
		if(mJoinMembers != null ) {
			for(int i = 0; i < whos.length; i++) {
				StringBuilder text = new StringBuilder(whos[i]);
				InterphoneMember remove = null;
				int index = -1 ;
				for (InterphoneMember interphoneMember :mJoinMembers) {
					if(interphoneMember.voipId.equals(whos[i])) {
						index = mJoinMembers.indexOf(interphoneMember);
						if(mJoinMembers.indexOf(interphoneMember) >= 0 ) {
							remove = mJoinMembers.remove(index);
						}
						
						break;
					}
				}
				if(remove != null ) {
					if (type == MESSAGE_TYPE_JOIN) {
						remove.online = TYPE_ONLINE ;
						text.append(getString(R.string.str_join_inter_phone_success));
						//increase();
					} else if (type == MESSAGE_TYPE_EXIT) {
						remove.online = TYPE_UNONLINE ;
						text.append(getString(R.string.str_quit_inter_phone));
						//decrease();
					} else if (type == MESSAGE_TYPE_CONTROL_MIC){
						remove.mic = TYPE_MIC_CONTROLER ;
						text.append(getString(R.string.str_speaking));
						for(InterphoneMember member : mJoinMembers) {
							member.mic = TYPE_MIC_UNCONTROL ;
						}
					}else if(type == MESSAGE_TYPE_RELEASE_MIC) {
						text = new StringBuilder(getString(R.string.str_can_control_mic));
						remove.mic = TYPE_MIC_UNCONTROL ;
					} else {
						text = new StringBuilder(getString(R.string.str_can_control_mic));
					}
					
					mJoinMembers.add(index ,remove);
					mNoticeTips.setText(text.toString());
				}
				
			}
			if(mPersonCount != null && mJoinMembers != null  ) {
				mPersonCount.setText(onLineNum + "/" + mJoinMembers.size());
			}
		}
		if(mIPoneMemAdapter != null ) {
			mIPoneMemAdapter.notifyDataSetChanged();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mJoinMembers != null ) {
			mJoinMembers.clear();
			mJoinMembers = null ;
		}
		if(mHandler != null ) {
			mHandler = null;
		}
		
	}
	
	// talkback member online / total number of Statistics
	synchronized void increase(int inc) {
		onLineNum =  onLineNum + inc;
	}

	synchronized void decrease(int dec) {
		if(onLineNum - dec >= 0) {
			onLineNum = onLineNum - dec;
		} else {
			onLineNum = 0 ;
		}
	}
	
}
