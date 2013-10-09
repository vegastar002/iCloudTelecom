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
 */package com.voice.demo.tools;


import java.lang.Thread.UncaughtExceptionHandler;

import com.voice.demo.voip.CCPBaseActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 
 * @author Jorstin Chan
 * @version 3.3
 */
public class CrashHandler implements UncaughtExceptionHandler {

	/** Debug Log tag */
	public static final String TAG = "CrashHandler";
	/**
	 * �Ƿ�����־���,��Debug״̬�¿���, ��Release״̬�¹ر�����ʾ��������
	 * */
	public static final boolean DEBUG = false;
	/** ϵͳĬ�ϵ�UncaughtException������ */
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	/** CrashHandlerʵ�� */
	private static CrashHandler INSTANCE;
	/** �����Context���� */
	private Context mContext;

	/** ��ֻ֤��һ��CrashHandlerʵ�� */
	private CrashHandler() {
	}

	/** ��ȡCrashHandlerʵ�� ,����ģʽ */
	public static CrashHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	/**
	 * ��ʼ��,ע��Context����, ��ȡϵͳĬ�ϵ�UncaughtException������, ���ø�CrashHandlerΪ�����Ĭ�ϴ�����
	 * 
	 * @param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * ��UncaughtException����ʱ��ת��ú���������
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		mContext.sendBroadcast(new Intent(CCPBaseActivity.INTETN_ACTION_EXIT_CCP_DEMO));
		if ( mDefaultHandler != null) {
			// ����û�û�д�������ϵͳĬ�ϵ��쳣������������
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// Sleepһ����������
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error : ", e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

}
