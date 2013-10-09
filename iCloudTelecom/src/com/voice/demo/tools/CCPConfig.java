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
package com.voice.demo.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

import com.hust.wa.icloudtelecom.R;

/**
 * Config info Manager.
 * 
 * 
 * @version 1.0.0
 * @see #Config()
 */
public final class CCPConfig {
	

	private static Properties properties;

	public static String LOCAL_PATH = CCPUtil.getExternalStorePath() + "/config.properties";
	
	/**
	 * ≥ı ºªØ Ù–‘
	 */
	public static boolean initProperties(Context context) {
		
		if (properties == null) {
			properties = new Properties();
		}
		
		if (isExistExternalStore()) {
			String content = readContentByFile(LOCAL_PATH);
			if (content != null) {
				try {
					properties.load(new FileInputStream(LOCAL_PATH));
					return loadConfigByProperties();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			} 
		} 
		
		// if SDcar
		
		try {
//			properties.load(context.getResources().openRawResource(R.raw.config));
			return loadConfigByProperties();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private CCPConfig() {

	}

	public static String readContentByFile(String path) {
		BufferedReader reader = null;
		String line = null;
		try {
			File file = new File(path);
			if (file.exists()) {
				StringBuilder sb = new StringBuilder();
				reader = new BufferedReader(new FileReader(file));
				while ((line = reader.readLine()) != null) {
					sb.append(line.trim());
				}
				return sb.toString().trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	
	public static boolean isExistExternalStore() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 *  Demo rest server address info
	 */
	public static String REST_SERVER_ADDRESS = "app.cloopen.com";
	
	/**
	 *  Demo rest server port info
	 */
	public static String REST_SERVER_PORT = "8883";
	
	/**
	 * Demo test main account info
	 */
	public static String Main_Account = "aaf98f8940c47b6b0140c597e2a20017";
	
	/**
	 * Demo test main token info
	 */
	public static String Main_Token = "4dc88531e3154505b874a0d6d8476b6c";
	
	/**
	 * Demo test sub account info
	 */
//	public static String Sub_Account = "ff80808140df78e00140f14f6b28041f";
	public static String Sub_Account = "0000000040df770f0141116672af164c";
	
	/**
	 * Demo test sub token info
	 */
//	public static String Sub_Token = "ae7ddc6d864342d9bc93ed9a521cc534";
	public static String Sub_Token = "443922b547494d39b0ed2be6406124ae";
	
	/**
	 * Demo test sub nikename
	 */
//	public static String Sub_Name = "vegastar001@163.com";
	public static String Sub_Name = "vegastar006@163.com";
	
	/**
	 * Demo test VoIP account
	 */
//	public static String VoIP_ID = "80467600000001";
	public static String VoIP_ID = "80467600000006";
	
	/**
	 * Demo test VoIP password
	 */
//	public static String VoIP_PWD = "7j9yUFl3";
	public static String VoIP_PWD = "T1DLw01h";
	
	/**
	 * Demo test app id
	 */
	public static String App_ID = "aaf98fda40ceab4c0140d53fb2490079";
	
	/**
	 * Demo test self phone number
	 */
	public static String Src_phone = "18657132175";
	
	
	public static String Sub_Account_LIST = "0000000040df770f0141116672af164c";
	public static String Sub_Token_LIST = "443922b547494d39b0ed2be6406124ae";
	public static String VoIP_ID_LIST = "80467600000006";
	public static String VoIP_PWD_LIST = "T1DLw01h";
	
	
	/**
	 * load config info from config.properties
	 */
	private static boolean loadConfigByProperties() {
		REST_SERVER_ADDRESS = properties.getProperty("server_address");
		REST_SERVER_PORT = properties.getProperty("server_port");
		
		
		Main_Account = properties.getProperty("main_account");
		Main_Token = properties.getProperty("main_token");
		Sub_Account_LIST/*Sub_Account*/ = properties.getProperty("sub_account");
		Sub_Token_LIST/*Sub_Token*/ = properties.getProperty("sub_token");
		VoIP_ID_LIST/*VoIP_ID*/ = properties.getProperty("voip_account");
		VoIP_PWD_LIST/*VoIP_PWD*/ = properties.getProperty("voip_password");
		App_ID = properties.getProperty("app_id");
		return check();
	}
	

	public static boolean check() {
		if (Main_Account != null && !Main_Account.equals("")
				&& REST_SERVER_ADDRESS != null && !REST_SERVER_PORT.equals("")
				&& Main_Token != null && !Main_Token.equals("")
				&& Sub_Account_LIST != null && !Sub_Account_LIST.equals("")
				&& Sub_Token_LIST != null && !Sub_Token_LIST.equals("")
				&& VoIP_ID_LIST != null && !VoIP_ID_LIST.equals("")
				&& VoIP_PWD_LIST != null && !VoIP_PWD_LIST.equals("")
				&& App_ID != null && !App_ID.equals("")) {
			return true;
		}

		return false;
	}
}
