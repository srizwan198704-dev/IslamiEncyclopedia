package com.srizwan.islamipedia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BlogonlineActivity extends AppCompatActivity {

	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private FloatingActionButton _fab;
	private String alldata = "";
	private String error = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	private HashMap<String, Object> map = new HashMap<>();
	private String blogid = "";
	private String api = "";
	private HashMap<String, Object> mapv2 = new HashMap<>();
	private String url = "";
	private String pagetoken = "";
	private HashMap<String, Object> mp = new HashMap<>();

	private ArrayList<HashMap<String, Object>> shut = new ArrayList<>();
	private ArrayList<String> allraw = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> readypost = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> mappp = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> mapl = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list0;
	private LinearLayout box;
	private LinearLayout LinearLayout1;
	private ImageView searchimg;
	private TextView heading;
	private TextView version;
	private ProgressBar spinber;
	private LinearLayout Nointernet;
	private ImageView imageview3;
	private TextView textview1;
	private MaterialButton materialbutton1;
	private LinearLayout searxhmain;
	private RecyclerView recyclerview1;
	private ProgressBar progressbar1;
	private LinearLayout nores;
	private TextInputLayout boxofsearch;
	private ImageView imageview2;
	private EditText searchbox;
	private ImageView noresult;
	private TextView no_result;

	private AlertDialog.Builder d;
	private Intent in = new Intent();
	private RequestNetwork rn;
	private RequestNetwork.RequestListener _rn_request_listener;
	private SharedPreferences get;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.blogonline);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		_fab = findViewById(R.id._fab);
		toolbar = findViewById(R.id.toolbar);
		spin = findViewById(R.id.spin);
		content = findViewById(R.id.content);
		list0 = findViewById(R.id.list0);
		box = findViewById(R.id.box);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		searchimg = findViewById(R.id.searchimg);
		heading = findViewById(R.id.heading);
		version = findViewById(R.id.version);
		spinber = findViewById(R.id.spinber);
		Nointernet = findViewById(R.id.Nointernet);
		imageview3 = findViewById(R.id.imageview3);
		textview1 = findViewById(R.id.textview1);
		materialbutton1 = findViewById(R.id.materialbutton1);
		searxhmain = findViewById(R.id.searxhmain);
		recyclerview1 = findViewById(R.id.recyclerview1);
		progressbar1 = findViewById(R.id.progressbar1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);
		d = new AlertDialog.Builder(this);
		rn = new RequestNetwork(this);
		get = getSharedPreferences("me", Activity.MODE_PRIVATE);

		list0.setOnClickListener(new View.OnClickListener() {
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
				map = new HashMap<>();
				map.put("maxResults", "10");
				rn.setParams(map, RequestNetworkController.REQUEST_PARAM);
				rn.startRequestNetwork(RequestNetworkController.GET, "https://www.googleapis.com/blogger/v3/blogs/".concat(blogid.concat("/posts?key=".concat(api))), "a", _rn_request_listener);
				recyclerview1.setAdapter(new Recyclerview1Adapter(mapl));
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
				if (mapl.size() == 0) {
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

		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_loadmore();
				progressbar1.setVisibility(View.VISIBLE);
			}
		});

		_rn_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				try {
					org.json.JSONObject object = new org.json.JSONObject(_response);

					pagetoken = object.has("nextPageToken") ? object.getString("nextPageToken") : "";

					JSONArray array = object.has("items") ? object.getJSONArray("items") : new JSONArray();

					for (int i = 0; i < array.length(); i++) {
						org.json.JSONObject ob = array.getJSONObject(i);
						String id = ob.getString("id");
						String title = ob.getString("title");
						String url = ob.getString("url");
						String content = ob.getString("content");
						String published = ob.getString("published");
						String author = ob.getJSONObject("author").getString("displayName");

						SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");

						String formattedDate = "";
						try {
							Date date = format1.parse(published);
							formattedDate = format2.format(date);
						} catch (java.text.ParseException e) {
							e.printStackTrace();
						}

						Document document = org.jsoup.Jsoup.parse(content);
						Elements elements = document.select("img");

						String image = "";
						if (!elements.isEmpty()) {
							image = elements.get(0).attr("src");
						}

						HashMap<String, Object> mp = new HashMap<>();
						mp.put("title", title);
						mp.put("content", content);
						mp.put("date", formattedDate);
						mp.put("url", url);
						mp.put("author", author);

						if (mp != null) {
							mapl.add(mp);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException("JSON Parsing Error");
				}

				spin.setVisibility(View.GONE);
				progressbar1.setVisibility(View.GONE);
				recyclerview1.smoothScrollBy(0, 500);
				if (recyclerview1.getAdapter() != null) {
					recyclerview1.getAdapter().notifyDataSetChanged();
				}
				searchimg.setVisibility(View.VISIBLE);
				_fab.setVisibility(View.VISIBLE);
                alldata = new Gson().toJson(mapl);
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				spin.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.VISIBLE);
			}
		};
	}

	private void initializeLogic() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		_status_bar_color("#FF01837A", "#FF01837A");
		_fab.setVisibility(View.GONE);
		searchimg.setVisibility(View.GONE);
		blogid = BuildConfig.blogid;
		api = BuildConfig.api2;
		map = new HashMap<>();
		map.put("maxResults", "10");
		rn.setParams(map, RequestNetworkController.REQUEST_PARAM);
		rn.startRequestNetwork(RequestNetworkController.GET, "https://www.googleapis.com/blogger/v3/blogs/".concat(blogid.concat("/posts?key=".concat(api))), "a", _rn_request_listener);
		recyclerview1.setAdapter(new Recyclerview1Adapter(mapl));
		recyclerview1.setLayoutManager(new LinearLayoutManager(this));

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

	public void _json_search(final String _charSeq) {
		mapl = new Gson().fromJson(alldata, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = mapl.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = mapl.get((int)r).get("title").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {

			} else {
				mapl.remove((int)(r));
			}
			r--;
		}
		recyclerview1.setAdapter(new Recyclerview1Adapter(mapl));
		recyclerview1.setLayoutManager(new LinearLayoutManager(this));
	}


	public void _status_bar_color(final String _colour1, final String _colour2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Window w = this.getWindow(); w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			w.setStatusBarColor(Color.parseColor(_colour1)); w.setNavigationBarColor(Color.parseColor(_colour2));
		}
	}


	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০").replace("January", "জানুয়ারি").replace("February", "ফেব্রুয়ারী").replace("March", "মার্চ").replace("April", "এপ্রিল").replace("May", "মে").replace("June", "জুন").replace("July", "জুলাই").replace("August", "আগষ্ট").replace("September", "সেপ্টেম্বর").replace("October", "অক্টোবর").replace("November", "নভেম্বর").replace("December", "ডিসেম্বর").replace("-","");

		return result;
	}


	public void _loadmore() {
		mapv2 = new HashMap<>();
		mapv2.put("maxResults", "10");
		url = "https://www.googleapis.com/blogger/v3/blogs/".concat(blogid.concat("/posts?pageToken=".concat(pagetoken.concat("&key=".concat(api)))));
		rn.setParams(mapv2, RequestNetworkController.REQUEST_PARAM);
		rn.startRequestNetwork(RequestNetworkController.GET, url, "b", _rn_request_listener);
		mapl.remove(mp);
		progressbar1.setVisibility(View.GONE);
	}

	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {

		ArrayList<HashMap<String, Object>> _data;

		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.book1, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}

		@Override
		public void onBindViewHolder(ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
			View _view = _holder.itemView;

			final LinearLayout main = _view.findViewById(R.id.main);
			final LinearLayout contentbox = _view.findViewById(R.id.contentbox);
			final LinearLayout datebase = _view.findViewById(R.id.datebase);
			final LinearLayout linear5 = _view.findViewById(R.id.linear5);
			final TextView bookname = _view.findViewById(R.id.bookname);
			final TextView textview7 = _view.findViewById(R.id.textview7);
			final ImageView imageview2 = _view.findViewById(R.id.imageview2);
			final TextView date = _view.findViewById(R.id.date);
			final TextView textview4 = _view.findViewById(R.id.textview4);
			final TextView authorname = _view.findViewById(R.id.authorname);

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*13);
				SketchUi.setStroke(d*1,0xFF01837A);
				main.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				main.setBackground(SketchUi_RD);
			}
			try{
				textview7.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
				bookname.setText(mapl.get((int)_position).get("title").toString());
				date.setText(mapl.get((int)_position).get("date").toString());
				authorname.setText(mapl.get((int)_position).get("author").toString());
			}catch(Exception e){

			}
			main.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					try {
						in.setClass(getApplicationContext(), ViewActivity1.class);
						SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
						editor.putString("content", mapl.get((int)_position).get("content").toString());
						editor.commit();
						in.putExtra("name", mapl.get((int)_position).get("title").toString());
						startActivity(in);



					} catch(Exception e)

					{
						error = e.toString();

						Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();


					};
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
