/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.voice.demo.outboundmarketing;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.Response;
import com.hisun.phone.core.voice.net.HttpHelper;
import com.hisun.phone.core.voice.token.Base64;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.voice.demo.outboundmarketing.model.LandingCall;
import com.voice.demo.tools.CCPConfig;

/**
 * Tell developer how can get sub-account and sub-password etc. by REST network
 * interface.
 * 
 * @version 1.0.0
 */
public class RestHelper {

	public static final String TAG = RestHelper.class.getSimpleName();
	private static RestHelper instance;
	
	public static final int KEY_LANDING_CALL = 970101;
	public static final int KEY_VERIFY_CODE = 970102;

	public static RestHelper getInstance() {
		if(instance == null ) {
			instance = new RestHelper();
		}
		
		return instance;
	}
	private onRestHelperListener mListener;

	// Private Constructs
	private RestHelper() {

	}
	
	public enum ERequestState {
		Success ,Failed
	}

	// 营销外呼...
	public void LandingCalls(String toVoip ,String audioName ,String appId , String diaplayNum) {
		// 时间戳
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());
		// md5(主账户ID + 主账户授权令牌 + 时间戳)
		String sig = VoiceUtil.md5(CCPConfig.Main_Account
				+ CCPConfig.Main_Token + formatTimestamp);

		// request url
		StringBuffer url = new StringBuffer("https://"
				+ CCPConfig.REST_SERVER_ADDRESS + ":"
				+ CCPConfig.REST_SERVER_PORT);
		url.append("/2013-03-22/Accounts/").append(CCPConfig.Main_Account)
				.append("/Calls/LandingCalls?sig=").append(sig);
		Log4Util.d(Device.TAG, "url: " + url + "\r\n");

		// check url
		if (!URLUtil.isHttpsUrl(url.toString())) {
			throw new RuntimeException("address invalid.");
		}

		// request header
		String authorization = Base64
				.encode((CCPConfig.Main_Account + ":" + formatTimestamp)
						.getBytes());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/xml");
		headers.put("Content-Type", "application/xml;charset=utf-8");
		headers.put("Authorization", authorization);

		// request body
		// marketingcall.wav
		final StringBuffer requestBody = new StringBuffer("<LandingCall>\r\n");
		requestBody.append("\t<appId>").append(appId).append("</appId>\r\n");
		if(!TextUtils.isEmpty(audioName)) {
			requestBody.append("\t<mediaName>").append(audioName).append("</mediaName>\r\n");
		} else {
			requestBody.append("\t<mediaName>").append("marketingcall.wav").append("</mediaName>\r\n");
		}
		requestBody.append("\t<to>").append(toVoip).append("</to>\r\n");
		
		if(!TextUtils.isEmpty(diaplayNum)) {
			requestBody.append("\t<diaplayNum>").append(diaplayNum).append("</diaplayNum>\r\n");
		}
		requestBody.append("</LandingCall>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url.toString(), headers,
					requestBody.toString());
			Log4Util.d(Device.TAG, xml + "\r\n");

			LandingCall landingCall = (LandingCall) doParser(KEY_LANDING_CALL,new ByteArrayInputStream(xml.getBytes()));
			if (landingCall != null && ! landingCall.isError()) {
				mListener.onLandingCAllsStatus(ERequestState.Success ,landingCall.callSid);
			} else {
				if (mListener != null) {
					mListener.onLandingCAllsStatus(ERequestState.Failed ,null);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mListener != null) {
				mListener.onLandingCAllsStatus(ERequestState.Failed ,null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}
	
	// 语音验证码..
	public void VoiceVerifyCode(String verifyCode ,String playTimes , String toVoip , String diaplayNum , String respUrl) {
		// 时间戳
		String formatTimestamp = VoiceUtil.formatTimestamp(System.currentTimeMillis());
		// md5(主账户ID + 主账户授权令牌 + 时间戳)
		String sig = VoiceUtil.md5(CCPConfig.Main_Account+ CCPConfig.Main_Token + formatTimestamp);

		// request url
		StringBuffer url = new StringBuffer("https://"+ CCPConfig.REST_SERVER_ADDRESS + ":"+ CCPConfig.REST_SERVER_PORT);
		url.append("/2013-03-22/Accounts/").append(CCPConfig.Main_Account).append("/Calls/VoiceVerify?sig=").append(sig);
		Log4Util.d(Device.TAG, "url: " + url + "\r\n");

		// check url
		if (!URLUtil.isHttpsUrl(url.toString())) {
			throw new RuntimeException("address invalid.");
		}

		// request header
		String authorization = Base64.encode((CCPConfig.Main_Account + ":" + formatTimestamp).getBytes());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/xml");
		headers.put("Content-Type", "application/xml;charset=utf-8");
		headers.put("Authorization", authorization);

		final StringBuffer requestBody = new StringBuffer("<VoiceVerify>\r\n");
		requestBody.append("\t<appId>").append(CCPConfig.App_ID).append("</appId>\r\n");
		requestBody.append("\t<verifyCode>").append(verifyCode).append("</verifyCode>\r\n");
		requestBody.append("\t<playTimes>").append(playTimes).append("</playTimes>\r\n");
		requestBody.append("\t<to>").append(toVoip).append("</to>\r\n");
		if(!TextUtils.isEmpty(diaplayNum)) {
			requestBody.append("\t<diaplayNum>").append(diaplayNum).append("</diaplayNum>\r\n");
		}
		if(!TextUtils.isEmpty(respUrl)) {
			requestBody.append("\t<respUrl>").append(respUrl).append("</respUrl>\r\n");
		}
		requestBody.append("</VoiceVerify>\r\n");
		Log4Util.i(TAG, requestBody.toString());

		try {
			String xml = HttpHelper.httpPost(url.toString(), headers,
					requestBody.toString());
			Log4Util.d(Device.TAG, xml + "\r\n");

			Response response = doParser(KEY_VERIFY_CODE,new ByteArrayInputStream(xml.getBytes()));
			
			if (response != null) {
				if (response.isError()) {
					if (mListener != null) {
						mListener.onVoiceCode(ERequestState.Failed);
					}
				} else {
					if (mListener != null) {
						mListener.onVoiceCode(ERequestState.Success);
					}
				}
			} else {
				if (mListener != null) {
					mListener.onVoiceCode(ERequestState.Failed);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mListener != null) {
				mListener.onVoiceCode(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}

	private  Response doParser(int parseType,InputStream is) throws Exception {
		if (is == null) {
			throw new IllegalArgumentException("resource is null.");
		}
		XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
		Response response = null;
		try {
			xmlParser.setInput(is, null);
			xmlParser.nextTag();
			String rootName = xmlParser.getName();
			if (!isRootNode(rootName)) {
				throw new IllegalArgumentException("xml root node is invalid.");
			}

			response = getResponseByKey(parseType);
			xmlParser.require(XmlPullParser.START_TAG, null, rootName);
			while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
				String tagName = xmlParser.getName();
				if (tagName != null && tagName.equals("statusCode")) {
					String text = xmlParser.nextText();
					response.statusCode = text;
				} else if (parseType == KEY_LANDING_CALL) {
					parseLandingCallBody(xmlParser, (LandingCall)response);
				} else if (parseType == KEY_VERIFY_CODE) {
					parseVerifyCodeBody(xmlParser, response);
				} else {
					xmlParser.nextText();
				}
			}
			xmlParser.require(XmlPullParser.END_TAG, null, rootName);
			xmlParser.next();
			xmlParser.require(XmlPullParser.END_DOCUMENT, null, null);
			print(response);
		} catch (Exception e) {
			e.printStackTrace();
			if (response != null) {
				response.released();
				response = null;
			}
			throw new Exception("parse xml occur errors:" + e.getMessage());
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
			xmlParser = null;
		}

		return response;
	}
	
	private void parseVerifyCodeBody(XmlPullParser xmlParser, Response response)  throws Exception {
		while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
			String tagName = xmlParser.getName();
			if (tagName.equals("accountSid")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			} else if (tagName.equals("callSid")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}  else if (tagName.equals("dateCreated")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}  else if (tagName.equals("status")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}  else if (tagName.equals("to")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}  else if (tagName.equals("uri")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}   else if (tagName.equals("verifyCode")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					
				}
			}else {
				xmlParser.nextText();
			}
		}
		
	}

	boolean isRootNode(String rootName) {
		if (rootName == null || (!rootName.equalsIgnoreCase("Response"))) {
			return false;
		}
		return true;
	}
	
	Response getResponseByKey(int key) {
		if(key == KEY_LANDING_CALL ) {
			return new LandingCall();
		}
		return new Response();
	}
	
	void print(Response r) {
		if (r != null) {
			r.print();
		}
	}
	
	private static void parseLandingCallBody(XmlPullParser xmlParser, LandingCall response) throws Exception {
		while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
			String tagName = xmlParser.getName();
			if (tagName.equals("callSid")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.callSid = text.trim();
				}
			} else if (tagName.equals("dateCreated")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.dateCreated = text.trim();
				}
			} else {
				xmlParser.nextText();
			}
		}
	}

	public void setOnRestHelperListener(onRestHelperListener mListener) {
		this.mListener = mListener;
	}

	public interface onRestHelperListener {
		void onLandingCAllsStatus(ERequestState reason , String callId);

		void onVoiceCode(ERequestState reason);
	}
}
