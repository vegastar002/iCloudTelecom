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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.chatroom.Chatroom;
import com.hust.wa.icloudtelecom.R;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;

/**
 * ChatRoom Converstion list ...
 *
 */
public class ChatRoomConversation extends CCPBaseActivity implements View.OnClickListener ,OnItemClickListener{

	private LinearLayout mChatRoomEmpty;
	private ListView mChatRoomLv;
	
	// voip helper
	private ChatRoomConvAdapter mRoomAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_chatroom_conversation_activity);
		
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.app_title_chatroom_conv)
				, getString(R.string.str_button_create_chatroom));
		
		initResourceRefs();
		
		// init voice helper ..
		if(VoiceHelper.getInstance().getDevice() == null ) {
			CCPUtil.registDialog(this) ;
			//finish();
		} else {
			VoiceHelper.getInstance().setHandler(mChatRoomHandler);
			
			registerReceiver(new String[]{VoiceHelper.INTENT_CHAT_ROOM_RECIVE});
			
			VoiceHelper.getInstance().getDevice().queryChatrooms(CCPConfig.App_ID, null);
			
		}
		
		
		registerReceiver(new String[]{VoiceHelper.INTENT_CHAT_ROOM_DISMISS});
		
	}
	
	@Override
	protected void onResume() {
		VoiceHelper.getInstance().setHandler(mChatRoomHandler);
		super.onResume();
	}

	private void initResourceRefs() {
		mChatRoomLv = (ListView) findViewById(R.id.chatroom_list);
		mChatRoomLv.setOnItemClickListener(this);
		findViewById(R.id.begin_create_chatroom).setOnClickListener(this);
		mChatRoomEmpty = (LinearLayout) findViewById(R.id.chatroom_empty);
		initListView();
	}

	private void initListView() {
		if(chatRoomList != null && !chatRoomList.isEmpty()) {
			mRoomAdapter = new ChatRoomConvAdapter(this, chatRoomList);
			mChatRoomLv.setAdapter(mRoomAdapter);
			mChatRoomLv.setVisibility(View.VISIBLE);
			mChatRoomEmpty.setVisibility(View.GONE);
		} else {
			mChatRoomEmpty.setVisibility(View.VISIBLE);
			mChatRoomLv.setVisibility(View.GONE);
		}
	}

	@Override
	protected void handleTitleAction(int direction) {
		if(direction == TITLE_RIGHT_ACTION) {
			startActivity(new Intent(ChatRoomConversation.this, ChatRoomName.class));
		} else {
			
			super.handleTitleAction(direction);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.begin_create_chatroom:
			handleTitleAction(TITLE_RIGHT_ACTION);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mRoomAdapter != null ) {
			
			mRoomAdapter = null ;
		}
		
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mRoomAdapter != null && mRoomAdapter.getItem(position) != null ) {
			Chatroom chatRoomInfo = mRoomAdapter.getItem(position);
			Intent intent = new Intent(ChatRoomConversation.this , ChatRoomActivity.class);
			intent.putExtra(Device.CONFNO, chatRoomInfo.getRoomNo());
			intent.putExtra(ChatRoomName.CHATROOM_CREATOR, chatRoomInfo.getCreator());
			if(TextUtils.isEmpty(chatRoomInfo.getRoomName())) {
				if(TextUtils.isEmpty(chatRoomInfo.getCreator())){
					return;
				}
				intent.putExtra(ChatRoomName.CHATROOM_NAME, getString(R.string.app_title_default 
						, chatRoomInfo.getCreator().substring(chatRoomInfo.getCreator().length() - 3, chatRoomInfo.getCreator().length())));
			} else {
				intent.putExtra(ChatRoomName.CHATROOM_NAME, chatRoomInfo.getRoomName());
			}
			startActivity(intent) ;
		}
		
	}
	
	class ChatRoomConvAdapter extends ArrayAdapter<Chatroom> {

		LayoutInflater mInflater;
		
		public ChatRoomConvAdapter(Context context, ArrayList<Chatroom> objects) {
			super(context, 0, objects);
			
			mInflater = getLayoutInflater();
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ChatRoomHolder holder;
			if (convertView == null|| convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.list_item_chatroom, null);
				holder = new ChatRoomHolder();
				
				holder.chatRoomName = (TextView) convertView.findViewById(R.id.chatroom_name);
				holder.chatRoomTips = (TextView) convertView.findViewById(R.id.chatroom_tips);
			} else {
				holder = (ChatRoomHolder) convertView.getTag();
			}
			
			try {
				// do ..
				Chatroom item = getItem(position);
				if(item != null ) {
					holder.chatRoomName.setText(item.getRoomName());
					int resourceId ;
					if("8".equals(item.getJoined())) {
						//
						resourceId = R.string.str_chatroom_list_join_full;
					} else {
						
						resourceId = R.string.str_chatroom_list_join_unfull;
					}
					holder.chatRoomTips.setText(getString(resourceId, item.getJoined() , item.getCreator()));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return convertView;
		}
		
		
		class ChatRoomHolder {
			TextView chatRoomName;
			TextView chatRoomTips;
		}
		
	}

	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if (intent != null && VoiceHelper.INTENT_CHAT_ROOM_RECIVE.equals(intent.getAction())) {
			if(intent.hasExtra("ChatRoomInfo")) {
				Chatroom cRoomInfo = (Chatroom) intent.getSerializableExtra("ChatRoomInfo");
				if(cRoomInfo != null) {
					if(chatRoomList == null) {
						chatRoomList = new ArrayList<Chatroom>();
					}
					chatRoomList.add(cRoomInfo);
					initListView();
				} 
			} else {
				VoiceHelper.getInstance().getDevice().queryChatrooms(CCPConfig.App_ID, null);
			}
		} else if(intent.getAction().equals(VoiceHelper.INTENT_CHAT_ROOM_DISMISS)) {
			if(intent.hasExtra("roomNo")) {
				String roomNo = intent.getStringExtra("roomNo");
				if(!TextUtils.isEmpty(roomNo) && chatRoomList != null ) {
					for(Chatroom chatroom : chatRoomList){
						if(chatroom.getRoomNo().equals(roomNo)) {
							chatRoomList.remove(chatroom);
							break;
						}
					}
					
					initListView();
					
				}
			}
		}
		
	}
	
	// 回调handler，更新界面显示
	private ArrayList<Chatroom> chatRoomList;
	private android.os.Handler mChatRoomHandler = new android.os.Handler() {


		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Bundle b = null;
			int reason = 0;
			// 获取通话ID
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(Device.REASON);
			}
			
			switch (msg.what) {
			//receive a new voice mail messages...
			case VoiceHelper.WHAT_ON_CHATROOM_LIST:
				if(reason == 0 ) {
					chatRoomList = (ArrayList<Chatroom>) b.getSerializable(Device.CHATROOM_LIST);
					initListView();
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.toast_get_chatroom_list_failed, reason , 0), Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
			
		}
	};

	
}
