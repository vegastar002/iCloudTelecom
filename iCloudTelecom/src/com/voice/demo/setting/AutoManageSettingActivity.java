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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.hisun.phone.core.voice.Device.AudioMode;
import com.hisun.phone.core.voice.Device.AudioType;
import com.hust.wa.icloudtelecom.R;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.voip.CCPBaseActivity;
import com.voice.demo.voip.VoiceHelper;

public class AutoManageSettingActivity extends CCPBaseActivity implements  OnItemClickListener {
	public static final int SETTING_AUTOMANAGE = 0x0; // 自动增益控制
	public static final int SETTING_ECHOCANCELLED = 0x1; // 回音消除
	public static final int SETTING_SILENCERESTRAIN = 0x2; // 静音抑制

	private ListView myListView;
	private int settingType;
	private AutoManageAdapter mAdapter;
	private List<String> mDataArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting_automanage_activity);

		init(savedInstanceState);

		myListView = (ListView) findViewById(R.id.setting_automanage_list);
		myListView.setOnItemClickListener(this);

		mDataArray = getData();
		mAdapter = new AutoManageAdapter(getApplicationContext(), mDataArray);
		myListView.setAdapter(mAdapter);
	}

	private void init(Bundle savedInstanceState) {
		// head
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		settingType = (Integer) extras.get("SettingType");

		int appTitleResourceId = R.string.app_name;
		switch (settingType) {
		case SETTING_AUTOMANAGE: {
			appTitleResourceId = R.string.str_setting_item_automanage;
		}
			break;
		case SETTING_ECHOCANCELLED: {
			appTitleResourceId = R.string.str_setting_item_echocancel;
		}
			break;
		case SETTING_SILENCERESTRAIN: {
			appTitleResourceId = R.string.str_setting_item_silencerestrain;
		}
			break;
		default:
			break;
		}
		
		handleTitleDisplay(getString(R.string.btn_title_back),
				getString(appTitleResourceId), getString(R.string.str_setting_save));
	}

	private List<String> getData() {
		List<String> listItem = new ArrayList<String>();

		switch (settingType) {
		case SETTING_AUTOMANAGE: {
			listItem.add("AgcUnchanged");
			listItem.add("AgcDefault");
			listItem.add("AgcAdaptiveAnalog");
			listItem.add("AgcAdaptiveDigital");
			listItem.add("AgcFixedDigital");
		}
			break;
		case SETTING_ECHOCANCELLED: {
			listItem.add("EcUnchanged");
			listItem.add("EcDefault");
			listItem.add("EcConference");
			listItem.add("EcAec");
			listItem.add("EcAecm");
		}
			break;
		case SETTING_SILENCERESTRAIN: {
			listItem.add("NsUnchanged");
			listItem.add("NsDefault");
			listItem.add("NsConference");
			listItem.add("NsLowSuppression");
			listItem.add("NsModerateSuppression");
			listItem.add("NsHighSuppression");
			listItem.add("NsVeryHighSuppression");
		}
			break;
		default:
			break;
		}

		return listItem;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		CheckBox cBox = (CheckBox) arg1.findViewById(R.id.check_box);
		cBox.toggle();// Changes the state of checkbox access to click in every
						// times.

		for (int i = 0; i < mAdapter.getIsSelected().size(); i++) {
			mAdapter.getIsSelected().put(i, false);
		}

		mAdapter.getIsSelected().put(position, cBox.isChecked());
		mAdapter.notifyDataSetChanged();
	}

	
	@Override
	protected void handleTitleAction(int direction) {
		if(direction == TITLE_RIGHT_ACTION) {
			// 需保存设置再关闭
			if (mAdapter != null) {
				HashMap<Integer, Boolean> isSelected = mAdapter.getIsSelected();

				for (int i = 0; isSelected != null && i < isSelected.size(); i++) {
					if (isSelected.get(i)) {
						// 获取SharedPreferences对象
						Context ctx = AutoManageSettingActivity.this;
						SharedPreferences sp = ctx.getSharedPreferences(
								CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);
						Editor editor = sp.edit();

						AudioType audioType = AudioType.AUDIO_AGC;
						AudioMode audioMode = AudioMode.kNsDefault;

						switch (settingType) {
						case SETTING_AUTOMANAGE: {
							// 存入数据
							editor.putString("AUTOMANAGE_CONTENT_KEY",
									mDataArray.get(i));
							editor.putInt("AUTOMANAGE_INDEX_KEY", i);

							audioType = AudioType.AUDIO_AGC;
							audioMode = CCPUtil.getAudioMode(
									AudioType.AUDIO_AGC, i);
						}
							break;
						case SETTING_ECHOCANCELLED: {
							editor.putString("ECHOCANCELLED_CONTENT_KEY",
									mDataArray.get(i));
							editor.putInt("ECHOCANCELLED_INDEX_KEY", i);

							audioType = AudioType.AUDIO_EC;
							audioMode = CCPUtil.getAudioMode(
									AudioType.AUDIO_EC, i);

						}
							break;
						case SETTING_SILENCERESTRAIN: {
							editor.putString("SILENCERESTRAIN_CONTENT_KEY",
									mDataArray.get(i));
							editor.putInt("SILENCERESTRAIN_INDEX_KEY", i);

							audioType = AudioType.AUDIO_NS;
							audioMode = CCPUtil.getAudioMode(
									AudioType.AUDIO_NS, i);
						}
							break;
						default:
							break;
						}

						editor.commit();
						VoiceHelper.getInstance().getDevice().setAudioConfigEnabled(audioType,
										true, audioMode);
						setResult(RESULT_OK);
						break;
					}
				}
			}

			finish();
		}  else{
			super.handleTitleAction(direction);
		}
	}

	class AutoManageAdapter extends ArrayAdapter<String> {
		LayoutInflater mInflater;
		HashMap<Integer, Boolean> isSelected;

		int count;

		public AutoManageAdapter(Context context, List<String> objects) {
			super(context, 0, objects);

			mInflater = getLayoutInflater();
			init(objects);
		}

		// initialize all checkbox
		public void init(List<String> objects) {
			if (isSelected != null) {
				isSelected.clear();
			} else {
				isSelected = new HashMap<Integer, Boolean>();
			}

			Context ctx = AutoManageSettingActivity.this;
			SharedPreferences sp = ctx.getSharedPreferences(
					CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);
			int index = 0;

			if (settingType == SETTING_AUTOMANAGE) {
				index = sp.getInt("AUTOMANAGE_INDEX_KEY", 3);
			} else if (settingType == SETTING_ECHOCANCELLED) {
				index = sp.getInt("ECHOCANCELLED_INDEX_KEY", 4);
			} else if (settingType == SETTING_SILENCERESTRAIN) {
				index = sp.getInt("SILENCERESTRAIN_INDEX_KEY", 6);
			}

			for (int i = 0; i < objects.size(); i++) {
				if (i == index) {
					isSelected.put(i, true);
				} else {
					isSelected.put(i, false);
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			InviteHolder holder;

			if (convertView == null || convertView.getTag() == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_setting_automanage, null);
				holder = new InviteHolder();

				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.check_box);
				holder.checkBox
						.setButtonDrawable(R.drawable.checkbox_btn_radio);
			} else {
				holder = (InviteHolder) convertView.getTag();
			}

			String name = getItem(position);

			if (!TextUtils.isEmpty(name)) {
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
