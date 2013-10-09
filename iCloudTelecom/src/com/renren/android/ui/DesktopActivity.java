package com.renren.android.ui;

import xu.ye.view.other.SystemScreenInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.renren.android.BaseApplication;
import com.renren.android.appscenter.AppsCenter;
import com.renren.android.desktop.Desktop;
import com.renren.android.desktop.Desktop.onChangeViewListener;
import com.renren.android.ui.base.FlipperLayout;
import com.renren.android.ui.base.FlipperLayout.OnOpenListener;
import com.renren.android.util.View_Util;

public class DesktopActivity extends Activity implements OnOpenListener {
	private BaseApplication mApplication;
	private FlipperLayout mRoot;
	private Desktop mDesktop;
//	private User mUser;
//	private NewsFeed mNewsFeed;
//	private Message mMessage;
	private BBS mBBS;
	private BlankPage mPage;
//	private Friends mFriends;
//	private Page mPage;
//	private Location mLocation;
//	private Search mSearch;
	private AppsCenter mAppsCenter;
	private CallNet mCallNet;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (BaseApplication) getApplication();
		mRoot = new FlipperLayout(DesktopActivity.this);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mRoot.setLayoutParams(params);
		
		mDesktop = new Desktop(mApplication, this);
//		mNewsFeed = new NewsFeed(mApplication, this, this);
//		mUser = new User(mApplication, this, this);
//		mMessage = new Message(this);
		mBBS = new BBS(mApplication, this,this);
		mPage = new BlankPage(this);
//		mFriends = new Friends(mApplication, this, this);
//		mPage = new Page(mApplication, this, this);
//		mLocation = new Location(mApplication, this, this);
//		mSearch = new Search(mApplication, this, this);
		mAppsCenter = new AppsCenter(mApplication, this, this);
//		mCallNet = new CallNet(mApplication, this, this);
		
		mRoot.addView(mDesktop.getView(), params);
		mRoot.addView(mBBS.getView(), params);
		setContentView(mRoot);
		setListener();
	}

	private void setListener() {
		mBBS.setOnOpenListener(this);
		mPage.setOnOpenListener(this);
//		mCallNet.setOnOpenListener(this);
		mAppsCenter.setOnOpenListener(this);
		
		mDesktop.setOnChangeViewListener(new onChangeViewListener() {

			public void onChangeView(int arg0) {
				switch (arg0) {
				case View_Util.Information:
//					mUser.init();
//					mRoot.close(mUser.getView());
					break;

				case View_Util.NewsFeed:
//					mRoot.close(mNewsFeed.getView());
					break;

				case View_Util.Message:
//					mRoot.close(mMessage.getView());
					break;
				case View_Util.Apps_Center:
					mRoot.close(mAppsCenter.getView());
					break;
				case View_Util.Chat:
					mRoot.close(mBBS.getView());
					break;

				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Uri uri = null;
		switch (requestCode) {
		case 0:
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
					return;
				}
				mApplication.mImageType = 0;
//				startActivity(new Intent(DesktopActivity.this,	PhotosEdit.class));
//				overridePendingTransition(R.anim.roll_up, R.anim.roll);
			} else {
				Toast.makeText(this, "取消拍照", Toast.LENGTH_SHORT).show();
			}
			break;

		case 1:
			if (data == null) {
				Toast.makeText(this, "取消上传", Toast.LENGTH_SHORT).show();
				return;
			}
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
					return;
				}
				uri = data.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(uri, proj, null, null, null);
				if (cursor != null) {
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						mApplication.mImagePath = cursor
								.getString(column_index);
						if (mApplication.mImagePath != null) {
							mApplication.mImageType = 1;
//							startActivity(new Intent(DesktopActivity.this,	PhotosEdit.class));
//							overridePendingTransition(R.anim.roll_up, R.anim.roll);
						} else {
							Toast.makeText(this, "图片未找到", Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						Toast.makeText(this, "图片未找到", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(this, "图片未找到", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "获取错误", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_CLOSE) {
//				mRoot.open();
//			} else {
//				dialog();
//			}
			
			if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_CLOSE) {
				dialog();
			} else {
				mRoot.closeWithout();
			}
			
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void dialog() {
		AlertDialog.Builder builder = new Builder(DesktopActivity.this);
		builder.setMessage("您确定要退出吗?");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				BaseApplication.getInstance().quitApp();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	public void open() {
		if (mRoot.getScreenState() == FlipperLayout.SCREEN_STATE_CLOSE) {
			mRoot.open();
		}
	}
}
