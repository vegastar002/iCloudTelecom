package com.renren.android;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import xu.ye.bean.ContactBean;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.hisun.phone.core.voice.CCPCall;
import com.hisun.phone.core.voice.model.im.IMAttachedMsg;
import com.hust.wa.icloudtelecom.R;
import com.voice.demo.sqlite.CCPSqliteManager;
import com.voice.demo.tools.CCPUtil;
import com.voice.demo.tools.CrashHandler;

public class BaseApplication extends Application {
	public String mLocation;
	public double mLongitude;
	public double mLatitude;
	public String mImagePath;
	public int mImageType = -1;

	private static BaseApplication instance;
	public static ArrayList<String> interphoneIds = null;
	public static ArrayList<String> chatRoomIds;

	public final static String VALUE_DIAL_MODE_FREE = "voip_talk";
	public final static String VALUE_DIAL_MODE_BACK = "back_talk";
	public final static String VALUE_DIAL_MODE_DIRECT = "direct_talk";
	public final static String VALUE_DIAL_MODE = "mode";
	public final static String VALUE_DIAL_SOURCE_PHONE = "srcPhone";
	public final static String VALUE_DIAL_VOIP_INPUT = "VoIPInput";
	public final static String VALUE_DIAL_MODE_VEDIO = "vedio_talk";
	
	public final static String Server_Address = "http://192.168.3.106:8080/eFlowIM_Upload/p/fatie";

	//这玩意我把它拿出来了 做全局使用
	public List<ContactBean> listContacts = new ArrayList<ContactBean>();
	public ArrayList<String> contactName = new ArrayList<String>();
	public List<TieItems> mTieItems = new ArrayList<TieItems>();
	
	private File vStore;

	private boolean isConnect = false;

	public boolean isConnect() {
		return isConnect;
	}

	public void setConnect(boolean isConnect) {
		this.isConnect = isConnect;
	}

	boolean isChecknet = false;

	public boolean isChecknet() {

		return isChecknet;
	}

	public void setChecknet(boolean isChecknet) {

		this.isChecknet = isChecknet;
	}
	
	
	private List<ContactBean> contactBeanList;
	
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}
	
	
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		initFileStore();
		initCrashHandler();
		if (interphoneIds == null) {
			interphoneIds = new ArrayList<String>();
		}
		if (chatRoomIds == null) {
			chatRoomIds = new ArrayList<String>();
		}
	}
	
	
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}
	
	
	private void initCrashHandler() {
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}

	/**
	 * 初始化数据库
	 */
	public void initSQLiteManager() {
		CCPSqliteManager.getInstance();
	}

	private void initFileStore() {
		if (!CCPUtil.isExistExternalStore()) {
			Toast.makeText(getApplicationContext(), R.string.media_ejected,
					Toast.LENGTH_LONG).show();
			return;
		}
		File directory = new File(Environment.getExternalStorageDirectory(),
				CCPUtil.DEMO_ROOT_STORE);
		if (!directory.exists() && !directory.mkdirs()) {
			Toast.makeText(getApplicationContext(),
					"Path to file could not be created", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		vStore = directory;
	}

	public File getVoiceStore() {
		return vStore;
	}

	public void showToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	public void showToast(int resId) {
		Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT)
				.show();
	}
	
	public static BaseApplication getInstance() {
		return instance;
	}
	
	public String getUser_Agent() {
		String ua = "Android;" + getOSVersion() + ";" + getVersion() + ";"
				+ getVendor() + "-" + getDevice();
		return ua;
	}

	/**
	 * device model name, e.g: GT-I9100
	 * 
	 * @return the user_Agent
	 */
	public String getDevice() {
		return Build.MODEL;
	}

	/**
	 * device factory name, e.g: Samsung
	 * 
	 * @return the vENDOR
	 */
	public String getVendor() {
		return Build.BRAND;
	}

	/**
	 * @return the SDK version
	 */
	public int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * @return the OS version
	 */
	public String getOSVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * Retrieves application's version number from the manifest
	 * 
	 * @return versionName
	 */
	public String getVersion() {
		String version = "0.0.0";
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	/**
	 * Retrieves application's version code from the manifest
	 * 
	 * @return versionCode
	 */
	public int getVersionCode() {
		int code = 1;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			code = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return code;
	}

	/**
	 * 设置播放模式
	 * 
	 * @param mode
	 */
	public void setAudioMode(int mode) {
		AudioManager audioManager = (AudioManager) getApplicationContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
			audioManager.setMode(mode);
		}
	}

	/**
	 * 直接拨打电话
	 * 
	 * @param phoneNum
	 */
	public void startCalling(String phoneNum) {
		try {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://"
					+ phoneNum));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			showToast(R.string.toast_call_phone_error);
		}
	}

	public void quitApp() {
		try {
			// update sending message status ...
			CCPSqliteManager.getInstance().updateAllIMMessageSendFailed();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// just for demo
		CCPCall.shutdown();
//		System.exit(0);
	}

	/**
	 * 通过Appliaction中转Object的引用，中转结束后注意调用removeData方法
	 * 
	 * @param key
	 * @param list
	 */

	public void setSpeakerEnable(boolean isEnable) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
			audioManager.setSpeakerphoneOn(isEnable);
		}
	}

	public static HashMap<String, IMAttachedMsg> rMediaMsgList = new HashMap<String, IMAttachedMsg>();

	public IMAttachedMsg getMediaData(String key) {
		if (key != null) {
			return rMediaMsgList.get(key);
		} else {
			return null;
		}
	}

	/**
	 * 通过Appliaction中转Object的引用，中转结束后注意调用removeData方法
	 * 
	 * @param key
	 * @param list
	 */
	public void putMediaData(String key, IMAttachedMsg obj) {
		if (key != null && obj != null) {
			rMediaMsgList.put(key, obj);
		}
	}

	public void removeMediaData(String key) {
		if (key != null) {
			rMediaMsgList.remove(key);
		}
	}

	public HashMap<String, IMAttachedMsg> getMediaMsgList() {
		return rMediaMsgList;
	}
	
	
	private HashMap<String, Object> dataMap = new HashMap<String, Object>();
	/**
	 * 通过Appliaction中转Object的引用，中转结束后注意调用removeData方法
	 * @param key
	 * @param list
	 */
	public void putData(String key, Object obj) {
		if (key != null && obj != null) {
			dataMap.put(key, obj);
		}
	}

	public void removeData(String key) {
		if (key != null) {
			dataMap.remove(key);
		}
	}
	public Object getData(String key) {
		if (key != null) {
			return dataMap.get(key);
		} else {
			return null;
		}
	}
	
	/**
	 * To obtain the system preferences to save the file to edit the object
	 * @return
	 */
	public Editor getSharedPreferencesEditor() {
		SharedPreferences cCPreferences = getSharedPreferences(CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);
		Editor edit = cCPreferences.edit();
		
		return edit;
	}
	/**
	 * To obtain the system preferences to save the file to edit the object
	 * @return
	 */
	public SharedPreferences getSharedPreferences() {
		return getSharedPreferences(CCPUtil.CCP_DEMO_PREFERENCE, MODE_PRIVATE);
	}
	
	
	/**
	 * 获取动态时间
	 * 
	 * @param key
	 * @return
	 */
    public String getSettingParams(String key) {
		SharedPreferences settings = getSharedPreferences();
		return settings.getString(key, "");
	}
    
    /**
     * 保存动态时间
     * @param key
     * @param value
     */
	public void saveSettingParams(String key, String value) {
		SharedPreferences settings = getSharedPreferences();
		settings.edit().putString(key, value).commit();
	}
	
    /**
     * 清除动态时间
     * @param key
     * @param value
     */
	public void clearSettingParams() {
		SharedPreferences settings = getSharedPreferences();
		settings.edit().clear().commit();
	}
	
	/**
	 * 删除配置
	 * @param key
	 */
	public void removeSettingParam(String key) {
		SharedPreferences settings = getSharedPreferences("Dynamic_Time_Preferences", 0);
		settings.edit().remove(key).commit();
	}
	
	
	public String retrieveInputStream(HttpEntity httpEntity) {
		int length = (int) httpEntity.getContentLength();
		if (length < 0)
			length = 10000;
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("", e.getMessage());
		} catch (IOException e) {
			Log.e("", e.getMessage());
		}
		return stringBuffer.toString();
	}

	public void catNetTie(String xmlString){
		try {
			mTieItems.clear();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));
			NodeList employees = document.getChildNodes();
			for (int i = 0; i < employees.getLength(); i++) {
				org.w3c.dom.Node employee = employees.item(i);
				NodeList employeeInfo = employee.getChildNodes();
				for (int j = 0; j < employeeInfo.getLength(); j++) {
					org.w3c.dom.Node node = employeeInfo.item(j);
					NodeList employeeMeta = node.getChildNodes();
					TieItems tis = new TieItems();
					
					for (int k = 0; k < employeeMeta.getLength(); k++) {
						
						if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("NoID") ){
							tis.NoID = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("auth") ) {
							tis.auth = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("title") ) {
							tis.title = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("category") ) {
							tis.category = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("zcontent") ) {
							tis.zcontent = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("time") ) {
							tis.time = employeeMeta.item(k).getTextContent();
						}
						
					}
					
					mTieItems.add(tis);
				}
			}
			
//			Log.i("", "传值1> "+ mApplication.mTieItems.size());
//			for (int j = 0; j < mApplication.mTieItems.size(); j++) {
//				Log.i("", mApplication.mTieItems.get(j).auth+ mApplication.mTieItems.get(j).category+ mApplication.mTieItems.get(j).NoID+ mApplication.mTieItems.get(j).time+ mApplication.mTieItems.get(j).title+ mApplication.mTieItems.get(j).zcontent );
//				
//			}
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public class TieItems{
		public String NoID = "";
		public String auth = "";
		public String title = "";
		public String category = "";
		public String zcontent = "";
		public String time = "";
	}
	
	
}
