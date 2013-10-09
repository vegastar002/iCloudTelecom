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
package com.voice.demo.outboundmarketing;


import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisun.phone.core.voice.util.VoiceUtil;
import com.hust.wa.icloudtelecom.R;
import com.voice.demo.outboundmarketing.RestHelper.ERequestState;
import com.voice.demo.outboundmarketing.RestHelper.onRestHelperListener;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.voip.CCPBaseActivity;

// Processing marketing call number is called out, 
// according to the status of marketing results update UI.
public class MarketStateActivity extends CCPBaseActivity implements View.OnClickListener, onRestHelperListener
{	
	private TextView							headNoteTxt;
	private Button								finishButton;
	private ListView							outboundList;
	private ArrayList<String> 					phoneNumArray;
	private String				mAudioName;
	private ArrayList<HashMap<String, Object>>	mDataArrayList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_market_state_activity);
		
		handleTitleDisplay(getString(R.string.btn_title_back)
				, getString(R.string.str_market_state_head_text)
				, null);
		
		// Remove the transmission of telephone number from the intent .
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		phoneNumArray = bundle.getStringArrayList("Outbound_phoneNum");
		mAudioName = bundle.getString("audio_name");
		
		
		headNoteTxt = (TextView)findViewById(R.id.head_note_Txt);
		headNoteTxt.setText(getString(R.string.str_state_head_note, phoneNumArray.size()));
		
		finishButton = (Button)findViewById(R.id.btn_finish_outbound);
		finishButton.setOnClickListener(this);
		
		outboundList = (ListView)findViewById(R.id.market_state_list);
		
		mDataArrayList = getData();
		MyAdapter listItemAdapter = new MyAdapter(this);
        outboundList.setAdapter(listItemAdapter);
        
        // Outgoing requests
        RestHelper.getInstance().setOnRestHelperListener(this);
        new LandingCallAsyncTask().execute();
	}
	
	private ArrayList<HashMap<String, Object>> getData()
	{
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
 
	    for(int i=0; i < phoneNumArray.size(); i++)
	    {
	    	HashMap<String, Object> map = new HashMap<String, Object>();
	    	
	    	if (i==0)
			{
		    	map.put("ItemImage", R.drawable.status_speaking);// Image resources ID
		    	map.put("ItemState", getString(R.string.str_market_state_answer_success));
			}
	    	else if (i==1)
			{
		    	map.put("ItemImage", R.drawable.status_quit);// Image resources ID
		    	map.put("ItemState", getString(R.string.str_market_state_other_busy));
			}
	    	else if (i==2)
			{
		    	map.put("ItemImage", R.drawable.status_join);// Image resources ID
		    	map.put("ItemState", getString(R.string.str_market_state_calling));
			}
	    	else
	    	{
		    	map.put("ItemImage", R.drawable.status_wait);// Image resources ID
		    	map.put("ItemState", getString(R.string.str_market_state_call_wait));
			}
	    	
	    	map.put("ItemNum", phoneNumArray.get(i));
	    	listItem.add(map);
	    }

        return listItem;
    }
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_finish_outbound:	// Complete
		{
			handleTitleAction(TITLE_LEFT_ACTION);
		}
			break;

		default:
			break;
		}		
	}
	
	@Override
	public void onLandingCAllsStatus(ERequestState reason, String callId)
	{
		
	}
	
	@Override
	public void onVoiceCode(ERequestState reason)
	{

	}
	
	public final class ViewHolder
	{	
		public ImageView	stateImg;
		public TextView		numTxt;
		public TextView		stateTxt;
	}
	
	public class MyAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;
		
		public MyAdapter(Context context)
		{
			this.mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount()
		{
			return mDataArrayList.size();
		}

		@Override
		public Object getItem(int arg0)
		{
			return null;
		}

		@Override
		public long getItemId(int arg0)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder = null;
			
			if (convertView == null)
			{	
				holder = new ViewHolder();
				
				convertView = mInflater.inflate(R.layout.list_item_market_state, null);
				holder.stateImg = (ImageView)convertView.findViewById(R.id.StateImg);
				holder.numTxt = (TextView)convertView.findViewById(R.id.StateNumTxt);
				holder.stateTxt = (TextView)convertView.findViewById(R.id.StateTxt);
				convertView.setTag(holder);	
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
			
			try
			{
				holder.stateImg.setBackgroundResource((Integer)mDataArrayList.get(position).get("ItemImage"));
				holder.numTxt.setText((String)mDataArrayList.get(position).get("ItemNum"));
				holder.stateTxt.setText((String)mDataArrayList.get(position).get("ItemState"));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return convertView;
		}
	}
	
	public class LandingCallAsyncTask extends AsyncTask<Void, Void, String>
	{
		@Override
		protected String doInBackground(Void... params)
		{
	        for (int i = 0; i < phoneNumArray.size(); i++)
			{
	        	RestHelper.getInstance().LandingCalls(VoiceUtil.getStandardMDN(phoneNumArray.get(i)) , mAudioName , CCPConfig.App_ID , "");
			}

			return null;
		}
	}
}
