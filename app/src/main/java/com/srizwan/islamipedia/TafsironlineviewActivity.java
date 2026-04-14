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
	
	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> chapter = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap_cache = new ArrayList<>();
	
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
				book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", _book_request_listener);
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
				if (chapter.size() == 0) {
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
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
					try{
						listmap_cache = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						n = 0;
						for(int _repeat40 = 0; _repeat40 < (int)(listmap_cache.size()); _repeat40++) {
							if (listmap_cache.get((int)n).get("sura").toString().equals(getIntent().getStringExtra("sura"))) {
								ListMap = new HashMap<>();
								ListMap.put("verses", listmap_cache.get((int)n).get("verses").toString());
								ListMap.put("names", listmap_cache.get((int)n).get("names").toString());
								ListMap.put("words", listmap_cache.get((int)n).get("words").toString());
								ListMap.put("name", listmap_cache.get((int)n).get("name").toString());
								ListMap.put("khazainul", listmap_cache.get((int)n).get("khazainul").toString());
								ListMap.put("irfanul", listmap_cache.get((int)n).get("irfanul").toString());
								ListMap.put("ibnabbas", listmap_cache.get((int)n).get("ibnabbas").toString());
								ListMap.put("majhari", listmap_cache.get((int)n).get("majhari").toString());
								ListMap.put("nurulirfan", listmap_cache.get((int)n).get("nurulirfan").toString());
								ListMap.put("tabari", listmap_cache.get((int)n).get("tabari").toString());
								ListMap.put("ibnkasir", listmap_cache.get((int)n).get("ibnkasir").toString());
								ListMap.put("rejviya", listmap_cache.get((int)n).get("rejviya").toString());
								ListMap.put("baizabi", listmap_cache.get((int)n).get("baizabi").toString());
								ListMap.put("kurtubi", listmap_cache.get((int)n).get("kurtubi").toString());
								chapter.add(ListMap);
							}
							n++;
						}
						ListView1.setAdapter(new ListView1Adapter(chapter));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(chapter);
						searchimg.setVisibility(View.VISIBLE);
					}catch(Exception e){
						 
					}
				} else {
					try{
						if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
							listmap_cache = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
							n = 0;
							for(int _repeat96 = 0; _repeat96 < (int)(listmap_cache.size()); _repeat96++) {
								if (listmap_cache.get((int)n).get("sura").toString().equals(getIntent().getStringExtra("sura"))) {
									ListMap = new HashMap<>();
									ListMap.put("verses", listmap_cache.get((int)n).get("verses").toString());
									ListMap.put("names", listmap_cache.get((int)n).get("names").toString());
									ListMap.put("words", listmap_cache.get((int)n).get("words").toString());
									ListMap.put("name", listmap_cache.get((int)n).get("name").toString());
									ListMap.put("khazainul", listmap_cache.get((int)n).get("khazainul").toString());
									ListMap.put("irfanul", listmap_cache.get((int)n).get("irfanul").toString());
									ListMap.put("ibnabbas", listmap_cache.get((int)n).get("ibnabbas").toString());
									ListMap.put("majhari", listmap_cache.get((int)n).get("majhari").toString());
									ListMap.put("nurulirfan", listmap_cache.get((int)n).get("nurulirfan").toString());
									ListMap.put("tabari", listmap_cache.get((int)n).get("tabari").toString());
									ListMap.put("ibnkasir", listmap_cache.get((int)n).get("ibnkasir").toString());
									ListMap.put("rejviya", listmap_cache.get((int)n).get("rejviya").toString());
									ListMap.put("baizabi", listmap_cache.get((int)n).get("baizabi").toString());
									ListMap.put("kurtubi", listmap_cache.get((int)n).get("kurtubi").toString());
									chapter.add(ListMap);
								}
								n++;
							}
							ListView1.setAdapter(new ListView1Adapter(chapter));
							((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
							searchimg.setVisibility(View.VISIBLE);
							getsearch = new Gson().toJson(chapter);
						}
					}catch(Exception e){
						 
					}
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
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
					try{
						listmap_cache = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						n = 0;
						for(int _repeat86 = 0; _repeat86 < (int)(listmap_cache.size()); _repeat86++) {
							if (listmap_cache.get((int)n).get("sura").toString().equals(getIntent().getStringExtra("sura"))) {
								ListMap = new HashMap<>();
								ListMap.put("verses", listmap_cache.get((int)n).get("verses").toString());
								ListMap.put("names", listmap_cache.get((int)n).get("names").toString());
								ListMap.put("words", listmap_cache.get((int)n).get("words").toString());
								ListMap.put("name", listmap_cache.get((int)n).get("name").toString());
								ListMap.put("khazainul", listmap_cache.get((int)n).get("khazainul").toString());
								ListMap.put("irfanul", listmap_cache.get((int)n).get("irfanul").toString());
								ListMap.put("ibnabbas", listmap_cache.get((int)n).get("ibnabbas").toString());
								ListMap.put("majhari", listmap_cache.get((int)n).get("majhari").toString());
								ListMap.put("nurulirfan", listmap_cache.get((int)n).get("nurulirfan").toString());
								ListMap.put("tabari", listmap_cache.get((int)n).get("tabari").toString());
								ListMap.put("ibnkasir", listmap_cache.get((int)n).get("ibnkasir").toString());
								ListMap.put("rejviya", listmap_cache.get((int)n).get("rejviya").toString());
								ListMap.put("baizabi", listmap_cache.get((int)n).get("baizabi").toString());
								ListMap.put("kurtubi", listmap_cache.get((int)n).get("kurtubi").toString());
								chapter.add(ListMap);
							}
							n++;
						}
						ListView1.setAdapter(new ListView1Adapter(chapter));
						((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
						getsearch = new Gson().toJson(chapter);
						searchimg.setVisibility(View.VISIBLE);
					}catch(Exception e){
						 
					}
				} else {
					if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
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
		if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
			try{
				listmap_cache = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				n = 0;
				for(int _repeat40 = 0; _repeat40 < (int)(listmap_cache.size()); _repeat40++) {
					if (listmap_cache.get((int)n).get("sura").toString().equals(getIntent().getStringExtra("sura"))) {
						ListMap = new HashMap<>();
						ListMap.put("verses", listmap_cache.get((int)n).get("verses").toString());
						ListMap.put("names", listmap_cache.get((int)n).get("names").toString());
						ListMap.put("words", listmap_cache.get((int)n).get("words").toString());
						ListMap.put("name", listmap_cache.get((int)n).get("name").toString());
						ListMap.put("khazainul", listmap_cache.get((int)n).get("khazainul").toString());
						ListMap.put("irfanul", listmap_cache.get((int)n).get("irfanul").toString());
						ListMap.put("ibnabbas", listmap_cache.get((int)n).get("ibnabbas").toString());
						ListMap.put("majhari", listmap_cache.get((int)n).get("majhari").toString());
						ListMap.put("nurulirfan", listmap_cache.get((int)n).get("nurulirfan").toString());
						ListMap.put("tabari", listmap_cache.get((int)n).get("tabari").toString());
						ListMap.put("ibnkasir", listmap_cache.get((int)n).get("ibnkasir").toString());
						ListMap.put("rejviya", listmap_cache.get((int)n).get("rejviya").toString());
						ListMap.put("baizabi", listmap_cache.get((int)n).get("baizabi").toString());
						ListMap.put("kurtubi", listmap_cache.get((int)n).get("kurtubi").toString());
						chapter.add(ListMap);
					}
					n++;
				}
				ListView1.setAdapter(new ListView1Adapter(chapter));
				((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
				getsearch = new Gson().toJson(chapter);
				spin.setVisibility(View.GONE);
				content.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
				searchimg.setVisibility(View.VISIBLE);
			}catch(Exception e){
				 
			}
		} else {
			if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
				FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat("/ইসলামী বিশ্বকোষ/.অনলাইন বই ২/")));
				if (Rizwan.isConnected(getApplicationContext())) {
					book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.tafsir, "", _book_request_listener);
				} else {
					ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
					
					if (activeNetwork == null || !activeNetwork.isConnected()) {
						   Nointernet.setVisibility(View.VISIBLE); Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
					}
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.অনলাইন বই ২/তাফসির সমগ্র"))) {
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
			try{
				if (chapter.get((int)_position).containsKey("verses")) {
					number.setText(_replaceArabicNumber(chapter.get((int)_position).get("verses").toString()));
					ayaarabic.setText(chapter.get((int)_position).get("names").toString());
					words.setText(chapter.get((int)_position).get("words").toString());
					textkanzuliman.setText(_replaceArabicNumber(chapter.get((int)_position).get("verses").toString().concat(". ".concat(chapter.get((int)_position).get("name").toString()))));
					texttafsirkhazainulirfan.setText(chapter.get((int)_position).get("khazainul").toString());
					textifranulkuran.setText(chapter.get((int)_position).get("irfanul").toString());
					texttafsiribnabbas.setText(chapter.get((int)_position).get("ibnabbas").toString());
					texttafsirmajhari.setText(chapter.get((int)_position).get("majhari").toString());
					texttafsirnurulirfan.setText(chapter.get((int)_position).get("nurulirfan").toString());
					texttafsirtabari.setText(chapter.get((int)_position).get("tabari").toString());
					texttafsiribnkasir.setText(chapter.get((int)_position).get("ibnkasir").toString());
					texttafsirkurtubi.setText(chapter.get((int)_position).get("kurtubi").toString());
					texttafsirrezviya.setText(chapter.get((int)_position).get("rejviya").toString());
					texttafsirbaizabi.setText(chapter.get((int)_position).get("baizabi").toString());
				} else {
					
				}
			}catch(Exception e){
				 
			}
			texttafsiribnabbas.setVisibility(View.GONE);
			texttafsirkhazainulirfan.setVisibility(View.GONE);
			texttafsirnurulirfan.setVisibility(View.GONE);
			texttafsirtabari.setVisibility(View.GONE);
			texttafsirmajhari.setVisibility(View.GONE);
			texttafsiribnkasir.setVisibility(View.GONE);
			texttafsirkurtubi.setVisibility(View.GONE);
			texttafsirbaizabi.setVisibility(View.GONE);
			texttafsirrezviya.setVisibility(View.GONE);
			headingtafsiribnabbas.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsiribnabbas.getVisibility() == View.GONE) {
						texttafsiribnabbas.setVisibility(View.VISIBLE);
					} else {
						texttafsiribnabbas.setVisibility(View.GONE);
					}
				}
			});
			headingkhazainulirfan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirkhazainulirfan.getVisibility() == View.GONE) {
						texttafsirkhazainulirfan.setVisibility(View.VISIBLE);
					} else {
						texttafsirkhazainulirfan.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirnurulirfan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirnurulirfan.getVisibility() == View.GONE) {
						texttafsirnurulirfan.setVisibility(View.VISIBLE);
					} else {
						texttafsirnurulirfan.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirtabari.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirtabari.getVisibility() == View.GONE) {
						texttafsirtabari.setVisibility(View.VISIBLE);
					} else {
						texttafsirtabari.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirmajhari.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirmajhari.getVisibility() == View.GONE) {
						texttafsirmajhari.setVisibility(View.VISIBLE);
					} else {
						texttafsirmajhari.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirkurtubi.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirkurtubi.getVisibility() == View.GONE) {
						texttafsirkurtubi.setVisibility(View.VISIBLE);
					} else {
						texttafsirkurtubi.setVisibility(View.GONE);
					}
				}
			});
			headingtafsiribnkasir.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsiribnkasir.getVisibility() == View.GONE) {
						texttafsiribnkasir.setVisibility(View.VISIBLE);
					} else {
						texttafsiribnkasir.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirbaizabi.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirbaizabi.getVisibility() == View.GONE) {
						texttafsirbaizabi.setVisibility(View.VISIBLE);
					} else {
						texttafsirbaizabi.setVisibility(View.GONE);
					}
				}
			});
			headingtafsirrezbiya.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (texttafsirrezviya.getVisibility() == View.GONE) {
						texttafsirrezviya.setVisibility(View.VISIBLE);
					} else {
						texttafsirrezviya.setVisibility(View.GONE);
					}
				}
			});
			headingkanzulimaan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (textkanzuliman.getVisibility() == View.GONE) {
						textkanzuliman.setVisibility(View.VISIBLE);
					} else {
						textkanzuliman.setVisibility(View.GONE);
					}
				}
			});
			headingirfanulkuran.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (textifranulkuran.getVisibility() == View.GONE) {
						textifranulkuran.setVisibility(View.VISIBLE);
					} else {
						textifranulkuran.setVisibility(View.GONE);
					}
				}
			});
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsiribnabbas.getText().toString())) {
				maintafsiribnabbas.setVisibility(View.GONE);
			} else {
				maintafsiribnabbas.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirkhazainulirfan.getText().toString())) {
				mainkhazainulirfan.setVisibility(View.GONE);
			} else {
				mainkhazainulirfan.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirnurulirfan.getText().toString())) {
				maintafsirnurulirfan.setVisibility(View.GONE);
			} else {
				maintafsirnurulirfan.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirtabari.getText().toString())) {
				maintafsirtabari.setVisibility(View.GONE);
			} else {
				maintafsirtabari.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirmajhari.getText().toString())) {
				maintafsirmajhari.setVisibility(View.GONE);
			} else {
				maintafsirmajhari.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsiribnkasir.getText().toString())) {
				maintafsiribnkasir.setVisibility(View.GONE);
			} else {
				maintafsiribnkasir.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirkurtubi.getText().toString())) {
				maintafsirkurtubi.setVisibility(View.GONE);
			} else {
				maintafsirkurtubi.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirbaizabi.getText().toString())) {
				maintafsirbaizabi.setVisibility(View.GONE);
			} else {
				maintafsirbaizabi.setVisibility(View.VISIBLE);
			}
			if ("তাফসির যুক্ত করা হয়নি".equals(texttafsirrezviya.getText().toString())) {
				maintafsirrezbiya.setVisibility(View.GONE);
			} else {
				maintafsirrezbiya.setVisibility(View.VISIBLE);
			}
			if ("শব্দার্থ যুক্ত করা হয়নি".equals(words.getText().toString())) {
				words.setVisibility(View.GONE);
			} else {
				words.setVisibility(View.VISIBLE);
			}
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
	}
}
