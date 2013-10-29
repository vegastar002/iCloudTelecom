package com.renren.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;
import com.renren.android.BaseApplication.TieItems;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;

public class BBS {
	private Context mContext;
	private View mBBS, askbar_layout, utilbar_layout, moreView;
	private BaseApplication mApp;
	private Activity mActivity;

	private ImageView mFlip, mAddFriends, fatieBTN, freshBtn;
	private OnOpenListener mOnOpenListener;
	private ViewPager viewPager;
	private RadioButton askbar, utilbar;
	private List<View> manyViews;
	private ListView askList = null;
//	private PullListAdapter plAdapter;
	public TextView moreTV;
	private ArrayList<Map<String, String>> array;
//	private ProgressDialog pDialog;
	public TextView moreTxt;
	private final int step=10; //每次加载的数目
	private ProgressDialog pDialog;
	private final static int MSG_GET_TIEZI_LIST = 1;
	private final static int MSG_GET_TIEZI_FAILUE = 2;
	private MyAdapter adapter;
	private ListView listView;
	public List<TieItems> spTieItems = new ArrayList<TieItems>();
	private LinearLayout more_buttom;
	private ProgressBar prograssing;
	
	
	
	public BBS(BaseApplication application, Context context, Activity ak) {
		mContext = context;
		mApp = application;
		mActivity = ak;
		
		mBBS = LayoutInflater.from(context).inflate(R.layout.bbs, null);

		findViewById();
		setListener();
	}

	private void findViewById() {
		pDialog = new ProgressDialog(mContext);
		pDialog.setMessage("获取列表中...");
		pDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
				}
				return true;
			}
		});
		

		for (int i = 0; i < mApp.mTieItems.size(); i++) {
			if ( i >= 10 ) break;
			spTieItems.add(mApp.mTieItems.get(i));
		}
		
		viewPager = (ViewPager) mBBS.findViewById(R.id.bbsPage);
		manyViews = new ArrayList<View>();
		
		LayoutInflater mInflater = mActivity.getLayoutInflater();
		askbar_layout = mInflater.inflate(R.layout.askbar_layout, null);
		utilbar_layout = mInflater.inflate(R.layout.company_group_list, null);
//		moreView = mInflater.inflate(R.layout.more_request_dialog, null);
		
		manyViews.add(askbar_layout);
		manyViews.add(utilbar_layout);
		
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
		
		askbar = (RadioButton) mBBS.findViewById(R.id.askbar);
		askbar.setText(mApp.Page_1_Wenba);
		askbar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(0);
			}
		});
		
		utilbar = (RadioButton) mBBS.findViewById(R.id.utilbar);
		utilbar.setText(mApp.Page_2_Xian);
		utilbar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
			}
		});
		
		
		fatieBTN = (ImageView) mBBS.findViewById(R.id.fatieBTN);
		fatieBTN.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String category="";
				if ( viewPager.getCurrentItem() == 0 ){
					category = mApp.Page_1_Wenba;
				}
				else if ( viewPager.getCurrentItem() == 1 ) {
					category = mApp.Page_2_Xian;
				}
				Intent mIntent = new Intent(mActivity, newTieZiActivity.class);
				mIntent.putExtra("category", category);
				mContext.startActivity(mIntent);
			}
		});
		
		
		freshBtn = (ImageView) mBBS.findViewById(R.id.freshBtn);
		freshBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pDialog.show();
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						HttpPost httpRequest = new HttpPost(mApp.Server_Address);
						List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
						Vaparams.add(new BasicNameValuePair("fatie", "catch" ));
						
						try {
							httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
							HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

							if (httpResponse.getStatusLine().getStatusCode() == 200) {
								String preTel = mApp.retrieveInputStream(httpResponse.getEntity());
//								Log.i("", preTel);
								
								Message msg = new Message();
								msg.what = MSG_GET_TIEZI_LIST;
								msg.obj = preTel;
								mHandler.sendMessage(msg);
							}
						} catch (Exception e) {
							e.printStackTrace();
							Message msg = new Message();
							msg.what = MSG_GET_TIEZI_FAILUE;
							mHandler.sendMessage(msg);
						}
						
						
					}
				}).start();
				
				
			}
		});
		
		
		
		
		
		moreTxt = (TextView) askbar_layout.findViewById(R.id.moreTxt);
		prograssing = (ProgressBar) askbar_layout.findViewById(R.id.prograssing);
		
		more_buttom = (LinearLayout) askbar_layout.findViewById(R.id.more_buttom);
		more_buttom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				prograssing.setVisibility(View.VISIBLE);
				loaddata();
			}
		});
		
		
		listView = (ListView) askbar_layout.findViewById(R.id.askList);
		adapter = new MyAdapter(mContext);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						more_buttom.setVisibility(View.VISIBLE);
					}else {
						more_buttom.setVisibility(View.GONE);
					}
					break;
					
				case OnScrollListener.SCROLL_STATE_FLING:
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						more_buttom.setVisibility(View.VISIBLE);
					}else {
						more_buttom.setVisibility(View.GONE);
					}
	                break;
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				Log.i("", "position> "+ position);
//				if( position == 10){
//					pb=(ProgressBar) view.findViewById(R.id.pbloading);
//					more=(TextView) view.findViewById(R.id.tasksubmit_tvloading);
//					pb.setVisibility(View.VISIBLE);
//					more.setText("");
//					new Thread(){
//						public void run() {
//							loaddata();
////							hh.sendEmptyMessage(0);
//							mHandler.sendEmptyMessage(60);
//						}
//					}.start();
//				}
				
				Intent mIntent = new Intent(mContext, TieZiDetail.class);
				mIntent.putExtra("title", spTieItems.get(position).title);
				mIntent.putExtra("auth", spTieItems.get(position).auth);
				mIntent.putExtra("time", spTieItems.get(position).time);
				mIntent.putExtra("NoID", spTieItems.get(position).NoID);
				mIntent.putExtra("content", spTieItems.get(position).zcontent);
				mContext.startActivity(mIntent);
			}
		});
		
		
		mFlip = (ImageView) mBBS.findViewById(R.id.chat_flip);
		mAddFriends = (ImageView) mBBS.findViewById(R.id.chat_addfriends);
	}

	
	public class MyAdapter extends BaseAdapter{
		Context context;
		int count;
		LayoutInflater inflater;
		
		public MyAdapter(Context context) {
			this.context=context;
			inflater=LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			count = spTieItems.size();
			return count;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView=inflater.inflate(R.layout.item, null);
			TextView title = (TextView) convertView.findViewById(R.id.title);
			TextView auth = (TextView) convertView.findViewById(R.id.auth);
			TextView time = (TextView) convertView.findViewById(R.id.time);
			TextView numc = (TextView) convertView.findViewById(R.id.numc);
			
			title.setText(spTieItems.get(position).title);
			auth.setText(spTieItems.get(position).auth);
			time.setText(spTieItems.get(position).time);
			numc.setText(spTieItems.get(position).numHuiTie);
			
			return convertView;
		}

	}
	
	
	
	 Handler hh=new Handler(){
	    	public void handleMessage(android.os.Message msg) {
	    		adapter.notifyDataSetChanged();
	    	}
	    };
	    
	    
	protected void loaddata() {
		int curCount = spTieItems.size();
		if ( curCount == mApp.mTieItems.size() ){
			Toast.makeText(mContext, "数据加载完毕", Toast.LENGTH_SHORT).show();
			more_buttom.setVisibility(View.GONE);
			prograssing.setVisibility(View.GONE);
			return;
		}
		
		for (int i = curCount; i < mApp.mTieItems.size(); i++) {
			if ( i >= step+curCount ) break;
			spTieItems.add(mApp.mTieItems.get(i));
		}
		adapter.notifyDataSetChanged();
		more_buttom.setVisibility(View.GONE);
		prograssing.setVisibility(View.GONE);
	}
	
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_GET_TIEZI_LIST:
				String xmlString = msg.obj.toString();
				mApp.catNetTie(xmlString);
				adapter.notifyDataSetChanged();
				pDialog.dismiss();
				break;
				
			case MSG_GET_TIEZI_FAILUE:
				pDialog.dismiss();
				Toast.makeText(mContext, "获取失败", Toast.LENGTH_SHORT).show();
				break;
				
			case 60:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
			
		}
	};
	
	
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
				utilbar.setChecked(false);
//				toggleButton();
				askbar.setChecked(true);
				
				break;
			case 1:
//				toggleButton();
				utilbar.setChecked(true);
				askbar.setChecked(false);
				break;
			default:
				break;
			}
		}
		
	}
	
	public void toggleButton(){
		utilbar.setChecked(false);
		askbar.setChecked(false);
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
		return mBBS;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
