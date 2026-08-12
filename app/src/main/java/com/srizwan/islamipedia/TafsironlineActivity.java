package com.srizwan.islamipedia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TafsironlineActivity extends AppCompatActivity {

	// sura.json এখন শুধু তালিকা (sura, suraName, type, versess, suraArabic, link) রাখে।
	// প্রতিটি সূরার পূর্ণ তাফসির আলাদা আলাদা ফাইলে (link) থাকে, যা TafsironlineviewActivity লোড করে।
	private static final String SURA_LIST_URL = "https://cdn.jsdelivr.net/gh/srizwan198704-dev/PE@main/sura.json";

	private Timer _timer = new Timer();

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
	private double n = 0;
	private HashMap<String, Object> listMap = new HashMap<>();
	private String saveMe = "";

	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap_cache = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private TextView bookname;
	private ImageView refresh;
	private ProgressBar progressbar1;
	private ImageView searchimg;
	private TextView version;
	private ProgressBar spinber;
	private LinearLayout Nointernet;
	private ImageView imageview3;
	private TextView textview1;
	private MaterialButton materialbutton1;
	private LinearLayout searxhmain;
	private ListView listview;
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
	private TimerTask timer;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.tafsironline);
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
		bookname = findViewById(R.id.bookname);
		refresh = findViewById(R.id.refresh);
		progressbar1 = findViewById(R.id.progressbar1);
		searchimg = findViewById(R.id.searchimg);
		version = findViewById(R.id.version);
		spinber = findViewById(R.id.spinber);
		Nointernet = findViewById(R.id.Nointernet);
		imageview3 = findViewById(R.id.imageview3);
		textview1 = findViewById(R.id.textview1);
		materialbutton1 = findViewById(R.id.materialbutton1);
		searxhmain = findViewById(R.id.searxhmain);
		listview = findViewById(R.id.listview);
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

		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (Rizwan.isConnected(getApplicationContext())) {
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
						refresh.setVisibility(View.GONE);
						progressbar1.setVisibility(View.VISIBLE);
						searchimg.setVisibility(View.GONE);
						FileUtil.deleteFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"));
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										materialbutton1.performClick();
										((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
									}
								});
							}
						};
						_timer.schedule(timer, (int)(50));
						map.clear();
						Toast.makeText(getApplicationContext(), "আপডেট হচ্ছে....।", Toast.LENGTH_SHORT).show();
					} else {

					}
				} else {
					Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				}
			}
		});

		searchimg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searxhmain.getVisibility() == View.GONE) {
					searxhmain.setVisibility(View.VISIBLE);
				} else {
					searxhmain.setVisibility(View.GONE);
				}
			}
		});

		materialbutton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				book.startRequestNetwork(RequestNetworkController.GET, SURA_LIST_URL, "", _book_request_listener);
				spinber.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
			}
		});

		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searchbox.getText().toString().equals("")) {
					searxhmain.setVisibility(View.GONE);
				} else {
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
					listview.setVisibility(View.GONE);
				} else {
					nores.setVisibility(View.GONE);
					listview.setVisibility(View.VISIBLE);
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
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
					map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					listview.setAdapter(new ListviewAdapter(map));
					((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(map);
					refresh.setVisibility(View.VISIBLE);
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
					searchimg.setVisibility(View.VISIBLE);
					progressbar1.setVisibility(View.GONE);
				} else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
						map = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						listview.setAdapter(new ListviewAdapter(map));
						((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(map);
						refresh.setVisibility(View.VISIBLE);
						spin.setVisibility(View.GONE);
						content.setVisibility(View.VISIBLE);
						Nointernet.setVisibility(View.GONE);
						searchimg.setVisibility(View.VISIBLE);
						progressbar1.setVisibility(View.GONE);
						saveMe = new Gson().toJson(map);
						FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"), saveMe);
					}
				}
				if (map.size() == 0) {
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					searchimg.setVisibility(View.GONE);
					refresh.setVisibility(View.GONE);
				} else {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
					refresh.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
					map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					listview.setAdapter(new ListviewAdapter(map));
					((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(map);
					refresh.setVisibility(View.VISIBLE);
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
					searchimg.setVisibility(View.VISIBLE);
					progressbar1.setVisibility(View.GONE);
				} else {
					refresh.setVisibility(View.VISIBLE);
					Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
					spinber.setVisibility(View.GONE);
					Nointernet.setVisibility(View.VISIBLE);
				}
				if (map.size() == 0) {
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					searchimg.setVisibility(View.GONE);
					refresh.setVisibility(View.GONE);
				} else {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
					refresh.setVisibility(View.VISIBLE);
				}
			}
		};
	}

	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		bookname.setText("তাফসির সমগ্র");
		_marquue(bookname, "তাফসির সমগ্র");
		click = 0;
		boxofsearch.setBoxCornerRadii((float)100, (float)100, (float)100, (float)100);
		boxofsearch.setBoxBackgroundColor(0xFFFFFFFF);
		Nointernet.setVisibility(View.GONE);
		searxhmain.setVisibility(View.GONE);
		searchimg.setVisibility(View.GONE);
		nores.setVisibility(View.GONE);
		refresh.setVisibility(View.GONE);
		if (map.size() == 0) {
			spin.setVisibility(View.VISIBLE);
			content.setVisibility(View.GONE);
			searchimg.setVisibility(View.GONE);
		} else {
			content.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		}
		if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
			map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			listview.setAdapter(new ListviewAdapter(map));
			((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
			getsearch = new Gson().toJson(map);
			refresh.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			Nointernet.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		} else {
			FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/")));
			if (Rizwan.isConnected(getApplicationContext())) {
				book.startRequestNetwork(RequestNetworkController.GET, SURA_LIST_URL, "Rizwan", _book_request_listener);
			} else {
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

				if (activeNetwork == null || !activeNetwork.isConnected()) {
					   Nointernet.setVisibility(View.VISIBLE); Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				}
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/সূরা তালিকা"))) {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
				} else {
					Toast.makeText(getApplicationContext(), "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					Nointernet.setVisibility(View.VISIBLE);
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
		map = new Gson().fromJson(getsearch, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = map.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = map.get((int)r).get("suraName").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {

			} else {
				map.remove((int)(r));
			}
			r--;
		}
		listview.setAdapter(new ListviewAdapter(map));
		((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
	}


	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");

		return result;
	}


	public void _rippleRoundStroke(final View _view, final String _focus, final String _pressed, final double _round, final double _stroke, final String _strokeclr) {
		android.graphics.drawable.GradientDrawable GG = new android.graphics.drawable.GradientDrawable();
		GG.setColor(Color.parseColor(_focus));
		GG.setCornerRadius((float)_round);
		GG.setStroke((int) _stroke,
		Color.parseColor("#" + _strokeclr.replace("#", "")));
		android.graphics.drawable.RippleDrawable RE = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor(_pressed)}), GG, null);
		_view.setBackground(RE);
	}

	public class ListviewAdapter extends BaseAdapter {

		ArrayList<HashMap<String, Object>> _data;

		public ListviewAdapter(ArrayList<HashMap<String, Object>> _arr) {
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
				_view = _inflater.inflate(R.layout.tafsirview, null);
			}

			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout bookpic = _view.findViewById(R.id.bookpic);
			final LinearLayout boxofcontent = _view.findViewById(R.id.boxofcontent);
			final TextView suraArabic = _view.findViewById(R.id.suraArabic);
			final TextView number = _view.findViewById(R.id.number);
			final TextView suraName = _view.findViewById(R.id.suraName);
			final TextView verses = _view.findViewById(R.id.verses);

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*20);
				SketchUi.setStroke(d*1,0xFF01837A);
				linear1.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
				linear1.setClickable(true);
			}
			if (map.get((int)_position).containsKey("suraName")) {
				suraName.setText(map.get((int)_position).get("suraName").toString());
				verses.setText(map.get((int)_position).get("versess").toString().concat(" | ".concat(map.get((int)_position).get("type").toString())));
				number.setText(_replaceArabicNumber(map.get((int)_position).get("sura").toString()));
				suraArabic.setText(map.get((int)_position).get("suraArabic").toString());
			} else {

			}
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (map.get((int)_position).get("suraName").toString().equals("none")) {
						Toast.makeText(getApplicationContext(), "বই যুক্ত করা হয়নি", Toast.LENGTH_SHORT).show();
					} else {
						in.setClass(getApplicationContext(), TafsironlineviewActivity.class);
						in.putExtra("name", map.get((int)_position).get("suraName").toString());
						in.putExtra("author", map.get((int)_position).get("versess").toString().concat(" | ".concat(map.get((int)_position).get("type").toString())));
						in.putExtra("sura", map.get((int)_position).get("sura").toString());
						in.putExtra("link", map.get((int)_position).get("link").toString());
						in.putExtra("bookname", bookname.getText().toString());
						startActivity(in);
					}
				}
			});

			return _view;
		}
	}
									  }
