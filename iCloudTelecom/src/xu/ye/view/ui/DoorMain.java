package xu.ye.view.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;

import com.hust.wa.icloudtelecom.R;

public class DoorMain extends Activity implements OnClickListener{

	ViewPager viewPager;
	RadioButton team_chat_btn, friends_chat_btn;
	List<View> lists;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doormain);
		
		lists = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		View exView = mInflater.inflate(R.layout.history_list, null);
		View coView = mInflater.inflate(R.layout.company_group_list, null);
		lists.add(exView);
		lists.add(coView);
		
		
		viewPager = (ViewPager) findViewById(R.id.contacts_vPager);
		viewPager.setAdapter(new MyPagerAdapter(lists));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new XTOnPageChangeListener());
		viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		team_chat_btn = (RadioButton) findViewById(R.id.team_chat_btn);
		friends_chat_btn = (RadioButton) findViewById(R.id.friends_chat_btn);
		
		team_chat_btn.setOnClickListener(this);
		friends_chat_btn.setOnClickListener(this);
		
		
	}

	
	class XTOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int page) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int page) {
			switch (page) {
			case 0:
				friends_chat_btn.setChecked(true);
				team_chat_btn.setChecked(false);
				break;
			case 1:
				friends_chat_btn.setChecked(false);
				team_chat_btn.setChecked(true);
				break;
			default:
				break;
			}
		}
		
	}
	
	
	class MyPagerAdapter extends PagerAdapter{
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListViews.size();
		}
		
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (arg1);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.team_chat_btn:
			break;
		case R.id.friends_chat_btn:
			break;

		default:
			break;
		}
	}

}
