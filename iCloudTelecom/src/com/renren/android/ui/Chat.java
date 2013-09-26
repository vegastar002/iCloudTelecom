package com.renren.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xu.ye.bean.ContactBean;
import xu.ye.bean.GroupBean;
import xu.ye.view.HomeContactActivity;
import xu.ye.view.adapter.ContactHomeAdapter;
import xu.ye.view.other.SystemScreenInfo;
import xu.ye.view.ui.QuickAlphabeticBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;

public class Chat {
	private Context mContext;
	private View mChat;

	private Activity mActivity;
	private ImageView mFlip;
	private ImageView mAddFriends;
	private Button mChoose;
	private OnOpenListener mOnOpenListener;
	private ListView personList;
	private QuickAlphabeticBar alpha;
	private AsyncQueryHandler asyncQueryContacts;
	private Map<Integer, ContactBean> contactIdMap = null;
	private List<ContactBean> listContacts;
	private ContactHomeAdapter adapterContacts;
	private String ACTION1 = "SET_DEFAULT_SIG";
	private BaseReceiver1 receiver1 = null;
	
	

	public Chat(Context context, Activity ak) {
		mContext = context;
		mActivity = ak;
		
		mChat = LayoutInflater.from(context).inflate(R.layout.home_contact_page, null);

		findViewById();
//		setListener();
	}

	int ssHight = 0;
	private void findViewById() {
		personList = (ListView) mChat.findViewById(R.id.acbuwa_list);
		alpha = (QuickAlphabeticBar) mChat.findViewById(R.id.fast_scroller);
        
		ViewTreeObserver vto2 = alpha.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {  
            @Override    
            public void onGlobalLayout() {  
            	alpha.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            	ssHight = alpha.getHeight();
            }    
        });
        
		asyncQueryContacts = new ConstactsAsyncQueryHandler(mContext.getContentResolver());
		initContacts();
		startReceiver1();
		
		mFlip = (ImageView) mChat.findViewById(R.id.chat_flip);
		mAddFriends = (ImageView) mChat.findViewById(R.id.chat_addfriends);
	}
	
	private void startReceiver1() {
		if(null==receiver1){
			IntentFilter localIntentFilter = new IntentFilter(ACTION1);
			receiver1 = new BaseReceiver1();
			mContext.registerReceiver(receiver1, localIntentFilter);
		}
	}
	
	
	private void stopReceiver1() {
		if (null != receiver1)
			mContext.unregisterReceiver(receiver1);
	}
	
	public class BaseReceiver1 extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION1)) {
				String str_bean = intent.getStringExtra("groupbean");
				Gson gson = new Gson();
				GroupBean gb = gson.fromJson(str_bean, GroupBean.class);
				if(gb.getId() == 0){
					initContacts();
				}else{
					queryGroupMember(gb);
				}
			}
		}
	}
	
	
	private void queryGroupMember(GroupBean gb){
		String[] RAW_PROJECTION = new String[]{ContactsContract.Data.RAW_CONTACT_ID};  
		Cursor cur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,RAW_PROJECTION,  
				ContactsContract.Data.MIMETYPE+" = '"+GroupMembership.CONTENT_ITEM_TYPE  
				+"' AND "+ContactsContract.Data.DATA1+"="+ gb.getId(), null, "data1 asc"); 

		StringBuilder inSelectionBff = new StringBuilder().append(ContactsContract.RawContacts._ID + " IN ( 0");
		while(cur.moveToNext()){
			inSelectionBff.append(',').append(cur.getLong(0));
		}
		cur.close();	
		inSelectionBff.append(')');

		Cursor contactIdCursor =  mContext.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,  
				new String[] { ContactsContract.RawContacts.CONTACT_ID }, inSelectionBff.toString(), null, ContactsContract.Contacts.DISPLAY_NAME+"  COLLATE LOCALIZED asc "); 
		Map<Integer,Integer> map=new HashMap<Integer,Integer>();  
		while (contactIdCursor.moveToNext()) {  
			map.put(contactIdCursor.getInt(0), 1);  
		}  
		contactIdCursor.close(); 

		Set<Integer> set = map.keySet();
		Iterator<Integer> iter = set.iterator();
		List<ContactBean> list=new ArrayList<ContactBean>();
		while(iter.hasNext()){
			Integer key = iter.next();
			list.add(queryMemberOfGroup(key));
		}
		setAdapterContacts(list);
	}

	private ContactBean queryMemberOfGroup(int id){
		ContactBean cb = null;
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; // 查询的列
		Cursor cursor = mContext.getContentResolver().query(uri, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			listContacts = new ArrayList<ContactBean>();
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(1);
				String number = cursor.getString(2);
				String sortKey = cursor.getString(3);
				int contactId = cursor.getInt(4);
				Long photoId = cursor.getLong(5);
				String lookUpKey = cursor.getString(6);

				cb = new ContactBean();
				cb.setDisplayName(name);
//				if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
//					cb.setPhoneNum(number.substring(3));
//				} else {
					cb.setPhoneNum(number);
//				}
				cb.setSortKey(sortKey);
				cb.setContactId(contactId);
				cb.setPhotoId(photoId);
				cb.setLookUpKey(lookUpKey);
			}
		}
		cursor.close();
		return cb;
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
				
				listContacts = new ArrayList<ContactBean>();
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
//					if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
//						cb.setPhoneNum(number.substring(3));
//					} else {
						cb.setPhoneNum(number);
//					}
						cb.setSortKey(sortKey);
						cb.setContactId(contactId);
						cb.setPhotoId(photoId);
						cb.setLookUpKey(lookUpKey);
						listContacts.add(cb);
						contactIdMap.put(contactId, cb);
					}
				}
				if (listContacts.size() > 0) {
					setAdapterContacts(listContacts);
				}
			}
		}

	}
	
	
	private void setAdapterContacts(List<ContactBean> list) {
		adapterContacts = new ContactHomeAdapter(mContext, list, alpha);
		personList.setAdapter(adapterContacts);
		alpha.init(mChat);
		alpha.setListView(personList);
		alpha.setHight(ssHight);
		alpha.setVisibility(View.VISIBLE);
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapterContacts.getItem(position);
//				showContactDialog(lianxiren1, cb, position);
			}
		});
	}
	
	
	private void initContacts(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; // 查询的列
		asyncQueryContacts.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
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
		return mChat;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
