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
 */package com.voice.demo.ExConsultation;



import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.voice.demo.ExConsultation.model.Expert;
import com.voice.demo.ExConsultation.model.ServiceNum;
import com.voice.demo.group.utils.ITask;
import com.voice.demo.outboundmarketing.RestHelper.ERequestState;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.tools.CheckUtil;
import com.voice.demo.voip.CallOutActivity;

/**
 * 
 * @author Jorstin Chan
 * @version 3.3
 */
public class ExpertConmuiActivity extends ExpertBaseActivity implements OnClickListener {

	private Button mCallNormal;//400拨打
	private Button mCallFree; //免费呼叫
	
	private EditText mPhoneNumber;
	private Expert expert;
	private int callType=0;
	private String sipphone;
	
	@Override
	protected void handleActionLockExpert(ERequestState reason) {
		super.handleActionLockExpert(reason);
		closeConnectionProgress();
		if(reason == ERequestState.Success) {
			//finish();
        	String number = (String) BaseApplication.getInstance().getSettingParams("ivrphone");
			if (callType == R.id.xh_call_normal) {
				startCalling(number);
			} else if (callType == R.id.xh_call_free) {
				Intent intent = new Intent(this, CallOutActivity.class);
				intent.putExtra(BaseApplication.VALUE_DIAL_VOIP_INPUT, sipphone);
				intent.putExtra(BaseApplication.VALUE_DIAL_MODE,BaseApplication.VALUE_DIAL_MODE_FREE);
				startActivity(intent);
			} 	
		} else {
			BaseApplication.getInstance().showToast(R.string.request_failed);
		}
	}
	
	@Override
	protected void handleGet400ServerPort(ERequestState reason,
			ServiceNum xConfig) {
		super.handleGet400ServerPort(reason, xConfig);
		if(reason == ERequestState.Success) {
			BaseApplication.getInstance().saveSettingParams("ivrphone", xConfig.getIvrphone());
            BaseApplication.getInstance().saveSettingParams("voipphone", xConfig.getVoipphone());
            
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_conmu_layer_xh);
		mPhoneNumber = (EditText) findViewById(R.id.xh_input_number);
		expert = (Expert) BaseApplication.getInstance().getData("lawyer");
		BaseApplication.getInstance().removeData("lawyer");
		mCallNormal = (Button) findViewById(R.id.xh_call_normal);
		mCallFree = (Button) findViewById(R.id.xh_call_free);
		mCallNormal.setOnClickListener(this);
		mCallFree.setOnClickListener(this);

		String phoneNumber = getSharedPreferences().getString(CCPUtil.SP_KEY_PHONE_NUMBER, "");
		mPhoneNumber.setText(phoneNumber);
		mPhoneNumber.setSelection(mPhoneNumber.length());
		
		TextView mLawyerFree = (TextView) findViewById(R.id.lawyer_free);
		Spanned fromHtml = Html.fromHtml(getString(R.string.str_the_lawyer_fee));
		mLawyerFree.setText(fromHtml);
		
		
		TextView xh_lawyer_console = (TextView)findViewById(R.id.xh_lawyer_console_title);
		if(expert != null){
			xh_lawyer_console.setText(getString(R.string.str_xh_call_with, expert.getName()));
		}else{
			xh_lawyer_console.setText("未知");
		}
		if(TextUtils.isEmpty(BaseApplication.getInstance().getSettingParams("ivrphone"))){
			ITask iTask = new ITask(ExpertManagerHelper.KEY_SERVICE_NUM);
			addTask(iTask);
			
		}
	}
	
	@Override
	protected void handleTaskBackGround(ITask iTask) {
		super.handleTaskBackGround(iTask);
		int key = iTask.getKey();
		if(key == ExpertManagerHelper.KEY_SERVICE_NUM) {
			ExpertManagerHelper.getInstance().getServiceNum();
		} else if (key == ExpertManagerHelper.KEY_LOCK_EXPERT) {
			String lid = (String) iTask.getTaskParameters("lid");
			String srcnumber = (String) iTask.getTaskParameters("srcnumber");
			ExpertManagerHelper.getInstance().lockExpert(lid, srcnumber);
		}
	} 
	

	@Override
	public void onClick(View view) {
		if(!TextUtils.isEmpty(mPhoneNumber.getText().toString())) {
			getSharedPreferencesEditor().putString(CCPUtil.SP_KEY_PHONE_NUMBER, mPhoneNumber.getText().toString()).commit();
		}
		switch (view.getId() ) {
		case R.id.xh_call_normal:
			callType = R.id.xh_call_normal;
			String srcphone = mPhoneNumber.getText().toString();
			if(TextUtils.isEmpty(srcphone)){
				BaseApplication.getInstance().showToast("请输入本机号码");
				return;
			}
		    if(!CheckUtil.checkMDN(srcphone)){
		    	BaseApplication.getInstance().showToast("不是有效的电话号码！");
		    	return;
		    }
		    if(TextUtils.isEmpty(BaseApplication.getInstance().getSettingParams("ivrphone"))){
		    	BaseApplication.getInstance().showToast("未获取到400服务号码，请检查网络后重试！");
		    	return;
		    }
			if(expert != null && !TextUtils.isEmpty(expert.getId()) ){
				doActionLockLawer();
			}else{
				BaseApplication.getInstance().showToast("编号已经失效！");
			}
			break;
		case R.id.xh_call_free:
			callType = R.id.xh_call_free;
			sipphone = BaseApplication.getInstance().getSettingParams(
					"voipphone");
			if (TextUtils.isEmpty(sipphone)) {
				BaseApplication.getInstance().showToast(
						getString(R.string.Toast_market_phone_empty_text));
				return;
			}
			/*if (TextUtils.isEmpty(mPhoneNumber.getText().toString())) {
				BaseApplication.getInstance().showToast("请输入本机号码");
				return;
			}*/
			if (expert != null && !TextUtils.isEmpty(expert.getId())) {
				doActionLockLawer();

			} else {
				BaseApplication.getInstance().showToast("编号已经失效！");
			}
			break;

		default:
			break;
		}
	}

	private void doActionLockLawer() {
		showConnectionProgress(getString(R.string.progress_text_2));
		ITask iTask = new ITask(ExpertManagerHelper.KEY_LOCK_EXPERT);
		iTask.setTaskParameters("lid", expert.getId());
		if(callType == R.id.xh_call_free) {
			iTask.setTaskParameters("srcnumber", CCPConfig.VoIP_ID);
		} else {
			iTask.setTaskParameters("srcnumber", mPhoneNumber.getText().toString());
		}
		addTask(iTask);
	}
	

}
