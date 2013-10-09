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
package com.voice.demo.group.model;

import com.hisun.phone.core.voice.model.Response;

public class IMMember extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 604352159818994119L;

	public String voipAccount = null;//�û�voip�˺�
	public String displayName = null;//�û�����
	public String sex = null;//�Ա�
	public String birth = null;//�û�����
	public String tel = null;//�û��绰
	public String sign = null;//�û���ǩ��
	public String mail = null;//�û�����
	public String remark = null;//�û��ı�ע
	public String belong = null;//����Ⱥ��
	
	public int isBan = 0;        // �Ƿ����
	public int rule = 0;        // �Ƿ����Ⱥ����Ϣ
}
