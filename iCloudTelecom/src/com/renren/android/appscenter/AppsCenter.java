package com.renren.android.appscenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xu.ye.bean.CallLogBean;
import xu.ye.bean.ContactBean;
import xu.ye.bean.GroupBean;
import xu.ye.view.adapter.ContactHomeAdapter;
import xu.ye.view.adapter.HomeDialAdapter;
import xu.ye.view.adapter.T9Adapter;
import xu.ye.view.ui.QuickAlphabeticBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.renren.android.desktop.Desktop.onChangeViewListener;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;

public class AppsCenter {
	private View mAppsCenter;
	private ImageView mFlip;
	private OnOpenListener mOnOpenListener;
	private BaseApplication mApp;
	private Context mContext;
	private Activity mActivity;
	
	
	private AsyncQueryHandler asyncQuery;
	
	private HomeDialAdapter adapter;
	private ListView callLogList;
	
	private List<CallLogBean> listCallLog;
	private ArrayList<ContactBean> listContact;
	
	
	private LinearLayout bohaopan;
	private LinearLayout keyboard_show_ll;
	private Button keyboard_show;
	
	private Button phone_view;
	private Button delete;
	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	private SoundPool spool;
	private AudioManager am = null;
	
	private ListView listView;
	private T9Adapter t9Adapter;
	private onChangeViewListener mOnChangeViewListener;
	ViewPager viewPager;
	List<View> lists;
	RadioButton team_chat_btn, friends_chat_btn;
	View exView, coView;
	private ListView personList;
	private QuickAlphabeticBar alpha;
	private String ACTION1 = "SET_DEFAULT_SIG";
	private BaseReceiver1 receiver1 = null;
	
	
	public AppsCenter(BaseApplication mApplication, Context context, Activity activity) {
		mApp = mApplication;
		mContext = context;
		mActivity = activity;
		
//		mAppsCenter = LayoutInflater.from(context).inflate(R.layout.appscenter,	null);
		mAppsCenter = LayoutInflater.from(context).inflate(R.layout.home_dial_page,	null);
		
		viewPager = (ViewPager) mAppsCenter.findViewById(R.id.contacts_vPager);
		lists = new ArrayList<View>();
		LayoutInflater mInflater = activity.getLayoutInflater();
		exView = mInflater.inflate(R.layout.ex_list_call_log, null);
		coView = mInflater.inflate(R.layout.home_contact_page, null);
		
		lists.add(exView);
		lists.add(coView);
		viewPager.setAdapter(new MyPagerAdapter(lists));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new XTOnPageChangeListener());
		viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		findViewById();
		findCoView();
		setListener();
		
		keyboard_show.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
		
		am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
		map.put(0, spool.load(mContext, R.raw.dtmf0, 0));
		map.put(1, spool.load(mContext, R.raw.dtmf1, 0));
		map.put(2, spool.load(mContext, R.raw.dtmf2, 0));
		map.put(3, spool.load(mContext, R.raw.dtmf3, 0));
		map.put(4, spool.load(mContext, R.raw.dtmf4, 0));
		map.put(5, spool.load(mContext, R.raw.dtmf5, 0));
		map.put(6, spool.load(mContext, R.raw.dtmf6, 0));
		map.put(7, spool.load(mContext, R.raw.dtmf7, 0));
		map.put(8, spool.load(mContext, R.raw.dtmf8, 0));
		map.put(9, spool.load(mContext, R.raw.dtmf9, 0));
		map.put(11, spool.load(mContext, R.raw.dtmf11, 0));
		map.put(12, spool.load(mContext, R.raw.dtmf12, 0));
		
		
		init();
		startReceiver1();
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
					initContactUri();
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
				+"' AND "+ContactsContract.Data.DATA1+"="+ gb.getId(),     
				null,  
				"data1 asc"); 

		StringBuilder inSelectionBff = new StringBuilder().append(ContactsContract.RawContacts._ID + " IN ( 0");
		while(cur.moveToNext()){
			inSelectionBff.append(',').append(cur.getLong(0));
		}
		cur.close();	
		inSelectionBff.append(')');

		Cursor contactIdCursor =  mContext.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,  
				new String[] { ContactsContract.RawContacts.CONTACT_ID }, inSelectionBff.toString(), null, ContactsContract.Contacts.DISPLAY_NAME+"  COLLATE LOCALIZED asc "); 
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();  
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
		setAdapterContact(list);
	}

	private void setAdapterContact(List<ContactBean> list) {
		final ContactHomeAdapter adapter = new ContactHomeAdapter(mContext, list, alpha);
		personList.setAdapter(adapter);
		alpha.init(mActivity);
		alpha.setListView(personList);
		alpha.setHight(alpha.getHeight());
		alpha.setVisibility(View.VISIBLE);
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
//				showContactDialog(lianxiren1, cb, position);
			}
		});
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
			listContact = new ArrayList<ContactBean>();
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
	
	private void startReceiver1() {
		if(null==receiver1){
			IntentFilter localIntentFilter = new IntentFilter(ACTION1);
			receiver1 = new BaseReceiver1();
			mContext.registerReceiver(receiver1, localIntentFilter);
		}
	}
	
	
	public void findCoView(){
		personList = (ListView) coView.findViewById(R.id.acbuwa_list);
		alpha = (QuickAlphabeticBar) coView.findViewById(R.id.fast_scroller);
		asyncQuery = new MyAsyncQueryHandler(mContext.getContentResolver());
		initContactUri();
	}
	
	
	private void initContactUri(){
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
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}
	
	
	class XTOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int page) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int page) {
			switch (page) {
			case 0:
				friends_chat_btn.setChecked(true);
				team_chat_btn.setChecked(false);
				break;
			case 1:
				friends_chat_btn.setChecked(false);
				team_chat_btn.setChecked(true);
				break;
			default:
				break;
			}
		}
		
	}
	
	
	
	class MyPagerAdapter extends PagerAdapter{
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListViews.size();
		}
		
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (arg1);
		}
		
	}
	
	
	private void init(){
		Uri uri = CallLog.Calls.CONTENT_URI;
		
		String[] projection = { 
				CallLog.Calls.DATE,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls._ID
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
	}
	

	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				listCallLog = new ArrayList<CallLogBean>();
				SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
				Date date;
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
//					String date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
					String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
					int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
					String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
					int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));

					CallLogBean clb = new CallLogBean();
					clb.setId(id);
					clb.setNumber(number);
					clb.setName(cachedName);
					if(null == cachedName || "".equals(cachedName)){
						clb.setName(number);
					}
					clb.setType(type);
					clb.setDate(sfd.format(date));
					
					listCallLog.add(clb);
				}
				if (listCallLog.size() > 0) {
					setAdapter(listCallLog);
				}
			}
		}

	}


	private void setAdapter(List<CallLogBean> list) {
		adapter = new HomeDialAdapter(mContext, list);
//		TextView tv = new TextView(mContext);
//		tv.setBackgroundResource(R.drawable.dial_input_bg2);
//		callLogList.addFooterView(tv);
		callLogList.setAdapter(adapter);
		callLogList.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					if(bohaopan.getVisibility() == View.VISIBLE){
						bohaopan.setVisibility(View.GONE);
						keyboard_show_ll.setVisibility(View.VISIBLE);
					}
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
		
		callLogList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				
			}
		});
	}
	
	
	private void play(int id) {
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);

		float value = (float)0.7 / max * current;
		spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
	}
	private void input(String str) {
		String p = phone_view.getText().toString();
		phone_view.setText(p + str);
	}
	private void delete() {
		String p = phone_view.getText().toString();
		if(p.length()>0){
			phone_view.setText(p.substring(0, p.length()-1));
		}
	}
	private void call(String phone) {
		Uri uri = Uri.parse("tel:" + phone);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		mActivity.startActivityForResult(it, -1);
	}
	
	
	public void dialPadShow(){
		if(bohaopan.getVisibility() == View.VISIBLE){
			bohaopan.setVisibility(View.GONE);
			keyboard_show_ll.setVisibility(View.VISIBLE);
		}else{
			bohaopan.setVisibility(View.VISIBLE);
			keyboard_show_ll.setVisibility(View.GONE);
		}
	}
	
	
	private void findViewById(){
		mFlip = (ImageView)mAppsCenter.findViewById(R.id.appscenter_flip);
		
		listView = (ListView) exView.findViewById(R.id.contact_list);
		bohaopan = (LinearLayout) exView.findViewById(R.id.bohaopan);
		keyboard_show_ll = (LinearLayout) exView.findViewById(R.id.keyboard_show_ll);
		keyboard_show = (Button) exView.findViewById(R.id.keyboard_show);
		callLogList = (ListView) exView.findViewById(R.id.call_log_list);
		asyncQuery = new MyAsyncQueryHandler(mContext.getContentResolver());
		
		
		team_chat_btn = (RadioButton) mAppsCenter.findViewById(R.id.team_chat_btn);
		team_chat_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				viewPager.setCurrentItem(1);
			}
		});
		
		
		friends_chat_btn = (RadioButton) mAppsCenter.findViewById(R.id.friends_chat_btn);
		friends_chat_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				viewPager.setCurrentItem(0);
			}
		});
		
		phone_view = (Button) exView.findViewById(R.id.phone_view);
		phone_view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call(phone_view.getText().toString());
				phone_view.setText("");
			}
		});
		
		phone_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(null == mApp.getContactBeanList() || mApp.getContactBeanList().size() < 1 || "".equals(s.toString())){
					listView.setVisibility(View.INVISIBLE);
					callLogList.setVisibility(View.VISIBLE);
				}else{
					if(null == t9Adapter){
						t9Adapter = new T9Adapter(mContext);
						t9Adapter.assignment(mApp.getContactBeanList());
//						TextView tv = new TextView(HomeDialActivity.mContext);
//						tv.setBackgroundResource(R.drawable.dial_input_bg2);
//						listView.addFooterView(tv);
						listView.setAdapter(t9Adapter);
						listView.setTextFilterEnabled(true);
						listView.setOnScrollListener(new OnScrollListener() {
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
									if(bohaopan.getVisibility() == View.VISIBLE){
										bohaopan.setVisibility(View.GONE);
										keyboard_show_ll.setVisibility(View.VISIBLE);
									}
								}
							}
							public void onScroll(AbsListView view, int firstVisibleItem,
									int visibleItemCount, int totalItemCount) {
							}
						});
					}else{
						callLogList.setVisibility(View.INVISIBLE);
						listView.setVisibility(View.VISIBLE);
						t9Adapter.getFilter().filter(s);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void afterTextChanged(Editable s) {
			}
		});
		
		
		exView.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				delete();
			}
		});
		
		
		exView.findViewById(R.id.dialNum1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(1);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(2);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum3).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(3);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum4).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(4);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum5).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(5);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum6).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(6);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum7).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(7);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum8).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(8);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum9).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(9);
					input(v.getTag().toString());
				}
			}
		});
		
		exView.findViewById(R.id.dialNum0).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(0);
					input(v.getTag().toString());
				}
			}
		});
		
	}
	
	
	private void setListener(){
		mFlip.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (mOnOpenListener!=null) {
					mOnOpenListener.open();
				}
			}
		});
	}
	
	
	public View getView() {
		return mAppsCenter;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
