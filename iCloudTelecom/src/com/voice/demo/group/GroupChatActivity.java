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
package com.voice.demo.group;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.im.IMAttachedMsg;
import com.hisun.phone.core.voice.model.im.IMTextMsg;
import com.hisun.phone.core.voice.model.im.InstanceMsg;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.group.model.IMChatMessageDetail;
import com.voice.demo.group.utils.ITask;
import com.voice.demo.group.utils.MimeTypesTools;
import com.voice.demo.group.utils.RecordPopupWindow;
import com.voice.demo.sqlite.CCPSqliteManager;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.VoiceHelper;

/**
 * The details of interface group chat, voice chat and send files or text.
 * @version Time: 2013-7-21
 */
public class GroupChatActivity extends GroupBaseActivity implements View.OnClickListener ,View.OnTouchListener{

	private static final String USER_DATA 											= "容联易通";
	public static final int CHAT_MODE_IM_POINT 										= 0x1;
	public static final int CHAT_MODE_IM_GROUP 										= 0x2;
	
	// cancel recording sliding distance field.
	private static final int CANCLE_DANSTANCE 										= -60;
	
	public static final int REQUEST_CODE_TAKE_PICTURE 								= 11;
	public static final int REQUEST_CODE_SELECT_FILE 								= 12;
	
	// recording of three states
	public static final int RECORD_NO 												= 0;
	public static final int RECORD_ING 												= 1;
	public static final int RECODE_ED 												= 2;

	public static int RECODE_STATE 													= 0; 
	// the most short recording time, in milliseconds seconds, 
	// 0 for no time limit is set to 1000, suggestion
	// recording time
	// microphone gain volume value
	private static final int MIX_TIME 												= 1000; 
	
    public static HashMap<String, Boolean> voiceMessage = new HashMap<String, Boolean>();
	
	private RecordPopupWindow popupWindow = null;
	private IMGroupChatItemAdapter mIMGroupApapter;
	private String currentRecName;
	private String mGroupId;
	private String mGroupName;
	
	private LinearLayout mToolsStub;
	private ListView mIMGroupListView;
	private EditText mImEditText;
	private Button mGroudChatRecdBtn;
	private Button mIMsend;
	
	private TextView rVoiceCancleText;
	private TextView mNoticeTips;
	private ImageView ampImage;
	
	private View mChatFooter = null;
	private View mVoiceShortLy;
	private View mVoiceLoading;
	private View mVoiceRecRy;
	private View mCancleIcon;
	
	private boolean isRecordAndSend = false;
	private boolean isCancle = false;
	private int chatModel = CHAT_MODE_IM_POINT;
	private float mTouchStartY = 0;
	private float mDistance = 0;
	
	private long recodeTime = 0; 
	
	private static final int ampValue[] = {
		0,20,30,45,60,85,100
	};
	private static final int ampIcon[] = {
		R.drawable.voice_interphone01,
		R.drawable.voice_interphone02,
		R.drawable.voice_interphone03,
		R.drawable.voice_interphone04,
		R.drawable.voice_interphone05,
		R.drawable.voice_interphone06,
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_group_chat_activity);
		
		
		initResourceRefs();
		
		
		if(VoiceHelper.getInstance().getDevice() == null) {
			CCPUtil.registDialog(this) ;
			//finish();
		} else {
			VoiceHelper.getInstance().setHandler(mIMChatHandler);
			initialize(savedInstanceState);
			
			String  title = null;
			String  rightButton = null;
			if(chatModel == CHAT_MODE_IM_GROUP) {
				title = mGroupName;
				rightButton = getString(R.string.str_title_right_group_info);
			} else {
				title = "TO:" + mGroupName;
				rightButton = getString(R.string.btn_clear_all_text);
			}
			handleTitleDisplay(getString(R.string.btn_title_back)
					, title
					, rightButton);
		}
		
		registerReceiver(new String[]{VoiceHelper.INTENT_IM_RECIVE 
				,INTENT_REMOVE_FROM_GROUP,INTENT_DELETE_GROUP_MESSAGE});
		
		SharedPreferences ccpDemoSP = getSharedPreferences();
		isRecordAndSend = ccpDemoSP.getBoolean(CCPUtil.SP_KEY_VOICE_ISCHUNKED, true);
	}
	
	private void initResourceRefs() {
		
		final View activityRootView = findViewById(R.id.im_root);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
		        if (heightDiff > 100) { 
		        	// If the difference is more than 100 pixels, is likely to be a soft keyboard...
		            // do something here
		        	
		        	if(mIMGroupListView != null && mIMGroupApapter != null) {
		        		mIMGroupListView.setSelection(mIMGroupApapter.getCount() - 1 );
		        		
		        	}
		        	// The judge of this input method is the pop-up state, 
		        	// then set the record button is not available
		        	
		        	//mGroudChatRecdBtn.setEnabled(false);
		        	mToolsStub.setVisibility(View.GONE);
		        } else {
		        	mToolsStub.setVisibility(View.VISIBLE);
		        	//mGroudChatRecdBtn.setEnabled(true);
		        }
		     }
		});
		
		mToolsStub = (LinearLayout)findViewById(R.id.tools_stub);
		mToolsStub.setVisibility(View.GONE);
		mNoticeTips = (TextView) findViewById(R.id.notice_tips);
		mNoticeTips.setVisibility(View.GONE);
		mIMGroupListView = (ListView) findViewById(R.id.im_chat_list);
		
		mIMGroupListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				HideSoftKeyboard();
				
				// After the input method you can use the record button.
				//mGroudChatRecdBtn.setEnabled(true);
				return false;
			}
		});
		
		mImEditText = (EditText) findViewById(R.id.im_content_et);
		mIMsend = (Button) findViewById(R.id.im_send_btn);
		mIMsend.setOnClickListener(this);
		
		findViewById(R.id.btn_file).setOnClickListener(this);
		findViewById(R.id.btn_voice).setOnClickListener(this);
		
		// When a finger pressing time to start recording audio data 
		// need to change the position of the background, the opposite is 
		// the original background
		
		mGroudChatRecdBtn = (Button) findViewById(R.id.voice_record_bt);
		mGroudChatRecdBtn.setOnTouchListener(this);
		
		 mChatFooter = findViewById(R.id.im_chat_foot);
		
	}

	private void initialize(Bundle savedInstanceState) {
		// Read parameters or previously saved state of this activity.
		Intent intent = getIntent();
		if (intent.hasExtra(KEY_GROUP_ID)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mGroupId = (String) extras.get(KEY_GROUP_ID);
				
			}
		}
		
		if (intent.hasExtra("groupName")) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mGroupName = (String) extras.get("groupName");
				
			}
		}
		
		if(TextUtils.isEmpty(mGroupId)) {
			Toast.makeText(getApplicationContext(), R.string.toast_group_id_error, Toast.LENGTH_SHORT).show();
			finish();
		}
		
		if(TextUtils.isEmpty(mGroupName)) {
			mGroupName = mGroupId;
		}
		
		
		
		if(mGroupId.startsWith("g")) {
			chatModel = CHAT_MODE_IM_GROUP;
		} else {
			chatModel = CHAT_MODE_IM_POINT;
		}
		
		new IMListyncTask().execute(mGroupId + "");
	}
	
	@Override
	protected void handleTaskBackGround(ITask iTask) {
		super.handleTaskBackGround(iTask);
		int key = iTask.getKey();
		if(key == TASK_KEY_DEL_MESSAGE) {
			try {
				CCPSqliteManager.getInstance().deleteIMMessageBySessionId(mGroupId);
				if(mIMGroupApapter != null) {
					for (int i = 0; i < mIMGroupApapter.getCount(); i++) {
						IMChatMessageDetail item = mIMGroupApapter.getItem(i);
						if(item == null || item.getMessageType() == IMChatMessageDetail.TYPE_MSG_TEXT) {
							continue;
						}
						
						CCPUtil.delFile(item.getFilePath());
						
					}
				}
				sendbroadcast();
				closeConnectionProgress();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void handleTitleAction(int direction) {
		
		if(direction == TITLE_RIGHT_ACTION) {
			if(chatModel == CHAT_MODE_IM_GROUP) {
				if(!TextUtils.isEmpty(mGroupId)) {
					
					Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
					intent.putExtra(KEY_GROUP_ID, mGroupId);
					intent.putExtra("isJoin", true);
					startActivity(intent);
					
				} else {
					
					// failed ..
					Toast.makeText(getApplicationContext(), R.string.toast_click_into_group_error, Toast.LENGTH_SHORT).show();
				}
				return ;
			}
			
			showConnectionProgress(getString(R.string.str_dialog_message_default));
			ITask iTask = new ITask(TASK_KEY_DEL_MESSAGE);
			addTask(iTask);
			
		} else {
			super.handleTitleAction(direction);
		}
	}

	String uniqueId = null;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if(!CCPUtil.isExistExternalStore()) {
			Toast.makeText(getApplicationContext(), R.string.media_ejected, Toast.LENGTH_LONG).show();
			return false;
		}
		
		int[] location = new int[2];  
        v.getLocationOnScreen(location);  
        mTouchStartY = location[1];  
        
        switch (event.getAction()) {
		case  MotionEvent.ACTION_DOWN:
			if (RECODE_STATE != RECORD_ING) {
				RECODE_STATE = RECORD_ING;
				readyOperation();
				showVoiceDialog(findViewById(R.id.im_root).getHeight() - mChatFooter.getHeight());
				
				// True audio data recorded immediately transmitted to the server
				// False just recording audio data, then send audio file after the completion of recording..
				//isRecordAndSend = true; 
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						currentRecName =   + System.currentTimeMillis() + ".amr";
						File directory = getCurrentVoicePath();
						
						// If it is sent in non chunked mode, only second parameters
						uniqueId = VoiceHelper.getInstance().getDevice().startVoiceRecording(mGroupId, directory.getAbsolutePath(), isRecordAndSend , USER_DATA);
						voiceMessage.put(uniqueId, true);
					}
				}).start();
				
				mGroudChatRecdBtn.setBackgroundResource(R.drawable.group_chat_bg_pressed);
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			mDistance = event.getRawY() - mTouchStartY;
			if(mDistance < CANCLE_DANSTANCE){
				//cancle send voice ...
				if(rVoiceCancleText != null) {
					rVoiceCancleText.setText(R.string.voice_cancel_rcd_release);
					mCancleIcon.setVisibility(View.VISIBLE);
					ampImage.setVisibility(View.GONE);
				}
				isCancle = true;
			} else {
				rVoiceCancleText.setText(R.string.voice_cancel_rcd);
				mCancleIcon.setVisibility(View.GONE);
				ampImage.setVisibility(View.VISIBLE);
				isCancle = false;
			}
			
			break;
		case MotionEvent.ACTION_UP:
			mGroudChatRecdBtn.setEnabled(false);
			if(isCancle) {
				VoiceHelper.getInstance().getDevice().cancelVoiceRecording();
			} else {
				VoiceHelper.getInstance().getDevice().stopVoiceRecording();
			}
			recodeEnd(isCancle);
			break;
		}
		return false;
	}

	private static final int WHAT_ON_COMPUTATION_TIME = 10000;
	private long computationTime = -1L;
	private Toast mRecordTipsToast;
	private void readyOperation() {
		computationTime = -1L;
		mRecordTipsToast = null;
		playTone(ToneGenerator.TONE_PROP_BEEP, TONE_LENGTH_MS);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				stopTone();
			}
		}, TONE_LENGTH_MS);
		vibrate(50L);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_file:
			
			new AlertDialog.Builder(this)
			.setItems(R.array.chat_select_item, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	           		// The 'which' argument contains the index position
						// of the selected item
						if(which == 0){// take pic 
							takePicture();
						}else if (which == 1){//save
							if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
								Toast.makeText(getApplicationContext(), R.string.sdcard_not_file_trans_disable, Toast.LENGTH_SHORT).show();
								return;
							}
							Intent intent = new Intent(GroupChatActivity.this, FileBrowserActivity.class) ;
							//intent.putExtra("to", recipient);
							startActivityForResult(intent , REQUEST_CODE_SELECT_FILE);
						}
	               }
	        }).setTitle(R.string.dialog_list_item_title)
			.create().show();
			
			break;
			
		case R.id.im_send_btn:
			Editable text = mImEditText.getText();
			String content = text.toString();
			if(TextUtils.isEmpty(content.trim()))
			{
				return;
			}
			IMChatMessageDetail chatMessageDetail = IMChatMessageDetail.getGroupItemMessage(IMChatMessageDetail.TYPE_MSG_TEXT 
					, IMChatMessageDetail.STATE_IM_SENDING , mGroupId);
			chatMessageDetail.setMessageContent(content);
			
			try {
				String uniqueID = VoiceHelper.getInstance().getDevice().sendInstanceMessage(mGroupId, text.toString(), null, USER_DATA);
				if(TextUtils.isEmpty(uniqueID)) {
					BaseApplication.getInstance().showToast(R.string.toast_send_group_message_failed);
					chatMessageDetail.setImState(IMChatMessageDetail.STATE_IM_SEND_FAILED);
					return ;
				}
				chatMessageDetail.setMessageId(uniqueID);
				chatMessageDetail.setUserData(USER_DATA);
				CCPSqliteManager.getInstance().insertIMMessage(chatMessageDetail);
				sendbroadcast();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			text.clear();
			
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		HideSoftKeyboard();
		updateReadStatus();
		
	}

	private void updateReadStatus() {
		try {
			CCPSqliteManager.getInstance().updateIMMessageUnreadStatusToReadBySessionId(mGroupId,IMChatMessageDetail.STATE_READED);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			VoiceHelper.getInstance().getDevice().stopVoiceMsg();
			VoiceHelper.getInstance().getDevice().enableLoudsSpeaker(false);
			VoiceHelper.getInstance().getDevice().stopVoiceRecording();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mIMGroupApapter = null ;
		
		if(mIMChatHandler != null ) {
			mIMChatHandler = null;
		}
		
		isPalye = false;
		vAnimDra = null;
		vPlayState = 4;;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		DisplaySoftKeyboard();
	}
	
	String fileName;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log4Util.d(VoiceHelper.DEMO_TAG ,"[IMChatActivity] onActivityResult: requestCode=" + requestCode
				+ ", resultCode=" + resultCode + ", data=" + data);

		// If there's no data (because the user didn't select a file or take pic  and
		// just hit BACK, for example), there's nothing to do.
		if (requestCode != REQUEST_CODE_TAKE_PICTURE || requestCode == REQUEST_CODE_SELECT_FILE 
			) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[GroupChatActivity] onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}
		
		String fileName = null ;
		String filePath = null;
		switch (requestCode) {
			case REQUEST_CODE_TAKE_PICTURE: {
				File file = takepicfile;
				if(!file.exists()) {
					return;
				}
				//Uri uri = Uri.fromFile(file);
				//addImage(uri, false);
				
				filePath = file.getAbsolutePath();
				// do send pic ...
				break;
			}
			
			case REQUEST_CODE_SELECT_FILE: {
				
				if(data.hasExtra("file_name")) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						fileName = extras.getString("file_name");
					}
				}
				
				if(data.hasExtra("file_url")) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						filePath = extras.getString("file_url");
					}
				}
				
				break;
			}
		}
		
		if(TextUtils.isEmpty(filePath)) {
			// Select the local file does not exist or has been deleted.
			Toast.makeText(GroupChatActivity.this, R.string.toast_file_exist, Toast.LENGTH_SHORT).show();
			return ;
		}
		
		
		
		if(TextUtils.isEmpty(fileName)) {
			fileName = new File(filePath).getName();
			//fileName = filePath.substring(filePath.indexOf("/"), filePath.length());
		}
		
		IMChatMessageDetail chatMessageDetail = IMChatMessageDetail.getGroupItemMessage(IMChatMessageDetail.TYPE_MSG_FILE 
				, IMChatMessageDetail.STATE_IM_SENDING , mGroupId) ;
		chatMessageDetail.setMessageContent(fileName);
		chatMessageDetail.setFilePath(filePath);
		String extensionName = VoiceUtil.getExtensionName(fileName);
		if("amr".equals(extensionName)) {
			chatMessageDetail.setMessageType(IMChatMessageDetail.TYPE_MSG_VOICE);
		}
		chatMessageDetail.setFileExt(extensionName);
		
		
		try {
			String uniqueID = VoiceHelper.getInstance().getDevice().sendInstanceMessage(mGroupId, null, filePath, USER_DATA);
			chatMessageDetail.setMessageId(uniqueID);
			
			CCPSqliteManager.getInstance().insertIMMessage(chatMessageDetail);
			chatMessageDetail.setUserData(USER_DATA);
			notifyGroupDateChange(chatMessageDetail);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void notifyGroupDateChange(IMChatMessageDetail chatMessageDetail) {
		if(mIMGroupApapter == null ) {
			ArrayList<IMChatMessageDetail> iChatMsg = new ArrayList<IMChatMessageDetail>();
			iChatMsg.add(chatMessageDetail);
			mIMGroupApapter = new IMGroupChatItemAdapter(iChatMsg);
			mIMGroupListView.setAdapter(mIMGroupApapter);
		} else {
			mIMGroupApapter.insert(chatMessageDetail, mIMGroupApapter.getCount());
		}
		
		mIMGroupListView.setSelection(mIMGroupListView.getCount());
	}
	
	
	private void sendbroadcast() {
		Intent intent = new Intent(VoiceHelper.INTENT_IM_RECIVE);
		intent.putExtra(KEY_GROUP_ID, mGroupId);
		sendBroadcast(intent);
	}
	
	class IMGroupChatItemAdapter extends ArrayAdapter<IMChatMessageDetail> {

		LayoutInflater mInflater;
		public IMGroupChatItemAdapter(List<IMChatMessageDetail> iChatMsg) {
			super(GroupChatActivity.this, 0, iChatMsg);
			
			mInflater = getLayoutInflater();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final GroupMsgHolder holder;
			if (convertView == null|| convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.list_item_voice_mseeage, null);
				holder = new GroupMsgHolder();
				
				holder.lavatar = (ImageView) convertView.findViewById(R.id.voice_chat_avatar_l);
				holder.ravatar = (ImageView) convertView.findViewById(R.id.voice_chat_avatar_r);
				holder.gLayoutLeft = (LinearLayout) convertView.findViewById(R.id.voice_item_left);
				holder.gLayoutRight = (LinearLayout) convertView.findViewById(R.id.voice_item_right);
				
				
				holder.gTime = (TextView) convertView.findViewById(R.id.voice_chat_time);
				
				holder.gNameleft = (TextView) convertView.findViewById(R.id.name_l);
				holder.gNameRight = (TextView) convertView.findViewById(R.id.name_r);
				
				holder.gVoiceChatLyLeft = (LinearLayout) convertView.findViewById(R.id.voice_chat_ly_l);
				holder.gIMChatLyLeft = (LinearLayout) convertView.findViewById(R.id.im_chat_ly);
				
				holder.gVoiceChatLyRight = (LinearLayout) convertView.findViewById(R.id.voice_chat_ly_r);
				holder.gIMChatLyRight = (LinearLayout) convertView.findViewById(R.id.im_chat_ly_r);
				
				
				
				holder.imFileIconL = (ImageView) convertView.findViewById(R.id.im_chatting_file_icon_l);
				holder.imFileIconR = (ImageView) convertView.findViewById(R.id.im_chatting_file_icon);
				
				holder.imFileNameLeft = (TextView) convertView.findViewById(R.id.file_name_left);
				holder.imFileNameRight = (TextView) convertView.findViewById(R.id.file_name_right);
				
				holder.imTimeLeft = (TextView) convertView.findViewById(R.id.im_chat_time_left);
				holder.imTimeRight = (TextView) convertView.findViewById(R.id.im_chat_time_right);
				
				holder.rProBar = (ProgressBar) convertView.findViewById(R.id.voice_sending_r);
				
				
				// voice item  time
				holder.lDuration = (TextView) convertView.findViewById(R.id.voice_content_len_l);
				holder.rDuration = (TextView) convertView.findViewById(R.id.voice_content_len_r);
				
				// vioce chat animation
				holder.vChatContentFrom = (ImageView) convertView.findViewById(R.id.voice_chat_recd_tv_l);
				holder.vChatContentTo = (ImageView) convertView.findViewById(R.id.voice_chat_recd_tv_r);
				
				
				holder.vErrorIcon = (ImageView) convertView.findViewById(R.id.error_Icon);
			} else {
				holder = (GroupMsgHolder) convertView.getTag();
			}
			
			final IMChatMessageDetail item = getItem(position);
			if(item != null ) {
				View.OnClickListener onClickListener = new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(GroupChatActivity.this,
								GroupMemberCardActivity.class);
						intent.putExtra(KEY_GROUP_ID, mGroupId);
						intent.putExtra("voipAccount", item.getGroupSender());
						intent.putExtra("modify", false);
						startActivity(intent);
						
					}
				};
				if(item.getImState() == IMChatMessageDetail.STATE_IM_RECEIVEED) {
					holder.gLayoutLeft.setVisibility(View.VISIBLE);
					holder.gLayoutRight.setVisibility(View.GONE);
					String groupSender = item.getGroupSender();
					if(!TextUtils.isEmpty(groupSender) && groupSender.length() > 4) {
						groupSender = groupSender.substring(groupSender.length() - 4, groupSender.length());
					}
					if(chatModel == CHAT_MODE_IM_GROUP){
						holder.lavatar.setOnClickListener(onClickListener);
					}
					
					holder.gNameleft.setText(groupSender);
					
					if(item.getMessageType() == IMChatMessageDetail.TYPE_MSG_VOICE) { //voice chat ...itme
						
						// If the speech information, you need to display the voice information 
						// distribution, and voice information unified time display in the middle 
						// position above the voice information
						// And hidden files IM layout
						holder.gVoiceChatLyLeft.setVisibility(View.VISIBLE);
						holder.gIMChatLyLeft.setVisibility(View.GONE);
						holder.gTime.setVisibility(View.VISIBLE);
						
						int duration = (int) Math.ceil(VoiceHelper.getInstance().getDevice().getVoiceDuration(item.getFilePath())/1000) ;
						holder.lDuration.setText(duration + "''");
						
						holder.gVoiceChatLyLeft.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ViewPlayAnim(holder.vChatContentFrom ,item.getFilePath() ,vPlayState);
							}

						});
						
					} else {
						// TEXT FILE
						// If not the voice information, you need to display the IM file layout, 
						// and the layout of item audio information hiding, also need the voice 
						// information time display view hide this time, only need to display 
						// the time view IM style
						holder.gVoiceChatLyLeft.setVisibility(View.GONE);
						holder.gIMChatLyLeft.setVisibility(View.VISIBLE);
						holder.gTime.setVisibility(View.GONE);
						if(item.getMessageType() == IMChatMessageDetail.TYPE_MSG_TEXT) {
							holder.imFileNameLeft.setText(item.getMessageContent());
							holder.imFileIconL.setVisibility(View.GONE);
							
						} else if (item.getMessageType() == IMChatMessageDetail.TYPE_MSG_FILE) {
							holder.imFileIconL.setVisibility(View.VISIBLE);
							
							
							holder.imFileNameLeft.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									//String vLocalPath =  new File(BaseApplication.getInstance().getVoiceStore() , item.getMessageContent()).getAbsolutePath();
									snedFilePrevieIntent(item.getFilePath());
								}
							});
							
							//file name
							holder.imFileNameLeft.setText(item.getMessageContent());
							
						}
						
						holder.imTimeLeft.setText(item.getCurDate());
					}
				} else {
					if(chatModel == CHAT_MODE_IM_GROUP){
						holder.ravatar.setOnClickListener(onClickListener);
					}
					holder.gLayoutLeft.setVisibility(View.GONE);
					holder.gLayoutRight.setVisibility(View.VISIBLE);
					holder.gNameRight.setText(CCPConfig.VoIP_ID.substring(CCPConfig.VoIP_ID.length() - 4, CCPConfig.VoIP_ID.length()));
					
					if(item.getMessageType() == IMChatMessageDetail.TYPE_MSG_VOICE) { //voice chat ...itme
						holder.gVoiceChatLyRight.setVisibility(View.VISIBLE);
						holder.gIMChatLyRight.setVisibility(View.GONE);
						holder.gTime.setVisibility(View.VISIBLE);
						
						int duration = (int) Math.ceil(VoiceHelper.getInstance().getDevice().getVoiceDuration(item.getFilePath())/1000) ;
						holder.rDuration.setText(duration + "''");
						
						holder.gVoiceChatLyRight.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ViewPlayAnim(holder.vChatContentTo ,item.getFilePath() ,vPlayState);
							}
						});
						
					} else {
						// TEXT FILE
						holder.gVoiceChatLyRight.setVisibility(View.GONE);
						holder.gIMChatLyRight.setVisibility(View.VISIBLE);
						holder.gTime.setVisibility(View.GONE);
						
						
						if(item.getMessageType() == IMChatMessageDetail.TYPE_MSG_TEXT) {
							holder.imFileNameRight.setText(item.getMessageContent());
							holder.imFileIconR.setVisibility(View.GONE);
							
							// If it is sent the text is not realistic loading ...
							holder.rProBar.setVisibility(View.GONE);
							
						} else if (item.getMessageType() == IMChatMessageDetail.TYPE_MSG_FILE) {
							holder.imFileIconR.setVisibility(View.VISIBLE);
							//file name
							holder.imFileNameRight.setText(item.getMessageContent());
							
							holder.imFileNameRight.setOnClickListener(new View.OnClickListener() {
								
								@Override
								public void onClick(View v) {
									snedFilePrevieIntent(item.getFilePath());
								}
							});
							
						}
						
						holder.imTimeRight.setText(item.getCurDate());
						
					}
					
					// is sending ?
					if(item.getImState() == IMChatMessageDetail.STATE_IM_SENDING) {
						holder.rProBar.setVisibility(View.VISIBLE);
						holder.vErrorIcon.setVisibility(View.GONE);
					} else if (item.getImState() == IMChatMessageDetail.STATE_IM_SEND_SUCCESS) {
						holder.rProBar.setVisibility(View.GONE);
						holder.vErrorIcon.setVisibility(View.GONE);
					} else if (item.getImState() == IMChatMessageDetail.STATE_IM_SEND_FAILED) {
						holder.vErrorIcon.setVisibility(View.VISIBLE);
						holder.rProBar.setVisibility(View.GONE);
					}
				}
				
				holder.gTime.setText(item.getCurDate());
				
			}
			
			
			return convertView;
		}
		
		class GroupMsgHolder {
			ImageView lavatar;
			ImageView ravatar;
			// root layout
			LinearLayout gLayoutLeft;
			LinearLayout gLayoutRight;
			
			TextView gTime;
			
			TextView gNameleft;
			TextView gNameRight;
			
			LinearLayout gVoiceChatLyLeft; // 语音布局
			LinearLayout gIMChatLyLeft;    // IM布局
			
			LinearLayout gVoiceChatLyRight; // 语音布局
			LinearLayout gIMChatLyRight;    // IM布局
			
			ImageView imFileIconL;       // 	IM FILE
			ImageView imFileIconR;
			TextView imFileNameLeft;
			TextView imFileNameRight;
			TextView imTimeLeft;
			TextView imTimeRight;
			
			ProgressBar rProBar;
			
			// voice time 
			TextView lDuration;
			TextView rDuration;
			
			ImageView vChatContentFrom;
			ImageView vChatContentTo;
			
			
			ImageView vErrorIcon;
		}
		
		
		
		
	}

	class IMListyncTask extends AsyncTask<String , Void, ArrayList<IMChatMessageDetail>>{

		@Override
		protected ArrayList<IMChatMessageDetail> doInBackground(String... params) {
			if(params != null && params.length > 0) {
				
				try {
					updateReadStatus();
					return CCPSqliteManager.getInstance().queryIMMessagesBySessionId(params[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<IMChatMessageDetail> result) {
			super.onPostExecute(result);
			
			if(result != null && !result.isEmpty()) {
				mIMGroupApapter = new IMGroupChatItemAdapter(result);
				mIMGroupListView.setAdapter(mIMGroupApapter);
			} else {
				mIMGroupListView.setAdapter(null);
			}
		}
	}
	
	
	
	/**
	 * 打开文件预览
	 * @param fileName
	 */
	void snedFilePrevieIntent(String fileName) { 
	   Intent intent = new Intent(); 
	   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	   intent.setAction(android.content.Intent.ACTION_VIEW); 
	   String type = MimeTypesTools.getMimeType(getApplicationContext(), fileName);
	   File file = new File(fileName);
	   intent.setDataAndType(Uri.fromFile(file), type); 
	   startActivity(intent); 
	 } 

	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if(intent == null ) {
			return;
		}
		
		if (VoiceHelper.INTENT_IM_RECIVE.equals(intent.getAction())
				|| INTENT_DELETE_GROUP_MESSAGE.equals(intent.getAction())) {
			//update UI...
			//new IMListyncTask().execute();
			if(intent.hasExtra(KEY_GROUP_ID)) {
				String sender = intent.getStringExtra(KEY_GROUP_ID);
				if(!TextUtils.isEmpty(sender) && sender.equals(mGroupId)) {
					new IMListyncTask().execute(mGroupId);
				}
			}
			
		}else if (INTENT_REMOVE_FROM_GROUP.equals(intent.getAction())) {
			// remove from group ...
			this.finish();
		}
	}

	
	private android.os.Handler mIMChatHandler = new android.os.Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = -1;
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
			}
			
			switch (msg.what) {
			case VoiceHelper.WHAT_ON_SEND_MEDIAMSG_RES:
				if(b == null ){
					return ;
				}
				// receive a new IM message
				// then shown in the list.
				try {
					reason = b.getInt(Device.REASON);
					InstanceMsg instancemsg = (InstanceMsg)b.getSerializable(Device.MEDIA_MESSAGE);
					if(instancemsg == null ) {
						return ;
					}
					
					//IMChatMessageDetail chatMessageDetail = null;
					int sendType = IMChatMessageDetail.STATE_IM_SEND_FAILED;
					String messageId = null;
					if(instancemsg instanceof IMAttachedMsg) {
						IMAttachedMsg rMediaInfo = (IMAttachedMsg)instancemsg;
						messageId = rMediaInfo.getMsgId();
						if (reason == 0 ) {

							sendType = IMChatMessageDetail.STATE_IM_SEND_SUCCESS;
							
							try {
								CCPUtil.playNotifycationMusic(getApplicationContext(), "voice_message_sent.mp3");
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							if(reason == 230007 && isCancle) {
								// Here need to determine whether is it right? You cancel this recording, 
								// and callback upload failed in real-time recording uploaded case, 
								// so we need to do that here, when cancel the upload is not prompt the user interface
								// 230007 is the server did not receive a normal AMR recording end for chunked...
								return ;
							}
							
							
							if(GroupChatActivity.voiceMessage.containsKey(rMediaInfo.getMsgId()) ) {
								isRecordAndSend = false;
								return;
							}
							
							// This is a representative chunked patterns are transmitted speech file
							// If the execution returns to the false, is not chunked or send files
							//VoiceSQLManager.getInstance().updateIMChatMessage(rMediaInfo.getMsgId(), IMChatMsgDetail.TYPE_MSG_SEND_FAILED);
							sendType = IMChatMessageDetail.STATE_IM_SEND_FAILED;
							
							// failed
							// If the recording mode of data collection is the recording side upload (chunked), 
							// then in the recording process may be done to interrupt transfer for various reasons,
							// so, This failed reason can callback method ,But can't immediately begin to upload file voice, 
							// because there may not completed recording ,
							// You can set a Identification here on behalf of the recording process, transmission failure, 
							// wait for real recording completed then sending voice recording file
							
							// If it is after recording then uploading files,When the transmission 
							// failure can be sent second times in this callback methods
							Toast.makeText(getApplicationContext(), R.string.toast_voice_send_failed, Toast.LENGTH_SHORT).show();
							if(mIMGroupApapter != null )  {
								//mIMGroupApapter.remove(msgDetail);
								//mIMGroupApapter = null ;
							}
						}
						
					} else if (instancemsg instanceof IMTextMsg) {
						IMTextMsg imTextMsg = (IMTextMsg)instancemsg;
						messageId = imTextMsg.getMsgId();
						if(reason == 0 ) {
							sendType = IMChatMessageDetail.STATE_IM_SEND_SUCCESS;
						} else {
							// do send text message failed ..
							sendType = IMChatMessageDetail.STATE_IM_SEND_FAILED;
						}
					}
					CCPSqliteManager.getInstance().updateIMMessageSendStatusByMessageId(messageId, sendType);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				sendbroadcast();
				break;
			case VoiceHelper.WHAT_ON_AMPLITUDE:
				
				double amplitude = b.getDouble(Device.VOICE_AMPLITUDE);
				displayAmplitude(amplitude);
				
				break;
				
			case VoiceHelper.WHAT_ON_RECODE_TIMEOUT:
				
				mGroudChatRecdBtn.setEnabled(false);
				recodeEnd(false);
				break;
				
			case VoiceHelper.WHAT_ON_PLAY_VOICE_FINSHING: 
				releaseAnim();
				vPlayState = TYPE_VOICE_STOP;
				BaseApplication.getInstance().setSpeakerEnable(false);
				break;
			case VoiceHelper.WHAT_ON_DIMISS_DIALOG:
				if(popupWindow != null ) {
					popupWindow.dismiss();
					RECODE_STATE = RECORD_NO;
					recodeTime = 0;
					mGroudChatRecdBtn.setBackgroundResource(android.R.color.transparent);
					mGroudChatRecdBtn.setEnabled(true);
				}
				
			case WHAT_ON_COMPUTATION_TIME:
				if(promptRecordTime() && RECODE_STATE == RECORD_ING) {
					sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
				}
				
				break;
			default:
				break;
			}
		}


	};
	
	/**************************** voice record   ******************************************/
	// recode 
	void displayAmplitude(double amplitude) {
		if(mVoiceLoading.getVisibility() == View.VISIBLE) {
			
			// If you are in when being loaded, then send to start recording
			mIMChatHandler.removeMessages(WHAT_ON_COMPUTATION_TIME);
			mIMChatHandler.sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
		}
		mVoiceRecRy.setVisibility(View.VISIBLE);
		mVoiceLoading.setVisibility(View.GONE);
		mVoiceShortLy.setVisibility(View.GONE);
		
		for (int i = 0; i < ampValue.length; i++) {
			if(amplitude >= ampValue[i] && amplitude < ampValue[i+1]) {
				ampImage.setBackgroundResource(ampIcon[i]);
				return ;
			} else {
				continue;
			}
		}
	}
	
	// voice local save path ..
	private File getCurrentVoicePath() {
		File directory = new File(BaseApplication.getInstance().getVoiceStore(),currentRecName);
		return directory;
	}
	
	
	// display dialog recordings
	void showVoiceDialog(int height) {
		lockScreen();
		int heightDensity = Math.round(180 * getResources().getDisplayMetrics().densityDpi / 160.0F);
		int density = CCPUtil.getMetricsDensity(this , 50.0F);
		if(popupWindow == null ) {
			View view = getLayoutInflater().inflate(R.layout.voice_rec_dialog, null);
			popupWindow = new RecordPopupWindow(view, WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
			ampImage = ((ImageView)popupWindow.getContentView().findViewById(R.id.dialog_img));
			mCancleIcon = ((ImageView)popupWindow.getContentView().findViewById(R.id.voice_rcd_cancle_icon));
	        rVoiceCancleText = ((TextView)this.popupWindow.getContentView().findViewById(R.id.voice_rcd_cancel));
	        mVoiceLoading = this.popupWindow.getContentView().findViewById(R.id.voice_rcd_hint_loading);
	        mVoiceRecRy = this.popupWindow.getContentView().findViewById(R.id.voice_rcd_rl);
	        mVoiceShortLy = this.popupWindow.getContentView().findViewById(R.id.voice_rcd_tooshort);
		}
		mVoiceLoading.setVisibility(View.VISIBLE);
		mVoiceShortLy.setVisibility(View.GONE);
		mVoiceRecRy.setVisibility(View.GONE);
		ampImage.setVisibility(View.VISIBLE);
		ampImage.setBackgroundResource(ampIcon[0]);
		mCancleIcon.setVisibility(View.GONE);
		popupWindow.showAtLocation(mChatFooter, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, density +(height - heightDensity) / 2);
	}
	
	public void removePopuWindow() {
	    if (popupWindow != null)  {
	    	popupWindow.dismiss();
	    	mVoiceRecRy.setVisibility(View.VISIBLE);
	    	ampImage.setVisibility(View.VISIBLE);
	    	mCancleIcon.setVisibility(View.GONE);
	    	mVoiceLoading.setVisibility(View.GONE);
	    	mVoiceShortLy.setVisibility(View.GONE);
	    	rVoiceCancleText.setText(R.string.voice_cancel_rcd);
	    }
	    releaseLockScreen();
	  }
	
	public void tooShortPopuWindow() {
		mGroudChatRecdBtn.setEnabled(false);
		if (popupWindow != null) {
			mVoiceShortLy.setVisibility(View.VISIBLE);
			mVoiceLoading.setVisibility(View.GONE);
			mVoiceRecRy.setVisibility(View.GONE);
			popupWindow.update();
		}
		if (mIMChatHandler != null) {
			mIMChatHandler.sendEmptyMessageDelayed(
					VoiceHelper.WHAT_ON_DIMISS_DIALOG, 500L);
		}
	}
	
	
	private void recodeEnd(boolean isCancleSend) {
		if (RECODE_STATE == RECORD_ING) {
			if(new File(getCurrentVoicePath().getAbsolutePath()).exists()) {
				recodeTime = VoiceHelper.getInstance().getDevice().getVoiceDuration(
						getCurrentVoicePath().getAbsolutePath());
			}

			if (recodeTime < MIX_TIME && !isCancleSend) {
				tooShortPopuWindow();
				return;
			}

			removePopuWindow();

			if(!isCancleSend) {
				
				IMChatMessageDetail mVoicechatMessageDetail = IMChatMessageDetail
				.getGroupItemMessage(IMChatMessageDetail.TYPE_MSG_VOICE,IMChatMessageDetail.STATE_IM_SENDING, mGroupId);
				
				mVoicechatMessageDetail.setFilePath(getCurrentVoicePath()
						.getAbsolutePath());
				
				if (!isRecordAndSend) {
					// send
					uniqueId = VoiceHelper.getInstance().getDevice().sendInstanceMessage(mGroupId,
							null, getCurrentVoicePath().getAbsolutePath(), USER_DATA);
				} else {
					voiceMessage.remove(uniqueId);
				}
				
				try {
					mVoicechatMessageDetail.setMessageId(uniqueId);
					mVoicechatMessageDetail.setUserData(USER_DATA);
					mVoicechatMessageDetail.setFileExt("amr");
					CCPSqliteManager.getInstance().insertIMMessage(
							mVoicechatMessageDetail);
					
					
					
					notifyGroupDateChange(mVoicechatMessageDetail);
					
				} catch (SQLException e) {
					e.printStackTrace();
					
				}
				
			}

		}
		mGroudChatRecdBtn.setBackgroundResource(android.R.color.transparent);
		mGroudChatRecdBtn.setEnabled(true);
		RECODE_STATE = RECORD_NO;
		recodeTime = 0;
		isCancle = false;
	}
	
	
	boolean isPalye = false;
	AnimationDrawable vAnimDra = null;
	ImageView vAnimImage;
	
	private int vPlayState = 4;;
	private static final int TYPE_VOICE_PLAYING = 3;
	private static final int TYPE_VOICE_STOP = 4;
	
	void ViewPlayAnim(final ImageView iView , String path , int state) {
		releaseAnim();
		
		try {
			AnimationDrawable vAnim =  (AnimationDrawable) iView.getDrawable();
			// local downloaded file
			if(!TextUtils.isEmpty(path)&& isLocalAmr(path)) {
				if(vPlayState == TYPE_VOICE_PLAYING) {
					if(VoiceHelper.getInstance().getDevice() == null) {
						return ;
					}
					VoiceHelper.getInstance().getDevice().stopVoiceMsg();
					BaseApplication.getInstance().setSpeakerEnable(false);
					vAnim.stop();
					vPlayState = TYPE_VOICE_STOP;
				} else if (vPlayState == TYPE_VOICE_STOP) {
					if(VoiceHelper.getInstance().getDevice() == null) {
						return ;
					}
					BaseApplication.getInstance().setSpeakerEnable(true);
					VoiceHelper.getInstance().getDevice().playVoiceMsg(path);
					vAnim.start();
					vPlayState = TYPE_VOICE_PLAYING;
				}
			}
			
			vAnimDra = vAnim;
			vAnimImage = iView;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void releaseAnim() {
		if(vAnimDra != null ) {
			vAnimDra.stop();
			int id = 0;
			if(vAnimImage.getId() == R.id.voice_chat_recd_tv_l) {
				id = R.anim.voice_play_from;
			}else if (vAnimImage.getId() == R.id.voice_chat_recd_tv_r) {
				id = R.anim.voice_play_to;
			}
			vAnimImage.setImageResource(0);
			vAnimImage.setImageResource(id);
		}
	}
	
	boolean isLocalAmr(String url){
		if(new File(url).exists()) {
			return true ;
		} 
		Toast.makeText(this, R.string.toast_local_voice_file_does_not_exist, Toast.LENGTH_SHORT).show();
		return false;
	}
	
	private File takepicfile;
	private void takePicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takepicfile = CCPUtil.TackPicFilePath();
		if (takepicfile != null) {
			Uri uri = Uri.fromFile(takepicfile);
			if (uri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			}
		}
		startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		
	}
	
	
   private boolean promptRecordTime() {
	   if(computationTime == -1L) {
			computationTime = SystemClock.elapsedRealtime();
		}
		long period = SystemClock.elapsedRealtime() - computationTime;
		int duration ;
		if(period >= 50000L && period <= 60000L) {
			if(mRecordTipsToast == null) {
				vibrate(50L);
				duration = (int )((60000L - period) / 1000L) ;
				Log4Util.i(VoiceHelper.DEMO_TAG, "The remaining recording time :" + duration);
				mRecordTipsToast = Toast.makeText(getApplicationContext(), getString(R.string.chatting_rcd_time_limit, duration), Toast.LENGTH_SHORT);
			}
		}else {
			if(period < 60000L) {
				//sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
				return true;
			}
			
			return false;
			
		}
		
		if(mRecordTipsToast != null ) {
			duration = (int )((60000L - period) / 1000L) ;
			Log4Util.i(VoiceHelper.DEMO_TAG, "The remaining recording time :" + duration);
			mRecordTipsToast.setText(getString(R.string.chatting_rcd_time_limit, duration));
			mRecordTipsToast.show();
		}
		return true;
   }
    
}
