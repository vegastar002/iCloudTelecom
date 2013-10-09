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
package com.voice.demo.interphone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.wa.icloudtelecom.R;
import com.voice.demo.group.GroupBaseActivity;
import com.voice.demo.group.GroupChatActivity;
import com.voice.demo.group.GroupDetailActivity;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.SelectVoiceActivity;
import com.voice.demo.voip.VoIPCallActivity;

/**
 * invited to select contact interface
 * 1, select the VoIP network telephone contact.
 * 2, select the video call contacts.
 * 3, select the voice message recipient
 * 4, select the voice group chat participants
 *
 */
public class InviteInterPhoneActivity extends CCPBaseActivity implements View.OnClickListener ,OnItemClickListener{

	//public static final int CREATE_TO_DELAY_VOICE = 0x0;
	public static final int CREATE_TO_INTER_PHONE_VOICE = 0x1;
	public static final int CREATE_TO_VOIP_CALL = 0x2 ;
	public static final int CREATE_TO_VIDEO_CALL = 0x3 ;
	public static final int CREATE_TO_IM_TALK = 0x4 ;
	
	public static final int SIGLE_CHOICE = 0x5 ;
	public static final int MULTIPLE_CHOICE = 0x6 ;
	
	private ListView InterMemList;
	private Button mJoinInter;
	private TextView mJoinTips;
	private TextView mHeadTips;
	
	private InviteAdapter mInviteAdapter;
	private List<String> arr;
	
	private int mVoiceType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_invite_member_activity);
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.str_title_select_join_member)
				, getString(R.string.str_add_number_to_list));
		
		
		InterMemList = (ListView) findViewById(R.id.interphone_member_list);
		mJoinTips = (TextView) findViewById(R.id.join_select_tips);
		mHeadTips = (TextView) findViewById(R.id.notice_tips);
		
		// Allows the user to input a number is added to the list of choices .
		mJoinInter = (Button) findViewById(R.id.id_confirm);
		mJoinInter.setVisibility(View.VISIBLE);
		mJoinInter.setOnClickListener(this);
		mJoinInter.setEnabled(false);
		
		InterMemList.setOnItemClickListener(this);
		String[] mInviterMember = null;
		if( CCPConfig.VoIP_ID_LIST != null) {
			mInviterMember = CCPConfig.VoIP_ID_LIST.split(",");
			if(mInviterMember == null || mInviterMember.length == 0) {
				throw new IllegalArgumentException("Load the VOIP account information errors" +
						", configuration information can not be empty" + mInviterMember);
			}
		}
		arr = Arrays.asList(mInviterMember);
		arr= CCPUtil.removeString(arr, CCPConfig.VoIP_ID);
		mInviteAdapter = new InviteAdapter(getApplicationContext(), arr);
		InterMemList.setAdapter(mInviteAdapter);
		
		initialize(savedInstanceState);
		
	}

	private void initialize(Bundle savedInstanceState) {
		// Read parameters or previously saved state of this activity.
		Intent intent = getIntent();
		if (intent.hasExtra("create_to")) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mVoiceType = (Integer) extras.get("create_to");
			}
		}
		if (mVoiceType < 0 ) {
			//throw new IllegalStateException(
			//		"Invalid session  information:" + " " + mThread);
			mVoiceType = SIGLE_CHOICE;
		} 
		
		int resourceId = 0;
		if(mVoiceType == CREATE_TO_INTER_PHONE_VOICE ) {
			resourceId = R.string.str_check_participants;
		} else if (mVoiceType == CREATE_TO_VOIP_CALL) {
			resourceId = R.string.str_check_participants_voip_call;
			
			//  video ...
		} else if (mVoiceType == CREATE_TO_VIDEO_CALL) {
			resourceId = R.string.str_tips_video_nvited;
		} else if (mVoiceType == CREATE_TO_IM_TALK) {
			resourceId = R.string.str_tips_im_nvited;
		} else if (mVoiceType == MULTIPLE_CHOICE ) {
			resourceId =  R.string.str_check_participants_group;
		}
		mHeadTips.setText(resourceId);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_confirm :
			
			// start a session interphone ...
			ArrayList<String> list = new ArrayList<String>();
			if(mInviteAdapter!=null){
				HashMap<Integer, Boolean> isSelected =mInviteAdapter.getIsSelected();
				for (int i = 0; isSelected!=null&&i < isSelected.size(); i++) {
					if(isSelected.get(i)){
						list.add(arr.get(i));
					}
				}
			}
			if(list == null || list.isEmpty()) {
				return ;
			}
			
			Intent intent = new Intent() ;
			/*if(mVoiceType == CREATE_TO_DELAY_VOICE) {
				intent.setClass(InviteInterPhoneActivity.this, VoiceChatActivity.class);
				StringBuilder builder = new StringBuilder();
				for (String str : list) {
					builder.append(str).append(",");
				}
				String recipent = builder.toString();
				if(recipent.toString().length() > 0) {
					recipent = recipent.substring(0, recipent.lastIndexOf(","));
				}
				try {
					int thread = VoiceSQLManager.getInstance().isExistsId(recipent);
					if(thread > 0) {
						intent.putExtra("Thread_id", thread);
					} else {
						intent.putExtra("to",recipent);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			} else*/ if(mVoiceType == CREATE_TO_INTER_PHONE_VOICE) {
				intent.setClass(InviteInterPhoneActivity.this, InterPhoneRoomActivity.class);
				intent.putStringArrayListExtra("InviterMember", list) ;
			} else if (mVoiceType == CREATE_TO_VOIP_CALL) {
				intent.setClass(InviteInterPhoneActivity.this, VoIPCallActivity.class);
				intent.putExtra("VOIP_CALL_NUMNBER", list.get(0)) ;
			} else if (mVoiceType == CREATE_TO_VIDEO_CALL) {
				intent.setClass(InviteInterPhoneActivity.this, SelectVoiceActivity.class);
				intent.putExtra("VOIP_CALL_NUMNBER", list.get(0)) ;
			} else if (mVoiceType == CREATE_TO_IM_TALK) {
				intent.setClass(InviteInterPhoneActivity.this, GroupChatActivity.class);
				intent.putExtra(GroupBaseActivity.KEY_GROUP_ID, list.get(0)) ;
			} else if (mVoiceType == MULTIPLE_CHOICE ) {
				intent.setClass(InviteInterPhoneActivity.this, GroupDetailActivity.class);
				intent.putStringArrayListExtra("to", list);
			}
			
			// If the video call or call VOIP function returns...
			if(mVoiceType == CREATE_TO_VOIP_CALL || mVoiceType == CREATE_TO_VIDEO_CALL || mVoiceType == MULTIPLE_CHOICE )  {
				setResult(RESULT_OK,intent);
			} else {
				startActivity(intent);
			}
			finish() ;
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void handleTitleAction(int direction) {
		
		if(direction == TITLE_RIGHT_ACTION) {
			// display an input dialog .
			// Allows the user to input a number is added to the list of choices .
			// Click the OK button to enter the number added to the list of choices...
			//showEditDialog();
			showEditTextDialog(DIALOG_SHOW_KEY_INVITE, getString(R.string.dialog_title_input_number), null);
		} else {
			super.handleTitleAction(direction);
		}
	}
	
	@Override
	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {
		super.handleEditDialogOkEvent(requestKey, editText, checked);
		
		if(requestKey == DIALOG_SHOW_KEY_INVITE) {
			String mPhoneNumber = editText;
			if(mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber)){
				// invite this phone call ...
				if(arr == null ) {
					arr = new ArrayList<String>();
				}
				arr.add(mPhoneNumber);
				mInviteAdapter.isSelected.put((Integer)arr.size() - 1, false);
				
				if(mInviteAdapter == null) {
					mInviteAdapter = new InviteAdapter(getApplicationContext(), arr);
					InterMemList.setAdapter(mInviteAdapter);
				} 
				mInviteAdapter.notifyDataSetChanged();
				
			}
		}
	}

	private int count ;
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CheckBox cBox = (CheckBox) view.findViewById(R.id.check_box);
		if(!cBox.isChecked() && mVoiceType == CREATE_TO_INTER_PHONE_VOICE && count >= 7) {
			Toast.makeText(InviteInterPhoneActivity.this, "������������,����������8��", Toast.LENGTH_SHORT).show();
		} else {
			cBox.toggle();// Changes the state of checkbox access to click in every times.
			if(mVoiceType == CREATE_TO_VOIP_CALL || mVoiceType == CREATE_TO_VIDEO_CALL || mVoiceType == CREATE_TO_IM_TALK) {
				for (int i = 0 ; i < mInviteAdapter.getIsSelected().size() ; i++) {
					mInviteAdapter.getIsSelected().put(i, false);
				}
			}
			if(cBox.isChecked()) {
				count ++;
			} else {
				if(count > 0) {
					count --;
				}
			}
			mInviteAdapter.getIsSelected().put(position, cBox.isChecked());
		}
		mInviteAdapter.notifyDataSetChanged();
		for (Map.Entry<Integer, Boolean> entry : mInviteAdapter.isSelected.entrySet()) {
			if(entry.getValue()) {
				mJoinInter.setEnabled(true);
				int resourceId = 0;
				if(mVoiceType == CREATE_TO_INTER_PHONE_VOICE ) {
					resourceId = R.string.str_check_to_participants;
				} else if (mVoiceType == CREATE_TO_VOIP_CALL) {
					resourceId = R.string.str_check_to_participants_voip_call;
				} else if (mVoiceType == CREATE_TO_VIDEO_CALL) {
					resourceId = R.string.str_check_to_participants_video_call;
				
					// im
				} else if (mVoiceType == CREATE_TO_IM_TALK) {
					
					resourceId = R.string.str_check_to_participants_im_talk;
				} else if (mVoiceType == MULTIPLE_CHOICE) {
					resourceId = R.string.str_check_to_participants_group;
				}
				mJoinTips.setText(resourceId);
				return ;
			} 
			
			mJoinInter.setEnabled(false);
			mJoinTips.setText(R.string.str_not_check_participants);
		}
	}
	
	class InviteAdapter extends ArrayAdapter<String> {

		LayoutInflater mInflater;
		HashMap<Integer, Boolean> isSelected;
		
		int count ;
		
		public InviteAdapter(Context context,List<String> objects) {
			super(context, 0, objects);
			
			mInflater = getLayoutInflater();
			init(objects);
		}
		
		// initialize all checkbox are not selected
		public void init(List<String> objects) {
			if(isSelected!=null){
				isSelected.clear();
			}else{
				isSelected = new HashMap<Integer, Boolean>();
			}
			for (int i = 0; i < objects.size(); i++) {
				isSelected.put(i, false);
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			InviteHolder holder;
			if (convertView == null|| convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.invite_member_list_item, null);
				holder = new InviteHolder();
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
				if (mVoiceType == CREATE_TO_VIDEO_CALL || mVoiceType == CREATE_TO_VOIP_CALL || mVoiceType == CREATE_TO_IM_TALK) {
					holder.checkBox.setButtonDrawable(R.drawable.checkbox_btn_radio);
				} else {
					holder.checkBox.setButtonDrawable(R.drawable.checkbox_btn);
				}
			} else {
				holder = (InviteHolder) convertView.getTag();
			}
			
			// do ..
			String name = getItem(position);
			if(!TextUtils.isEmpty(name)) {
				holder.name.setText(name);
			}
			holder.checkBox.setChecked(isSelected.get(position));
			return convertView;
		}
		
		
		class InviteHolder {
			TextView name;
			CheckBox checkBox;
		}
		
		public HashMap<Integer, Boolean> getIsSelected() {
			return isSelected;
		}

		
	}

}