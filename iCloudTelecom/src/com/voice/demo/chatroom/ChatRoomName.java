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

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hust.wa.icloudtelecom.R;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.voip.CCPBaseActivity;

/**
 * Set ChatRoom Name ...
 *
 */
public class ChatRoomName extends CCPBaseActivity implements View.OnClickListener{
	public static final String CHATROOM_NAME = "ChatRoomName" ;
	public static final String CHATROOM_CREATOR = "ChatRoomCreator" ;
	
	private EditText mChatRoomName;
	private Button mSubmit ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_set_chatroom_name_activity);
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.app_title_chatroom_create)
				, null);
		
		
		initResourceRefs();
	}

	private void initResourceRefs() {
		
		mChatRoomName = (EditText) findViewById(R.id.chatroom_name);
		mChatRoomName.setSelection(mChatRoomName.getText().length());
		mChatRoomName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(mChatRoomName.getText().length() <= 0) {
					mSubmit.setEnabled(false);
				} else {
					mSubmit.setEnabled(true);
					
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		mSubmit = (Button) findViewById(R.id.create_chatroom_submit);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.create_chatroom_submit:
			Intent intent = new Intent(ChatRoomName.this, ChatRoomActivity.class);
			intent.putExtra(CHATROOM_NAME, mChatRoomName.getText().toString());
			intent.putExtra(CHATROOM_CREATOR, CCPConfig.VoIP_ID);
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}
}
