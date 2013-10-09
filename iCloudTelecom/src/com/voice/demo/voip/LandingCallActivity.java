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
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;

public class LandingCallActivity extends CCPBaseActivity implements OnClickListener{

	private static final int VOIP_CALL_TYPE_BACK_TALK = 203;
	private static final int VOIP_CALL_TYPE_DIRECT_TALK = 204;
	
	private EditText mPhoneNum;
	private EditText mOutPhoneNumEt;
	
	private Button mDirectCallBtn;
	private Button mCallBackBtn;
	
	private ImageView mReadIcon;
	private TextView mReadTips;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_netphone_landing_call_activity);
		
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.app_title_landingcall)
				, null);
		
		initResourceRefs();
	}

	private void initResourceRefs() {
		mPhoneNum = (EditText) findViewById(R.id.phone_num_input);
		SharedPreferences sp = getSharedPreferences();
		mPhoneNum.setText(sp.getString(CCPUtil.SP_KEY_PHONE_NUMBER, ""));
		mPhoneNum.setSelection(mPhoneNum.length());
		
		mOutPhoneNumEt = (EditText) findViewById(R.id.number_input);
		mDirectCallBtn = (Button) findViewById(R.id.netphone_direct_call);
		mCallBackBtn = (Button) findViewById(R.id.netphone_call_back);
		
		mReadIcon = (ImageView) findViewById(R.id.ready_icon);
		mReadTips = (TextView) findViewById(R.id.ready_tips);
		
		mDirectCallBtn.setOnClickListener(this);
		mCallBackBtn.setOnClickListener(this);
		mPhoneNum.addTextChangedListener(localTextWatcher);
		
		mOutPhoneNumEt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(TextUtils.isEmpty(s.toString())) {
					mDirectCallBtn.setEnabled(false);
					mCallBackBtn.setEnabled(false);
					mReadIcon.setImageResource(R.drawable.status_quit);
					mReadTips.setText("�������뱾������ͺ���������ܽ��лز�����");
				} else {
					mDirectCallBtn.setEnabled(true);
					mReadIcon.setImageResource(R.drawable.status_speaking);
					if(TextUtils.isEmpty(mPhoneNum.getText().toString())) {
						mCallBackBtn.setEnabled(false);
						mReadTips.setText("���Բ�������ֱ��,���뱾�����벦������ز�");
					} else {
						mCallBackBtn.setEnabled(true);
						mReadTips.setText("������׼������,���Բ�����ص绰");
					}
				}
				
				
			}
		});
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.netphone_direct_call: // Direct-dial
			//�����û�������Ϣ����תҳ��
			CCPConfig.Src_phone = mPhoneNum.getText().toString();
			VoiceHelper.getInstance().getDevice().setSelfPhoneNumber(CCPConfig.Src_phone);
			startVoipCall(VOIP_CALL_TYPE_DIRECT_TALK);
			break;
		case R.id.netphone_call_back: // Call back ...
			//�����û�������Ϣ����תҳ��
			CCPConfig.Src_phone = mPhoneNum.getText().toString();
			VoiceHelper.getInstance().getDevice().setSelfPhoneNumber(CCPConfig.Src_phone);
			startVoipCall(VOIP_CALL_TYPE_BACK_TALK);
			
			break;

		default:
			break;
		}
		Context ctx = LandingCallActivity.this;
		SharedPreferences sp = ctx.getSharedPreferences(CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("KEY_MYSELF_PHONENUMBER", CCPConfig.Src_phone);
		editor.commit();
		
		if(!TextUtils.isEmpty(CCPConfig.Src_phone)) {
			getSharedPreferencesEditor().putString(CCPUtil.SP_KEY_PHONE_NUMBER, CCPConfig.Src_phone).commit();
		}
	}
	
	void startVoipCall(int mCallType) {

		String phoneStr = mOutPhoneNumEt.getText().toString();
		
		if (TextUtils.isEmpty(phoneStr)) {
			BaseApplication.getInstance().showToast(R.string.edit_input_empty);
			return;
		}

		Intent intent = new Intent(this, CallOutActivity.class);
		// ��Ҫ����ģʽ������Ӧ����
		switch (mCallType) {
		case VOIP_CALL_TYPE_DIRECT_TALK:
			// ֱ���绰
			if (!phoneStr.startsWith("0") && !phoneStr.startsWith("1")) {
				// �ж�����Ϸ���
				BaseApplication.getInstance().showToast(
						getString(R.string.network_dial_number_format));
				return;
			}
			intent.putExtra(BaseApplication.VALUE_DIAL_VOIP_INPUT, phoneStr);
			intent.putExtra(BaseApplication.VALUE_DIAL_MODE,
					BaseApplication.VALUE_DIAL_MODE_DIRECT);
			startActivity(intent);
			break;
		case VOIP_CALL_TYPE_BACK_TALK:
			// �ز��绰
			if (!phoneStr.startsWith("0") && !phoneStr.startsWith("1")) {
				// �ж�����Ϸ���
				BaseApplication.getInstance().showToast(
						getString(R.string.network_dial_number_format));
				return;
			}
			if (CCPConfig.Src_phone == null || CCPConfig.Src_phone.equals("")) {
				// �жϱ��������Ƿ���ȷ
				BaseApplication.getInstance().showToast(
						getString(R.string.src_phone_empty));
				return;
			}
			intent.putExtra(BaseApplication.VALUE_DIAL_VOIP_INPUT, phoneStr);
			intent.putExtra(BaseApplication.VALUE_DIAL_MODE,
					BaseApplication.VALUE_DIAL_MODE_BACK);
			startActivity(intent);

			break;
		}
	}
	
	TextWatcher localTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if(TextUtils.isEmpty(s.toString()) || TextUtils.isEmpty(mOutPhoneNumEt.getText().toString())) {
				mCallBackBtn.setEnabled(false);
				if(TextUtils.isEmpty(mOutPhoneNumEt.getText().toString())){
					mReadIcon.setImageResource(R.drawable.status_quit);
					mReadTips.setText("�������뱾������ͺ���������ܽ�����ص绰����");
				} else {
					mReadIcon.setImageResource(R.drawable.status_speaking);
					mReadTips.setText("���Բ�������ֱ��,���뱾�����벦������ز�");
				}
			} else {
				mCallBackBtn.setEnabled(true);
				mReadIcon.setImageResource(R.drawable.status_speaking);
				mReadTips.setText("������׼������,���Բ�����ص绰");
			}
		}
		
	};
}
