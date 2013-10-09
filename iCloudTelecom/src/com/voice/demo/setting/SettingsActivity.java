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
package com.voice.demo.setting;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisun.phone.core.voice.Device.AudioMode;
import com.hisun.phone.core.voice.Device.AudioType;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.netmonitor.NetworkMonitoringActivity;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;

public class SettingsActivity extends CCPBaseActivity implements View.OnClickListener ,SlipButton.OnChangedListener{

	public static final String INTENT_P2P_ENABLED = "com.voice.demo.Intent.VoiceIntent.ACTION_P2P_ENABLED";
	
	private static final int REUQEST_KEY_SHOW_VIDEOSTREAMCONTROL = 0x1;
	private static final int REUQEST_KEY_SHOW_P2P = 0x2;
	
	private static final String P2P_SERVER_DEFAULT = "42.121.15.99";
	private static final int P2P_PORT_DEFAULT = 3478;
	private int[] titleNameArray = {
			R.string.str_setting_item_automanage,           // Automatic gain control(自动增益控制)
			R.string.str_setting_item_echocancel,           // Echo cancellation(回音消除)
			R.string.str_setting_item_silencerestrain,      // Silence suppression(静音抑制)
			R.string.str_setting_item_videostreamcontrol,   // Video control(视频码流控制)
			R.string.str_setting_item_netcheck,             // net check (网络检测)
			R.string.str_setting_item_ischunked,            // While recording and uploading pronunciation（语音边录边传）
			R.string.str_setting_item_p2p};                 // P2P 
	
	public static class CCPSetting {
		public static final int SETTING_AUTOMANAGE_ID = 1;
		public static final int SETTING_ECHOCANCEL_ID = 2;
		public static final int SETTING_SILENCERESTRAIN_ID = 3;
		public static final int SETTING_VIDEOSTREAMCONTROL_ID = 4;
		public static final int SETTING_NETCHECK_ID = 5;
		public static final int SETTING_ISCHUNKED_ID = 6;
		public static final int SETTING_P2P_ID = 7;
	}
	
	public static class SettingSubValue {
		public static final int SETTING_AUTOMANAGE_ID = 1;
		public static final int SETTING_ECHOCANCEL_ID = 2;
		public static final int SETTING_SILENCERESTRAIN_ID = 3;
		public static final int SETTING_VIDEOSTREAMCONTROL_ID = 4;
		public static final int SETTING_NETCHECK_ID = 5;
		public static final int SETTING_ISCHUNKED_ID = 6;
		public static final String SETTING_SUB_P2P = "P2P_SERVER";
	}
	public static class SettingSwitch {
		public static final int SETTING_AUTOMANAGE_ID = 1;
		public static final int SETTING_ECHOCANCEL_ID = 2;
		public static final int SETTING_SILENCERESTRAIN_ID = 3;
		public static final int SETTING_VIDEOSTREAMCONTROL_ID = 4;
		public static final int SETTING_NETCHECK_ID = 5;
		public static final int SETTING_ISCHUNKED_ID = 6;
		public static final String SETTING_SWITCH_P2P = "P2P_SERVER_SWITCH";
	}
	
	private HashMap<Integer, String>	mSettingArray;
	private HashMap<Integer, Boolean>   mSettingCheckArray;
	private LinearLayout mOtherLayout ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_setting_home_activity);

		handleTitleDisplay(getString(R.string.btn_title_back),
				getString(R.string.str_setting_head), null);
		
		
		mOtherLayout = (LinearLayout) findViewById(R.id.setting_other);
		
		initSettingData();
		
		initViewListView();
		
		registerReceiver(new String[]{INTENT_P2P_ENABLED});
	}
	
	/**
	 * 创建一个View 并设置id
	 *
	 * @param layoutid
	 * @param id
	 * @return
	 */
	private View createItemView(int layoutid, int id) {
		View view = getLayoutInflater().inflate(layoutid, null);
		view.setId(id);
		int index = id-1;
		TextView mNoticeTips  = (TextView) view.findViewById(R.id.notice_tips);
		if(id == CCPSetting.SETTING_AUTOMANAGE_ID) {
			mNoticeTips.setText(R.string.str_setting_head1);
			mNoticeTips.setVisibility(View.VISIBLE);
		} else if (id == CCPSetting.SETTING_NETCHECK_ID) {
			mNoticeTips.setText(R.string.str_setting_head2);
			mNoticeTips.setVisibility(View.VISIBLE);
		} else {
			mNoticeTips.setVisibility(View.GONE);
		}
		
		
		
		TextView viewName = (TextView) view.findViewById(R.id.item_textView);
		viewName.setText(getString(titleNameArray[index]));
		TextView viewSubName = (TextView) view.findViewById(R.id.item_sub_textView);
		//viewSubName.setText(getString(titleNameArray[index]));
		if(mSettingArray != null && !TextUtils.isEmpty(mSettingArray.get(id))) {
			viewSubName.setText(mSettingArray.get(id));
			viewSubName.setVisibility(View.VISIBLE);
		} else{
			viewSubName.setVisibility(View.GONE);
		}
		if(id != CCPSetting.SETTING_NETCHECK_ID) {
			
			SlipButton slipButton = (SlipButton) view.findViewById(R.id.switch_btn);
			if(mSettingCheckArray != null && mSettingCheckArray.get(id) != null) {
				slipButton.setChecked(mSettingCheckArray.get(id));
				slipButton.setVisibility(View.VISIBLE);
				//if(id != CCPSetting.SETTING_P2P_ID) {
					// just for demo test ..
					// The current P2P unstable, if you need to set up, 
					// the judge can be removed
					slipButton.SetOnChangedListener(getString(titleNameArray[index]), this);
				//}
			} else{
				slipButton.setVisibility(View.GONE);
			}
		}
		
		//if(id != CCPSetting.SETTING_P2P_ID) {
			// just for demo test ..
			// The current P2P unstable, if you need to set up, 
			// the judge can be removed
			view.setOnClickListener(this);
		//}
		
		return view;
	}
	
	/**
	 * 创建一条栏目
	 *
	 * @param id
	 */
	private void addItemSettingView(int id) {
		if(id != CCPSetting.SETTING_P2P_ID) {
			View view = createItemView(R.layout.list_setting_item, id);
			mOtherLayout.addView(view);
		}

	}
	
	/**
	 *
	 * 初始化设置列表;
	 */
	private void initViewListView() {
		for (int i = 1; i <= CCPSetting.SETTING_P2P_ID; i++) {
			addItemSettingView(i);
		}
	}
	
	
	void initSettingData(){
        if(mSettingArray == null ) {
        	mSettingArray = new HashMap<Integer, String>();
        }
        mSettingArray.clear();
        SharedPreferences sp = getSharedPreferences();;
        mSettingArray.put(CCPSetting.SETTING_AUTOMANAGE_ID, sp.getString("AUTOMANAGE_CONTENT_KEY", "kAgcAdaptiveDigital"));
        mSettingArray.put(CCPSetting.SETTING_ECHOCANCEL_ID, sp.getString("ECHOCANCELLED_CONTENT_KEY", "kEcAecm"));
        mSettingArray.put(CCPSetting.SETTING_SILENCERESTRAIN_ID, sp.getString("SILENCERESTRAIN_CONTENT_KEY", "kNsVeryHighSuppression"));
        mSettingArray.put(CCPSetting.SETTING_VIDEOSTREAMCONTROL_ID, sp.getString("VIDEOSTREAM_CONTENT_KEY", "150"));
        mSettingArray.put(CCPSetting.SETTING_P2P_ID, sp.getString(SettingSubValue.SETTING_SUB_P2P, "42.121.15.99:3478"));
    	
        initNewCheckState();
    }
	
	
	void initNewCheckState(){
		if(mSettingCheckArray == null ) {
			mSettingCheckArray = new HashMap<Integer, Boolean>();
		}
		mSettingCheckArray.clear();
		SharedPreferences sp = getSharedPreferences();;
		mSettingCheckArray.put(CCPSetting.SETTING_AUTOMANAGE_ID, sp.getBoolean("AUTOMANAGE_SWITCH_KEY", false));
		mSettingCheckArray.put(CCPSetting.SETTING_ECHOCANCEL_ID, sp.getBoolean("ECHOCANCELLED_SWITCH_KEY", true));
		mSettingCheckArray.put(CCPSetting.SETTING_SILENCERESTRAIN_ID, sp.getBoolean("SILENCERESTRAIN_SWITCH_KEY", true));
		mSettingCheckArray.put(CCPSetting.SETTING_VIDEOSTREAMCONTROL_ID, sp.getBoolean("VIDEOSTREAM_SWITCH_KEY", true));
		mSettingCheckArray.put(CCPSetting.SETTING_ISCHUNKED_ID, sp.getBoolean(CCPUtil.SP_KEY_VOICE_ISCHUNKED, true));
		mSettingCheckArray.put(CCPSetting.SETTING_P2P_ID, sp.getBoolean(SettingSwitch.SETTING_SWITCH_P2P, false));
	}
	
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		switch (id) {
		case CCPSetting.SETTING_AUTOMANAGE_ID: {
			if (mSettingCheckArray != null && mSettingCheckArray.get(id)) {
				// switch状态为on才允许用户进去{
				intent = new Intent(SettingsActivity.this, AutoManageSettingActivity.class);
				intent.putExtra("SettingType", AutoManageSettingActivity.SETTING_AUTOMANAGE);
				startActivityForResult(intent, AutoManageSettingActivity.SETTING_AUTOMANAGE);
			}
			 
			break;
		}
		case CCPSetting.SETTING_ECHOCANCEL_ID: {
			if (mSettingCheckArray != null && mSettingCheckArray.get(id)) {
				intent = new Intent(SettingsActivity.this, AutoManageSettingActivity.class);
				intent.putExtra("SettingType", AutoManageSettingActivity.SETTING_ECHOCANCELLED);
				startActivityForResult(intent, AutoManageSettingActivity.SETTING_ECHOCANCELLED);
			}
			break;
		}
		case CCPSetting.SETTING_SILENCERESTRAIN_ID: {
			if (mSettingCheckArray != null && mSettingCheckArray.get(id)) {
				intent = new Intent(SettingsActivity.this, AutoManageSettingActivity.class);
				intent.putExtra("SettingType", AutoManageSettingActivity.SETTING_SILENCERESTRAIN);
				startActivityForResult(intent, AutoManageSettingActivity.SETTING_SILENCERESTRAIN);
			}
			break;
		}
		case CCPSetting.SETTING_VIDEOSTREAMCONTROL_ID:{
			if (mSettingCheckArray != null && mSettingCheckArray.get(id)) {
				//视频码流控制 { if (listItemAdapter.getIsSelected().get(3))
				//如果用户把视频码流的switch关掉，则不允许输入 { showEditDialog(); } }
				showEditDialog(REUQEST_KEY_SHOW_VIDEOSTREAMCONTROL , InputType.TYPE_CLASS_NUMBER
						, getString(R.string.str_setting_item_videostreamcontrol), getString(R.string.str_setting_dialog_input_vide_rule));
			}
			break;
		}
		case CCPSetting.SETTING_NETCHECK_ID:
			startActivity(new Intent(SettingsActivity.this, NetworkMonitoringActivity.class));
			break;
		case CCPSetting.SETTING_ISCHUNKED_ID:
			break;
		case CCPSetting.SETTING_P2P_ID:
			/*if (mSettingCheckArray != null && mSettingCheckArray.get(id)) {
				//视频码流控制 { if (listItemAdapter.getIsSelected().get(3))
				//如果用户把视频码流的switch关掉，则不允许输入 { showEditDialog(); } }
				showEditDialog(REUQEST_KEY_SHOW_P2P, InputType.TYPE_CLASS_TEXT
						, getString(R.string.str_setting_item_p2p), getString(R.string.str_setting_dialog_input_p2p_rule));
			}*/
			break;
		}
	}
	
	
	void showEditDialog(final int requestKey , int inputType , String title , String message)
	{		
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(/*R.string.str_setting_item_videostreamcontrol*/title);
		View view = getLayoutInflater().inflate(R.layout.dialog_edittext_and_textview, null);
		TextView dialogText = (TextView)view.findViewById(R.id.notice_tips);
		if(!TextUtils.isEmpty(message)){
			dialogText.setVisibility(View.VISIBLE);
			dialogText.setText(message);
		} else {
			dialogText.setVisibility(View.GONE);
		}
		final EditText mInvitEt = (EditText) view.findViewById(R.id.video_stream_edit);
		mInvitEt.setInputType(inputType);
		if(inputType == InputType.TYPE_CLASS_NUMBER) {
			mInvitEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
		}
		
		// Monitor whether focus pop-up or hidden input....
		mInvitEt.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				if (hasFocus) {
					new Handler().postDelayed(new Runnable() {
						public void run() {
							InputMethodManager imm = (InputMethodManager) mInvitEt
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);

							if (hasFocus) {
								imm.toggleSoftInput(0,
										InputMethodManager.HIDE_NOT_ALWAYS);
							} else {
								imm.hideSoftInputFromWindow(
										mInvitEt.getWindowToken(), 0);
							}
						}
					}, 300);
				}
			}
		});
		
		builder.setPositiveButton(R.string.dialog_btn,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String mPhoneNumber = mInvitEt.getText().toString();
				handleEditDialogOkEvent(requestKey, mPhoneNumber, true);
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(R.string.dialog_cancle_btn,
				new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}

		});
		
		builder.setView(view);
		dialog = builder.create();
		dialog.show();
		dialog.setCanceledOnTouchOutside(false);
	}
	
	
	@Override
	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {
		super.handleEditDialogOkEvent(requestKey, editText, checked);
		Editor editor = getSharedPreferences().edit();
		if(REUQEST_KEY_SHOW_VIDEOSTREAMCONTROL == requestKey){
			
			int bitrates = 150;
			if(!TextUtils.isEmpty(editText)) {
				editor.putString("VIDEOSTREAM_CONTENT_KEY", editText);
		        
				bitrates = Integer.valueOf(editText).intValue();
				VoiceHelper.getInstance().getDevice().setVideoBitRates(bitrates);
			}
			mSettingArray.put(CCPSetting.SETTING_VIDEOSTREAMCONTROL_ID, bitrates + "");
			changeSubView(CCPSetting.SETTING_VIDEOSTREAMCONTROL_ID);
		} else if (REUQEST_KEY_SHOW_P2P == requestKey) {
			mSettingArray.put(CCPSetting.SETTING_P2P_ID, editText);
			editor.putString(SettingSubValue.SETTING_SUB_P2P, editText);
			changeSubView(CCPSetting.SETTING_P2P_ID);
		}
		editor.commit();
	}

	@Override
	public void onChanged(String strname, boolean checkState) {
		SharedPreferences sp = getSharedPreferences();
        Editor editor = sp.edit();
        
        AudioType audioType = AudioType.AUDIO_AGC;
        AudioMode audioMode = AudioMode.kNsDefault;
        
		if (strname.equals(getString(titleNameArray[3])))
		{
			editor.putBoolean("VIDEOSTREAM_SWITCH_KEY", checkState);
			int bitrates = 150;
			
			if (checkState)
			{
				String bitString = sp.getString("VIDEOSTREAM_CONTENT_KEY", "150");
				bitrates = Integer.valueOf(bitString).intValue();
			}

			VoiceHelper.getInstance().getDevice().setVideoBitRates(bitrates);
		} else if (getString(titleNameArray[5]).equals(strname)) {
			editor.putBoolean(CCPUtil.SP_KEY_VOICE_ISCHUNKED, checkState);
		} else if (getString(titleNameArray[6]).equals(strname)) {
			if(checkState) {
				
				// Set to 1 to enable p2p
				// Set to 0 to disable the P2P
				String p2pServerPort = getSharedPreferences().getString(SettingSubValue.SETTING_SUB_P2P, P2P_SERVER_DEFAULT +":"+ P2P_PORT_DEFAULT);
				String[] split = p2pServerPort.split(":");
				if(split != null && split.length > 1){
					/*int port = P2P_PORT_DEFAULT;
					try {
						port = Integer.parseInt(split[1]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}*/
					
					//VoiceApplication.getInstance().getVoiceHelper().getDevice().setStunServer(split[0], port);
				} else if (split != null && split.length ==1) {
					//VoiceApplication.getInstance().getVoiceHelper().getDevice().setStunServer(split[0]);
				}
				//VoiceApplication.getInstance().getVoiceHelper().getDevice().setFirewallPolicy(1);
			} else {
				//VoiceApplication.getInstance().getVoiceHelper().getDevice().setFirewallPolicy(0);
			}
			editor.putBoolean(SettingSwitch.SETTING_SWITCH_P2P, checkState);
		}else {
			if (strname.equals(getString(titleNameArray[0])))
			{
				editor.putBoolean("AUTOMANAGE_SWITCH_KEY", checkState);
				audioType = AudioType.AUDIO_AGC;
				audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_AGC, sp.getInt("AUTOMANAGE_INDEX_KEY", 3));
			}
			else if (strname.equals(getString(titleNameArray[1])))
			{
				editor.putBoolean("ECHOCANCELLED_SWITCH_KEY", checkState);
				audioType = AudioType.AUDIO_EC;
				audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_EC, sp.getInt("ECHOCANCELLED_INDEX_KEY", 4));
			}
			else if (strname.equals(getString(titleNameArray[2])))
			{
				editor.putBoolean("SILENCERESTRAIN_SWITCH_KEY", checkState);
				audioType = AudioType.AUDIO_NS;
				audioMode = CCPUtil.getAudioMode(AudioType.AUDIO_NS, sp.getInt("SILENCERESTRAIN_INDEX_KEY", 6));
			}
			
			VoiceHelper.getInstance().getDevice().setAudioConfigEnabled(audioType, checkState, audioMode);
		}
		
		editor.commit();
		initNewCheckState();
	}
	
	
	void changeSubView(int id ) {
		View view = findViewById(id);
		TextView suTextView = (TextView) view.findViewById(R.id.item_sub_textView);
		suTextView.setText(mSettingArray.get(id));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log4Util.d(VoiceHelper.DEMO_TAG ,"[IMChatActivity] onActivityResult: requestCode=" + requestCode
				+ ", resultCode=" + resultCode + ", data=" + data);

		// If there's no data (because the user didn't select a file or take pic  and
		// just hit BACK, for example), there's nothing to do.
		if (resultCode != RESULT_OK) {
			Log4Util.d(VoiceHelper.DEMO_TAG ,"[GroupChatActivity] onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}
		switch (requestCode) {
		case AutoManageSettingActivity.SETTING_AUTOMANAGE:
			mSettingArray.put(CCPSetting.SETTING_AUTOMANAGE_ID, getSharedPreferences().getString("AUTOMANAGE_CONTENT_KEY", "kAgcAdaptiveDigital"));
			changeSubView(CCPSetting.SETTING_AUTOMANAGE_ID);
			break;
		case AutoManageSettingActivity.SETTING_ECHOCANCELLED:
			mSettingArray.put(CCPSetting.SETTING_ECHOCANCEL_ID, getSharedPreferences().getString("ECHOCANCELLED_CONTENT_KEY", "kAgcAdaptiveDigital"));
			changeSubView(CCPSetting.SETTING_ECHOCANCEL_ID);
			break;
		case AutoManageSettingActivity.SETTING_SILENCERESTRAIN:
			mSettingArray.put(CCPSetting.SETTING_SILENCERESTRAIN_ID, getSharedPreferences().getString("SILENCERESTRAIN_CONTENT_KEY", "kAgcAdaptiveDigital"));
			changeSubView(CCPSetting.SETTING_SILENCERESTRAIN_ID);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if(intent.getAction().equals(INTENT_P2P_ENABLED)) {
			BaseApplication.getInstance().showToast(R.string.dialog_p2p_mesage);
		}
	}
}
