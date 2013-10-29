package com.renren.android.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.hust.wa.icloudtelecom.R;
import com.renren.android.BaseApplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class TieZiReply extends Activity implements OnClickListener, Callback{

	ImageView backImg;
	EditText input;
	RadioButton send;
	BaseApplication mApplication;
	Handler mHandler;
	ProgressDialog pDialog;
	Timer timer = new Timer();
	String NoID="" ;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tiezi_reply_send_layout);
		
		mApplication = (BaseApplication) getApplication();
		mHandler = new Handler(this);
		
		backImg = (ImageView) findViewById(R.id.backImg);
		backImg.setOnClickListener(this);
		input = (EditText) findViewById(R.id.input);
		
		send = (RadioButton) findViewById(R.id.send);
		send.setOnClickListener(this);
		
		String auth = getIntent().getStringExtra("auth");
		String time = getIntent().getStringExtra("time");
		String content = getIntent().getStringExtra("content");
		NoID = getIntent().getStringExtra("NoID");
//		String newString = content+"\r\n\t\t\t --------- "+ auth+"  "+time+"\r\n ========================= \r\n\r\n";
		
		String newString="";
		if ( auth != null ){
			newString = auth+"  "+time+"\r\n" + content+ "\r\n ------------------------- \r\n";
		}
		
		
		input.setText(newString);
		Spannable spanText = (Spannable)input.getText(); 
        Selection.setSelection(spanText,input.getText().length());
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 500);
		
		
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("提交中...");
		pDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
				}
				return true;
			}
		});
		
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backImg:
			finish();
			break;
			
		case R.id.send:
			pDialog.show();
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					String username = "jeff xia"; //这里要重新设计
					String replyPa = "reply-" + username +"#" + NoID;
					HttpPost httpRequest = new HttpPost(mApplication.Server_Address);
					List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
					Vaparams.add(new BasicNameValuePair("fatie", replyPa ));
					Vaparams.add(new BasicNameValuePair("fatie_content", input.getText().toString() ));
					
					try {
						httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
						HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							String preTel = mApplication.retrieveInputStream(httpResponse.getEntity());
//							Log.i("", preTel);
							Message msg = new Message();
							msg.what = 1;
							msg.obj = preTel;
							mHandler.sendMessage(msg);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
				}
			}).start();
			break;

		default:
			break;
		}
	}


	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 1:
			if ( pDialog.isShowing() ){
				pDialog.dismiss();
			}
			Toast.makeText(this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
			finish();
			break;

		default:
			break;
		}
		return false;
	}

}
