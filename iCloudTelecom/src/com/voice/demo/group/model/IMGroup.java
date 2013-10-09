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

public class IMGroup extends Response {
	
	public static final int MODEL_GROUP_PUBLIC = 0x0;
	public static final int MODEL_GROUP_AUTHENTICATION = 0x1;
	public static final int MODEL_GROUP_PRIVATE = 0x2;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1317673848969906965L;

	public String groupId = null;//Ⱥ��ID
	public String name = null;//Ⱥ������
	public String type = null;//Ⱥ������ 0����ʱ�� 1����ͨ�� 2��VIP��
	public String declared = null;//Ⱥ�鹫��
	public String createdDate = null; //��Ⱥ��Ĵ���ʱ��
	public String permission = null;//�������ģʽ 0��Ĭ��ֱ�Ӽ���1����Ҫ�����֤z
	
	public String owner;
	public String count;
	
}
