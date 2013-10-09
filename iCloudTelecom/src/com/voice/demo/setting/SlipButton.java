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

package com.voice.demo.setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.hust.wa.icloudtelecom.R;


/**
 * 
 *  ���һ�����
 *
 */
public class SlipButton extends View implements OnTouchListener{
	private String strName;
	private boolean enabled = true;
	public boolean flag = false;//���ó�ʼ��״̬ 
	public boolean NowChoose = false;//��¼��ǰ��ť�Ƿ��,trueΪ��,flaseΪ�ر�
	private boolean OnSlip = false;//��¼�û��Ƿ��ڻ����ı���
	public float DownX=0f,NowX=0f;//����ʱ��x,��ǰ��x,NowX>100ʱΪON����,��֮ΪOFF����
	private Rect Btn_On,Btn_Off;//�򿪺͹ر�״̬��,�α��Rect

	private boolean isChgLsnOn = false;
	private OnChangedListener ChgLsn;
	private Bitmap bg_on,bg_off,slip_btn;


	public SlipButton(Context context) {
		super(context);
		init();
	}

	public SlipButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setChecked(boolean fl){
		if(fl){
			flag = true; NowChoose = true; NowX = 80;
		}else{
			flag = false; NowChoose = false; NowX = 0;
		}
		refreshDrawableState();
		invalidate();//�ػ��ؼ�
	}

	public boolean isChecked(){
		return NowChoose;
	}

	public void setEnabled(boolean b){
		if(b){
			enabled = true;
		}else{
			enabled = false;
		}
	}

	private void init(){//��ʼ��
		//����ͼƬ��Դ
		bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.open_icon_bg);
		bg_off = BitmapFactory.decodeResource(getResources(), R.drawable.close_icon_bg);
		slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.open_close_icon);
		//�����Ҫ��Rect����
		Btn_On = new Rect(0,0,slip_btn.getWidth(),slip_btn.getHeight());
		Btn_Off = new Rect(
				bg_off.getWidth()-slip_btn.getWidth(),
				0,
				bg_off.getWidth(),
				slip_btn.getHeight());
		setOnTouchListener(this);//���ü�����,Ҳ����ֱ�Ӹ�дOnTouchEvent
	}

	@Override
	protected void onDraw(Canvas canvas) {//��ͼ����
		super.onDraw(canvas);
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x;
		{  
			if (flag) {NowX = 80;flag = false;
			}//bg_on.getWidth()=71
			if(NowX<(bg_on.getWidth()/2)){//������ǰ�������εı�����ͬ,�ڴ����ж�// ����ط���������ģ�
				canvas.drawBitmap(bg_off,matrix, paint);//�����ر�ʱ�ı���
			}
			else {
				canvas.drawBitmap(bg_on,matrix, paint);//������ʱ�ı���
			}
			if(OnSlip)//�Ƿ����ڻ���״̬,
			{   
				if(NowX >= bg_on.getWidth())//�Ƿ񻮳�ָ����Χ,�������α��ܵ���ͷ,����������ж�
					x = bg_on.getWidth()-slip_btn.getWidth()/2;//��ȥ�α�1/2�ĳ���...
				else
					x = NowX - slip_btn.getWidth()/2;
			}else{//�ǻ���״̬
				if(NowChoose)//�������ڵĿ���״̬���û��α��λ��
					x = Btn_Off.left;
				else
					x = Btn_On.left;
			}

			if(x<0)//���α�λ�ý����쳣�ж�...
				x = 0;
			else if(x>bg_on.getWidth()-slip_btn.getWidth())
				x = bg_on.getWidth()-slip_btn.getWidth();
			canvas.drawBitmap(slip_btn,x, 0, paint);//�����α�.
		}
	}


	public boolean onTouch(View v, MotionEvent event) {
		if(!enabled){
			return false;
		}
		boolean LastChoose = false;
		switch(event.getAction())//���ݶ�����ִ�д���
		{
		case MotionEvent.ACTION_MOVE://����
			NowX = event.getX();// ���������;
			break;
		case MotionEvent.ACTION_DOWN://����
			if(event.getX()>bg_on.getWidth()||event.getY()>bg_on.getHeight()) // �޶��们��λ��, ����
				return false;
			OnSlip = true;
			DownX = event.getX();
			NowX = DownX;
			break;
		case MotionEvent.ACTION_UP://�ɿ�
			OnSlip = false;
			LastChoose = NowChoose;
			//event.getX �����������������;
			if(event.getX()>=(bg_on.getWidth()/2)) {
				NowChoose = true;}
			else
				NowChoose = false;
			if(isChgLsnOn&&(LastChoose!=NowChoose))//��������˼�����,�͵����䷽��..
				ChgLsn.onChanged(strName, NowChoose);
			break;
		default:
			OnSlip = false;
			LastChoose = NowChoose;
			if(NowX >=(bg_on.getWidth()/2)) {
				NowChoose = true;}
			else
				NowChoose = false;
			if(isChgLsnOn&&(LastChoose!=NowChoose))//��������˼�����,�͵����䷽��..
				ChgLsn.onChanged(strName, NowChoose);
			break;
		}
		invalidate();//�ػ��ؼ�
		return true;
	}

	public void SetOnChangedListener(String name, OnChangedListener l){//���ü�����,��״̬�޸ĵ�ʱ��
		strName = name;
		isChgLsnOn = true;
		ChgLsn = l;
	}

	public interface OnChangedListener {
		public void onChanged(String strname , boolean checkState);
	}
}
