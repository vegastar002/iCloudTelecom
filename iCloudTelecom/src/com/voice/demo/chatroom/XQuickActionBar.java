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
package com.voice.demo.chatroom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hust.wa.icloudtelecom.R;


/**
 * ��дpopupWindow
 * 
 * @author chao
 *
 */
public class XQuickActionBar extends PopupWindow {

	private View root;
	private Context context;

	private View anchor;

	private PopupWindow window;
	private Drawable background = null;
	private WindowManager windowManager;
	private XBarAdapter adapter;
	private ListView listView;
	private int[] arrays;

	public XQuickActionBar(View anchor) {

		super(anchor);

		this.anchor = anchor;

		this.window = new PopupWindow(anchor.getContext());

		/**
		 * ��popwindow�������رո�window
		 */
		window.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					// ������ʧ
					XQuickActionBar.this.window.dismiss();
					return true;
				}
				return false;
			}
		});

		context = anchor.getContext();
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		root = (ViewGroup) inflater.inflate(R.layout.quick_popo_right, null);

		adapter = new XBarAdapter(arrays);

		listView = (ListView)root.findViewById(R.id.listview);
		listView.setOnItemClickListener(itemClickListener);
		listView.setAdapter(adapter);
		setContentView(root);

	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			dismissBar();
			// TODO Auto-generated method stub
			if(onPopClickListener != null){
				onPopClickListener.onPopClick(arrays[position]);
			}
		}

	};

	/**
	 * ����ListView �¼���������
	 * @return
	 */
	public ListView getListView() {
		return listView;
	}

	public void show(int[] aStrings){
		if(aStrings != null){
			arrays = aStrings;
		}
		show();
	}

	/**
	 * ��������
	 */
	public void show() {
		if (adapter != null) {
			adapter.setArrays(arrays);
			adapter.notifyDataSetChanged();
		} else {
			adapter = new XBarAdapter(arrays);
		}


		preShow();

		int[] location = new int[2];

		// �õ�anchor��λ��
		anchor.getLocationOnScreen(location);
		// ��anchor��λ�ù���һ������
		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth = root.getMeasuredWidth();

		// �õ���Ļ�Ŀ�
		int screenWidth = windowManager.getDefaultDisplay().getWidth();

		// ���õ���������λ�õ�X y
		int xPos = (screenWidth - rootWidth);
		int yPos = anchorRect.bottom;

		xPos = (location[0] + (anchor.getWidth()-rootWidth)/2);

		if(xPos < 5) {
			xPos = 5;
		}

		if(xPos > (screenWidth - rootWidth-5))
			xPos = screenWidth - rootWidth-5;

		// ���õ����������
		window.setAnimationStyle(R.style.Animations_PopDownMenu_Top);
		// ��ָ��λ�õ�������
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	/**
	 * ��ʧ
	 */
	public void dismissBar() {
		// ������ʧ
		if(XQuickActionBar.this.window != null && XQuickActionBar.this.window.isShowing())
			XQuickActionBar.this.window.dismiss();
	}

	/**
	 * Ԥ������
	 */
	protected void preShow() {

		if (root == null) {
			throw new IllegalStateException("��ҪΪ�������ò���");
		}

		if (background == null) {
			window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			window.setBackgroundDrawable(background);
		}

//		window.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeResource(anchor.getContext().getResources(), R.color.transparent)));
		// ���ÿ��
		window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		// ���ø߶�
		window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

		window.setTouchable(true);
		window.setFocusable(true);
		window.setOutsideTouchable(true);
		// ָ������
		window.setContentView(root);
	}

	class XBarAdapter extends BaseAdapter {

		private int[] arrays = null;

		public XBarAdapter (int[] aStrings) {
			this.arrays = aStrings;
		}

		public void setArrays(int[] strings) {
			if(arrays != null)
				arrays = null;
			this.arrays = strings;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(arrays == null || arrays.length == 0)
				return 0;
			return arrays.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if(arrays == null || arrays.length == 0)
				return null;
			return arrays[position%arrays.length];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			if(arrays == null || arrays.length == 0)
				return 0;
			return position%arrays.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = ((LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.pop_list_item, null);
				holder = new ViewHolder();
				holder.nameTv = (TextView)convertView.findViewById(R.id.pop_right);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder)convertView.getTag();
			}
			holder.nameTv.setText(context.getText(arrays[position]));
			return convertView;
		}
		private class ViewHolder {
			TextView nameTv;
		}
	}

	/**
	 * @return the arrays
	 */
	public int[] getArrays() {
		return arrays;
	}

	/**
	 * @param arrays the arrays to set
	 */
	public void setArrays(int[] arrays) {
		this.arrays = arrays;
	}

	public interface OnPopClickListener {
		public void onPopClick(int index);
	}

	private OnPopClickListener onPopClickListener;

	/**
	 * @return the onPopClickListener
	 */
	public OnPopClickListener getOnPopClickListener() {
		return onPopClickListener;
	}

	/**
	 * @param onPopClickListener the onPopClickListener to set
	 */
	public void setOnPopClickListener(OnPopClickListener onPopClickListener) {
		this.onPopClickListener = onPopClickListener;
	}

}
