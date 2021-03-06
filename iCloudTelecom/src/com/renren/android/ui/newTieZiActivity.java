package com.renren.android.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
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
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class newTieZiActivity extends Activity implements OnClickListener, Callback{

	TextView originTitle, titleT, contentT;
	RadioButton send;
	ProgressDialog pDialog;
	Handler mHandler;
	BaseApplication mApp;
	String category;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tiezi_layout);
		
		mApp = (BaseApplication) getApplication();
		originTitle = (TextView) findViewById(R.id.originTitle);
		category = getIntent().getStringExtra("category");
		originTitle.setText("知道 -- " + category);
		
		titleT = (TextView) findViewById(R.id.titleT);
		contentT = (TextView) findViewById(R.id.contentT);
		send = (RadioButton) findViewById(R.id.send);
		send.setOnClickListener(this);
		
		mHandler = new Handler(this);
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
		case R.id.send:
			if ( titleT.getText().length()< 1 || contentT.getText().length()< 1 ){
				return;
			}
			
			pDialog.show();
			final String title = "new-vegastar#" + titleT.getText().toString() + "#" + category;
			final String content = contentT.getText().toString();
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					HttpPost httpRequest = new HttpPost(BaseApplication.Server_Address);
					List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
					Vaparams.add(new BasicNameValuePair("fatie", title));
					Vaparams.add(new BasicNameValuePair("fatie_content",  content));
					
					try {
						httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
						HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							String preTel = mApp.retrieveInputStream(httpResponse.getEntity());
							
							if ( preTel.contains("成功") ){
								
							}
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
			
		case 2:
			break;

		default:
			break;
		}
		return false;
	}

}
