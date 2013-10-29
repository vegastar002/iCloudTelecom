package com.renren.android;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import xu.ye.bean.ContactBean;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hisun.phone.core.voice.DeviceListener.Reason;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.ui.DesktopActivity;
import com.voice.demo.tools.CCPConfig;
import com.voice.demo.voip.VoiceHelper;
import com.voice.demo.voip.VoiceLoginActivity;

public class AppStart extends Activity {

	private String[] mVoipArray;
	private Dialog mLoginDialog;
	private BaseApplication mApplication;
	private AsyncQueryHandler asyncQueryContacts;
	private Map<Integer, ContactBean> contactIdMap = null;
	private final static int MSG_GET_TIEZI_LIST = 1;
	private final static int MSG_CONNECT_FAILUE = 2;
//	private List<TieItems> mTieItems = new ArrayList<TieItems>();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_app_start);
		
		
		mApplication = (BaseApplication) getApplication();
		asyncQueryContacts = new ConstactsAsyncQueryHandler(getContentResolver());
		
		if ( !mApplication.isConnectingToInternet() ){
			showInitErrDialog("网络连接错误, 请重新登陆");
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
//				initConfigInfomation();
//				VoiceHelper.init(BaseApplication.getInstance(), helperHandler);
				
				
				HttpPost httpRequest = new HttpPost(mApplication.Server_Address);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				Vaparams.add(new BasicNameValuePair("fatie", "catch" ));
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = client.execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = mApplication.retrieveInputStream(httpResponse.getEntity());
//						Log.i("", "xml> " + preTel);
						
						Message msg = new Message();
						msg.what = MSG_GET_TIEZI_LIST;
						msg.obj = preTel;
						helperHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.what = MSG_CONNECT_FAILUE;
					helperHandler.sendMessage(msg);
				}
				
				
			}
		}).start();



//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				initContacts();
//				Intent intent = new Intent();
//				intent.setClass(AppStart.this, DesktopActivity.class);
//				startActivity(intent);
//				finish();
//				
//				initConfigInfomation();
//				VoiceHelper.init(BaseApplication.getInstance(), helperHandler);
//			}
//		}).start();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		helperHandler = null;
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
	
	
	private void initContacts(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		};
		asyncQueryContacts.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}
	
	
	private class ConstactsAsyncQueryHandler extends AsyncQueryHandler {

		public ConstactsAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, ContactBean>();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					
					if (contactIdMap.containsKey(contactId)) {
						
					}else{
						ContactBean cb = new ContactBean();
						cb.setDisplayName(name);
						cb.setPhoneNum(number);
						cb.setSortKey(sortKey);
						cb.setContactId(contactId);
						cb.setPhotoId(photoId);
						cb.setLookUpKey(lookUpKey);
						mApplication.listContacts.add(cb);
						
						mApplication.contactName.add(name);
					}
				}
				
				
//				Log.i("", "联系人初始化> " + mApplication.listContacts.size() );
				if ( mApplication.listContacts.size() == 0) {
				}
			}
		}

	}
	
	
	
	private Handler helperHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
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
				} else if (msg.what == VoiceLoginActivity.WHAT_INIT_ERROR) {
					// 
					if(!BaseApplication.getInstance().isConnect()) {
						showInitErrDialog(null);
					}
				}else if (msg.what == MSG_GET_TIEZI_LIST) {
					String xmlString = msg.obj.toString();
					mApplication.catNetTie(xmlString);
					
					initContacts();
					Intent intent = new Intent();
					intent.setClass(AppStart.this, DesktopActivity.class);
					startActivity(intent);
					finish();
				} else if (msg.what == MSG_CONNECT_FAILUE) {
					Toast.makeText(AppStart.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
					finish();
				}
				
				else {
					Log4Util.d(VoiceHelper.DEMO_TAG , "Sorry , can't handle a message " + msg.what);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			super.handleMessage(msg);
		}

	};
	
	
//	public void catNetTie(String xmlString){
//		try {
//			mApplication.mTieItems.clear();
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			org.w3c.dom.Document document = db.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));
//			NodeList employees = document.getChildNodes();
//			for (int i = 0; i < employees.getLength(); i++) {
//				org.w3c.dom.Node employee = employees.item(i);
//				NodeList employeeInfo = employee.getChildNodes();
//				for (int j = 0; j < employeeInfo.getLength(); j++) {
//					org.w3c.dom.Node node = employeeInfo.item(j);
//					NodeList employeeMeta = node.getChildNodes();
//					TieItems tis = new TieItems();
//					
//					for (int k = 0; k < employeeMeta.getLength(); k++) {
//						
//						if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("NoID") ){
//							tis.NoID = employeeMeta.item(k).getTextContent();
//						}
//						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("auth") ) {
//							tis.auth = employeeMeta.item(k).getTextContent();
//						}
//						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("title") ) {
//							tis.title = employeeMeta.item(k).getTextContent();
//						}
//						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("category") ) {
//							tis.category = employeeMeta.item(k).getTextContent();
//						}
//						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("zcontent") ) {
//							tis.zcontent = employeeMeta.item(k).getTextContent();
//						}
//						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("time") ) {
//							tis.time = employeeMeta.item(k).getTextContent();
//						}
//						
//					}
//					
//					mApplication.mTieItems.add(tis);
//				}
//			}
//			
////			Log.i("", "传值1> "+ mApplication.mTieItems.size());
////			for (int j = 0; j < mApplication.mTieItems.size(); j++) {
////				Log.i("", mApplication.mTieItems.get(j).auth+ mApplication.mTieItems.get(j).category+ mApplication.mTieItems.get(j).NoID+ mApplication.mTieItems.get(j).time+ mApplication.mTieItems.get(j).title+ mApplication.mTieItems.get(j).zcontent );
////				
////			}
//			
//		} catch (FileNotFoundException e) {
//			System.out.println(e.getMessage());
//		} catch (ParserConfigurationException e) {
//			System.out.println(e.getMessage());
//		} catch (SAXException e) {
//			System.out.println(e.getMessage());
//		} catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
//	}
//	
//	public class TieItems{
//		public String NoID = "";
//		public String auth = "";
//		public String title = "";
//		public String category = "";
//		public String zcontent = "";
//		public String time = "";
//	}
	
	
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
		intent.setClass(AppStart.this, DesktopActivity.class);
		startActivity(intent);
		BaseApplication.getInstance().initSQLiteManager();
		finish();
	}
	
	void showInitErrDialog(String reason) {
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
						finish();
					}
				}).create();
		
		mLoginDialog.setCancelable(false);
		mLoginDialog.setCanceledOnTouchOutside(false);
		mLoginDialog.show();
	}
	
	
	private void initConfigInfomation() {
		if(CCPConfig.VoIP_ID_LIST != null ) {
			mVoipArray = CCPConfig.VoIP_ID_LIST.split(",");
			
			if(mVoipArray == null || mVoipArray.length == 0) {
				throw new IllegalArgumentException("Load the VOIP account information errors" +
						", configuration information can not be empty" + mVoipArray);
			}
		}
	}

	
}
