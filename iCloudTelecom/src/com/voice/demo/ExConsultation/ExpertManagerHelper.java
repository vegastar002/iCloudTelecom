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
package com.voice.demo.ExConsultation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.webkit.URLUtil;

import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.model.Response;
import com.hisun.phone.core.voice.net.HttpHelper;
import com.hisun.phone.core.voice.token.Base64;
import com.hisun.phone.core.voice.util.Log4Util;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.voice.demo.ExConsultation.model.CateGoryList;
import com.voice.demo.ExConsultation.model.Category;
import com.voice.demo.ExConsultation.model.Expert;
import com.voice.demo.ExConsultation.model.ExpertList;
import com.voice.demo.ExConsultation.model.ServiceNum;
import com.voice.demo.outboundmarketing.RestHelper.ERequestState;
import com.voice.demo.tools.CCPConfig;

/**
 * 
 * @author Jorstin Chan
 * @version 3.3
 */
public class ExpertManagerHelper {

	public static final String TAG = ExpertManagerHelper.class.getSimpleName();
	
	public static final int KEY_EXPERT_LIST 				= 900001;
 	public static final int KEY_CATEGORY_LIST 				= 900002;
 	public static final int KEY_LOCK_EXPERT					= 900003;
 	public static final int KEY_SERVICE_NUM 				= 900004;
 	
 	
 	public static final String LAWER_SERVER 					= "http://112.124.0.43:8700/lawyer/interface";
 	
 	
 	public static final String URI_XH_GET_LAWER_CLASSIC 		= LAWER_SERVER + "/getlawyerlist.php";
 	public static final String URI_XH_GET_CLIENT_CLASSIC 		= LAWER_SERVER + "/getcategorylist.php";
 	public static final String URI_XH_CALL_ACTION_TO_LAWER 		= LAWER_SERVER + "/locklawyer.php";
 	public static final String URI_XH_CALL_400_SERVER 			= LAWER_SERVER + "/getconfig.php";
	
	private static ExpertManagerHelper mInstance = null;
	
	private onExpertManagerHelpListener mExpertListener = null;
	
	public static ExpertManagerHelper getInstance(){
		if (mInstance == null) {
			mInstance = new ExpertManagerHelper();
		}
		return mInstance;
	}
	
	private ExpertManagerHelper(){
		
	}
	
	private StringBuffer getSubAccountRequestURL(int requestType, String formatTimestamp){
		// md5(���˻�ID + ���˻���Ȩ���� + ʱ���)
		String sig = VoiceUtil.md5(CCPConfig.Sub_Account
				+ CCPConfig.Sub_Token + formatTimestamp);

		// request url
		StringBuffer url = new StringBuffer("https://" + CCPConfig.REST_SERVER_ADDRESS + ":" + CCPConfig.REST_SERVER_PORT);
		url.append("/2013-03-22/SubAccounts/").append(CCPConfig.Sub_Account).append("/Consult");
		
		switch (requestType) {
		case KEY_SERVICE_NUM:
			url.append("/GetServiceNum");
			break;
		case KEY_CATEGORY_LIST:
			url.append("/GetCategoryList");
			break;
		case KEY_EXPERT_LIST:
			url.append("/GetExpertList");
			break;
		case KEY_LOCK_EXPERT:
			url.append("/LockExpert");
			break;
		default:
			break;
		}
		
		url.append("?sig=").append(sig);
		
		return url;
	}
	
	private HashMap<String, String> getSubAccountRequestHead(int requestType, String formatTimestamp){
		String authorization = Base64
				.encode((CCPConfig.Sub_Account + ":" + formatTimestamp)
						.getBytes());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/xml");
		headers.put("Content-Type", "application/xml;charset=utf-8");
		headers.put("Authorization", authorization);
		return headers;
	}
	
	public void getServiceNum(){
		int keyValue = KEY_SERVICE_NUM;
				
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());

		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		String url = urlBuf.toString();
		
		Log4Util.w(Device.TAG, "url: " + url + "\r\n");
		
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}

		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);
		
		try {
			String xml = HttpHelper.httpGet(url, headers);
			Log4Util.w(Device.TAG, xml + "\r\n");

			ServiceNum xConfig = (ServiceNum)  doLawerResponse(KEY_SERVICE_NUM, new ByteArrayInputStream(xml.getBytes()));
			
			if (mExpertListener != null){
				if (xConfig != null && ! xConfig.isError()) {
					mExpertListener.onGet400ServerPort(ERequestState.Success , xConfig);
				} else {
					mExpertListener.onGet400ServerPort(ERequestState.Failed ,null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mExpertListener != null) {
				mExpertListener.onGet400ServerPort(ERequestState.Failed , null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	/**
	 * ��ѯ�б�
	 */
	public void getCategoryList(){
		int keyValue = KEY_CATEGORY_LIST;
		
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());
		
		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		String url = urlBuf.toString();
		
		Log4Util.w(Device.TAG, "url: " + url + "\r\n");
		
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}
		
		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);
		
		try {
			String xml = HttpHelper.httpGet(url, headers);
			Log4Util.w(Device.TAG, xml + "\r\n");
			
			CateGoryList xCateGoryList = (CateGoryList) doLawerResponse(KEY_CATEGORY_LIST, new ByteArrayInputStream(xml.getBytes()));
			
			if (mExpertListener != null){
				if (xCateGoryList != null && ! xCateGoryList.isError()) {
					mExpertListener.onGetClientGategory(ERequestState.Success, xCateGoryList.xCategorys);
				} else {
					mExpertListener.onGetClientGategory(ERequestState.Failed ,null);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mExpertListener != null) {
				mExpertListener.onGetClientGategory(ERequestState.Failed ,null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
		
		
	}
	/**
	 * ��ȡ��ѯר���б�
	 */
	public void getExpertList(String cid){
		int keyValue = KEY_EXPERT_LIST;
		
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());
		
		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		urlBuf.append("&categoryId=").append(cid);
		String url = urlBuf.toString();
		
		Log4Util.w(Device.TAG, "url: " + url + "\r\n");
		
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}
		
		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);
		
		try {
			String xml = HttpHelper.httpGet(url, headers);
			Log4Util.w(Device.TAG, xml + "\r\n");
			
			ExpertList expertList = (ExpertList) doLawerResponse(KEY_EXPERT_LIST, new ByteArrayInputStream(xml.getBytes()));
			
			if (mExpertListener != null){
				if (expertList != null && ! expertList.isError()) {
					mExpertListener.onGetExpertClassic(ERequestState.Success, expertList.xExperts);
				} else {
					mExpertListener.onGetExpertClassic(ERequestState.Failed ,null);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mExpertListener != null) {
				mExpertListener.onGetExpertClassic(ERequestState.Failed ,null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
		
		
	}
	/**
	 * ��ȡ��ѯר���б�
	 */
	public void lockExpert(String expertId, String srcPhone){
		int keyValue = KEY_LOCK_EXPERT;
		
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());
		
		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		String url = urlBuf.toString();
		
		Log4Util.w(Device.TAG, "url: " + url + "\r\n");
		
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}
		
		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);
		
		final StringBuffer requestBody = new StringBuffer("<LockExpert>\r\n");
		requestBody.append("\t<expertId>").append(expertId).append("</expertId>\r\n");
		requestBody.append("\t<srcPhone>").append(srcPhone).append("</srcPhone>\r\n");
		requestBody.append("</LockExpert>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers ,requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");
			
			Response response =  doLawerResponse(KEY_LOCK_EXPERT, new ByteArrayInputStream(xml.getBytes()));
			
			if (mExpertListener != null){
				if (response != null && ! response.isError()) {
					mExpertListener.onActionLockExpert(ERequestState.Success);
				} else {
					mExpertListener.onActionLockExpert(ERequestState.Failed);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mExpertListener != null) {
				mExpertListener.onActionLockExpert(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
		
		
	}
	
	
	
/******************************************XH_SHOW************************************/
	
	/**
	 * ����400�������
	 */
	private void parseServerConfiForXHBody(XmlPullParser xmlParser,
			ServiceNum config) throws Exception {
		String tagName = xmlParser.getName();
		if (tagName != null && tagName.equals("ServiceNum")) {
			while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
				tagName = xmlParser.getName();
				if (tagName != null && tagName.equals("ivrPhone")) {
					String ivrphone = xmlParser.nextText();
					config.setIvrphone(ivrphone);
				}else if(tagName != null && tagName.equals("voipPhone")){
					String voipphone = xmlParser.nextText();
					config.setVoipphone(voipphone);
				}else {
					xmlParser.nextText();
				}
			}
		} else {
			xmlParser.nextText();
		}

	}
	
	/**
	 * ����������LawerInfoʵ��
	 * @param xmlParser
	 * @param info
	 * @throws Exception
	 */
	private void parseXHLawerInfoBody(XmlPullParser xmlParser, ExpertList lawersList) throws Exception{
		String tagName = xmlParser.getName();
		if (tagName != null && tagName.equals("expert")) {
			Expert info = new Expert();
			while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
				tagName = xmlParser.getName();
				if (tagName != null && tagName.equals("id")) {
					String lawer_id = xmlParser.nextText();
					info.setId(lawer_id);
				} else if (tagName != null && tagName.equals("categoryId")) {
					String lawer_id = xmlParser.nextText();
					info.setCategoryId(lawer_id);
				} else if (tagName != null && tagName.equals("name")) {
					String lawer_name = xmlParser.nextText();
					info.setName(lawer_name);
				} else if (tagName != null && tagName.equals("grade")) {
					String lawer_grade = xmlParser.nextText();
					info.setGrade(lawer_grade);
				} else if (tagName != null && tagName.equals("personInfo")) {
					String lawer_pdetail = xmlParser.nextText();
					info.setPdetail(lawer_pdetail);
				} else if (tagName != null && tagName.equals("detail")) {
					String lawer_detail = xmlParser.nextText();
					info.setDetail(lawer_detail);
				} else {
					xmlParser.nextText();
				}
			}
			lawersList.xExperts.add(info);
		}else {
			xmlParser.nextText();
		}
	}
	
	private void parseXHCategoryBody(XmlPullParser xmlParser, CateGoryList cateGoryList) throws Exception{
		String tagName = xmlParser.getName();
		if (tagName != null && tagName.equals("category")) {
			Category info = new Category();
			while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
				tagName = xmlParser.getName();
				if (tagName != null && tagName.equals("id")) {
					String category_id = xmlParser.nextText();
					info.setCategory_id(category_id);
				} else if (tagName != null && tagName.equals("name")) {
					String category_name = xmlParser.nextText();
					info.setCategory_name(category_name);
				}  else if (tagName != null && tagName.equals("detail")) {
					String category_name = xmlParser.nextText();
					info.setCategory_detail(category_name);
				}  else if (tagName != null && tagName.equals("postition")) {
					String category_name = xmlParser.nextText();
					info.setCategory_postition(category_name);
				} else {
					xmlParser.nextText();
				}
			}
			cateGoryList.xCategorys.add(info);
		} else {
			xmlParser.nextText();
		}

	}
	
	
	/******************************************XH_SHOW************************************/
	
	private  Response doLawerResponse(int parseType,InputStream is) throws Exception {
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
				} else if(parseType == KEY_EXPERT_LIST){
					parseXHLawerInfoBody(xmlParser, (ExpertList)response);
				} else if(parseType == KEY_CATEGORY_LIST){
					parseXHCategoryBody(xmlParser, (CateGoryList)response);
				} else if(parseType == KEY_SERVICE_NUM){
					parseServerConfiForXHBody(xmlParser, (ServiceNum)response);
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
	
	
	void print(Response r) {
		if (r != null) {
			r.print();
		}
	}
	
	boolean isRootNode(String rootName) {
		if (rootName == null || (!rootName.equalsIgnoreCase("Response"))) {
			return false;
		}
		return true;
	}
	
	
	Response getResponseByKey(int key) {
		switch (key) {
		case KEY_EXPERT_LIST:
			return new ExpertList();

		case KEY_CATEGORY_LIST:
			return new CateGoryList();
			
			
			
		case KEY_SERVICE_NUM:
			return new ServiceNum();
			
		default:
			return new Response();
		}
		
	}
	
	public void setOnExpertManagerHelpListener(onExpertManagerHelpListener listener)
	{
		this.mExpertListener = listener;
	}
	
	public interface onExpertManagerHelpListener{
		void onGetExpertClassic(ERequestState reason , ArrayList<Expert> xExperts);
		void onGetClientGategory(ERequestState reason , ArrayList<Category> xcCategories);
		void onActionLockExpert(ERequestState reason);
		void onGet400ServerPort(ERequestState reason , ServiceNum serviceNum);
	}
}
