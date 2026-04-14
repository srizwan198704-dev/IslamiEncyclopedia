package com.srizwan.islamipedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class AllinonemeActivity2 extends AppCompatActivity {

	private ImageView refresh;
	private ProgressBar refreshprogress;
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	private double n = 0;
	private HashMap<String, Object> ListMap = new HashMap<>();
	
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
	private RecyclerView recyclerview1;
	private LinearLayout nores;
	private TextInputLayout boxofsearch;
	private ImageView imageview2;
	private EditText searchbox;
	private ImageView noresult;
	private TextView no_result;
	
	private Intent in = new Intent();
	private RequestNetwork hadis;
	private RequestNetwork.RequestListener _hadis_request_listener;
	private SharedPreferences title;
	private TimerTask timer;
	private Timer _timer = new Timer();
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.allinoneme2);
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
		recyclerview1 = findViewById(R.id.recyclerview1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);
		hadis = new RequestNetwork(this);
		title = getSharedPreferences("hadis", Activity.MODE_PRIVATE);
		refreshprogress = findViewById(R.id.refreshprogress);
		refresh = findViewById(R.id.refresh);

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
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
						refresh.setVisibility(View.GONE);
						refreshprogress.setVisibility(View.VISIBLE);
						FileUtil.deleteFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))));
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
				hadis.startRequestNetwork(RequestNetworkController.GET, "https://api.topofstacksoftware.com/hadith/api/v2/book/".concat(getIntent().getStringExtra("book_id").concat("/chapter/".concat(getIntent().getStringExtra("id").concat("/titles")))), "", _hadis_request_listener);
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
					recyclerview1.setVisibility(View.GONE);
				} else {
					nores.setVisibility(View.GONE);
					recyclerview1.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		_hadis_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
					try {
						        // JSON Object থেকে "data" এর "rows" আলাদা করা
						        JSONObject jsonObject = new JSONObject(_response);
						        JSONObject dataObject = jsonObject.getJSONObject("data");
						        JSONArray rowsArray = dataObject.getJSONArray("rows");
						
						        // "rows" অ্যারে থেকে List<HashMap<String, Object>> তৈরি করা
						        chapter = new Gson().fromJson(rowsArray.toString(), new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
						
						        // RecyclerView-তে ডেটা সেট করা
						        recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
						        recyclerview1.setLayoutManager(new LinearLayoutManager(AllinonemeActivity2.this));
						
						    } catch (JSONException e) {
						        e.printStackTrace();
						        Toast.makeText(getApplicationContext(), "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
						    }
					search = new Gson().toJson(chapter);
					FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))), search);
					spin.setVisibility(View.GONE);
					searchimg.setVisibility(View.VISIBLE);
					refreshprogress.setVisibility(View.GONE);
					refresh.setVisibility(View.VISIBLE);
				} else {
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
						chapter = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", ""))))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
						search = new Gson().toJson(chapter);
						spin.setVisibility(View.GONE);
						searchimg.setVisibility(View.VISIBLE);
					}
				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
					Nointernet.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.GONE);
				} else {
					if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
						chapter = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", ""))))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
						recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
						spin.setVisibility(View.GONE);
						searchimg.setVisibility(View.VISIBLE);
						search = new Gson().toJson(chapter);
					}
				}
			}
		};
	}
	
	private void initializeLogic() {
		_marquue(bookname, title.getString("name", ""));
		author.setText(title.getString("author", ""));
		if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/"))) {
			FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/"));
		}
		if (!FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
			hadis.startRequestNetwork(RequestNetworkController.GET, "https://api.topofstacksoftware.com/hadith/api/v2/book/".concat(getIntent().getStringExtra("book_id").concat("/chapter/".concat(getIntent().getStringExtra("id").concat("/titles")))), "", _hadis_request_listener);
		} else {
			if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", "")))))) {
				chapter = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ/.হাদিস/".concat(getIntent().getStringExtra("id").concat(title.getString("name", ""))))), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
				recyclerview1.setLayoutManager(new LinearLayoutManager(this));
				spin.setVisibility(View.GONE);
				searchimg.setVisibility(View.VISIBLE);
				search = new Gson().toJson(chapter);
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
	
	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");
		
		return result;
	}
	
	
	public void _json_search(final String _charSeq) {
		chapter = new Gson().fromJson(search, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = chapter.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = chapter.get((int)r).get("title").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			} else {
				chapter.remove((int)(r));
			}
			r--;
		}
		recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
		recyclerview1.setLayoutManager(new LinearLayoutManager(this));
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
	
	
	public String _arabic(final String _n) {
		String result = _n.replace("(রহঃ)", "(رحمة الله)").replace("(রাঃ)", "(رضي الله عنه)").replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)").replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace("(‘আঃ)", "(عليه السلام)").replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)").replace("(রাঃ)", "(رضي الله عنه)").replace("(রা.)", "(رضي الله عنه)").replace("রা.", "(رضي الله عنه)").replace("( রা )", "(رضي الله عنه)").replace("(রাযি.)", "(رضي الله عنه)").replace("(রাযিঃ)", "(رضي الله عنه)").replace("[১]", "").replace("[২]", "").replace("[৩]", "").replace("(রহ)", "(رحمة الله)").replace("(রহ.)", "(رحمة الله)").replace("(রাহঃ)", "(رحمة الله)").replace("(রা)", "(رضي الله عنه)").replace("(সা)", "(ﷺ)").replace("( সা )", "(ﷺ)").replace("রাসূলুল্লাহ্ -", "রাসূলুল্লাহ্ - (ﷺ)").replace("ঃ", ":").replace("(সা.)", "(ﷺ)").replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace("(‘আ)", "(عليه السلام)").replace("(সাঃ)", "(ﷺ)").replace("(রাঃ)", "(رضي الله عنه)").replace("সূলুল্লাহ্ -এর বয়সের বিবরণ", "রাসূলুল্লাহ্ (ﷺ)-এর বয়সের বিবরণ").replace("রাসুলুল্লাহ্ -কে স্বপ্নে দেখার বিবরণ", "রাসুলুল্লাহ্ (ﷺ) -কে স্বপ্নে দেখার বিবরণ").replace("রাসূলুল্লাহ -এর জীবিকার বিবরণ", "রাসূলুল্লাহ (ﷺ)-এর জীবিকার বিবরণ").replace("রাসূলুল্লাহ -এর লজ্জাবোধ", "রাসূলুল্লাহ (ﷺ)-এর লজ্জাবোধ").replace("রাসূলুল্লাহ -এর কিরাতের বিবরণ", "রাসূলুল্লাহ (ﷺ)-এর কিরাতের বিবরণ").replace("রাসূলুল্লাহ -এর ইবাদতের বর্ণনা", "রাসূলুল্লাহ (ﷺ)-এর ইবাদতের বর্ণনা").replace("রাসূলুল্লাহ্ এর রাত্রে গল্প বলা", "রাসূলুল্লাহ্ (ﷺ) এর রাত্রে গল্প বলা").replace("রাসূলুল্লাহ্-এর ফলের বিবরণ", "রাসূলুল্লাহ্ (ﷺ)-এর ফলের বিবরণ").replace("আহারের পূর্বে ও পরে রাসূলুল্লাহ এর দুআ", "আহারের পূর্বে ও পরে রাসূলুল্লাহ (ﷺ) এর দুআ").replace("রাসূলূল্লাহ এর পোশাক-পরিচ্ছদ", "রাসূলূল্লাহ (ﷺ) এর পোশাক-পরিচ্ছদ").replace("রাসূলূল্লাহ এর মাথার চুল পরিপাটি করা", "রাসূলূল্লাহ (ﷺ) এর মাথার চুল পরিপাটি করা");
		return result;
	}
	
	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.bookonlinefull2, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
			View _view = _holder.itemView;
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout bookpic = _view.findViewById(R.id.bookpic);
			final LinearLayout boxofcontent = _view.findViewById(R.id.boxofcontent);
			final TextView number = _view.findViewById(R.id.number);
			final TextView name = _view.findViewById(R.id.name);
			
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*24);
				SketchUi.setStroke(d*1,0xFF01837A);
				linear1.setElevation(d*7);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
			}
			name.setText(_arabic(chapter.get((int)_position).get("title").toString()));
			number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					in.setClass(getApplicationContext(), HadisviewmainActivity.class);
					in.putExtra("name", chapter.get((int)_position).get("title").toString());
					in.putExtra("book_id", chapter.get((int)_position).get("book_id").toString());
					in.putExtra("chapter_id", chapter.get((int)_position).get("chapter_id").toString());
					in.putExtra("id", chapter.get((int)_position).get("id").toString());
					startActivity(in);
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
}
