package com.renren.android;

import java.util.List;

import xu.ye.bean.ContactBean;
import android.app.Application;

public class BaseApplication extends Application {
	public String mLocation;
	public double mLongitude;
	public double mLatitude;
	public String mImagePath;
	public int mImageType = -1;

	
	private List<ContactBean> contactBeanList;
	
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}
	
	
	public void onCreate() {
		super.onCreate();
	}

}
