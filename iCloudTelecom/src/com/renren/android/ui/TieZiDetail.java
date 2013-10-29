package com.renren.android.ui;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;

public class TieZiDetail extends Activity implements OnClickListener, Callback{

	TextView title;
	ListView tieziList;
	TItem mItem;
	ImageView chat_flip, reply;
	String titleStr = "", auth = "", time = "", NoID = "", zcontent = "";
	LinearLayout prBar;
	BaseApplication mApplication;
	Handler mHandler;
	private final static int MSG_GET_GenTie_LIST = 1;
	private final static int MSG_GET_GenTie_FAILUE = 2;
	private final static int MSG_Update_Adapter = 3;
	public List<ReplyItems> mReplyItems = new ArrayList<ReplyItems>();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tiezi_detail_layout);
		
		mApplication = (BaseApplication) getApplication();
		mHandler = new Handler(this);
		
		
		titleStr =  getIntent().getStringExtra("title");
		auth =  getIntent().getStringExtra("auth");
		time =  getIntent().getStringExtra("time");
		NoID =  getIntent().getStringExtra("NoID");
		zcontent =  getIntent().getStringExtra("content");
		
		ReplyItems tis = new ReplyItems();
		tis.auth = auth;
		tis.time = time;
		tis.comment = zcontent;
		mReplyItems.add(tis);
		
		title = (TextView) findViewById(R.id.title);
		title.setText(titleStr);
		tieziList = (ListView) findViewById(R.id.tieziList);
		chat_flip = (ImageView) findViewById(R.id.chat_flip);
		chat_flip.setOnClickListener(this);
		
		reply = (ImageView) findViewById(R.id.reply);
		reply.setOnClickListener(this);
		
		mItem = new TItem();
		tieziList.setAdapter(mItem);
		prBar = (LinearLayout) findViewById(R.id.prBar);

	}
	


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpPost httpRequest = new HttpPost(mApplication.Server_Address);
				List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
				Vaparams.add(new BasicNameValuePair("fatie", "replyList-"+ NoID ));
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String preTel = mApplication.retrieveInputStream(httpResponse.getEntity());
//						Log.i("", "xml> " + preTel);
						
						Message msg = new Message();
						msg.what = MSG_GET_GenTie_LIST;
						msg.obj = preTel;
						mHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.what = MSG_GET_GenTie_FAILUE;
					mHandler.sendMessage(msg);
				}
				
				
			}
		}).start();
	}





	class TItem extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mReplyItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			ViewHolder holder;
			if (view == null || (holder = (ViewHolder) view.getTag()) == null) {
				view = View.inflate(TieZiDetail.this, R.layout.tiezi_items_one, null);
				holder = new ViewHolder();
				holder.auth = (TextView) view.findViewById(R.id.auth);
				holder.time = (TextView) view.findViewById(R.id.time);
				holder.content = (TextView) view.findViewById(R.id.content);
				holder.zhuozhe = (TextView) view.findViewById(R.id.zhuozhe);
				holder.replyImg = (ImageButton) view.findViewById(R.id.replyImg);
				
				view.setTag(holder);
			}
			
			if ( position == 0 ){
				holder.zhuozhe.setText("楼主: ");
			}else {
				holder.zhuozhe.setText("作者: ");
			}
			holder.auth.setText(mReplyItems.get(position).auth);
			holder.time.setText(mReplyItems.get(position).time);
			holder.content.setText(mReplyItems.get(position).comment);
			holder.replyImg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					Intent mIntent = new Intent(TieZiDetail.this, TieZiReply.class);
					mIntent.putExtra("auth", mReplyItems.get(position).auth);
					mIntent.putExtra("time", mReplyItems.get(position).time);
					mIntent.putExtra("content", mReplyItems.get(position).comment);
					mIntent.putExtra("NoID", NoID);
					startActivity(mIntent);
				}
			});
			
			
//			class lvButtonListener implements OnClickListener { 
//		        private int position ; 
//
//		        lvButtonListener( int pos) { 
//		            position = pos; 
//		        } 
//		        
//		        @Override 
//		        public void onClick( View v) {
//		            int vid= v.getId();
//		            if ( vid == holder.reply.getId () ){
//		            	
//		            }
//		        } 
//		    }
			
			
			return view;
		}
		
	}
	
	public class ViewHolder {
		public TextView time = null;
		public TextView auth = null;
		public TextView content = null;
		public TextView zhuozhe = null;
		public ImageButton replyImg = null;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.chat_flip:
			finish();
			break;

		case R.id.reply:
			Intent mIntent = new Intent(TieZiDetail.this, TieZiReply.class);
//			mIntent.putExtra("auth", mReplyItems.get(0).auth);
//			mIntent.putExtra("time", mReplyItems.get(0).time);
//			mIntent.putExtra("content", mReplyItems.get(0).comment);
			mIntent.putExtra("NoID", NoID);
			startActivity(mIntent);
			break;
		default:
			break;
		}
	}

	
	public void catReplyTie(String xmlString){
		try {
			mReplyItems.clear();
			ReplyItems tise = new ReplyItems();
			tise.auth = auth;
			tise.time = time;
			tise.comment = zcontent;
			mReplyItems.add(tise);
			
			
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
					ReplyItems tis = new ReplyItems();
					
					for (int k = 0; k < employeeMeta.getLength(); k++) {
						
						if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("comment") ){
							tis.comment = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("auth") ) {
							tis.auth = employeeMeta.item(k).getTextContent();
						}
						else if ( employeeMeta.item(k).getNodeName().equalsIgnoreCase("time") ) {
							tis.time = employeeMeta.item(k).getTextContent();
						}
						
					}
					
					mReplyItems.add(tis);
				}
			}

			if ( mReplyItems.size() > 0 ){
				Message msg = new Message();
				msg.what = MSG_Update_Adapter;
				mHandler.sendMessage(msg);
				
			}else {
				Message msg = new Message();
				msg.what = MSG_GET_GenTie_FAILUE;
				mHandler.sendMessage(msg);
			}
			
			
//			Log.i("", "跟贴条数> "+ mReplyItems.size());
//			for (int j = 0; j < mReplyItems.size(); j++) {
//				Log.i("", mReplyItems.get(j).auth+  mReplyItems.get(j).comment+ mReplyItems.get(j).time);
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
	
	public class ReplyItems{
		public String comment = "";
		public String auth = "";
		public String time = "";
	}
	
	
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_GET_GenTie_LIST:
			String xmlsString = msg.obj.toString();
			if ( xmlsString.equals("没有回贴") ){
				prBar.setVisibility(View.GONE);
				tieziList.setVisibility(View.VISIBLE);
//				Toast.makeText(TieZiDetail.this, xmlsString, Toast.LENGTH_SHORT).show();
				
			}else {
				catReplyTie(xmlsString);
			}
			
			
			break;
			
		case MSG_GET_GenTie_FAILUE:
			prBar.setVisibility(View.GONE);
			Toast.makeText(TieZiDetail.this, "获取失败", Toast.LENGTH_SHORT).show();
			break;

		case MSG_Update_Adapter:
			prBar.setVisibility(View.GONE);
			tieziList.setVisibility(View.VISIBLE);
			mItem.notifyDataSetChanged();
//			for (int j = 0; j < mReplyItems.size(); j++) {
//				Log.i("", "有> "+ mReplyItems.get(j).auth+  mReplyItems.get(j).comment+ mReplyItems.get(j).time);
//				
//			}
			break;
		default:
			break;
		}
		return false;
	}
	

}
