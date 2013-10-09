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


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisun.phone.core.voice.DeviceListener.Reason;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;

public class VoiceLoginActivity extends /*CCPBase*/Activity  implements View.OnClickListener{

	public static final int WHAT_INIT_ERROR = 0x201C;
	
	private EditText mMainAccount ;
	private EditText mMainToken ;
	private TextView mVoipAccount ;
	private Button mLogin;
	private ImageButton mVoipSelect;
	
	private TextView mVoipToken;
	private TextView mSubAccount;
	private TextView mSubToken;
	
	private String[] mVoipArray;
	
	private LinearLayout mLayoutIdList;
	
	private Dialog mLoginDialog;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_show_account);
		if (!CCPConfig.check()) {
			
			// Whether the configuration information integrity
			BaseApplication.getInstance().showToast(R.string.config_error_text);
		}

		//head input 
		mMainAccount = (EditText) findViewById(R.id.main_account);
		mMainToken = (EditText) findViewById(R.id.main_token);
		mVoipAccount = (TextView) findViewById(R.id.voip_account);
		mVoipAccount.setOnClickListener(this);
		//buttton
		mLogin = (Button) findViewById(R.id.btn_login);
//		mLogin.setEnabled(false);
		mVoipSelect = (ImageButton) findViewById(R.id.btn_select_voip);
		
		mLogin.setOnClickListener(this);
		mVoipSelect.setOnClickListener(this);
		
		//update subaccount info ..
		mSubAccount = (TextView) findViewById(R.id.sub_account);
		mSubToken = (TextView) findViewById(R.id.sub_token);
		mVoipToken = (TextView) findViewById(R.id.voip_token);
		
		mLayoutIdList = (LinearLayout) findViewById(R.id.id_list_ly);
		
		mVoipAccount.addTextChangedListener(new TextWatcher() {
			
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
					mLogin.setEnabled(false);
				} else {
					mLogin.setEnabled(true);
				}
			}
		});
		
		
		initConfigInfomation();
		
	}
	
	private void initConfigInfomation() {
		
		if(CCPConfig.Main_Account != null ) {
			mMainAccount.setText(CCPConfig.Main_Account);
		}
		if(CCPConfig.Main_Token != null ) {
			mMainToken.setText(CCPConfig.Main_Token);
		}
		
		if(CCPConfig.VoIP_ID_LIST != null ) {
			mVoipArray = CCPConfig.VoIP_ID_LIST.split(",");
			
			if(mVoipArray == null || mVoipArray.length == 0) {
				throw new IllegalArgumentException("Load the VOIP account information errors" +
						", configuration information can not be empty" + mVoipArray);
			}
		}
	}
	
	private AlertDialog mVoiceDialog;
	private ProgressDialog mDialog;
	
	@Override
	public void onClick(View v) {
		if(CCPUtil.isInvalidClick()) {
			return;
		}
		switch (v.getId()) {
		case R.id.btn_login://login
			mDialog = new ProgressDialog(VoiceLoginActivity.this);
			mDialog.setMessage(getString(R.string.dialog_message_text));
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setCancelable(false);
			mDialog.show();
			VoiceHelper.init(BaseApplication.getInstance(), helperHandler);
			break;
		case R.id.voip_account:
		case R.id.btn_select_voip:
			
			//Select voip account from list dialog shower ...
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle(R.string.str_select_voip_account)
			           .setItems(mVoipArray, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   
			               // The 'which' argument contains the index position
			               // of the selected item
		            	   mVoipAccount.setText(mVoipArray[which]);	 
		            	   CCPConfig.VoIP_ID = mVoipArray[which];
		            	   fillSubAccountInfo(which);
		            	   mVoiceDialog.dismiss();
			           }

			    });
			    mVoiceDialog = builder.create();
			    mVoiceDialog.show();
			    
			    break;
			    
		case R.id.voice_right_btn:
			
			// help msg 
            Dialog dialog = new AlertDialog.Builder(VoiceLoginActivity.this)
            .setTitle(R.string.help_title_text)
            .setIcon(R.drawable.navigation_bar_help_icon)
            .setMessage("请参阅www.cloopen.com")
            .setPositiveButton(R.string.dialog_btn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
            dialog.show();
            
            break;
            
		default:
			break;
		}
	}
	
	void fillSubAccountInfo(int index) {
		if(CCPConfig.Sub_Account != null ) {
			String[] split = CCPConfig.Sub_Account_LIST.split(",");
			mSubAccount.setText(getString(R.string.sub_account_title_text, split[index]));
			CCPConfig.Sub_Account = split[index];
		}
		if(CCPConfig.Sub_Token != null ) {
			String[] split = CCPConfig.Sub_Token_LIST.split(",");
			mSubToken.setText(getString(R.string.sub_token_title_text, split[index]));
			CCPConfig.Sub_Token = split[index];
		}
		if(CCPConfig.VoIP_PWD != null ) {
			String[] split = CCPConfig.VoIP_PWD_LIST.split(",");
			mVoipToken.setText(getString(R.string.voip_pwd_title_text, split[index]));
			CCPConfig.VoIP_PWD = split[index];
		}
		
		mLayoutIdList.setVisibility(View.VISIBLE);
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		helperHandler = null;
	}
	
	
	private Handler helperHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(mDialog != null  && mDialog.isShowing()){
				mDialog.dismiss();
			}
			try {
				if (msg.what == VoiceHelper.WHAT_ON_CONNECT) {
					if(!BaseApplication.getInstance().isConnect()) {
						startAction();
						BaseApplication.getInstance().setConnect(true);
					}
				} else if (msg.what == VoiceHelper.WHAT_ON_DISCONNECT) {
					// do nothing ...
					Reason reason = Reason.UNKNOWN ;
					if (msg.obj instanceof Bundle){
						reason = (Reason) msg.obj;
					}
					if(!BaseApplication.getInstance().isConnect()) {
						showInitErrDialog(reason.toString());
					}
				} else if (msg.what == WHAT_INIT_ERROR) {
					// 
					if(!BaseApplication.getInstance().isConnect()) {
						showInitErrDialog(null);
					}
				}else {
					Log4Util.d(VoiceHelper.DEMO_TAG , "Sorry , can't handle a message " + msg.what);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			super.handleMessage(msg);
		}

		
	};
	

	private void startAction() {
		if(VoiceHelper.getInstance().getDevice() == null) {
			return;
		}
		// Confirmation Information,then send to next activity ,.  
		if (!CCPConfig.check()) {
			BaseApplication.getInstance().showToast(R.string.config_error_text);
			return;
		}
		Intent intent = new Intent();
//		intent.setClass(VoiceLoginActivity.this, SelectVoiceActivity.class);
		intent.setClass(VoiceLoginActivity.this, LandingCallActivity.class);
		startActivity(intent);
		mLogin.setEnabled(true);
		BaseApplication.getInstance().initSQLiteManager();
		this.finish();
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
	
	void showInitErrDialog(String reason) {
		if(mLoginDialog != null && mLoginDialog.isShowing()) {
			return;
		}
		String message = null ;
		if(TextUtils.isEmpty(reason)) {
			message = getString(R.string.str_dialog_init_error_message);
		} else {
			message = getString(R.string.str_dialog_init_error_message) + "(" +reason+ ")";
		}
		mLoginDialog = new AlertDialog.Builder(this).setTitle(R.string.str_dialog_init_error_title)
				.setMessage(message)
				.setPositiveButton(R.string.dialog_btn, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mLoginDialog = null;
					}
				}).create();
		
		mLoginDialog.show();
	}
	
}
