package com.renren.android.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;

public class CallNet {

	private Context mContext;
	private BaseApplication mApp;
	private Activity mActivity;
	private View mCallNet, exCallLogView, contactView;
	private OnOpenListener mOnOpenListener;
	private ImageView mFlip, mAddFriends;
	
	
	
	public CallNet(BaseApplication application, Context context, Activity ak) {
		mContext = context;
		mApp = application;
		mActivity = ak;
		
		mCallNet = LayoutInflater.from(context).inflate(R.layout.home_dial_page, null);

		findViewById();
	}
	
	
	private void findViewById() {
		mFlip = (ImageView) mCallNet.findViewById(R.id.chat_flip);
		mAddFriends = (ImageView) mCallNet.findViewById(R.id.chat_addfriends);
	}
	
	
	private void setListener() {
		mFlip.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mOnOpenListener != null) {
					mOnOpenListener.open();
				}
			}
		});
	}

	public View getView() {
		return mCallNet;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
	
	
}
