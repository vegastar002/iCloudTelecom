package xu.ye.service;

import java.util.ArrayList;

import xu.ye.bean.ContactBean;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;

import com.renren.android.BaseApplication;

public class T9Service extends Service {

	private AsyncQueryHandler asyncQuery;
	private static final String LOGTAG = "T9Service";

	public void onCreate() {
	}

	public void onStart(Intent intent, int startId) {
	}

	public void onDestroy() {
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		System.out.println("T9Service-begin");
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		initSQL();

		return super.onStartCommand(intent, flags, startId);
	}

	public void onRebind(Intent intent) {
	}

	public boolean onUnbind(Intent intent) {
		return true;
	}

	protected void initSQL() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; 
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
		}; 
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
	}

	private class MyAsyncQueryHandler extends AsyncQueryHandler {
		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			querying(cursor);
		}
	}

	private void querying(final Cursor cursor){

		Handler handlerInsertOrder = new Handler(){
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MAsyncTask.DOWNLOADING_START_MESSAGE:
					System.out.println("begin to sort out t9");
					break;
				case MAsyncTask.DOWNLOAD_END_MESSAGE:
					Bundle bundle1 = msg.getData();
					ArrayList<ContactBean> list = (ArrayList<ContactBean>) bundle1.get("完成");
					BaseApplication ma = (BaseApplication)getApplication();
					System.out.println(list.size());
					ma.setContactBeanList(list);
//					for(ContactInfo ci : list){
//						System.out.println(ci.getName());
//						System.out.println(ci.getPhoneNum());
//						System.out.println(ci.getFormattedNumber());
//						System.out.println(ci.getPinyin());
//						System.out.println("--------------------------------");
//					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

		MAsyncTask.startRequestServerData(T9Service.this, handlerInsertOrder, cursor);
	}
}
