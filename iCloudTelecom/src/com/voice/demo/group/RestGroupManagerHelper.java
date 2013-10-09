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
package com.voice.demo.group;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.voice.demo.group.model.IMGroup;
import com.voice.demo.group.model.IMGroupList;
import com.voice.demo.group.model.IMGroupMemberList;
import com.voice.demo.group.model.IMMember;
import com.voice.demo.outboundmarketing.RestHelper.ERequestState;
import com.voice.demo.tools.CCPConfig;


public class RestGroupManagerHelper {

	public static final String TAG = RestGroupManagerHelper.class.getSimpleName();
	
	public static final int KEY_CREATE_GROUP 		= 970201;
	public static final int KEY_MODIFY_GROUP 		= 970202;
	public static final int KEY_DELETE_GROUP 		= 970203;
	public static final int KEY_QUERY_GROUP_INFO 	= 970204;
	public static final int KEY_JOIN_GROUP			= 970205;
	public static final int KEY_INVITE_JOIN_GROUP 	= 970206;
	public static final int KEY_QUIT_GROUP 			= 970207;
	public static final int KEY_DEL_MEMBER_OF_GROUP	= 970208;
	public static final int KEY_ADD_GROUPCARD 		= 970209;
	public static final int KEY_MODIFY_GROUPCARD 	= 970210;
	public static final int KEY_QUERY_GROUPCARD 	= 970211;
	public static final int KEY_QUERY_MEMBERS_GROUP	= 970212;
	public static final int KEY_QUERY_GROUPS_VOIP 	= 970213;
	public static final int KEY_VERIFY_JOIN_GROUP 	= 970214;
	public static final int KEY_ANSWER_INVITE_GROUP	= 970215;
	public static final int KEY_SET_RULE_GROUP_MSG	= 970216;
	public static final int KEY_GET_PUBLIC_GROUPS_MSG = 970217;
	public static final int KEY_FORBIDS_PEAK		  = 970218;
	
	private static RestGroupManagerHelper mInstance = null;
	
	private onRestGroupManagerHelpListener mRestGroupListener = null;
	
	public static RestGroupManagerHelper getInstance(){
		if (mInstance == null) {
			mInstance = new RestGroupManagerHelper();
		}
		return mInstance;
	}
	
	private RestGroupManagerHelper(){
		
	}
	
	
	private StringBuffer getSubAccountRequestURL(int requestType, String formatTimestamp){
		// md5(���˻�ID + ���˻���Ȩ���� + ʱ���)
		String sig = VoiceUtil.md5(CCPConfig.Sub_Account
				+ CCPConfig.Sub_Token + formatTimestamp);

		// request url
		StringBuffer url = new StringBuffer("https://" + CCPConfig.REST_SERVER_ADDRESS + ":" + CCPConfig.REST_SERVER_PORT);
		url.append("/2013-03-22/SubAccounts/").append(CCPConfig.Sub_Account).append("/");
		
		switch (requestType) {
		case KEY_CREATE_GROUP:
			url.append("Group/CreateGroup");
			break;
		case KEY_MODIFY_GROUP:
			url.append("Group/ModifyGroup");
			break;
		case KEY_DELETE_GROUP:
			url.append("Group/DeleteGroup");
			break;
		case KEY_QUERY_GROUP_INFO:
			url.append("Group/QueryGroupDetail");
			break;
		case KEY_JOIN_GROUP:
			url.append("Group/JoinGroup");
			break;
		case KEY_INVITE_JOIN_GROUP:
			url.append("Group/InviteJoinGroup");
			break;
		case KEY_DEL_MEMBER_OF_GROUP:
			url.append("Group/DeleteGroupMember");
			break;
		case KEY_QUIT_GROUP:
			url.append("Group/LogoutGroup");
			break;
		case KEY_ADD_GROUPCARD:
			url.append("Member/AddCard");
			break;
		case KEY_MODIFY_GROUPCARD:
			url.append("Member/ModifyCard");
			break;
		case KEY_QUERY_GROUPCARD:
			url.append("Member/QueryCard");
			break;
		case KEY_QUERY_MEMBERS_GROUP:
			url.append("Member/QueryMember");
			break;
		case KEY_QUERY_GROUPS_VOIP:
			url.append("Member/QueryGroup");
			break;
		case KEY_VERIFY_JOIN_GROUP:
			url.append("Member/AskJoin");
			break;
		case KEY_ANSWER_INVITE_GROUP:
			url.append("Member/InviteGroup");
			break;
		case KEY_SET_RULE_GROUP_MSG:
			url.append("Member/SetGroupMsg");
			break;
		case KEY_GET_PUBLIC_GROUPS_MSG:
			url.append("Group/GetPublicGroups");
			break;
			
		case KEY_FORBIDS_PEAK:
			url.append("Member/ForbidSpeak");
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
	
	/**
	 * ����Ⱥ��
	 * @param name Ⱥ����
	 * @param type Ⱥ������ 0:��ʱ��(����100��) 1:��ͨ��(����200��) 2:VIP��(����500��)
	 * @param declared Ⱥ�鹫��
	 * @param permission �������ģʽ 0:Ĭ��ֱ�Ӽ���  1:��Ҫ�����֤  2:˽��Ⱥ��
	 */
	public void createGroup(String name, int type, String declared, int permission){
		
		int keyValue = KEY_CREATE_GROUP;
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<name>").append(name).append("</name>\r\n");
		requestBody.append("\t<type>").append(type).append("</type>\r\n");
		requestBody.append("\t<declared>").append(declared).append("</declared>\r\n");
		requestBody.append("\t<permission>").append(permission).append("</permission>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMGroup group = (IMGroup) doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (group != null && ! group.isError()) {
					mRestGroupListener.onCreateGroup(ERequestState.Success, group.groupId);
				} else {
					mRestGroupListener.onCreateGroup(ERequestState.Failed ,null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onCreateGroup(ERequestState.Failed ,null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}
	
	
	/**
	 * �޸�Ⱥ�����
	 * @param groupId Ⱥ��ID
	 * @param name Ⱥ������
	 * @param declared Ⱥ�鹫��
	 * @param permission �������ģʽ 0:Ĭ��ֱ�Ӽ���  1:��Ҫ�����֤  2:˽��Ⱥ��
	 */
	public void modifyGroup(String groupId, String name, String declared, int permission){
		
		int keyValue = KEY_MODIFY_GROUP;
		
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("\t<name>").append(name).append("</name>\r\n");
		declared = declared == null ? "" : declared;
		requestBody.append("\t<declared>").append(declared).append("</declared>\r\n");
		requestBody.append("\t<permission>").append(permission).append("</permission>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onModifyGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onModifyGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onModifyGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}
	
	
	/**
	 * ɾ��Ⱥ��
	 * @param groupId Ⱥ��id
	 */
	public void deleteGroup(String groupId){
		int keyValue = KEY_DELETE_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onDeleteGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onDeleteGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onDeleteGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}
	
	/**
	 * ��ѯȺ������
	 * @param groupId Ⱥ��id
	 */
	public void queryGroupWithGroupId(String groupId){
		int keyValue = KEY_QUERY_GROUP_INFO;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMGroup response = (IMGroup)doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					response.groupId = groupId;
					mRestGroupListener.onQueryGroupWithGroupId(ERequestState.Success, response);
				} else {
					mRestGroupListener.onQueryGroupWithGroupId(ERequestState.Failed, null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onQueryGroupWithGroupId(ERequestState.Failed, null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	
	/**
	 * �û��������Ⱥ��
	 * @param groupId Ⱥ��id
	 * @param asker ������
	 * @param declared ��������
	 */
	public void joinGroup(String groupId, String declared){
		int keyValue = KEY_JOIN_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		
		// Document 0.75 to delete the asker field
		declared = declared == null ? "" :declared;
		requestBody.append("\t<declared>").append(declared).append("</declared>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onJoinGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onJoinGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onJoinGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	/**
	 * Ⱥ�����Ա�����û�����Ⱥ��
	 * @param groupId Ⱥ��id
	 * @param owner Ⱥ���������
	 * @param members ��������
	 * @param declared ���������
	 * @param confirm �Ƿ���Ҫ��������ȷ�� 0:��Ҫ 1:����Ҫ(�Զ�����Ⱥ��)
	 */
	public void inviteSomebodyJoinGroup(String groupId, String owner, String[] members, String declared, String confirm){
		int keyValue = KEY_INVITE_JOIN_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");

		// ȥ��3.9�ӿڵ�onwer�ֶΣ�
		if(members != null)
		{
			requestBody.append("<members>");
			for (int i = 0; i < members.length; i++) {
				requestBody.append("<member>").append(members[i]).append("</member>");
			}
			requestBody.append("</members>");
		}
		
		declared = declared == null ? "" : declared;
		requestBody.append("\t<declared>").append(declared).append("</declared>\r\n");
		requestBody.append("\t<confirm>").append(confirm).append("</confirm>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onInviteSomebodyJoinGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onInviteSomebodyJoinGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onInviteSomebodyJoinGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	
	/**
	 * Ⱥ�����Աɾ����Ա
	 * @param groupId Ⱥ��id
	 * @param members ��ɾ����
	 */
	public void deleteMembersFromGroup(String groupId, String[] members){
		int keyValue = KEY_DEL_MEMBER_OF_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		if(members != null)
		{
			requestBody.append("<members>");
			for (int i = 0; i < members.length; i++) {
				requestBody.append("<member>").append(members[i]).append("</member>");
			}
			requestBody.append("</members>");
		}
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onDeleteMembersFromGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onDeleteMembersFromGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onDeleteMembersFromGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	/**
	 * Ⱥ���Ա�����˳�Ⱥ��
	 * @param groupId
	 * @param asker
	 */
	public void quitGroup(String groupId){
		int keyValue = KEY_QUIT_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onQuitGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onQuitGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onQuitGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	
	//��ӳ�Ա
	public void addGroupCard(IMMember member){
		int keyValue = KEY_ADD_GROUPCARD;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<display>").append(member.displayName).append("</display>\r\n");
		if(null != member.mail)
			requestBody.append("\t<mail>").append(member.mail).append("</mail>\r\n");
		if(null != member.remark) 
			requestBody.append("\t<remark>").append(member.remark).append("</remark>\r\n");
		if(null != member.tel)
			requestBody.append("\t<tel>").append(member.tel).append("</tel>\r\n");
		if(null != member.belong)
			requestBody.append("\t<belong>").append(member.belong).append("</belong>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onAddGroupCard(ERequestState.Success);
				} else {
					mRestGroupListener.onAddGroupCard(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onAddGroupCard(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	//�޸ĳ�Ա
	public void modifyGroupCard(IMMember member){
		int keyValue = KEY_MODIFY_GROUPCARD;
		
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());

		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		String url = urlBuf.toString();
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}

		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		if(!TextUtils.isEmpty(member.displayName))
				requestBody.append("\t<display>").append(member.displayName).append("</display>\r\n");
		if(!TextUtils.isEmpty(member.mail))
			requestBody.append("\t<mail>").append(member.mail).append("</mail>\r\n");
		if(!TextUtils.isEmpty(member.remark)) 
			requestBody.append("\t<remark>").append(member.remark).append("</remark>\r\n");
		if(!TextUtils.isEmpty(member.tel))
			requestBody.append("\t<tel>").append(member.tel).append("</tel>\r\n");
		requestBody.append("\t<belong>").append(member.belong).append("</belong>\r\n");
		requestBody.append("\t<voipAccount>").append(member.voipAccount).append("</voipAccount>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onModifyGroupCard(ERequestState.Success);
				} else {
					mRestGroupListener.onModifyGroupCard(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onModifyGroupCard(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
	}
	
	//��ѯ��
	public void queryGroupCard(IMMember member){
		int keyValue = KEY_QUERY_GROUPCARD;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<other>").append(member.voipAccount).append("</other>\r\n");
		requestBody.append("\t<belong>").append(member.belong).append("</belong>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMMember response = (IMMember)doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			response.voipAccount = member.voipAccount;
			response.belong = member.belong;
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onQueryGroupCard(ERequestState.Success, response);
				} else {
					mRestGroupListener.onQueryGroupCard(ERequestState.Failed, null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onQueryGroupCard(ERequestState.Failed, null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	//��ѯȺ���Ա
	public void queryMembersOfGroup(String groupId){
		int keyValue = KEY_QUERY_MEMBERS_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMGroupMemberList response = (IMGroupMemberList)doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onQueryMembersOfGroup(ERequestState.Success ,response.iMMemberList);
				} else {
					mRestGroupListener.onQueryMembersOfGroup(ERequestState.Failed, null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onQueryMembersOfGroup(ERequestState.Failed, null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	//��ѯ��Ա�����Ⱥ��
	public void queryGroupsOfVoip(){
		int keyValue = KEY_QUERY_GROUPS_VOIP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<asker>").append(CCPConfig.VoIP_ID).append("</asker>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMGroupList response = (IMGroupList)doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onQueryGroupsOfVoip(ERequestState.Success, response.iMGroups);
				} else {
					mRestGroupListener.onQueryGroupsOfVoip(ERequestState.Failed, null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onQueryGroupsOfVoip(ERequestState.Failed, null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	
	
	/**
	 * ����Ա��֤�û��������Ⱥ��
	 * @param groupId Ⱥ��ID
	 * @param asker �����Ա��VoIP�˺�
	 * @param confirm  0��ͨ�� 1���ܾ�
	 */
	public void verifyJoinGroup(String groupId, String asker, int confirm){
		int keyValue = KEY_VERIFY_JOIN_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("\t<asker>").append(asker).append("</asker>\r\n");
		requestBody.append("\t<confirm>").append(confirm).append("</confirm>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onVerifyJoinGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onVerifyJoinGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onVerifyJoinGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	/**
	 * �û���֤����Ա�������Ⱥ��
	 * @param groupId Ⱥ��ID
	 * @param asker �û�voip
	 * @param confirm  0��ͬ�� 1���ܾ�
	 */
	public void answerInviteGroup(String groupId,/* String asker, */int confirm){
		int keyValue = KEY_ANSWER_INVITE_GROUP;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("\t<confirm>").append(confirm).append("</confirm>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onAnswerInviteGroup(ERequestState.Success);
				} else {
					mRestGroupListener.onAnswerInviteGroup(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onAnswerInviteGroup(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}

	/**
	 * ����Ⱥ����Ϣ���չ���
	 * @param groupId Ⱥ��id
	 * @param rule ���չ��� 0������  1���ܾ�  Ĭ��Ϊ0
	 */
	public void setRuleOfReceiveGroupMsg(String groupId, int rule){
		int keyValue = KEY_SET_RULE_GROUP_MSG;
				
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

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("\t<rule>").append(rule).append("</rule>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		try {
			String xml = HttpHelper.httpPost(url, headers,
					requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onSetRuleOfReceiveGroupMsg(ERequestState.Success);
				} else {
					mRestGroupListener.onSetRuleOfReceiveGroupMsg(ERequestState.Failed);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onSetRuleOfReceiveGroupMsg(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	/**
	 * ��ѯ���й���Ⱥ��ӿ�
	 * @param start ��ȡ��λ��
	 * @param maxrows ������Ŀ
	 */
	public void getPublicGroups(String start, int maxrows){
		int keyValue = KEY_GET_PUBLIC_GROUPS_MSG;
				
		// ʱ���
		String formatTimestamp = VoiceUtil.formatTimestamp(System
				.currentTimeMillis());

		
		StringBuffer urlBuf = getSubAccountRequestURL(keyValue, formatTimestamp);
		//urlBuf.append("&start=").append(start).append("&maxrows=").append(maxrows);
		String url = urlBuf.toString();
		Log4Util.w(Device.TAG, "url: " + url + "\r\n");
		
		// check url
		if (!URLUtil.isHttpsUrl(url)) {
			throw new RuntimeException("address invalid.");
		}

		// request header
		HashMap<String, String> headers = getSubAccountRequestHead(keyValue, formatTimestamp);
		
		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<lastUpdateTime>").append(System.currentTimeMillis()).append("</lastUpdateTime>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		

		try {
			String xml = HttpHelper.httpPost(url, headers, requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");

			IMGroupList response = (IMGroupList)doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onGetPublicGroups(ERequestState.Success, response.iMGroups);
				} else {
					mRestGroupListener.onGetPublicGroups(ERequestState.Failed, null);
				}
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onGetPublicGroups(ERequestState.Failed, null);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}

	}
	
	
	/**
	 * The group user gag operation, not published information
	 * @param groupId
	 * @param member
	 * @param operation  0: can speak (default) 1: 2: will all members of the banned group
	 */
	public void ForbidSpeakForUser(String groupId , String member , int operation){
		int keyValue = KEY_FORBIDS_PEAK;
		
		if(TextUtils.isEmpty(groupId) ||
				TextUtils.isEmpty(member)) {
			throw new RuntimeException("group id or member can't empty , groupId " + groupId + " , member" + member);
		}
		
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
		
		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		requestBody.append("\t<groupId>").append(groupId).append("</groupId>\r\n");
		requestBody.append("\t<member>").append(member).append("</member>\r\n");
		requestBody.append("\t<operation>").append(operation).append("</operation>\r\n");
		requestBody.append("</Request>\r\n");
		Log4Util.i(TAG, requestBody.toString());
		
		
		
		try {
			String xml = HttpHelper.httpPost(url, headers, requestBody.toString());
			Log4Util.w(Device.TAG, xml + "\r\n");
			
			Response response = doParserGroupResponse(keyValue, new ByteArrayInputStream(xml.getBytes()));
			
			if (mRestGroupListener != null){
				if (response != null && ! response.isError()) {
					mRestGroupListener.onForbidSpeakForUser(ERequestState.Success);
				} else {
					mRestGroupListener.onForbidSpeakForUser(ERequestState.Failed);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			if (mRestGroupListener != null) {
				mRestGroupListener.onForbidSpeakForUser(ERequestState.Failed);
			}
		} finally {
			if (headers != null) {
				headers.clear();
				headers = null;
			}
		}
		
	}
	
	
	private void parseQueryGroupInfoBody(XmlPullParser xmlParser, IMGroup response) throws Exception {
		//while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
			String tagName = xmlParser.getName();
			if (tagName.equals("name")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.name = text.trim();
				}
			} else if (tagName.equals("type")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.type = text.trim();
				}
			} else if (tagName.equals("declared")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.declared = text.trim();
				}
			} else if (tagName.equals("dateCreated")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.createdDate = text.trim();
				}
			} else if (tagName.equals("owner")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.owner = text.trim();
				}
			} else if (tagName.equals("permission")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.permission = text.trim();
				}
			} else {
				xmlParser.nextText();
			}
		//}
	}
	
	private void parseQueryGroupCardBody(XmlPullParser xmlParser, IMMember response) throws Exception {
		//while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
			String tagName = xmlParser.getName();
			if (tagName.equals("display")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.displayName = text.trim();
				}
			} else if (tagName.equals("tel")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.tel = text.trim();
				}
			} else if (tagName.equals("mail")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.mail = text.trim();
				}
			} else if (tagName.equals("remark")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.remark = text.trim();
				}
			} else if (tagName.equals("belong")) {
				String text = xmlParser.nextText();
				if (text != null && !text.equals("")) {
					response.belong = text.trim();
				}
			} else {
				xmlParser.nextText();
			}
		//}
	}
	
	private void parseQueryMembersBody(XmlPullParser xmlParser, IMGroupMemberList response) throws Exception {
		while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
			String tagName = xmlParser.getName();
			if (tagName.equals("member")) {
				IMMember imMember = new IMMember();
				while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
					tagName = xmlParser.getName();
					if (tagName.equals("voipAccount")) {
						String text = xmlParser.nextText();
						if (text != null && !text.equals("")) {
							imMember.voipAccount = text;
						}
					} else if(tagName.equals("isBan")){
						String text = xmlParser.nextText();
						if (text != null && !text.equals("")) {
							imMember.isBan = Integer.parseInt(text);
						}
					} else if(tagName.equals("display")){
						String text = xmlParser.nextText();
						if (text != null && !text.equals("")) {
							imMember.displayName = text;
						}
					}else {
						xmlParser.nextText();
					}
					
				}
				response.iMMemberList.add(imMember);
			}else {
				xmlParser.nextText();
			}
		}
	}
	
	private void parseQueryGroupsBody(XmlPullParser xmlParser, IMGroupList response) throws Exception {
		
		String ptagName = xmlParser.getName();
		if (ptagName.equals("groups")) {
			while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
				ptagName = xmlParser.getName();
				if (ptagName.equals("group")) {
					IMGroup imGroup = new IMGroup();
					while (xmlParser.nextTag() != XmlPullParser.END_TAG) {
						String tagName = xmlParser.getName();
						if (tagName.equals("groupId")) {
							String text = xmlParser.nextText();
							if (text != null && !text.equals("")) {
								imGroup.groupId = text;
							}
						} else if(tagName.equals("name")){
							String text = xmlParser.nextText();
							if (text != null && !text.equals("")) {
								imGroup.name = text;
							}
						}  else if(tagName.equals("count")){
							String text = xmlParser.nextText();
							if (text != null && !text.equals("")) {
								imGroup.count = text;
							}
						} else if(tagName.equals("permission")){
							String text = xmlParser.nextText();
							if (text != null && !text.equals("")) {
								imGroup.permission = text;
							}
						}  else if(tagName.equals("type")){
							String text = xmlParser.nextText();
							if (text != null && !text.equals("")) {
								imGroup.type = text;
							}
						}   else {
							xmlParser.nextText();
						}
					}
					response.iMGroups.add(imGroup);
				} 
				
			}
		}else {
			xmlParser.nextText();
		}
	}
	
	private  Response doParserGroupResponse(int parseType,InputStream is) throws Exception {
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
				} else if (parseType == KEY_CREATE_GROUP) {
					if(tagName != null && tagName.equals("groupId")){
						((IMGroup)response).groupId = xmlParser.nextText();
					}
				}else if(parseType == KEY_QUERY_GROUP_INFO){
					parseQueryGroupInfoBody(xmlParser, (IMGroup)response);
				}else if(parseType == KEY_QUERY_GROUPCARD){
					parseQueryGroupCardBody(xmlParser, (IMMember)response);
				}else if(parseType == KEY_QUERY_MEMBERS_GROUP){
					parseQueryMembersBody(xmlParser, (IMGroupMemberList)response);
				}else if(parseType == KEY_QUERY_GROUPS_VOIP){
					parseQueryGroupsBody(xmlParser, (IMGroupList)response);
					
				}else if(parseType == KEY_GET_PUBLIC_GROUPS_MSG){
					//parseGetPublicGroupsBody(xmlParser, (IMGroupSummaryList)response);
					parseQueryGroupsBody(xmlParser, (IMGroupList)response);
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
	
	

	boolean isRootNode(String rootName) {
		if (rootName == null || (!rootName.equalsIgnoreCase("Response"))) {
			return false;
		}
		return true;
	}
	
	Response getResponseByKey(int key) {
		switch (key) {
		case KEY_CREATE_GROUP:
		case KEY_QUERY_GROUP_INFO:
			return new IMGroup();

		case KEY_QUERY_MEMBERS_GROUP:
			return new IMGroupMemberList();
			
			
		case KEY_GET_PUBLIC_GROUPS_MSG:
		case KEY_QUERY_GROUPS_VOIP:
			
			return new IMGroupList();
			
		case KEY_QUERY_GROUPCARD:
			return new IMMember();
			
		default:
			return new Response();
		}
		
	}
	
	void print(Response r) {
		if (r != null) {
			r.print();
		}
	}
	
	
	public void setOnRestGroupManagerHelpListener(onRestGroupManagerHelpListener listener)
	{
		this.mRestGroupListener = listener;
	}
	
	public interface onRestGroupManagerHelpListener{
		void onCreateGroup(ERequestState reason, String groupId);
		void onModifyGroup(ERequestState reason);
		void onDeleteGroup(ERequestState reason);
		void onQueryGroupWithGroupId(ERequestState reason, IMGroup group);
		void onJoinGroup(ERequestState reason);
		void onInviteSomebodyJoinGroup(ERequestState reason);
		void onDeleteMembersFromGroup(ERequestState reason);
		void onQuitGroup(ERequestState reason);
		void onAddGroupCard(ERequestState reason);
		void onModifyGroupCard(ERequestState reason);
		void onQueryGroupCard(ERequestState reason, IMMember member);
		void onQueryMembersOfGroup(ERequestState reason, ArrayList<IMMember> members);
		void onQueryGroupsOfVoip(ERequestState reason, ArrayList<IMGroup> imGroups);
		void onVerifyJoinGroup(ERequestState reason);
		void onAnswerInviteGroup(ERequestState reason);
		void onSetRuleOfReceiveGroupMsg(ERequestState reason);
		void onGetPublicGroups(ERequestState reason, ArrayList<IMGroup> members);
		
		void onForbidSpeakForUser(ERequestState reason);
	}
}
