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
package com.voice.demo.chatroom;

import java.util.ArrayList;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.chatroom.Chatroom;
import com.hisun.phone.core.voice.model.chatroom.ChatroomDismissMsg;
import com.hisun.phone.core.voice.model.chatroom.ChatroomExitMsg;
import com.hisun.phone.core.voice.model.chatroom.ChatroomJoinMsg;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMember;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMsg;
import com.hisun.phone.core.voice.model.chatroom.ChatroomRemoveMemberMsg;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.group.GroupBaseActivity;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.VoiceHelper;

/**
 * Voice ChatRoom
 * 1¡¢Display in the group chat member list.
 * 2¡¢Manual control is mute (whether the person can hear the sound).
 * @author Zhanjichun
 * @version v3.3
 */
public class ChatRoomActivity extends GroupBaseActivity implements View.OnClickListener{

	private static final int MODE_CHATROOM_INVITATION = 0x0;// Invitation model
	private static final int MODE_CHATROOM_INITIATED_INTERCOM = 0x1; // Creator pattern model
	
	private static int[] STATUS_ICON = new int[] {
		R.drawable.animation_box01
		,R.drawable.animation_box02
		,R.drawable.animation_box03
		,R.drawable.animation_box04
	};
	
	private static int[] CHATROOM_PULL = new int[] {
		R.string.pull_invited_phone_member
	    ,R.string.pull_manager_member
	    ,R.string.pull_dissolution_room	
		
	};
	
	private ArrayList<ChatroomMember> mCRoomMembers;
	
	
	private LinearLayout mChatMmber;
	private TextView mNoticeTips;
	private TextView mPersonCount;
	private ImageButton mChatRoomMike;
	
	private LinearLayout mCRoomStatusL;
	private LinearLayout mCRoomStatusR;
	private LinearLayout mCRoomCenterIcn;
	
	private XQuickActionBar xQuickActionBar;
	
	private int modeType ;
	// Whether the mute
	private boolean isMikeEnable = false;
	// Join Success
	private boolean isJion = false;
	
	private String mCurrentRoomNum;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//add by able for soft keyboard show
        
		setContentView(R.layout.layout_chatroom_activity);

		initResourceRefs();
		
		if(VoiceHelper.getInstance().getDevice() == null) {
			CCPUtil.registDialog(this) ;
			//finish();
		} else {
			VoiceHelper.getInstance().setHandler(mChatRoomHandler);
			initialize(savedInstanceState);
			
		}
		registerReceiver(new String[]{VoiceHelper.INTENT_CHAT_ROOM_DISMISS});
		isMikeEnable = VoiceHelper.getInstance().getDevice().getMuteStatus();
		new Thread(mikeAnimRunnable).start();
	}
	
	@Override
	protected void onResume() {
		if(VoiceHelper.getInstance() != null && mChatRoomHandler != null ) {
			VoiceHelper.getInstance().setHandler(mChatRoomHandler);
			lockScreen();
		} else {
			finish();
		}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseLockScreen();
	}

	private void initialize(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String roomName = null ;
		String creator = null;
		if(intent.hasExtra(ChatRoomName.CHATROOM_NAME)) {
			modeType = MODE_CHATROOM_INITIATED_INTERCOM;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				roomName = extras.getString(ChatRoomName.CHATROOM_NAME); 
				if(TextUtils.isEmpty(roomName)) {
					finish();
				} else {
					creator = extras.getString(ChatRoomName.CHATROOM_CREATOR); 
					if (CCPConfig.VoIP_ID.equals(creator)) {
						handleTitleDisplay(getString(R.string.btn_title_back)
								, roomName
								, getString(R.string.app_title_right_button_pull_down));
					} else {
						handleTitleDisplay(getString(R.string.btn_title_back)
								, roomName
								, null);
					}
				}
				
			}
		}
		
		if(intent.hasExtra(Device.CONFNO)) {
			// To invite voice group chat
			modeType = MODE_CHATROOM_INVITATION;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mCurrentRoomNum = extras.getString(Device.CONFNO); 
				if(TextUtils.isEmpty(mCurrentRoomNum)) {
					finish();
				}
			}
			
		}
		mNoticeTips.setText(R.string.top_tips_connecting_wait);
		
		// Launched a group chat room request, waiting for SDK to return.
		if(modeType == MODE_CHATROOM_INITIATED_INTERCOM ) {
			VoiceHelper.getInstance().getDevice().startChatroom(CCPConfig.App_ID, roomName, 8, null, null);
		} else if (modeType == MODE_CHATROOM_INVITATION) {
			
			// Initiate a join group chat room request, waiting for SDK to return.
			VoiceHelper.getInstance().getDevice().joinChatroom(mCurrentRoomNum);
		}
		
	}

	private void initResourceRefs() {
		//mChatMmber = (ListView) findViewById(R.id.chatroom_member_list);
		mChatMmber = (LinearLayout) findViewById(R.id.member_list);
		mNoticeTips = (TextView) findViewById(R.id.chatroom_notice_tips);
		mChatRoomMike = (ImageButton) findViewById(R.id.chatroom_mike);
		mPersonCount = (TextView) findViewById(R.id.count_tv);
		
		mChatRoomMike.setOnClickListener(this);
		
		mCRoomStatusL = (LinearLayout) findViewById(R.id.chatroom_l_status);
		mCRoomStatusR = (LinearLayout) findViewById(R.id.chatroom_r_status);
		mCRoomCenterIcn = (LinearLayout) findViewById(R.id.chatroom_center_status);
		
		 
		initBottomStatus(0);
	}
	
	@Override
	protected void handleTitleAction(int direction) {
		if(direction == TITLE_RIGHT_ACTION) {
			
			if(xQuickActionBar==null){
				xQuickActionBar = new XQuickActionBar(findViewById(R.id.voice_right_btn));
				xQuickActionBar.setOnPopClickListener(popListener);
			}
			xQuickActionBar.setArrays(CHATROOM_PULL);
			xQuickActionBar.show();
		} else {
			//super.handleTitleAction(direction);
			if(getTitleRightButton().getVisibility() == View.VISIBLE) {
				showAlertTipsDialog(DIALOG_REQUEST_KEY_EXIT_CHATROOM, getString(R.string.dialog_chatroom_title)
						, getString(R.string.dialog_chatroom_message)
						, getString(R.string.dialog_p_dissolution)
						, getString(R.string.dialog_n_exit));
			} else {
				exitOrDismissChatroom(false);
			}
		}
	}
	
	
	public void exitOrDismissChatroom(boolean exit) {
		if(!exit) {
			VoiceHelper.getInstance().getDevice().exitChatroom();
			finish();
		} else {
			showConnectionProgress(getString(R.string.str_dialog_message_default));
			VoiceHelper.getInstance().setHandler(null);
			VoiceHelper.getInstance().getDevice().dismissChatroom(CCPConfig.App_ID, mCurrentRoomNum);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			
			handleTitleAction(TITLE_LEFT_ACTION);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.chatroom_mike:
			try {
				mChatRoomMike.setEnabled(false);
				VoiceHelper.getInstance().getDevice().setMute(isMikeEnable);
				isMikeEnable = VoiceHelper.getInstance().getDevice().getMuteStatus();
				if(isMikeEnable) {
					initBottomStatus(0);
				} else {
					synchronized (mChatRoomHandler) {
						new Thread(mikeAnimRunnable).start();
						mChatRoomHandler.notify();
						
					}
					
				}
				mChatRoomMike.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		isJion = false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(VoiceHelper.getInstance().getDevice() != null ) {
			//getDevice().exitChatroom();
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(false);
		}
		if (isMikeEnable && VoiceHelper.getInstance().getDevice() != null) {
			VoiceHelper.getInstance().getDevice().setMute(isMikeEnable);
		}
		
		if(mChatRoomHandler != null){
			mChatRoomHandler = null;
		}
		
		if(mCRoomMembers != null ) {
			mCRoomMembers.clear();
			mCRoomMembers = null ;
		}
		
		isMikeEnable = false;
		isJion = false;
		mCurrentRoomNum  = null ;
	}
	
	private android.os.Handler mChatRoomHandler = new android.os.Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = 0;
			ChatroomMsg crmsg = null ;
			ArrayList<ChatroomMember> members = null;
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(Device.REASON);
				if (b.getString(Device.CONFNO) != null)
					mCurrentRoomNum = b.getString(Device.CONFNO);
				
				if (b.getSerializable(Device.CHATROOM_MSG) != null)
					crmsg = (ChatroomMsg) b.getSerializable(Device.CHATROOM_MSG);
				
				if (b.getSerializable(Device.CHATROOM_MEMBERS) != null)
					members = (ArrayList<ChatroomMember>) b.getSerializable(Device.CHATROOM_MEMBERS);
			}

			switch (msg.what) {
			case VoiceHelper.WHAT_ON_CHATROOM:
				if(reason == 0){
					synchronized (mChatRoomHandler) {
						this.notifyAll();
					}
					// join chatroom success ..
					VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(true);
					VoiceHelper.getInstance().getDevice().queryMembersWithChatroom(mCurrentRoomNum);
					mNoticeTips.setText(CCPConfig.VoIP_ID + getString(R.string.str_join_chatroom_success));
					
					isJion = true;
					new Thread(CenterAnimRunnable).start();
					
					if(modeType == ChatRoomActivity.MODE_CHATROOM_INITIATED_INTERCOM) {
						Chatroom chatRoomInfo = new Chatroom();
						chatRoomInfo.setCreator(CCPConfig.VoIP_ID);
						chatRoomInfo.setJoined("1");
						chatRoomInfo.setRoomNo(mCurrentRoomNum);
						chatRoomInfo.setRoomName(getActivityTitle());
						chatRoomInfo.setSquare("8");
						chatRoomInfo.setValidate("0");
						Intent intent = new Intent(VoiceHelper.INTENT_CHAT_ROOM_RECIVE);
						intent.putExtra("ChatRoomInfo",chatRoomInfo);
						sendBroadcast(intent);
					}
				}else{ 
					//  failed ..
					isJion = false;
					Log4Util.d(VoiceHelper.DEMO_TAG , "[InterPhoneRoomActivity] handleMessage: Sorry ,invite member inter phone failed ...");
					VoiceHelper.getInstance().getDevice().exitChatroom();
					BaseApplication.getInstance().showToast(getString(R.string.str_join_chatroom_failed, reason));
					finish();	
				}
				break;
			case VoiceHelper.WHAT_ON_CHATROOM_INVITE :
				
				if(reason == 0){
					// invite chatroom success ..
					Toast.makeText(ChatRoomActivity.this, R.string.toast_invite_join_room_success, Toast.LENGTH_SHORT).show();
				}else{ 
					//  failed ..
					Toast.makeText(ChatRoomActivity.this, getString(R.string.toast_invite_join_room_failed , reason), Toast.LENGTH_SHORT).show();
				}
				
				Message obtainMessage = mChatRoomHandler.obtainMessage(VoiceHelper.WHAT_ON_CHATROOMING);
				mChatRoomHandler.sendMessageDelayed(obtainMessage, 2000);
				
				break;
			case VoiceHelper.WHAT_ON_CHATROOM_MEMBERS:
				if(members!=null){
					if(mCRoomMembers == null ) {
						mCRoomMembers = new ArrayList<ChatroomMember>();
					}
					mCRoomMembers.clear();
					for (ChatroomMember i :members) {
						if(i.getNumber().equals(CCPConfig.VoIP_ID)) {
							mCRoomMembers.add(0, i);
						} else {
							mCRoomMembers.add(i);
						}
					}
					mPersonCount.setText(members.size() + "");
					initChatRoomListView(mCRoomMembers);
				} 
				break;
			case VoiceHelper.WHAT_ON_CHATROOM_SIP_MESSAGE:
				try {
					if(crmsg != null){
						if (crmsg instanceof ChatroomJoinMsg) {
							ChatroomJoinMsg crj = (ChatroomJoinMsg)crmsg;
							if(mCRoomMembers != null ) {
								StringBuilder builder = new StringBuilder();
								String[] whos = crj.getWhos();
								for(int i = 0 ; i < whos.length ; i++) {
									builder.append(whos[i]).append(",");
									
									// 
									boolean isHas = false;
									for(ChatroomMember cm :mCRoomMembers) {
										if(cm.getNumber().equals(whos[i])) {
											isHas = true;
											break;
										}
									}
									if(!isHas) {
										mCRoomMembers.add(new ChatroomMember(whos[i], "0"));
									}
								}
								
								String joinV = builder.toString();
								if(joinV != null && joinV.length() > 0) {
									joinV = joinV.substring(0, joinV.length() - 1);
								}
								mNoticeTips.setText(getString(R.string.str_chatroom_join, joinV));
								initChatRoomListView(mCRoomMembers);
							}
						} else if (crmsg instanceof ChatroomExitMsg) {
							ChatroomExitMsg cre = (ChatroomExitMsg)crmsg;
							if(mCRoomMembers != null ) {
								StringBuilder builder = new StringBuilder();
								ArrayList<ChatroomMember> mExitMember = new ArrayList<ChatroomMember>();
								String[] whos = cre.getWhos();
								for(int i = 0 ; i < whos.length ; i++) {
									builder.append(whos[i]).append(",");
									for (ChatroomMember eMember : mCRoomMembers) {
										if(eMember.getNumber().equals(whos[i])) {
											mExitMember.add(eMember);
											break;
										}
									}
								}
								
								mCRoomMembers.removeAll(mExitMember);
								initChatRoomListView(mCRoomMembers);
								String joinV = builder.toString();
								if(joinV != null && joinV.length() > 0) {
									joinV = joinV.substring(0, joinV.length() - 1);
								}
								mNoticeTips.setText(getString(R.string.str_chatroom_exit, joinV));
							}
						} else if (crmsg instanceof ChatroomDismissMsg) {
							ChatroomDismissMsg dismissMsg = (ChatroomDismissMsg) crmsg;
							if(dismissMsg.getRoomNo().equals(mCurrentRoomNum)) {
								showAlertTipsDialog(DIALOG_SHOW_KEY_DISSMISS_CHATROOM
										, getString(R.string.dialog_title_be_dissmiss_chatroom)
										, getString(R.string.dialog_message_be_dissmiss_chatroom)
										, getString(R.string.dialog_btn)
										, null);
							}
						} else if (crmsg instanceof ChatroomRemoveMemberMsg) {
							ChatroomRemoveMemberMsg crRemoveMemberMsg = (ChatroomRemoveMemberMsg) crmsg;
							if(CCPConfig.VoIP_ID.equals(crRemoveMemberMsg.getWho()) 
									&& mCurrentRoomNum.equals(crRemoveMemberMsg.getRoomNo())){
								// if sel..
								showAlertTipsDialog(DIALOG_SHOW_KEY_REMOVE_CHATROOM
										, getString(R.string.dialog_title_be_kick_chatroom)
										, getString(R.string.dialog_message_be_kick_chatroom)
										, getString(R.string.dialog_btn)
										, null);
							} else {
								mNoticeTips.setText(getString(R.string.str_chatroom_kick, crRemoveMemberMsg.getWho()));
								VoiceHelper.getInstance().getDevice().queryMembersWithChatroom(mCurrentRoomNum);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			case VoiceHelper.WHAT_ON_MIKE_ANIM:
				
				int abs = randomNum(6);  
				initBottomStatus(abs);
				break;
				
			case VoiceHelper.WHAT_ON_CNETER_ANIM:
				initCenterStatus(15);
				break;
			case VoiceHelper.WHAT_ON_CHATROOMING:
				
				mNoticeTips.setText(R.string.top_tips_chatroom_ing);
				break;
			default:
				break;
			}
		}


	};
	
	private int randomNum(int num) {
		Random rand = new Random();  
		return Math.abs(rand.nextInt() % num);
	}
	
	private void initChatRoomListView(ArrayList<ChatroomMember> mCRoomMembers) {
		if(mCRoomMembers == null ) {
			return ;
		}
		mChatMmber.removeAllViews();
		View view = null ;
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0 ; i < mCRoomMembers.size() ; i ++) {
			if(isOddCorrect(i)) {
				builder.append(mCRoomMembers.get(i).getNumber());
				list.add(builder.toString());
				builder = new StringBuilder();
			} else {
				if(i == mCRoomMembers.size() - 1) {
					builder.append(mCRoomMembers.get(i).getNumber());
					list.add(builder.toString());
					builder = new StringBuilder();
				} else {
					builder.append(mCRoomMembers.get(i).getNumber()).append(",");
				}
			}
		}
		for(String members : list) {
			view = getLayoutInflater().inflate(R.layout.list_chatroom_item, null);
			ImageView cRoomStatusL = (ImageView) view.findViewById(R.id.chatroom_join_statu_l);
			TextView cRoomNameL = (TextView) view.findViewById(R.id.chatroom_name_l);
			ImageView cRoomStatusR = (ImageView) view.findViewById(R.id.chatroom_join_statu_r);
			TextView cRoomNameR = (TextView) view.findViewById(R.id.chatroom_name_r);
			String[] split = members.split(",");
			if(split.length > 0 && !TextUtils.isEmpty(split[0])) {
				if(CCPConfig.VoIP_ID.equals(split[0])) {
					cRoomStatusL.setImageResource(R.drawable.touxiang);
				} else {
					cRoomStatusL.setImageResource(R.drawable.status_uncreateor);
				}
				cRoomNameL.setText(split[0]);
			} 
			if (split.length > 1 && !TextUtils.isEmpty(split[1])){
				if(CCPConfig.VoIP_ID.equals(split[1])) {
					cRoomStatusR.setImageResource(R.drawable.touxiang);
				} else {
					cRoomStatusR.setImageResource(R.drawable.status_uncreateor);
				}
				cRoomNameR.setText(split[1]);
				
			}
			mChatMmber.addView(view);
		}
		
		mPersonCount.setText(mCRoomMembers.size() + "");
		
	}
	
	boolean isOddCorrect(int i){  
        return i%2!=0;  
        
    } 
	
	@Override
	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {
		super.handleEditDialogOkEvent(requestKey, editText, checked);
		
		if(DIALOG_SHOW_KEY_INVITE == requestKey) {
			String mPhoneNumber = editText;
			if(mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber)){
				// invite this phone call ...
				VoiceHelper.getInstance().getDevice().inviteMembersJoinChatroom(new String[]{mPhoneNumber}, mCurrentRoomNum, CCPConfig.App_ID);
				mNoticeTips.setText(getString(R.string.str_invite_join_room , CCPConfig.VoIP_ID , mPhoneNumber));
			}
		}
	}
	
	
	@Override
	protected void handleDialogOkEvent(int requestKey) {
		super.handleDialogOkEvent(requestKey);
		 if (DIALOG_SHOW_KEY_DISSMISS_CHATROOM == requestKey 
				 || DIALOG_REQUEST_KEY_EXIT_CHATROOM == requestKey) {
				if(getTitleRightButton().getVisibility() == View.VISIBLE) {
					//getDevice().dismissChatroom(CCPConfig.App_ID, mCurrentRoomNum);
					exitOrDismissChatroom(true);
				} else {
					
					// Here is the receipt dissolution news, not so directly off the Page Creator
					finish();
					Intent intent = new Intent(VoiceHelper.INTENT_CHAT_ROOM_DISMISS);
					intent.putExtra("roomNo", mCurrentRoomNum);
					sendBroadcast(intent);
				}
		} else if (DIALOG_SHOW_KEY_REMOVE_CHATROOM == requestKey ) {
			// Here is the receipt dissolution news, not so directly off the Page Creator
			finish();
		}
		
	}
	
	@Override
	protected void handleDialogCancelEvent(int requestKey) {
		super.handleDialogCancelEvent(requestKey);
		if(requestKey == DIALOG_REQUEST_KEY_EXIT_CHATROOM) {
			exitOrDismissChatroom(false);
		}
	}
	
	
	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		closeConnectionProgress();
		if(intent.getAction().equals(VoiceHelper.INTENT_CHAT_ROOM_DISMISS)) {
			if(intent.hasExtra("roomNo")) {
				String roomNo = intent.getStringExtra("roomNo");
				if(!TextUtils.isEmpty(roomNo) && roomNo.equals(mCurrentRoomNum)) {
					finish();
				}
			}
		}
	}
	
	synchronized void  initBottomStatus(int num){//4
		mCRoomStatusL.removeAllViews();
		mCRoomStatusR.removeAllViews();
		for (int i = 0; i < 6; i++) {
			ImageView imageViewl_i = new ImageView(this);
			ImageView imageViewR_i = new ImageView(this);
			if(i > (6 - num - 1)) {//1
				imageViewl_i.setImageResource(R.drawable.chatroom_speaker);
			} else {
				imageViewl_i.setImageResource(R.drawable.chatroom_unspeaker);
				
			}
			if(i >= num) {//4
				imageViewR_i.setImageResource(R.drawable.chatroom_unspeaker);
			} else {
				imageViewR_i.setImageResource(R.drawable.chatroom_speaker);
			}
			mCRoomStatusL.addView(imageViewl_i ,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1.0f));
			mCRoomStatusR.addView(imageViewR_i, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1.0f));
		}
	}
	
	synchronized void  initCenterStatus(int num){//4
		mCRoomCenterIcn.removeAllViews();
		for (int i = 0; i < num; i++) {
			ImageView imageView = new ImageView(this);
			if(STATUS_ICON != null ) {
				imageView.setImageResource(STATUS_ICON[randomNum(4)]);
				mCRoomCenterIcn.addView(imageView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT , 1.0f));
			}
		}
	}
	
	Runnable mikeAnimRunnable = new Runnable() {
		
		@Override
		public void run() {
			while(!isMikeEnable) {
				if(isMikeEnable) {
					synchronized (mChatRoomHandler) {
						try {
							mChatRoomHandler.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if(mChatRoomHandler != null ) {
					mChatRoomHandler.sendEmptyMessage(VoiceHelper.WHAT_ON_MIKE_ANIM);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	
	Runnable CenterAnimRunnable = new Runnable() {
		
		@Override
		public void run() {
			while(isJion) {
				if(mChatRoomHandler != null ) {
					mChatRoomHandler.sendEmptyMessage(VoiceHelper.WHAT_ON_CNETER_ANIM);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private static final int REQUEST_CODE_KICK_MEMBER = 0x1;
	private XQuickActionBar.OnPopClickListener popListener = new XQuickActionBar.OnPopClickListener() {

		@Override
		public void onPopClick(int index) {
			VoiceHelper.getInstance().setHandler(mChatRoomHandler);
			switch (index) {
			case R.string.pull_invited_phone_member:
				
				 showEditTextDialog(DIALOG_SHOW_KEY_INVITE, getString(R.string.dialog_title_invite), getString(R.string.dialog_title_summary));
				break;
			case R.string.pull_manager_member:
				Intent intent = new Intent(ChatRoomActivity.this, ChatroomMemberManagerActivity.class);
				intent.putExtra(Device.CONFNO, mCurrentRoomNum);
				startActivityForResult(intent, REQUEST_CODE_KICK_MEMBER);
				break;
			case R.string.pull_dissolution_room:
				
				showAlertTipsDialog(DIALOG_SHOW_KEY_DISSMISS_CHATROOM
						, getString(R.string.dialog_title_dissmiss_chatroom)
						, getString(R.string.dialog_message_dissmiss_chatroom)
						, getString(R.string.dailog_button_dissmiss_chatroom)
						, getString(R.string.dialog_cancle_btn));
				break;
			}
			xQuickActionBar.dismissBar();
		}
	};

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// If there's no data (because the user didn't select a number and
		// just hit BACK, for example), there's nothing to do.
		if (requestCode != REQUEST_CODE_KICK_MEMBER) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {
			
			return;
		}
		
		switch (requestCode) {
		case REQUEST_CODE_KICK_MEMBER:
			if(data.hasExtra("isKicked")) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					boolean isKicked = extras.getBoolean("isKicked");
					
					if(isKicked) {
						VoiceHelper.getInstance().setHandler(mChatRoomHandler);
						VoiceHelper.getInstance().getDevice().queryMembersWithChatroom(mCurrentRoomNum);
					}
				}
			}
			
			break;
		default:
			break;
		}
	}
	
}
