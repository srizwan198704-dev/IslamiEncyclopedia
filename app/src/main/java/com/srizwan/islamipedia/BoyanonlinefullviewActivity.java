package com.srizwan.islamipedia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.os.Bundle;
import android.text.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.View;
import android.widget.*;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.*;
import com.google.android.material.textfield.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class BoyanonlinefullviewActivity extends AppCompatActivity {

	private String newName = "";
	private double click = 0;
	private String a = "";
	private String b = "";
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	private String value2 = "";
	private String value3 = "";
	private String getsearch = "";
	private String downloadDirectory = "";
	private String downloadzip = "";
	private boolean tap = false;
	private String vUrl = "";
	private String vFilename = "";
	private boolean vResumePause = false;
	private double download_progress = 0;
	private String Current_Size = "";
	private String Total_Size = "";
	private String debug_string = "";

	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> chapter = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private LinearLayout LinearLayout1;
	private ImageView searchimg;
	private TextView bookname;
	private TextView author;
	private ProgressBar spinber;
	private LinearLayout Nointernet;
	private ImageView imageview3;
	private TextView textview1;
	private MaterialButton materialbutton1;
	private LinearLayout searxhmain;
	private ListView ListView1;
	private LinearLayout nores;
	private TextInputLayout boxofsearch;
	private ImageView imageview2;
	private EditText searchbox;
	private ImageView noresult;
	private TextView no_result;

	private RequestNetwork book;
	private RequestNetwork.RequestListener _book_request_listener;
	private Intent in = new Intent();
	private AlertDialog.Builder deleted;
	private AlertDialog.Builder onlineoffline;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.bookonlinefullview);
		initialize(_savedInstanceState);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}

	private void initialize(Bundle _savedInstanceState) {
		toolbar = findViewById(R.id.toolbar);
		spin = findViewById(R.id.spin);
		content = findViewById(R.id.content);
		list = findViewById(R.id.list);
		box = findViewById(R.id.box);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		searchimg = findViewById(R.id.searchimg);
		bookname = findViewById(R.id.bookname);
		author = findViewById(R.id.author);
		spinber = findViewById(R.id.spinber);
		Nointernet = findViewById(R.id.Nointernet);
		imageview3 = findViewById(R.id.imageview3);
		textview1 = findViewById(R.id.textview1);
		materialbutton1 = findViewById(R.id.materialbutton1);
		searxhmain = findViewById(R.id.searxhmain);
		ListView1 = findViewById(R.id.ListView1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);
		book = new RequestNetwork(this);
		deleted = new AlertDialog.Builder(this);
		onlineoffline = new AlertDialog.Builder(this);

		list.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		searchimg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searxhmain.getVisibility() == View.GONE) {
					searxhmain.setVisibility(View.VISIBLE);
				}
				else {
						searxhmain.setVisibility(View.GONE);
				}
			}
		});

		materialbutton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.boyan, "", _book_request_listener);
				spinber.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
			}
		});

		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searchbox.getText().toString().equals("")) {
					searxhmain.setVisibility(View.GONE);
				}
				else {
					searchbox.setText("");
				}
			}
		});

		searchbox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				_json_search(_charSeq);
				if (map.size() == 0) {
					nores.setVisibility(View.VISIBLE);
					ListView1.setVisibility(View.GONE);
				}
				else {
					nores.setVisibility(View.GONE);
					ListView1.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {

			}

			@Override
			public void afterTextChanged(Editable _param1) {

			}
		});

		_book_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
					try{
						map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						chapter = new Gson().fromJson(new Gson().toJson((ArrayList<HashMap<String,Object>>)map.get((int)(Double.parseDouble(getIntent().getStringExtra("id")))).get("video")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						ListView1.setAdapter(new ListView1Adapter(chapter));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(chapter);
						searchimg.setVisibility(View.VISIBLE);
					}catch(Exception e){

					}
				}
				else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
						map = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						chapter = new Gson().fromJson(new Gson().toJson((ArrayList<HashMap<String,Object>>)map.get((int)(Double.parseDouble(getIntent().getStringExtra("id")))).get("video")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						ListView1.setAdapter(new ListView1Adapter(chapter));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						searchimg.setVisibility(View.VISIBLE);
						getsearch = new Gson().toJson(chapter);
					}
				}
				if (map.size() == 0) {
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					searchimg.setVisibility(View.GONE);
				}
				else {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
					map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					ListView1.setAdapter(new ListView1Adapter(map));
					((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(map);
					searchimg.setVisibility(View.VISIBLE);
				}
				else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
						Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
						spinber.setVisibility(View.GONE);
						Nointernet.setVisibility(View.VISIBLE);
					}
				}
			}
		};
	}

	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		_marquue(bookname, getIntent().getStringExtra("name"));
		_marquue(author, getIntent().getStringExtra("author"));
		click = 0;
		searchbox.setHint("সার্চ করুন");
		boxofsearch.setHint("");
		boxofsearch.setBoxCornerRadii((float)100, (float)100, (float)100, (float)100);
		boxofsearch.setBoxBackgroundColor(0xFFFFFFFF);
		Nointernet.setVisibility(View.GONE);
		searxhmain.setVisibility(View.GONE);
		searchimg.setVisibility(View.GONE);
		nores.setVisibility(View.GONE);
		ListView1.setFastScrollEnabled(true);
		if (map.size() == 0) {
			spin.setVisibility(View.VISIBLE);
			content.setVisibility(View.GONE);
			searchimg.setVisibility(View.GONE);
		}
		else {
			content.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		}
		if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
			map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			chapter = new Gson().fromJson(new Gson().toJson((ArrayList<HashMap<String,Object>>)map.get((int)(Double.parseDouble(getIntent().getStringExtra("id")))).get("video")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			ListView1.setAdapter(new ListView1Adapter(chapter));
			((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
			getsearch = new Gson().toJson(chapter);
			spin.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			Nointernet.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		}
		else {
			if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
				FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/")));
				if (Rizwan.isConnected(getApplicationContext())) {
					book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.boyan, "", _book_request_listener);
				}
				else {
					ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

					if (activeNetwork == null || !activeNetwork.isConnected()) {
						Nointernet.setVisibility(View.VISIBLE); Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
					}
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))) {
						spin.setVisibility(View.GONE);
						content.setVisibility(View.VISIBLE);
						Nointernet.setVisibility(View.GONE);
					}
					else {
						Toast.makeText(getApplicationContext(), "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
						spin.setVisibility(View.VISIBLE);
						content.setVisibility(View.GONE);
						Nointernet.setVisibility(View.VISIBLE);
					}
				}
			}
		}

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (searxhmain.getVisibility() == View.VISIBLE) {
					if (searchbox.getText().toString().equals("")) {
						searxhmain.setVisibility(View.GONE);
					} else {
						searchbox.setText("");
					}
				} else {
					finish();
				}
			}
		});
	}


	public void _marquue(final TextView _text, final String _texto) {
		_text.setText(_texto);
		_text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		_text.setSelected(true);
		_text.setHorizontallyScrolling(true);
		_text.setMarqueeRepeatLimit(-1);
		_text.setSingleLine(true);
		_text.setFocusable(true);
		_text.setFocusableInTouchMode(true);
	}


	public void _status_bar_color(final String _colour1, final String _colour2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Window w = this.getWindow(); w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			w.setStatusBarColor(Color.parseColor(_colour1)); w.setNavigationBarColor(Color.parseColor(_colour2));
		}
	}


	public void _json_search(final String _charSeq) {
		chapter = new Gson().fromJson(getsearch, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = chapter.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = chapter.get((int)r).get("name").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {

			}
			else {
				chapter.remove((int)(r));
			}
			r--;
		}
		ListView1.setAdapter(new ListView1Adapter(chapter));
		((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
	}


	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");

		return result;
	}

	public class ListView1Adapter extends BaseAdapter {

		ArrayList<HashMap<String, Object>> _data;

		public ListView1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public int getCount() {
			return _data.size();
		}

		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}

		@Override
		public long getItemId(int _index) {
			return _index;
		}

		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.bookonlinefull, null);
			}

			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout bookpic = _view.findViewById(R.id.bookpic);
			final LinearLayout boxofcontent = _view.findViewById(R.id.boxofcontent);
			final TextView number = _view.findViewById(R.id.number);
			final TextView name = _view.findViewById(R.id.name);

			bookpic.setVisibility(View.GONE);

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*20);
				SketchUi.setStroke(d*1,0xFF01837A);
				linear1.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
			}
			if (chapter.get((int)_position).containsKey("name")) {
				name.setText(chapter.get((int)_position).get("name").toString());
				number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
			}
			else {

			}
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (chapter.get((int)_position).get("link").toString().equals("")) {
						Toast.makeText(getApplicationContext(), "ভিডিও যুক্ত করা হয়নি", Toast.LENGTH_SHORT).show();
					}
					else {
						in.setClass(getApplicationContext(), PlayerActivity.class);
						in.putExtra("name", chapter.get((int)_position).get("name").toString());
						in.putExtra("author", chapter.get((int)_position).get("link").toString());
						startActivity(in);
					}
				}
			});
			if (chapter.get((int)_position).get("link").toString().equals("")) {
				{
					android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
					int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
					SketchUi.setColor(0xFF01837A);
					SketchUi.setCornerRadius(d*20);
					linear1.setElevation(d*5);
					android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
					linear1.setBackground(SketchUi_RD);
				}
				linear1.setEnabled(false);
				name.setTextColor(0xFFFFFFFF);
				bookpic.setVisibility(View.GONE);
				linear1.setGravity(Gravity.CENTER);
				name.setGravity(Gravity.CENTER);
				boxofcontent.setGravity(Gravity.CENTER);
			}
			else {
				{
					android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
					int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
					SketchUi.setColor(0xFFFFFFFF);
					SketchUi.setCornerRadius(d*20);
					SketchUi.setStroke(d*1,0xFF01837A);
					linear1.setElevation(d*5);
					android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
					linear1.setBackground(SketchUi_RD);
				}
				linear1.setEnabled(true);
				name.setTextColor(0xFF000000);
				bookpic.setVisibility(View.VISIBLE);
				linear1.setGravity(Gravity.CENTER_VERTICAL);
				name.setGravity(Gravity.CENTER_VERTICAL);
				boxofcontent.setGravity(Gravity.CENTER_VERTICAL);
			}

			return _view;
		}
	}
}
