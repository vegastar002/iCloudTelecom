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
package com.voice.demo.voicecode;

import java.util.Random;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hisun.phone.core.voice.util.Log4Util;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.hust.wa.icloudtelecom.R;
import com.voice.demo.outboundmarketing.RestHelper;
import com.voice.demo.outboundmarketing.RestHelper.ERequestState;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;

public class VoiceVerificationCodeActivity extends CCPBaseActivity implements View.OnClickListener 
																		,RestHelper.onRestHelperListener{

	private static final int REQUEST_CODE_VERIFY_RESULT = 12;
	
	private EditText mNumber;
	private EditText mVeriCode;
	private Button mCodeBtn;
	private Button mSubmit;
	
	private ProgressDialog mDialog;
	
	private String mCurrentCode;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_voice_verificaode_activity);
		
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.app_title_veri_code)
				, null);
		
		initResourceRefs();
		
		RestHelper.getInstance().setOnRestHelperListener(this);
	}

	private void initResourceRefs() {
		mNumber = (EditText) findViewById(R.id.number_input);
		SharedPreferences sp = getSharedPreferences();
		mNumber.setText(sp.getString(CCPUtil.SP_KEY_PHONE_NUMBER, ""));
		
		mVeriCode = (EditText) findViewById(R.id.code_input);
		mCodeBtn = (Button) findViewById(R.id.btn_code);
		mSubmit = (Button) findViewById(R.id.code_submit);
		mSubmit.setEnabled(true);
		
		mCodeBtn.setOnClickListener(this);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_code: // request verificatio code ...
			if(TextUtils.isEmpty(mNumber.getText().toString())) {
				Toast.makeText(VoiceVerificationCodeActivity.this, "请输入号码", Toast.LENGTH_SHORT).show();
				return ;
			}
			mCurrentCode = getCharAndNumr(6);
			mDialog = new ProgressDialog(this);
			mDialog.setMessage(getString(R.string.dialog_verify_message_text));
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.show();
			
			new VoiceVerifyCodeAsyncTask().execute();
			break;
		case R.id.code_submit: //
			String phoneNumber = mNumber.getText().toString();
			if(TextUtils.isEmpty(phoneNumber)) {
				Toast.makeText(VoiceVerificationCodeActivity.this, "请输入号码", Toast.LENGTH_SHORT).show();
				return ;
			}
			getSharedPreferencesEditor().putString(CCPUtil.SP_KEY_PHONE_NUMBER, phoneNumber).commit();
			
			if(TextUtils.isEmpty(mVeriCode.getText().toString())) {
				
				Toast.makeText(VoiceVerificationCodeActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			Intent intent = new Intent(VoiceVerificationCodeActivity.this, ValidationStatusActivity.class);
			String verifyCode = mVeriCode.getText().toString();
			if(!TextUtils.isEmpty(mCurrentCode) &&  !TextUtils.isEmpty(verifyCode) && mCurrentCode.equals(verifyCode.toLowerCase())) {
				intent.putExtra("ERequest_State", ERequestState.Success);
				
			}else {
				
				intent.putExtra("ERequest_State", ERequestState.Failed);
			}
			startActivityForResult(intent, REQUEST_CODE_VERIFY_RESULT);
			break;
		default:
			break;
		}
	}
	
	public class VoiceVerifyCodeAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			
			RestHelper.getInstance().VoiceVerifyCode(mCurrentCode, "3", VoiceUtil.getStandardMDN(mNumber.getText().toString()) , "" , "");

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if(mDialog != null && mDialog.isShowing()) {
				mDialog.cancel();
			}
		}
	}

	@Override
	public void onLandingCAllsStatus(ERequestState reason, String callId) {

	}

	@Override
	public void onVoiceCode(ERequestState reason) {
		Log4Util.d(VoiceHelper.DEMO_TAG ,"[VoiceVerificationCodeActivity] onVoiceCode: reason .. " + reason);
		Message obtainMessage = mCodeHandler.obtainMessage(VoiceHelper.WHAT_ON_VERIFY_CODE);
		obtainMessage.obj = reason;
		mCodeHandler.sendMessage(obtainMessage);
	}
	
	// Get 6 numbers and the composition string.
	String getCharAndNumr(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字

			if ("char".equalsIgnoreCase(charOrNum)) // 字符串
			{
				int choice = /*random.nextInt(2) % 2 == 0 ? 65 : 97*/97; // 取得大写字母还是小写字母
				val += (char) (choice + random.nextInt(26));
			} else if ("num".equalsIgnoreCase(charOrNum)) // 数字
			{
				val += String.valueOf(random.nextInt(10));
			}
		}

		return val;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log4Util.d(VoiceHelper.DEMO_TAG ,"[VoiceVerificationCodeActivity] onActivityResult: requestCode=" + requestCode
				+ ", resultCode=" + resultCode + ", data=" + data);

		// If there's no data (because the user didn't select a number and
		// just hit BACK, for example), there's nothing to do.
		if (resultCode != RESULT_OK) {
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[VoiceVerificationCodeActivity] onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}
		
		int Operating = ValidationStatusActivity.OPERATING_GET_NEW_VERIFY  ;
		if(data.hasExtra("Operating")) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				Operating = extras.getInt("Operating");
			}
		}
		
		mVeriCode.getText().clear();
		if(Operating == ValidationStatusActivity.OPERATING_INPUT_AGAIN) {
			mVeriCode.requestFocus();
			requestFocusAndShowInputMode(mVeriCode);
		} else if (Operating == ValidationStatusActivity.OPERATING_GET_NEW_VERIFY) {
			mNumber.getText().clear();
			mNumber.requestFocus();
			requestFocusAndShowInputMode(mNumber);
		} else if (Operating == ValidationStatusActivity.OPERATING_VIEW_OVER) {
			finish();
		} else {
			mNumber.getText().clear();
			mVeriCode.getText().clear();
		}
		
	}
	
	
	void requestFocusAndShowInputMode(final EditText tv) {
		
		// Bring up the softkeyboard so the user can immediately enter msg. This
		// call won't do anything on devices with a hard keyboard.
		
		tv.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							InputMethodManager imm = (InputMethodManager)
		                    tv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		                    if(hasFocus){
		                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		                    }else{
		                    	imm.hideSoftInputFromWindow(tv.getWindowToken(),0);
		                    }
						}
					} ,300);
				}
			}
		});
	}
	
	
	private android.os.Handler mCodeHandler = new android.os.Handler() {


		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			ERequestState reason = ERequestState.Failed;
			// 获取通话ID
			if (msg.obj instanceof ERequestState) {
				reason = (ERequestState) msg.obj;
			}
			
			switch (msg.what) {
			//receive a new voice mail messages...
			case VoiceHelper.WHAT_ON_VERIFY_CODE:
				if(reason == ERequestState.Success) {
					Toast.makeText(VoiceVerificationCodeActivity.this, "获取验证码成功,请等待系统来电", Toast.LENGTH_SHORT).show();
					mCodeBtn.setEnabled(false);
					new CountDownTimer(30000,1000) { 

						@Override 
						public void onTick(long millisUntilFinished) { 
							mCodeBtn.setText(getString(R.string.str_verify_code_timer, millisUntilFinished/1000)) ;
						} 

						@Override 
						public void onFinish() { 
							mCodeBtn.setText(getString(R.string.str_get_verify_code));
							mCodeBtn.setEnabled(true);
						} 
					}.start();
				} else {
					Toast.makeText(VoiceVerificationCodeActivity.this, "获取验证码失败,请重试", Toast.LENGTH_SHORT).show();
					
				}
				break;

			default:
				break;
			}
			
		}
	};
}
