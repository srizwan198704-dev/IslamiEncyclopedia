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
import android.widget.ScrollView;
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

public class TafsironlineviewActivity extends AppCompatActivity {

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
	private HashMap<String, Object> ListMap = new HashMap<>();

	private String suraNumber = "";
	private String suraLink = "";
	private String suraCacheFile = "";

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
		setContentView(R.layout.tafsironlineview);
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

		suraNumber = getIntent().getStringExtra("sura");
		suraLink = getIntent().getStringExtra("link");
		if (suraNumber == null) {
			suraNumber = "";
		}
		suraCacheFile = FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির/".concat(suraNumber.concat(".json")));

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
				} else {
					searxhmain.setVisibility(View.GONE);
				}
			}
		});

		materialbutton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (suraLink != null && !suraLink.equals("")) {
					book.startRequestNetwork(RequestNetworkController.GET, suraLink, "", _book_request_listener);
					spinber.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
				}
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
				if (chapter.size() == 0) {
					nores.setVisibility(View.VISIBLE);
					ListView1.setVisibility(View.GONE);
				} else {
					nores.setVisibility(View.GONE);
					ListView1.setVisibility(View.VISIBLE);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {}
			@Override
			public void afterTextChanged(Editable _param1) {}
		});

		_book_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				try {
					chapter = new Gson().fromJson(_param2, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					ListView1.setAdapter(new ListView1Adapter(chapter));
					((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
					getsearch = new Gson().toJson(chapter);
					searchimg.setVisibility(View.VISIBLE);
					spinber.setVisibility(View.GONE);
					FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির/")));
					FileUtil.writeFile(suraCacheFile, _param2);
				} catch (Exception e) {}
				if (chapter.size() == 0) {
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					searchimg.setVisibility(View.GONE);
				} else {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
				}
			}
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				if (FileUtil.isExistFile(suraCacheFile)) {
					try {
						chapter = new Gson().fromJson(FileUtil.readFile(suraCacheFile), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						ListView1.setAdapter(new ListView1Adapter(chapter));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(chapter);
						searchimg.setVisibility(View.VISIBLE);
						spin.setVisibility(View.GONE);
						content.setVisibility(View.VISIBLE);
						Nointernet.setVisibility(View.GONE);
						spinber.setVisibility(View.GONE);
					} catch (Exception e) {}
				} else {
					Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
					spinber.setVisibility(View.GONE);
					Nointernet.setVisibility(View.VISIBLE);
				}
				if (chapter.size() == 0) {
					spin.setVisibility(View.VISIBLE);
					content.setVisibility(View.GONE);
					searchimg.setVisibility(View.GONE);
				} else {
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
				}
			}
		};
	}

	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		_marquue(bookname, getIntent().getStringExtra("name"));
		_marquue(author, getIntent().getStringExtra("author"));
		click = 0;
		boxofsearch.setBoxCornerRadii((float)100, (float)100, (float)100, (float)100);
		boxofsearch.setBoxBackgroundColor(0xFFFFFFFF);
		Nointernet.setVisibility(View.GONE);
		searxhmain.setVisibility(View.GONE);
		searchimg.setVisibility(View.GONE);
		nores.setVisibility(View.GONE);
		if (chapter.size() == 0) {
			spin.setVisibility(View.VISIBLE);
			content.setVisibility(View.GONE);
			searchimg.setVisibility(View.GONE);
		} else {
			content.setVisibility(View.VISIBLE);
			spin.setVisibility(View.GONE);
			searchimg.setVisibility(View.VISIBLE);
		}

		if (FileUtil.isExistFile(suraCacheFile)) {
			try {
				chapter = new Gson().fromJson(FileUtil.readFile(suraCacheFile), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				ListView1.setAdapter(new ListView1Adapter(chapter));
				((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
				getsearch = new Gson().toJson(chapter);
				spin.setVisibility(View.GONE);
				content.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
				searchimg.setVisibility(View.VISIBLE);
			} catch (Exception e) {}
		} else {
			FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির/")));
			if (suraLink != null && !suraLink.equals("") && Rizwan.isConnected(getApplicationContext())) {
				spinber.setVisibility(View.VISIBLE);
				book.startRequestNetwork(RequestNetworkController.GET, suraLink, "", _book_request_listener);
			} else {
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
				if (activeNetwork == null || !activeNetwork.isConnected()) {
					   Nointernet.setVisibility(View.VISIBLE); Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				}
				Toast.makeText(getApplicationContext(), "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
				spin.setVisibility(View.VISIBLE);
				content.setVisibility(View.GONE);
				Nointernet.setVisibility(View.VISIBLE);
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
		for(int _repeat17 = 0; _repeat17 < (int)(length); _repeat17++) {
			value1 = chapter.get((int)r).get("name").toString();
			value2 = chapter.get((int)r).get("irfanul").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
			} else {
				if (!(_charSeq.length() > value2.length()) && value2.toLowerCase().contains(_charSeq.toLowerCase())) {
				} else {
					chapter.remove((int)(r));
				}
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

	public void _enable_copy_textview(final TextView _tv) {
		_tv.setTextIsSelectable(true);
	}

	public String _getVal(final HashMap<String, Object> _map, final String _key, final String _fallback) {
		if (_map != null && _map.containsKey(_key) && _map.get(_key) != null) {
			String v = _map.get(_key).toString().trim();
			if (!v.isEmpty() && !v.equalsIgnoreCase("null")) {
				return v;
			}
		}
		return _fallback;
	}

	// তোমার চাওয়া লজিক: টেক্সটে "তাফসির যুক্ত করা হয়নি" বা "শব্দার্থ যুক্ত করা হয়নি" থাকলে বা খালি থাকলে GONE
	public boolean _isEmptyTafsir(final String _val) {
		if (_val == null) return true;
		String t = _val.trim();
		if (t.isEmpty()) return true;
		if (t.contains("তাফসির যুক্ত করা হয়নি") || t.contains("তাফসির যুক্ত করা হয়নি")) return true;
		if (t.contains("শব্দার্থ যুক্ত করা হয়নি") || t.contains("শব্দার্থ যুক্ত করা হয়নি")) return true;
		return false;
	}

	public class ListView1Adapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> _data;
		public ListView1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		@Override
		public int getCount() { return _data.size(); }
		@Override
		public HashMap<String, Object> getItem(int _index) { return _data.get(_index); }
		@Override
		public long getItemId(int _index) { return _index; }

		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.tafsir, null);
			}

			final ScrollView vscrollmain = _view.findViewById(R.id.vscrollmain);
			final LinearLayout main = _view.findViewById(R.id.main);
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final TextView ayaarabic = _view.findViewById(R.id.ayaarabic);
			final TextView words = _view.findViewById(R.id.words);
			final LinearLayout maintafsiribnabbas = _view.findViewById(R.id.maintafsiribnabbas);
			final LinearLayout mainkanzulimaanlayout = _view.findViewById(R.id.mainkanzulimaanlayout);
			final LinearLayout mainkhazainulirfan = _view.findViewById(R.id.mainkhazainulirfan);
			final LinearLayout maintafsirnurulirfan = _view.findViewById(R.id.maintafsirnurulirfan);
			final LinearLayout mainirfanullayout = _view.findViewById(R.id.mainirfanullayout);
			final LinearLayout maintafsirtabari = _view.findViewById(R.id.maintafsirtabari);
			final LinearLayout maintafsirmajhari = _view.findViewById(R.id.maintafsirmajhari);
			final LinearLayout maintafsiribnkasir = _view.findViewById(R.id.maintafsiribnkasir);
			final LinearLayout maintafsirkurtubi = _view.findViewById(R.id.maintafsirkurtubi);
			final LinearLayout maintafsirbaizabi = _view.findViewById(R.id.maintafsirbaizabi);
			final LinearLayout maintafsirrezbiya = _view.findViewById(R.id.maintafsirrezbiya);
			final LinearLayout linear3 = _view.findViewById(R.id.linear3);
			final TextView number = _view.findViewById(R.id.number);
			final TextView headingtafsiribnabbas = _view.findViewById(R.id.headingtafsiribnabbas);
			final TextView texttafsiribnabbas = _view.findViewById(R.id.texttafsiribnabbas);
			final TextView headingkanzulimaan = _view.findViewById(R.id.headingkanzulimaan);
			final TextView textkanzuliman = _view.findViewById(R.id.textkanzuliman);
			final TextView headingkhazainulirfan = _view.findViewById(R.id.headingkhazainulirfan);
			final TextView texttafsirkhazainulirfan = _view.findViewById(R.id.texttafsirkhazainulirfan);
			final TextView headingtafsirnurulirfan = _view.findViewById(R.id.headingtafsirnurulirfan);
			final TextView texttafsirnurulirfan = _view.findViewById(R.id.texttafsirnurulirfan);
			final TextView headingirfanulkuran = _view.findViewById(R.id.headingirfanulkuran);
			final TextView textifranulkuran = _view.findViewById(R.id.textifranulkuran);
			final TextView headingtafsirtabari = _view.findViewById(R.id.headingtafsirtabari);
			final TextView texttafsirtabari = _view.findViewById(R.id.texttafsirtabari);
			final TextView headingtafsirmajhari = _view.findViewById(R.id.headingtafsirmajhari);
			final TextView texttafsirmajhari = _view.findViewById(R.id.texttafsirmajhari);
			final TextView headingtafsiribnkasir = _view.findViewById(R.id.headingtafsiribnkasir);
			final TextView texttafsiribnkasir = _view.findViewById(R.id.texttafsiribnkasir);
			final TextView headingtafsirkurtubi = _view.findViewById(R.id.headingtafsirkurtubi);
			final TextView texttafsirkurtubi = _view.findViewById(R.id.texttafsirkurtubi);
			final TextView headingtafsirbaizabi = _view.findViewById(R.id.headingtafsirbaizabi);
			final TextView texttafsirbaizabi = _view.findViewById(R.id.texttafsirbaizabi);
			final TextView headingtafsirrezbiya = _view.findViewById(R.id.headingtafsirrezbiya);
			final TextView texttafsirrezviya = _view.findViewById(R.id.texttafsirrezviya);

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*20);
				SketchUi.setStroke(d*1,0xFF01837A);
				main.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				main.setBackground(SketchUi_RD);
			}

			if (_data.get((int)_position).containsKey("verses")) {
				final HashMap<String, Object> _row = _data.get((int)_position);

				String versesNo = _getVal(_row, "verses", "");
				String arabic = _getVal(_row, "names", "");
				String wordMeaning = _getVal(_row, "words", "শব্দার্থ যুক্ত করা হয়নি");
				String kanzul = _getVal(_row, "name", "তাফসির যুক্ত করা হয়নি");
				String khazainul = _getVal(_row, "khazainul", "তাফসির যুক্ত করা হয়নি");
				String irfanul = _getVal(_row, "irfanul", "তাফসির যুক্ত করা হয়নি");
				String ibnabbas = _getVal(_row, "ibnabbas", "তাফসির যুক্ত করা হয়নি");
				String majhari = _getVal(_row, "majhari", "তাফসির যুক্ত করা হয়নি");
				String nurulirfan = _getVal(_row, "nurulirfan", "তাফসির যুক্ত করা হয়নি");
				String tabari = _getVal(_row, "tabari", "তাফসির যুক্ত করা হয়নি");
				String ibnkasir = _getVal(_row, "ibnkasir", "তাফসির যুক্ত করা হয়নি");
				String kurtubi = _getVal(_row, "kurtubi", "তাফসির যুক্ত করা হয়নি");
				String rejviya = _getVal(_row, "rejviya", "তাফসির যুক্ত করা হয়নি");
				String baizabi = _getVal(_row, "baizabi", "তাফসির যুক্ত করা হয়নি");

				number.setText(_replaceArabicNumber(versesNo));
				ayaarabic.setText(arabic);
				words.setText(wordMeaning);
				textkanzuliman.setText(_replaceArabicNumber(versesNo + ". " + kanzul));
				texttafsirkhazainulirfan.setText(khazainul);
				textifranulkuran.setText(irfanul);
				texttafsiribnabbas.setText(ibnabbas);
				texttafsirmajhari.setText(majhari);
				texttafsirnurulirfan.setText(nurulirfan);
				texttafsirtabari.setText(tabari);
				texttafsiribnkasir.setText(ibnkasir);
				texttafsirkurtubi.setText(kurtubi);
				texttafsirrezviya.setText(rejviya);
				texttafsirbaizabi.setText(baizabi);

				// শব্দার্থ
				if (_isEmptyTafsir(wordMeaning)) {
					words.setVisibility(View.GONE);
				} else {
					words.setVisibility(View.VISIBLE);
				}

				// --- তোমার চাওয়া মতো hide লজিক ---
				// kanzul এবং irfanul এর text VISIBLE থাকবে আগের মতো
				if (_isEmptyTafsir(kanzul)) {
					mainkanzulimaanlayout.setVisibility(View.GONE);
				} else {
					mainkanzulimaanlayout.setVisibility(View.VISIBLE);
					textkanzuliman.setVisibility(View.VISIBLE);
				}

				if (_isEmptyTafsir(irfanul)) {
					mainirfanullayout.setVisibility(View.GONE);
				} else {
					mainirfanullayout.setVisibility(View.VISIBLE);
					textifranulkuran.setVisibility(View.VISIBLE);
				}

				// বাকি গুলো - parent GONE/VISIBLE, ভিতরের text শুরুতে GONE
				mainkhazainulirfan.setVisibility(_isEmptyTafsir(khazainul) ? View.GONE : View.VISIBLE);
				maintafsiribnabbas.setVisibility(_isEmptyTafsir(ibnabbas) ? View.GONE : View.VISIBLE);
				maintafsirnurulirfan.setVisibility(_isEmptyTafsir(nurulirfan) ? View.GONE : View.VISIBLE);
				maintafsirtabari.setVisibility(_isEmptyTafsir(tabari) ? View.GONE : View.VISIBLE);
				maintafsirmajhari.setVisibility(_isEmptyTafsir(majhari) ? View.GONE : View.VISIBLE);
				maintafsiribnkasir.setVisibility(_isEmptyTafsir(ibnkasir) ? View.GONE : View.VISIBLE);
				maintafsirkurtubi.setVisibility(_isEmptyTafsir(kurtubi) ? View.GONE : View.VISIBLE);
				maintafsirbaizabi.setVisibility(_isEmptyTafsir(baizabi) ? View.GONE : View.VISIBLE);
				maintafsirrezbiya.setVisibility(_isEmptyTafsir(rejviya) ? View.GONE : View.VISIBLE);

				// বাকি গুলোর ভিতরের text আগের মতো বন্ধ থাকবে
				texttafsirkhazainulirfan.setVisibility(View.GONE);
				texttafsiribnabbas.setVisibility(View.GONE);
				texttafsirnurulirfan.setVisibility(View.GONE);
				texttafsirtabari.setVisibility(View.GONE);
				texttafsirmajhari.setVisibility(View.GONE);
				texttafsiribnkasir.setVisibility(View.GONE);
				texttafsirkurtubi.setVisibility(View.GONE);
				texttafsirbaizabi.setVisibility(View.GONE);
				texttafsirrezviya.setVisibility(View.GONE);
			}

			headingkanzulimaan.setOnClickListener(v -> {
				if (textkanzuliman.getVisibility() == View.GONE) textkanzuliman.setVisibility(View.VISIBLE);
				else textkanzuliman.setVisibility(View.GONE);
			});
			headingirfanulkuran.setOnClickListener(v -> {
				if (textifranulkuran.getVisibility() == View.GONE) textifranulkuran.setVisibility(View.VISIBLE);
				else textifranulkuran.setVisibility(View.GONE);
			});
			headingkhazainulirfan.setOnClickListener(v -> toggle(texttafsirkhazainulirfan));
			headingtafsiribnabbas.setOnClickListener(v -> toggle(texttafsiribnabbas));
			headingtafsirnurulirfan.setOnClickListener(v -> toggle(texttafsirnurulirfan));
			headingtafsirtabari.setOnClickListener(v -> toggle(texttafsirtabari));
			headingtafsirmajhari.setOnClickListener(v -> toggle(texttafsirmajhari));
			headingtafsiribnkasir.setOnClickListener(v -> toggle(texttafsiribnkasir));
			headingtafsirkurtubi.setOnClickListener(v -> toggle(texttafsirkurtubi));
			headingtafsirbaizabi.setOnClickListener(v -> toggle(texttafsirbaizabi));
			headingtafsirrezbiya.setOnClickListener(v -> toggle(texttafsirrezviya));

			_enable_copy_textview(ayaarabic);
			_enable_copy_textview(words);
			_enable_copy_textview(texttafsiribnabbas);
			_enable_copy_textview(textkanzuliman);
			_enable_copy_textview(texttafsirkhazainulirfan);
			_enable_copy_textview(texttafsirnurulirfan);
			_enable_copy_textview(textifranulkuran);
			_enable_copy_textview(texttafsirtabari);
			_enable_copy_textview(texttafsirmajhari);
			_enable_copy_textview(texttafsiribnkasir);
			_enable_copy_textview(texttafsirkurtubi);
			_enable_copy_textview(texttafsirbaizabi);
			_enable_copy_textview(texttafsirrezviya);

			return _view;
		}

		private void toggle(TextView tv) {
			if (tv.getVisibility() == View.GONE) tv.setVisibility(View.VISIBLE);
			else tv.setVisibility(View.GONE);
		}
	}
			}
