package com.renren.android.appscenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xu.ye.bean.CallLogBean;
import xu.ye.view.adapter.HomeDialAdapter;
import xu.ye.view.adapter.T9Adapter;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;

public class AppsCenter {
	private Context mContext;
	private BaseApplication mApp;
	private Activity mActivity;
	private View mCallLog, exCallLogView;

	private ImageView mFlip;
	private ViewPager viewPager;
	private RadioButton team_chat_btn, friends_chat_btn;
	private OnOpenListener mOnOpenListener;
	private List<View> manyViews;
	private LinearLayout bohaopan, keyboard_show_ll;
	private Button keyboard_show, phone_view, delete;
	private ListView callLogList;
	private AsyncQueryHandler asyncQueryCallLog;
	private List<CallLogBean> listCallLog;
	private HomeDialAdapter adapterCallLog;
	private SoundPool spool;
	private AudioManager am = null;
	private Map<Integer, Integer> mapSound = new HashMap<Integer, Integer>();
	private T9Adapter t9Adapter;

	
	
	public AppsCenter(BaseApplication application, Context context, Activity ak) {
		mContext = context;
		mApp = application;
		mActivity = ak;
		
		mCallLog = LayoutInflater.from(context).inflate(R.layout.home_dial_page, null);

		findViewById();
//		setListener();
	}

	private void findViewById() {
		viewPager = (ViewPager) mCallLog.findViewById(R.id.contacts_vPager);
		manyViews = new ArrayList<View>();
		LayoutInflater mInflater = mActivity.getLayoutInflater();
		exCallLogView = mInflater.inflate(R.layout.ex_list_call_log, null);
		View coView = mInflater.inflate(R.layout.company_group_list, null);
		
		manyViews.add(exCallLogView);
		manyViews.add(coView);
		viewPager.setAdapter(new MyPagerAdapter(manyViews));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new XTOnPageChangeListener());
		viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		team_chat_btn = (RadioButton) mCallLog.findViewById(R.id.team_chat_btn);
		team_chat_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
			}
		});
		
		friends_chat_btn = (RadioButton) mCallLog.findViewById(R.id.friends_chat_btn);
		friends_chat_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(0);
			}
		});
		
		bohaopan = (LinearLayout) exCallLogView.findViewById(R.id.bohaopan);
		keyboard_show_ll = (LinearLayout) exCallLogView.findViewById(R.id.keyboard_show_ll);
		keyboard_show = (Button) exCallLogView.findViewById(R.id.keyboard_show);
		callLogList = (ListView) exCallLogView.findViewById(R.id.call_log_list);
		asyncQueryCallLog = new CallLogAsyncQueryHandler(mContext.getContentResolver());
		
		keyboard_show.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
		
		am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
		mapSound.put(0, spool.load(mContext, R.raw.dtmf0, 0));
		mapSound.put(1, spool.load(mContext, R.raw.dtmf1, 0));
		mapSound.put(2, spool.load(mContext, R.raw.dtmf2, 0));
		mapSound.put(3, spool.load(mContext, R.raw.dtmf3, 0));
		mapSound.put(4, spool.load(mContext, R.raw.dtmf4, 0));
		mapSound.put(5, spool.load(mContext, R.raw.dtmf5, 0));
		mapSound.put(6, spool.load(mContext, R.raw.dtmf6, 0));
		mapSound.put(7, spool.load(mContext, R.raw.dtmf7, 0));
		mapSound.put(8, spool.load(mContext, R.raw.dtmf8, 0));
		mapSound.put(9, spool.load(mContext, R.raw.dtmf9, 0));
		mapSound.put(11, spool.load(mContext, R.raw.dtmf11, 0));
		mapSound.put(12, spool.load(mContext, R.raw.dtmf12, 0));
		
		phone_view = (Button) exCallLogView.findViewById(R.id.phone_view);
		phone_view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().toString().length() >= 4) {
					call(phone_view.getText().toString());
				}
			}
		});
		
		final ListView listView = (ListView) exCallLogView.findViewById(R.id.contact_list);
		phone_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(null == mApp.getContactBeanList() || mApp.getContactBeanList().size()<1 || "".equals(s.toString())){
					listView.setVisibility(View.GONE);
					callLogList.setVisibility(View.VISIBLE);
				}else{
					if(null == t9Adapter){
						t9Adapter = new T9Adapter(mContext);
						t9Adapter.assignment(mApp.getContactBeanList());
//						TextView tv = new TextView(HomeDialActivity.this);
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
						callLogList.setVisibility(View.GONE);
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
		
		delete = (Button) exCallLogView.findViewById(R.id.delete);
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				delete();
			}
		});
		delete.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				phone_view.setText("");
				return false;
			}
		});
		
		exCallLogView.findViewById(R.id.dialNum0).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(0);
					input(v.getTag().toString());
				}
			}
		});
		
		exCallLogView.findViewById(R.id.dialNum1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(1);
					input(v.getTag().toString());
				}
			}
		});
		
		exCallLogView.findViewById(R.id.dialNum2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(2);
					input(v.getTag().toString());
				}
			}
		});


		exCallLogView.findViewById(R.id.dialNum3).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(3);
					input(v.getTag().toString());
				}
			}
		});

		exCallLogView.findViewById(R.id.dialNum4).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(4);
					input(v.getTag().toString());
				}
			}
		});

		exCallLogView.findViewById(R.id.dialNum5).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(5);
					input(v.getTag().toString());
				}
			}
		});

		exCallLogView.findViewById(R.id.dialNum6).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(6);
					input(v.getTag().toString());
				}
			}
		});

		exCallLogView.findViewById(R.id.dialNum7).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(7);
					input(v.getTag().toString());
				}
			}
		});

		exCallLogView.findViewById(R.id.dialNum8).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(8);
					input(v.getTag().toString());
				}
			}
		});


		exCallLogView.findViewById(R.id.dialNum9).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone_view.getText().length() < 12) {
					play(9);
					input(v.getTag().toString());
				}
			}
		});


		initCallLog();
		
		mFlip = (ImageView) mCallLog.findViewById(R.id.chat_flip);
	}

	
	private void initCallLog(){
		Uri uri = CallLog.Calls.CONTENT_URI;
		
		String[] projection = { 
				CallLog.Calls.DATE,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls._ID
		}; // 查询的列
		asyncQueryCallLog.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
	}
	
	
	private class CallLogAsyncQueryHandler extends AsyncQueryHandler {

		public CallLogAsyncQueryHandler(ContentResolver cr) {
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
					setAdapterCallLog(listCallLog);
				}
			}
		}

	}
	
	
	private void setAdapterCallLog(List<CallLogBean> list) {
		adapterCallLog = new HomeDialAdapter(mContext, list);
//		TextView tv = new TextView(this);
//		tv.setBackgroundResource(R.drawable.dial_input_bg2);
//		callLogList.addFooterView(tv);
		callLogList.setAdapter(adapterCallLog);
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
	
	public void dialPadShow(){
		if(bohaopan.getVisibility() == View.VISIBLE){
			bohaopan.setVisibility(View.GONE);
			keyboard_show_ll.setVisibility(View.VISIBLE);
		}else{
			bohaopan.setVisibility(View.VISIBLE);
			keyboard_show_ll.setVisibility(View.GONE);
		}
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
		mContext.startActivity(it);
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
		return mCallLog;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
