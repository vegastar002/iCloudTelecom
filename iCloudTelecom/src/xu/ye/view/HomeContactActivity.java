package xu.ye.view;

import java.util.List;
import java.util.Map;

import xu.ye.bean.ContactBean;
import xu.ye.view.ui.MenuHorizontalScrollView;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.hust.wa.icloudtelecom.R;

public class HomeContactActivity extends Activity {

	private MenuHorizontalScrollView scrollView;
	private ListView menuList;
	private View acbuwaPage;
	private Button menuBtn;
	private View[] children;
	private LayoutInflater inflater;


	private ListView personList;
	private List<ContactBean> list;
	private AsyncQueryHandler asyncQuery;
	private Button addContactBtn;

	private Map<Integer, ContactBean> contactIdMap = null;
	

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		inflater = LayoutInflater.from(this);
		setContentView(inflater.inflate(R.layout.home_dial_page, null));

	}



}
