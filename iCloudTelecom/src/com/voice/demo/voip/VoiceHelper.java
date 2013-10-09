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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.hisun.phone.core.voice.CCPCall;
import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.Device.Codec;
import com.hisun.phone.core.voice.DeviceListener;
import com.hisun.phone.core.voice.model.DownloadInfo;
import com.hisun.phone.core.voice.model.chatroom.Chatroom;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMember;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMsg;
import com.hisun.phone.core.voice.model.im.IMAttachedMsg;
import com.hisun.phone.core.voice.model.im.IMDismissGroupMsg;
import com.hisun.phone.core.voice.model.im.IMInviterMsg;
import com.hisun.phone.core.voice.model.im.IMJoinGroupMsg;
import com.hisun.phone.core.voice.model.im.IMProposerMsg;
import com.hisun.phone.core.voice.model.im.IMQuitGroupMsg;
import com.hisun.phone.core.voice.model.im.IMRemoveMemeberMsg;
import com.hisun.phone.core.voice.model.im.IMReplyJoinGroupMsg;
import com.hisun.phone.core.voice.model.im.IMTextMsg;
import com.hisun.phone.core.voice.model.im.InstanceMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneInviteMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneMember;
import com.hisun.phone.core.voice.model.interphone.InterphoneMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneOverMsg;
import com.hisun.phone.core.voice.model.setup.UserAgentConfig;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.group.GroupBaseActivity;
import com.voice.demo.group.model.IMChatMessageDetail;
import com.voice.demo.group.model.IMSystemMessage;
import com.voice.demo.setting.SettingsActivity;
import com.voice.demo.sqlite.CCPSqliteManager;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;

/**
 * VOIP Helper for Activity, it already has been did something important jobs
 * that activity just show state of ui by handler.
 * 
 * Before running the demo, you should be become a developer by CCP web site so that 
 * you can get the main account and token, otherwise also see test info.
 * 
 * @version 1.0.0
 */
public class VoiceHelper implements CCPCall.InitListener, DeviceListener {

	public static final String DEMO_TAG = "CCP_Demo";
	// our suggestion this context should be ApplicationContext
	private Context context;

	// invoked after created it
	private Device device;
	
	//regist handle 
	private Handler helperHandler;
	
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	
	public static final int SDK_NOTIFICATION		    = 99;
	public static final int ICON_LEVEL_ORANGE		    = 0;
	public static final int ICON_LEVEL_GREEN		    = 1;
	public static final int ICON_LEVEL_RED			    = 2;
	public static final int ICON_LEVEL_BLACK			= 3;
	
	private static VoiceHelper sInstance;

	public static VoiceHelper getInstance() {
		return sInstance;
	}
	
	public static void init(Context context ,Handler handler) {
        sInstance = new VoiceHelper(context , handler);
    }
    
	/**
	 * Constructs a new {@code VoiceHelper} instance.
	 * 
	 * @param context
	 * @see #Context
	 */
	public VoiceHelper(Context context ,Handler handler) {
		this.context = context;
		this.helperHandler = handler;
		this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Log4Util.init(true);
		CCPCall.init(context, this);
		Log4Util.d(DEMO_TAG , "[VoiceHelper] CCPCallService init");
	}

	/**
	 * Callback this method when SDK init success.
	 * 
	 * Please note: you must write info that those remark start.
	 * 
	 * SDK init just once, but device can create more.
	 * 
	 * @see #onInitialized()
	 */
	@Override
	public void onInitialized() {
		try {
			createDevice();
		} catch (Exception e) {
			e.printStackTrace();
			//throw new RuntimeException(e);
			onError(e);
		}
	}
	
	private void createDevice() throws Exception {
		// 封装参数
		Map<String, String> params = new HashMap<String, String>();
		// * REST服务器地址
		params.put(UserAgentConfig.KEY_IP, CCPConfig.REST_SERVER_ADDRESS);
		// * REST服务器端口
		params.put(UserAgentConfig.KEY_PORT, CCPConfig.REST_SERVER_PORT);
		// * VOIP账号 , 可以填入CCP网站Demo管理中的测试VOIP账号信息
		params.put(UserAgentConfig.KEY_SID, CCPConfig.VoIP_ID);
		// * VOIP账号密码, 可以填入CCP网站Demo管理中的测试VOIP账号密码
		params.put(UserAgentConfig.KEY_PWD, CCPConfig.VoIP_PWD);
		// * 子账号, 可以填入CCP网站Demo管理中的测试子账号信息
		params.put(UserAgentConfig.KEY_SUBID, CCPConfig.Sub_Account);
		// * 子账号密码, 可以填入CCP网站Demo管理中的测试子账号密码
		params.put(UserAgentConfig.KEY_SUBPWD, CCPConfig.Sub_Token);
		// User-Agent
		params.put(UserAgentConfig.KEY_UA, BaseApplication.getInstance().getUser_Agent());

		// 创建Device
		device = CCPCall.createDevice(this /* DeviceListener */, params);

		// 设置当呼入请求到达时, 唤起的界面
		Intent intent = new Intent(context, CallInActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		device.setIncomingIntent(pendingIntent);
		device.setCodecEnabled(Codec.Codec_VP8, false);
		
		Log4Util.d(DEMO_TAG, "[onInitialized] sdk init success. finsh...");
	}
	
	/**
	 * Callback this method when sdk init errors.
	 * 
	 * @param exception
	 *            SDK init execption
	 */
	@Override
	public void onError(Exception exception) {
		CCPCall.shutdown();
		Log4Util.d(DEMO_TAG , "[onError] " + "SDK init error , " + exception.getMessage());
		Message msg = Message.obtain(helperHandler);
		msg.what = VoiceLoginActivity.WHAT_INIT_ERROR;
		msg.obj = "[onError] " + "SDK初始化错误, " + exception.getMessage();
		msg.sendToTarget();
	}

	/**
	 * handler 转换消息id
	 */
	public static final int WHAT_ON_CONNECT = 0x2000;
	public static final int WHAT_ON_DISCONNECT = 0x2001;
	public static final int WHAT_ON_CALL_ALERTING = 0x2002;
	public static final int WHAT_ON_CALL_ANSWERED = 0x2003;
	public static final int WHAT_ON_CALL_PAUSED = 0x2004;
	public static final int WHAT_ON_CALL_PAUSED_REMOTE = 0x2005;
	public static final int WHAT_ON_CALL_RELEASED = 0x2006;
	public static final int WHAT_ON_CALL_PROCEEDING = 0x2007;
	public static final int WHAT_ON_CALL_TRANSFERED = 0x2008;
	public static final int WHAT_ON_CALL_MAKECALL_FAILED = 0x2009;
	public static final int WHAT_ON_CALL_BACKING = 0x201B;
	
	//2013.3.11
	public static final int WHAT_ON_NEW_VOICE = 0x201C;
	public static final int WHAT_ON_AMPLITUDE = 0x201D;
	public static final int WHAT_ON_RECODE_TIMEOUT = 0x202A;
	public static final int WHAT_ON_UPLOAD_VOICE_RES = 0x202B;
	public static final int WHAT_ON_PLAY_VOICE_FINSHING = 0x202C;
	
	public static final int WHAT_ON_INTERPHONE = 0x203A;
	public static final int WHAT_ON_CONTROL_MIC = 0x203B;
	public static final int WHAT_ON_RELEASE_MIC = 0x203C;
	public static final int WHAT_ON_INTERPHONE_MEMBERS = 0x203D;
	public static final int WHAT_ON_INTERPHONE_SIP_MESSAGE = 0x203E;
	public static final int WHAT_ON_DIMISS_DIALOG = 0x204A;;
	
	public static final int WHAT_ON_REQUEST_MIC_CONTROL = 0x204C;
	public static final int WHAT_ON_RELESE_MIC_CONTROL = 0x204D;
	public static final int WHAT_ON_PLAY_MUSIC = 0x204E;
	public static final int WHAT_ON_STOP_MUSIC = 0x204F;

	
	public static final int WHAT_ON_VERIFY_CODE_SUCCESS = 0x205A;
	public static final int WHAT_ON_VERIFY_CODE_FAILED = 0x205B;
	
	// ChatRoom
	public static final int WHAT_ON_CHATROOM_SIP_MESSAGE = 0x205C;
	public static final int WHAT_ON_CHATROOM_MEMBERS = 0x205D;
	public static final int WHAT_ON_CHATROOM_LIST = 0x205E;
	public static final int WHAT_ON_CHATROOM = 0x206A;
	public static final int WHAT_ON_CHATROOM_INVITE = 0x206B;
	public static final int WHAT_ON_MIKE_ANIM = 0x206C;
	public static final int WHAT_ON_CNETER_ANIM = 0x206D;
	public static final int WHAT_ON_VERIFY_CODE = 0x206E;
	public static final int WHAT_ON_CHATROOMING = 0x207A;
	public static final int WHAT_ON_CHATROOM_KICKMEMBER = 0x207B;
	
	
	// IM
	public static final int WHAT_ON_SEND_MEDIAMSG_RES = 0x208A;
	public static final int WHAT_ON_NEW_MEDIAMSG = 0x208B;

	/**
	 * handler for update activity
	 */
	private Handler handler;

	/**
	 * set handler.
	 * 
	 * @param handler
	 *            activity handler
	 */
	public void setHandler(final Handler handler) {
		this.handler = handler;
	}

	/**
	 * get the device.
	 * 
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	long t = 0;

	/**
	 * send object to activity by handler.
	 * 
	 * @param what
	 *            message id of handler
	 * @param obj
	 *            message of handler
	 */
	private void sendTarget(int what, Object obj) {
		t = System.currentTimeMillis();
		// for kinds of mobile phones
		while (handler == null && (System.currentTimeMillis() - t < 3200)) {
			Log4Util.d(DEMO_TAG , "[VoiceHelper] handler is null, activity maybe destory, wait...");
			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
			}
		}

		if (handler == null) {
			Log4Util.d(DEMO_TAG , "[VoiceHelper] handler is null, need adapter it.");
			return;
		}

		Message msg = Message.obtain(handler);
		msg.what = what;
		msg.obj = obj;
		msg.sendToTarget();
	}

	/***********************************************************************************
	 *                                                                                 *
	 *            Following are DeviceListener Callback Methods                        *
	 *                                                                                 *
	 ************************************************************************************/

	/**
	 * Callback this method when register successful, developer can show
	 * notification to user.
	 */
	@Override
	public void onConnected() {
		this.context.sendBroadcast(new Intent(INTENT_CONNECT_CCP));
		Message msg = Message.obtain(helperHandler);
		msg.what = WHAT_ON_CONNECT;
		msg.sendToTarget();
		Log4Util.d(DEMO_TAG , "[VoiceHelper - onConnected]Connected on the cloud communication platform success..");
	}

	/**
	 * Callback this method when register failed, developer can show
	 * hint to user.
	 * 
	 * @param reason
	 *            register failed reason
	 */
	@Override
	public void onDisconnect(Reason reason) {
		if(reason == Reason.KICKEDOFF) {
			Log4Util.d(DEMO_TAG , "Login account in other places.");
			this.context.sendBroadcast(new Intent(INTENT_KICKEDOFF));
		} else {
			this.context.sendBroadcast(new Intent(INTENT_DISCONNECT_CCP));
			Message msg = Message.obtain(helperHandler);
			msg.what = WHAT_ON_DISCONNECT;
			msg.obj = reason;
			msg.sendToTarget();
			Log4Util.d(DEMO_TAG , "[VoiceHelper - onDisconnect]Can't connect the cloud communication platform" +
					", please check whether the network connection,");
		}
	}

	/**
	 * Callback this method when call arrived in remote.
	 * 
	 * @param callid
	 */
	@Override
	public void onCallAlerting(String callid) {
		sendTarget(WHAT_ON_CALL_ALERTING, callid);
	}

	/**
	 * Callback this method when remote answered.
	 * 
	 * @param callid
	 *           calling id
	 */
	@Override
	public void onCallAnswered(String callid) {
		sendTarget(WHAT_ON_CALL_ANSWERED, callid);
	}

	/**
	 * Callback this method when call arrived in soft-switch platform.
	 * 
	 * @param callid
	 *            calling id
	 */
	@Override
	public void onCallProceeding(String callid) {
		sendTarget(WHAT_ON_CALL_PROCEEDING, callid);
	}

	/**
	 * Callback this method when remote hangup call.
	 * 
	 * @param callid
	 *            calling id
	 */
	@Override
	public void onCallReleased(String callid) {
		sendTarget(WHAT_ON_CALL_RELEASED, callid);
	}

	/**
	 * Callback this method when make call failed.
	 * 
	 * @param callid
	 *            calling id
	 * @param destionation
	 *            destionation account
	 */
	@Override
	public void onCallTransfered(String callid, String destionation) {
		Bundle b = new Bundle();
		b.putString(Device.CALLID, callid);
		b.putString(Device.DESTIONATION, destionation);
		sendTarget(WHAT_ON_CALL_TRANSFERED, b);
	}

	/**
	 * Callback this method when make call failed.
	 * 
	 * @param callid
	 *            calling id
	 * @param reason
	 *            failed reason
	 */
	@Override
	public void onMakeCallFailed(String callid, Reason reason) {
		Bundle b = new Bundle();
		b.putString(Device.CALLID, callid);
		b.putSerializable(Device.REASON, reason);
		sendTarget(WHAT_ON_CALL_MAKECALL_FAILED, b);
	}


	/**
	 * Callback this method when dial-call success.
	 * 
	 * @param status
	 *            dial-call state
	 * @param self
	 *            Self phone number
	 * @param dest
	 *            Dest phone number
	 */
	@Override
	public void onCallback(CBState status, String self, String dest) {
		Bundle b = new Bundle();
		b.putSerializable(Device.CBSTATE, status);
		b.putString(Device.SELFPHONE, self);
		b.putString(Device.DESTPHONE, dest);
		sendTarget(WHAT_ON_CALL_BACKING, b);
	}

	/**
	 * Callback this method when localize pause current call.
	 * 
	 * @param callid
	 *            calling id
	 */
	@Override
	public void onCallPaused(String callid) {

	}

	/**
	 * Callback this method when Remote pause current call.
	 * 
	 * @param callid
	 *            calling id
	 */
	@Override
	public void onCallPausedByRemote(String callid) {

	}

	public void release() {
		cancelNotification();
		this.context = null;
		this.device = null;
		this.handler = null;
		this.helperHandler = null;
		this.mNotification = null;
	}
	
	/**
	 * 
	 * @param level
	 * @param topicId
	 * @param text
	 */
	public synchronized void showNotification(int level, int topicId, String text) {
		if (this.mNotification == null) {
			this.mNotification = new Notification(R.drawable.status_level, text, System.currentTimeMillis());
		}
		String topic = this.context.getResources().getString(topicId);
		this.mNotification.iconLevel = level;
		this.mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		this.mNotification.when = System.currentTimeMillis();
		PendingIntent notifyIntent = PendingIntent.getActivity(this.context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		this.mNotification.setLatestEventInfo(this.context, topic, text, notifyIntent);
		this.mNotificationManager.notify(SDK_NOTIFICATION, this.mNotification);
	}

	public void cancelNotification() {
		if (this.mNotificationManager != null) {
			this.mNotificationManager.cancel(SDK_NOTIFICATION);
		}
	}

	
	/**********************************************************************
	 *                     voice message                                  *
	 **********************************************************************/
	
	
	@Override
	public void onFinishedPlaying() {
		Log4Util.d(DEMO_TAG , "[onFinishedPlaying ] MediaPlayManager play is stop ..");
		Bundle b = new Bundle();
		sendTarget(WHAT_ON_PLAY_VOICE_FINSHING, b);
	}

	public static final String INTENT_INTER_PHONE_RECIVE = "com.voice.demo.Intent.ccpIntent.ACTION_RECIVE_INTERPHONE";
	public static final String INTENT_CHAT_ROOM_RECIVE = "com.voice.demo.Intent.ccpIntent.ACTION_RECIVE_CHATROOM";
	public static final String INTENT_CHAT_ROOM_DISMISS = "com.voice.demo.Intent.ccpIntent.ACTION_DISMISS_CHATROOM";
	public static final String INTENT_IM_RECIVE = "com.voice.demo.Intent.ccpIntent.ACTION_IM_RECIVE";
	public static final String INTENT_KICKEDOFF = "com.voice.demo.Intent.ccpIntent.ACTION_KICKEDOFF";
	
	
	public static final String INTENT_CONNECT_CCP = "com.voice.demo.Intent.ccpIntent.ACTION_CONNECT_CCP";
	public static final String INTENT_DISCONNECT_CCP = "com.voice.demo.Intent.ccpIntent.ACTION_DISCONNECT_CCP";
	

	@Override
	public void onRecordingAmplitude(double amplitude) {
		Bundle b = new Bundle();
		b.putDouble(Device.VOICE_AMPLITUDE, amplitude);
		sendTarget(WHAT_ON_AMPLITUDE, b);
		
	}

	@Override
	public void onRecordingTimeOut(long mills) {
		Bundle b = new Bundle();
		b.putLong("mills", mills);
		sendTarget(WHAT_ON_RECODE_TIMEOUT, b);
		
	}

	@Override
	public void onInterphoneState(int reason, String confNo) {
		Log4Util.d(DEMO_TAG , "[onInterphoneState ] oninter phone state  , reason  " +reason + " , and confNo " + confNo);
		if(reason==0){
			if(BaseApplication.interphoneIds.indexOf(confNo)<0){
				BaseApplication.interphoneIds.add(confNo);
				Intent intent = new Intent(INTENT_INTER_PHONE_RECIVE);
				context.sendBroadcast(intent);
			}
		}
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putString(Device.CONFNO, confNo);
		sendTarget(WHAT_ON_INTERPHONE, b);
	}

	@Override
	public void onControlMicState(int reason, String speaker) {
		Log4Util.d(DEMO_TAG , "[onControlMicState ] control mic return  , reason " +reason + " , and speaker " + speaker );
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putString(Device.SPEAKER, speaker);
		sendTarget(WHAT_ON_CONTROL_MIC, b);
	}

	@Override
	public void onReleaseMicState(int reason) {
		Log4Util.d(DEMO_TAG , "[onReleaseMicState ] on release mic return reason  .. " +reason);
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		sendTarget(WHAT_ON_RELEASE_MIC, b);
	}

	@Override
	public void onInterphoneMembers(int reason, List<InterphoneMember> member) {
		Log4Util.d(DEMO_TAG , "[onInterphoneMembers ] on inter phone members that .. " + member);
		Bundle b = new Bundle();
		b.putSerializable(Device.MEMBERS, (ArrayList<InterphoneMember>) member);
		sendTarget(WHAT_ON_INTERPHONE_MEMBERS, b);
	}

	@Override
	public void onReceiveInterphoneMsg(InterphoneMsg body) {
		Log4Util.d(DEMO_TAG , "[onReceiveInterphoneMsg ] Receive inter phone message  , id :" + body.interphoneId);
		if(body instanceof InterphoneOverMsg){
				BaseApplication.interphoneIds.remove(body.interphoneId);
				Intent intent = new Intent(INTENT_INTER_PHONE_RECIVE);
				context.sendBroadcast(intent);
		} else if (body instanceof InterphoneInviteMsg) {
			if(BaseApplication.interphoneIds.indexOf(body.interphoneId)<0){
				BaseApplication.interphoneIds.add(body.interphoneId);
			}
			Intent intent = new Intent(INTENT_INTER_PHONE_RECIVE);
			try {
				CCPUtil.showNewInterPhoneNoti(context, body.interphoneId);
			} catch (IOException e) {
				e.printStackTrace();
			}
			context.sendBroadcast(intent);
		} 
		Bundle b = new Bundle();
		b.putSerializable(Device.INTERPHONEMSG, body);
		sendTarget(WHAT_ON_INTERPHONE_SIP_MESSAGE, b);
	}

	@Override
	public void onChatroomState(int reason, String confNo) {
		Log4Util.d(DEMO_TAG , "[onChatRoomState ] reason " + reason + " , confNo "  +  confNo);
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putString(Device.CONFNO, confNo);
		sendTarget(WHAT_ON_CHATROOM, b);
	}


	@Override
	public void onReceiveChatroomMsg(ChatroomMsg msg) {
		Log4Util.d(DEMO_TAG , "[onReceiveChatRoomMsg ] Receive Chat Room message  , id :" + msg.getRoomNo());
		Bundle b = new Bundle();
		b.putSerializable(Device.CHATROOM_MSG, msg);
		sendTarget(WHAT_ON_CHATROOM_SIP_MESSAGE, b);
	}

	@Override
	public void onChatroomMembers(int reason, List<ChatroomMember> member) {
		Log4Util.d(DEMO_TAG , "[onChatRoomMembers ] on Chat Room  members that .. " + member);
		Bundle b = new Bundle();
		b.putSerializable(Device.CHATROOM_MEMBERS, (ArrayList<ChatroomMember>) member);
		sendTarget(WHAT_ON_CHATROOM_MEMBERS, b);
	}

	@Override
	public void onChatroomInviteMembers(int reason, String confNo) {
		Log4Util.d(DEMO_TAG , "[onChatRoomInvite ] reason " + reason + " , confNo "  +  confNo);
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putString(Device.CONFNO, confNo);
		sendTarget(WHAT_ON_CHATROOM_INVITE, b);
	}

	@Override
	public void onChatrooms(int reason, List<Chatroom> chatRoomList) {
		Log4Util.d(DEMO_TAG , "[onChatrooms ] on Chat Room  chatrooms that .. " + chatRoomList);
		Bundle b = new Bundle();
		b.putSerializable(Device.CHATROOM_LIST, (ArrayList<Chatroom>) chatRoomList);
		sendTarget(WHAT_ON_CHATROOM_LIST, b);
		
	}

	public void onSendInstanceMessage(int reason, InstanceMsg data) {
		Log4Util.d(DEMO_TAG , "[onSendInstanceMessage ] on send Instance Message that reason .. " + reason);
		if(data == null) {
			return;
		}
		try {
			// If the current activity is not in the chat interface, 
			// so need here to update the database
			// If you are in a chat interface, then because here has to update the database,
			// when the chat interface to update the database will not update message state 
			// Because this message state isn't IMChatMessageDetail.STATE_IM_SENDING
			int msgType = -1;
			if(reason == 0) {
				msgType = IMChatMessageDetail.STATE_IM_SEND_SUCCESS;
			} else {
				if(reason != 230007) {
					msgType = IMChatMessageDetail.STATE_IM_SEND_FAILED;
				}
			}
			if(msgType != -1) {
				String messageId = null;;
				if(data instanceof IMTextMsg) {
					messageId = ((IMTextMsg)data).getMsgId();
				} else if (data instanceof IMAttachedMsg) {
					messageId = ((IMAttachedMsg)data).getMsgId();
				}
				CCPSqliteManager.getInstance().updateIMMessageSendStatusByMessageId(messageId, msgType);
			}
		} catch (Exception e) {
			// 
		}
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putSerializable(Device.MEDIA_MESSAGE, data);
		sendTarget(WHAT_ON_SEND_MEDIAMSG_RES, b);
	}


	@Override
	public void onDownloadAttached(int reason, String fileName) {
		Log4Util.d(DEMO_TAG , "[onDownloadAttachmentFiles ]  reason " + reason +  " , fileName= " + fileName);
		if(reason == 0) {//success
			try {
				final IMAttachedMsg rMediaInfo = (IMAttachedMsg)BaseApplication.getInstance().getMediaData(fileName);
				Log4Util.d(DEMO_TAG , "[onDownloadAttachmentFiles ]  rMediaInfo " + rMediaInfo);
				if(rMediaInfo != null ) {
					//换成本地地址
					//if(getDevice().confirmIntanceMessage(msgid)) {
					String msgid[] = {rMediaInfo.getMsgId()};
					getDevice().confirmIntanceMessage(msgid);
							
					int index = rMediaInfo.getFileUrl().indexOf("fileName=");
					String msgContent = rMediaInfo.getFileUrl().substring(index+9, rMediaInfo.getFileUrl().length());
					
					try {
						String receiver = rMediaInfo.getReceiver();
						String sender = rMediaInfo.getSender();
						if(TextUtils.isEmpty(receiver)){
							return;
						}
						
						String contactId = "";
						
						if(CCPConfig.VoIP_ID.equals(receiver)) {
							// not group message 
							contactId = sender;
						} else {
							// group 
							contactId = receiver;
						}
						
						/*if(!TextUtils.isEmpty(receiver) && receiver.startsWith("g")) {
							// group chat 
							contactId = receiver;
							sender = rMediaInfo.getSender();
						} else{
							sender = rMediaInfo.getSender();
						}*/
						
						IMChatMessageDetail chatMessageDetail = null;
						if("amr".equals(rMediaInfo.getExt())) {
							chatMessageDetail = IMChatMessageDetail.getGroupItemMessageReceived(rMediaInfo.getMsgId(), IMChatMessageDetail.TYPE_MSG_VOICE 
									, contactId, sender);
						} else {
							chatMessageDetail = IMChatMessageDetail.getGroupItemMessageReceived(rMediaInfo.getMsgId(), IMChatMessageDetail.TYPE_MSG_FILE ,
									 contactId, sender) ;
							chatMessageDetail.setMessageContent(msgContent);
						}
						chatMessageDetail.setFileExt(rMediaInfo.getExt());
						chatMessageDetail.setFileUrl(rMediaInfo.getFileUrl()); // file path in server
						chatMessageDetail.setFilePath(fileName);               // local save path 
						chatMessageDetail.setUserData(rMediaInfo.getUserData());
						chatMessageDetail.setDateCreated(rMediaInfo.getDateCreated()); //file dateCreate in server ..
						
						
						CCPSqliteManager.getInstance().insertIMMessage(chatMessageDetail);
						Intent intent = new Intent(INTENT_IM_RECIVE);
						intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, contactId);
						context.sendBroadcast(intent);
						
						//paly music ...
						if(BaseApplication.getInstance().getMediaMsgList() != null
								&& BaseApplication.getInstance().getMediaMsgList().size() == 1 )
							CCPUtil.showNewMeidaMessageNoti(context ,chatMessageDetail);
					} catch (Exception e) {
						// TODO: handle exception
					}
					//}
				} 
				BaseApplication.getInstance().removeMediaData(fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} 		
	}

	@Override
	public void onReceiveInstanceMessage(InstanceMsg msg) {
		Log4Util.d(DEMO_TAG , "[onReceiveInstanceMessage ] "+msg.getClass().getName());
		if(msg != null) {
			if(msg instanceof IMAttachedMsg){
				IMAttachedMsg aMsg = (IMAttachedMsg)msg;
				if (aMsg.getSender() != null	&& aMsg.getSender().equals(CCPConfig.VoIP_ID)) {
					return;
				}
				// 收到消息先下载
				ArrayList<DownloadInfo> dLoadList = new ArrayList<DownloadInfo>();
				
				int index = aMsg.getFileUrl().indexOf("fileName=");
				String fileName = aMsg.getFileUrl().substring(index+9, aMsg.getFileUrl().length());
				String vLocalPath = new File(BaseApplication.getInstance().getVoiceStore(), fileName).getAbsolutePath();
				
				dLoadList.add(new DownloadInfo(aMsg.getFileUrl(), vLocalPath , aMsg.isChunked()));
				getDevice().downloadAttached(dLoadList);
				BaseApplication.getInstance().putMediaData(vLocalPath, aMsg);
			}else if(msg instanceof IMTextMsg){
				IMTextMsg aMsg = (IMTextMsg)msg;
				String sender = aMsg.getSender();
				String message = aMsg.getMessage();
				String receiver = aMsg.getReceiver();
				
				if(TextUtils.isEmpty(sender) || TextUtils.isEmpty(message) ||TextUtils.isEmpty(receiver)) {
					return ;
				}
				
				if(CCPConfig.VoIP_ID.equals(sender)) {
					return;
				}
				
				String contactId = "";
				if(CCPConfig.VoIP_ID.equals(receiver)) {
					// not group message 
					contactId = sender;
				} else {
					// group 
					contactId = receiver;
				}
				
				IMChatMessageDetail chatMessageDetail = IMChatMessageDetail.getGroupItemMessageReceived(aMsg.getMsgId(),IMChatMessageDetail.TYPE_MSG_TEXT
						, contactId, sender);
				chatMessageDetail.setMessageContent(message);
				chatMessageDetail.setDateCreated(aMsg.getDateCreated());
				chatMessageDetail.setUserData(aMsg.getUserData());
				
				try {
					CCPSqliteManager.getInstance().insertIMMessage(chatMessageDetail);
					Intent intent = new Intent(INTENT_IM_RECIVE);
					intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, contactId);
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			} else if (msg instanceof IMInviterMsg) {
				// Received the invitation to join the group
				IMInviterMsg imInviterMsg = (IMInviterMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Receive invitation to join the group ,that amdin " +
						imInviterMsg.getAdmin() + " , and group id :" + imInviterMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_INVITE_JOIN);
					Intent intent = new Intent(GroupBaseActivity.INTENT_RECEIVE_SYSTEM_MESSAGE);
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMProposerMsg) {
				
				// The receipt of the application to join the group
				IMProposerMsg imProposerMsg = (IMProposerMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Receive proposer message that Proposer " +
						imProposerMsg.getProposer() + " , and group id :" + imProposerMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_APPLY_JOIN);
					Intent intent = new Intent(GroupBaseActivity.INTENT_RECEIVE_SYSTEM_MESSAGE);
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMJoinGroupMsg) {
				
				// The receipt of the application to join the group
				IMJoinGroupMsg imJoinMsg = (IMJoinGroupMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Receive join message that Joiner " +
						imJoinMsg.getProposer() + " , and group id :" + imJoinMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_APPLY_JOIN_UNVALIDATION);
					Intent intent = new Intent(GroupBaseActivity.INTENT_RECEIVE_SYSTEM_MESSAGE);
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMRemoveMemeberMsg) {
				// Remove group received system information
				IMRemoveMemeberMsg imrMemeberMsg = (IMRemoveMemeberMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Received system information that " +
						"remove from group  id " + imrMemeberMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_REMOVE);
					Intent intent = new Intent(GroupBaseActivity.INTENT_REMOVE_FROM_GROUP);
					intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, imrMemeberMsg.getGroupId());
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMReplyJoinGroupMsg) {
				// Remove group received system information
				IMReplyJoinGroupMsg imAcceptRejectMsg = (IMReplyJoinGroupMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Received system information that " +
						"reject or accept from group  id " + imAcceptRejectMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_ACCEPT_OR_REJECT_JOIN);
					Intent intent = null;
					if("0".equals(imAcceptRejectMsg.getConfirm())){
						intent = new Intent(GroupBaseActivity.INTENT_JOIN_GROUP_SUCCESS);
						intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, imAcceptRejectMsg.getGroupId());
					} else {
						intent = new Intent(GroupBaseActivity.INTENT_RECEIVE_SYSTEM_MESSAGE);
					}
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMDismissGroupMsg) {
				// The group manager dismiss this group..
				IMDismissGroupMsg imDismissGroupMsg = (IMDismissGroupMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Received system information that " +
						"group manager dismiss this group  id " + imDismissGroupMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_GROUP_DISMISS);
					Intent intent = null;
					intent = new Intent(GroupBaseActivity.INTENT_REMOVE_FROM_GROUP);
					intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, imDismissGroupMsg.getGroupId());
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (msg instanceof IMQuitGroupMsg) {
				// The group manager dismiss this group..
				IMQuitGroupMsg imQuitGroupMsg = (IMQuitGroupMsg) msg;
				Log4Util.d(DEMO_TAG , "[VoiceHelper - onReceiveInstanceMessage ] Received system information that " +
						"Members quit from a group id " + imQuitGroupMsg.getGroupId());
				try {
					CCPSqliteManager.getInstance().insertNoticeMessage(msg, IMSystemMessage.SYSTEM_TYPE_GROUP_MEMBER_QUIT);
					Intent intent = null;
					intent = new Intent(GroupBaseActivity.INTENT_RECEIVE_SYSTEM_MESSAGE);
					intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, imQuitGroupMsg.getGroupId());
					context.sendBroadcast(intent);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
	}

	@Override
	public void onCallMediaUpdateRequest(String callid, int reason) {
		Log4Util.d(DEMO_TAG , "[onCallMediaUpdateRequest ]  callid="+ callid + ", reason="+reason);
	}

	@Override
	public void onCallMediaUpdateResponse(String callid, int reason) {
		Log4Util.d(DEMO_TAG , "[onCallMediaUpdateResponse ]  callid="+ callid + ", reason="+reason);
	}

	@Override
	public void onCallVideoRatioChanged(String callid, String resolution) {
		Log4Util.d(DEMO_TAG , "[onCallVideoRatioChanged ]  callid="+ callid + ", resolution="+resolution);
	}

	@Override
	public void onCallMediaInitFailed(String callid, int reason) {
		Log4Util.d(DEMO_TAG , "[onCallMediaInitFailed ]  callid="+ callid + ", reason="+reason);
	}

	@Override
	public void onConfirmIntanceMessage(int reason) {
		
	}

	@Override
	public void onChatroomDismiss(int reason, String roomNo) {
		Intent intent = new Intent(INTENT_CHAT_ROOM_DISMISS);
		intent.putExtra("roomNo", roomNo);
		context.sendBroadcast(intent);
	}

	@Override
	public void onChatroomRemoveMember(int reason, String member) {
		Bundle b = new Bundle();
		b.putInt(Device.REASON, reason);
		b.putString("kick_member", member);
		sendTarget(WHAT_ON_CHATROOM_KICKMEMBER, b);
	}

	@Override
	public void onFirewallPolicyEnabled() {
		Intent intent = new Intent(SettingsActivity.INTENT_P2P_ENABLED);
		context.sendBroadcast(intent);
	}

	/**
	 * Callback this method when networks changed.
	 * 
	 * @param apn
	 *            mobile access point name
	 * @param ns
	 *            mobile network state
	 */
	@Override
	public void onReceiveEvents(CCPEvents events/*, APN network, NetworkState ns*/) {
		if(events == CCPEvents.SYSCallComing)
			Log4Util.d(DEMO_TAG, "Receive system call ");
	}

}
