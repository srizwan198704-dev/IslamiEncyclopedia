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
import java.util.Timer;
import java.util.TimerTask;

public class BoyanActivity extends AppCompatActivity {

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

	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> updatebook = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private TextView bookname;
	private ImageView refresh;
	private ImageView searchimg;
	private ProgressBar spinber;
	private ProgressBar refreshprogress;
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
	private TextView version;

	private RequestNetwork book;
	private RequestNetwork.RequestListener _book_request_listener;
	private Intent in = new Intent();
	private AlertDialog.Builder deleted;
	private AlertDialog.Builder onlineoffline;
	private TimerTask timer;
	private RequestNetwork updateBook;
	private RequestNetwork.RequestListener _updateBook_request_listener;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.bookonline);
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
		searchimg = findViewById(R.id.searchimg);
		spinber = findViewById(R.id.spinber);
		refreshprogress = findViewById(R.id.refreshprogress);
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
		version = findViewById(R.id.version);
		book = new RequestNetwork(this);
		deleted = new AlertDialog.Builder(this);
		onlineoffline = new AlertDialog.Builder(this);
		updateBook = new RequestNetwork(this);

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
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/")))) {
						refresh.setVisibility(View.GONE);
						refreshprogress.setVisibility(View.VISIBLE);
						FileUtil.deleteFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/")));
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										materialbutton1.performClick();
									}
								});
							}
						};
						_timer.schedule(timer, (int)(50));
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
				book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.boyan, "", _book_request_listener);
				spinber.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
			}
		});

		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searchbox.getText().toString().isEmpty()) {
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
					ListView1.setVisibility(View.GONE);
				} else {
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
					map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					ListView1.setAdapter(new ListView1Adapter(map));
					((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(map);
					refresh.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
					refreshprogress.setVisibility(View.GONE);
					version.setText(map.get((int)0).get("version").toString());
				} else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
						map = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						ListView1.setAdapter(new ListView1Adapter(map));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(map);
						FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/".concat(bookname.getText().toString()))), getsearch);
						refresh.setVisibility(View.VISIBLE);
						refreshprogress.setVisibility(View.GONE);
						version.setText(map.get((int)0).get("version").toString());
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
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
					map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					ListView1.setAdapter(new ListView1Adapter(map));
					((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(map);
					refresh.setVisibility(View.VISIBLE);
				} else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
						refresh.setVisibility(View.VISIBLE);
						Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
						spinber.setVisibility(View.GONE);
						Nointernet.setVisibility(View.VISIBLE);
					}
				}
			}
		};

		_updateBook_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				updatebook = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				if (Double.parseDouble(version.getText().toString()) < Double.parseDouble(updatebook.get((int)0).get("version").toString())) {
					if(!BoyanActivity.this.isFinishing()) {
						final AlertDialog dialog1 = new AlertDialog.Builder(BoyanActivity.this).create();
						View inflate = getLayoutInflater().inflate(R.layout.update, null);
						dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
						dialog1.setView(inflate);
						TextView now = (TextView) inflate.findViewById(R.id.now);

						TextView version = (TextView) inflate.findViewById(R.id.version);

						TextView whats_new = (TextView) inflate.findViewById(R.id.whats_new);

						LinearLayout bg = (LinearLayout) inflate.findViewById(R.id.bg);
						version.setText(updatebook.get((int) 0).get("title").toString());
						whats_new.setText(updatebook.get((int) 0).get("new").toString());
						now.setText("রিফ্রেশ করুন");
						_rippleRoundStroke(bg, "#FFFFFF", "#000000", 15, 0, "#000000");
						_rippleRoundStroke(now, "#FF9800", "#40FFFFFF", 15, 0, "#000000");
						now.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								refresh.performClick();
								dialog1.hide();
							}
						});
						dialog1.setCancelable(true);
						dialog1.show();
					}
				} else {

				}
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;

			}
		};
	}

	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		bookname.setText("বয়ান");
		_marquue(bookname, "বয়ান");
		click = 0;
		searchbox.setHint("সার্চ করুন");
		boxofsearch.setHint("");
		boxofsearch.setBoxCornerRadii((float)100, (float)100, (float)100, (float)100);
		boxofsearch.setBoxBackgroundColor(0xFFFFFFFF);
		Nointernet.setVisibility(View.GONE);
		searxhmain.setVisibility(View.GONE);
		searchimg.setVisibility(View.GONE);
		nores.setVisibility(View.GONE);
		refresh.setVisibility(View.GONE);
		ListView1.setFastScrollEnabled(true);
		if (map.size() == 0) {
			spin.setVisibility(View.VISIBLE);
			content.setVisibility(View.GONE);
			searchimg.setVisibility(View.GONE);
		} else {
			content.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		}
		if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
			map = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান"))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			ListView1.setAdapter(new ListView1Adapter(map));
			((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
			getsearch = new Gson().toJson(map);
			refresh.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			Nointernet.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
			version.setText(map.get((int)0).get("version").toString());
			if (Rizwan.isConnected(getApplicationContext())) {
				updateBook.startRequestNetwork(RequestNetworkController.GET, "https://www.dropbox.com/scl/fi/k6qalkwdj6fl38bem554p/boyanupdate.json?rlkey=zhtai9yre9hrg9gxgxj4qsd9h&st=vt5qtfs9&dl=1", "Rizwan", _updateBook_request_listener);
			} else {

			}
		} else {
			FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/")));
			if (Rizwan.isConnected(getApplicationContext())) {
				book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.boyan, "Rizwan", _book_request_listener);
			} else {
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

				if (activeNetwork == null || !activeNetwork.isConnected()) {
					   Nointernet.setVisibility(View.VISIBLE); Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				}
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন ভিডিও ২/বয়ান")))) {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
				} else {
					Toast.makeText(getApplicationContext(), "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
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
			value1 = map.get((int)r).get("name").toString();
			value2 = map.get((int)r).get("author").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {

			} else {
				if (!(_charSeq.length() > value2.length()) && value2.toLowerCase().contains(_charSeq.toLowerCase())) {

				} else {
					map.remove((int)(r));
				}
			}
			r--;
		}
		ListView1.setAdapter(new ListView1Adapter(map));
		((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
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
				_view = _inflater.inflate(R.layout.onlinebokk, null);
			}

			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout bookpic = _view.findViewById(R.id.bookpic);
			final LinearLayout boxofcontent = _view.findViewById(R.id.boxofcontent);
			final TextView number = _view.findViewById(R.id.number);
			final TextView name = _view.findViewById(R.id.name);
			final TextView author = _view.findViewById(R.id.author);
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
				linear1.setClickable(true);
			}
			if (map.get((int)_position).containsKey("name")) {
				name.setText(map.get((int)_position).get("name").toString());
				author.setText("আলোচক : ".concat(map.get((int)_position).get("author").toString()));
				number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
			} else {

			}
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (map.get((int)_position).get("name").toString().equals("none")) {
						Toast.makeText(getApplicationContext(), "ভিডিও যুক্ত করা হয়নি", Toast.LENGTH_SHORT).show();
					} else {
						in.setClass(getApplicationContext(), BoyanonlinefullviewActivity.class);
						in.putExtra("name", map.get((int)_position).get("name").toString());
						in.putExtra("author", author.getText().toString());
						in.putExtra("id", map.get((int)_position).get("id").toString());
						startActivity(in);
					}
				}
			});

			return _view;
		}
	}
}
