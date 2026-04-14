package com.srizwan.islamipedia;

import android.content.Intent;
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
import android.widget.AdapterView;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class DuaActivity extends AppCompatActivity {
	
	private double n = 0;
	private HashMap<String, Object> map = new HashMap<>();
	private String search = "";
	private boolean searchbar_enabled = false;
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	private String sub = "";
	private double x1 = 0;
	private double x2 = 0;
	private double y1 = 0;
	private double y2 = 0;
	
	private ArrayList<HashMap<String, Object>> listmap_cache = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> lisy = new ArrayList<>();
	
	private LinearLayout linear1;
	private LinearLayout linear4;
	private LinearLayout searchbar;
	private LinearLayout subject;
	private LinearLayout bootombar;
	private LinearLayout linear38;
	private ListView listview;
	private ListView listview1;
	private LinearLayout linear37;
	private ImageView imageview2;
	private LinearLayout linear5;
	private ImageView imageview1;
	private TextView textview1;
	private ImageView btn_back;
	private EditText edittext1;
	private ImageView btn_clear;
	private EditText edittext2;
	private ImageView imageview4;
	private TextView textview9;
	private LinearLayout bar1;
	private LinearLayout bar2;
	private TextView textview7;
	private TextView textview8;
	private LinearLayout all_line;
	private LinearLayout subline;
	private TextView textview4;
	private TextView textview5;
	private ProgressBar waveProgressbar;
	
	private Intent i = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.dua);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		linear4 = findViewById(R.id.linear4);
		searchbar = findViewById(R.id.searchbar);
		subject = findViewById(R.id.subject);
		bootombar = findViewById(R.id.bootombar);
		linear38 = findViewById(R.id.linear38);
		listview = findViewById(R.id.listview);
		listview1 = findViewById(R.id.listview1);
		linear37 = findViewById(R.id.linear37);
		imageview2 = findViewById(R.id.imageview2);
		linear5 = findViewById(R.id.linear5);
		imageview1 = findViewById(R.id.imageview1);
		textview1 = findViewById(R.id.textview1);
		btn_back = findViewById(R.id.btn_back);
		edittext1 = findViewById(R.id.edittext1);
		btn_clear = findViewById(R.id.btn_clear);
		edittext2 = findViewById(R.id.edittext2);
		imageview4 = findViewById(R.id.imageview4);
		textview9 = findViewById(R.id.textview9);
		bar1 = findViewById(R.id.bar1);
		bar2 = findViewById(R.id.bar2);
		textview7 = findViewById(R.id.textview7);
		textview8 = findViewById(R.id.textview8);
		all_line = findViewById(R.id.all_line);
		subline = findViewById(R.id.subline);
		textview4 = findViewById(R.id.textview4);
		textview5 = findViewById(R.id.textview5);
		waveProgressbar = findViewById(R.id.waveProgressbar);
		
		listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				subject.setVisibility(View.VISIBLE);
				bootombar.setVisibility(View.GONE);
				listview1.setVisibility(View.GONE);
				listview.setVisibility(View.VISIBLE);
				edittext2.setText(lisy.get((int)_position).get("title").toString());
				textview9.setText(lisy.get((int)_position).get("title").toString());
				imageview1.setVisibility(View.INVISIBLE);
				imageview1.setEnabled(false);
				if (searchbar.getVisibility() == View.VISIBLE) {
					searchbar.setVisibility(View.GONE);
				}
				linear38.setVisibility(View.GONE);
			}
		});
		
		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
		
		imageview1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_btn_search1();
			}
		});
		
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_byn_back();
			}
		});
		
		edittext1.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				if (listview.getVisibility() == View.VISIBLE) {
					_json_search(_charSeq);
				}
				else {
					if (listview1.getVisibility() == View.VISIBLE) {
						_list(_charSeq);
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		btn_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_byn_clear();
			}
		});
		
		edittext2.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				_json(_charSeq);
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		imageview4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (subject.getVisibility() == View.VISIBLE) {
					subject.setVisibility(View.GONE);
					bootombar.setVisibility(View.VISIBLE);
					listview1.setVisibility(View.VISIBLE);
					listview.setVisibility(View.GONE);
					edittext2.setText("");
					subline.setBackgroundColor(0xFF009688);
					all_line.setBackgroundColor(0xFFFFFFFF);
					linear38.setVisibility(View.VISIBLE);
					if (searchbar.getVisibility() == View.VISIBLE) {
						edittext1.setText("");
						_byn_clear();
					}
					if (imageview1.getVisibility() == View.INVISIBLE) {
						imageview1.setVisibility(View.VISIBLE);
						imageview1.setEnabled(true);
					}
				}
				else {
					if (listview1.getVisibility() == View.VISIBLE) {
						subject.setVisibility(View.GONE);
						bootombar.setVisibility(View.VISIBLE);
						listview1.setVisibility(View.GONE);
						listview.setVisibility(View.VISIBLE);
						subline.setBackgroundColor(0xFFFFFFFF);
						all_line.setBackgroundColor(0xFF009688);
						linear38.setVisibility(View.VISIBLE);
						if (searchbar.getVisibility() == View.VISIBLE) {
							edittext1.setText("");
							_byn_clear();
						}
					}
					else {
						if (searchbar.getVisibility() == View.VISIBLE) {
							_byn_clear();
							edittext1.setText("");
						}
						else {
							finish();
						}
					}
				}
				if (listview1.getVisibility() == View.VISIBLE && searchbar.getVisibility() == View.VISIBLE) {
					_byn_clear();
				}
			}
		});
		
		bar1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				listview.setVisibility(View.VISIBLE);
				listview1.setVisibility(View.GONE);
				if (searchbar.getVisibility() == View.VISIBLE) {
					edittext1.setText("");
				}
				if (imageview1.getVisibility() == View.INVISIBLE) {
					imageview1.setVisibility(View.VISIBLE);
					imageview1.setEnabled(true);
				}
				all_line.setBackgroundColor(0xFF01837A);
				subline.setBackgroundColor(0xFFFFFFFF);
			}
		});
		
		bar2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				listview.setVisibility(View.GONE);
				listview1.setVisibility(View.VISIBLE);
				if (searchbar.getVisibility() == View.VISIBLE) {
					edittext1.setText("");
				}
				if (imageview1.getVisibility() == View.INVISIBLE) {
					imageview1.setVisibility(View.VISIBLE);
					imageview1.setEnabled(true);
				}
				subline.setBackgroundColor(0xFF01837A);
				all_line.setBackgroundColor(0xFFFFFFFF);
			}
		});
	}
	
	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		subject.setVisibility(View.GONE);
		all_line.setBackgroundColor(0xFF009688);
		subline.setBackgroundColor(0xFFFFFFFF);
		listview.setVisibility(View.VISIBLE);
		listview1.setVisibility(View.GONE);
		searchbar.setVisibility(View.GONE);
		linear37.setVisibility(View.GONE);
		try{
			java.io.InputStream inputstream1 = getAssets().open("dua");
			listmap_cache = new Gson().fromJson(Rizwan.copyFromInputStream(inputstream1), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			n = 0;
			for(int _repeat52 = 0; _repeat52 < (int)(listmap_cache.size()); _repeat52++) {
				if (n == 0) {
					map = new HashMap<>();
					map.put("id", listmap_cache.get((int)n).get("id").toString());
					map.put("title", listmap_cache.get((int)n).get("title").toString());
					map.put("status", listmap_cache.get((int)n).get("status").toString());
					listmap.add(map);
				}
				else {
					if (listmap_cache.get((int)n).get("id").toString().equals(listmap_cache.get((int)n - 1).get("id").toString())) {
						
					}
					else {
						map = new HashMap<>();
						map.put("id", listmap_cache.get((int)n).get("id").toString());
						map.put("title", listmap_cache.get((int)n).get("title").toString());
						map.put("status", listmap_cache.get((int)n).get("status").toString());
						listmap.add(map);
					}
				}
				n++;
			}
			listview.setAdapter(new ListviewAdapter(listmap));
			((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
		}catch(Exception e){
			 
		}
		search = new Gson().toJson(listmap);
		try{
			java.io.InputStream inputstream2 = getAssets().open("catagory");
			lisy = new Gson().fromJson(Rizwan.copyFromInputStream(inputstream2), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			listview1.setAdapter(new Listview1Adapter(lisy));
			((BaseAdapter)listview1.getAdapter()).notifyDataSetChanged();
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "ডাটা খুজে পাওয়া যায়নি", Toast.LENGTH_SHORT).show();

		}
		listview.setSelector(android.R.color.transparent);
		listview.setFastScrollEnabled(true);
		sub = new Gson().toJson(lisy);
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFFFFFF);
			bootombar.setElevation(d*7);
			bootombar.setBackground(SketchUi);
		}
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFF01837A);
			linear4.setElevation(d*7);
			linear4.setBackground(SketchUi);
		}
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFFFFFFFF);SketchUi.setStroke(d*1,0xFF01837A);
			bar1.setElevation(d*8);
			android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF80CBC4}), SketchUi, null);
			bar1.setBackground(SketchUi_RD);
		}
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFFFFFFFF);SketchUi.setStroke(d*1,0xFF01837A);
			bar2.setElevation(d*8);
			android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF80CBC4}), SketchUi, null);
			bar2.setBackground(SketchUi_RD);
		}


		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (subject.getVisibility() == View.VISIBLE) {
					subject.setVisibility(View.GONE);
					bootombar.setVisibility(View.VISIBLE);
					listview1.setVisibility(View.VISIBLE);
					listview.setVisibility(View.GONE);
					edittext2.setText("");
					subline.setBackgroundColor(0xFF009688);
					all_line.setBackgroundColor(0xFFFFFFFF);
					linear38.setVisibility(View.VISIBLE);
					if (searchbar.getVisibility() == View.VISIBLE) {
						edittext1.setText("");
						_byn_clear();
					}
					if (imageview1.getVisibility() == View.INVISIBLE) {
						imageview1.setVisibility(View.VISIBLE);
						imageview1.setEnabled(true);
					}
				}
				else {
					if (listview1.getVisibility() == View.VISIBLE) {
						subject.setVisibility(View.GONE);
						bootombar.setVisibility(View.VISIBLE);
						listview1.setVisibility(View.GONE);
						listview.setVisibility(View.VISIBLE);
						subline.setBackgroundColor(0xFFFFFFFF);
						all_line.setBackgroundColor(0xFF009688);
						linear38.setVisibility(View.VISIBLE);
						if (searchbar.getVisibility() == View.VISIBLE) {
							edittext1.setText("");
							_byn_clear();
						}
					}
					else {
						if (searchbar.getVisibility() == View.VISIBLE) {
							_byn_clear();
							edittext1.setText("");
						}
						else {
							finish();
						}
					}
				}
				if (listview1.getVisibility() == View.VISIBLE && searchbar.getVisibility() == View.VISIBLE) {
					_byn_clear();
				}
			}
		});
	}

	public void _btn_search1() {
		if (searchbar.getVisibility() == View.GONE) {
			searchbar_enabled = true;
			searchbar.setVisibility(View.VISIBLE);
			linear4.setVisibility(View.VISIBLE);
			edittext1.requestFocus();
		}
		else {
			_byn_back();
		}
	}
	
	
	public void _byn_clear() {
		if (edittext1.getText().toString().equals("")) {
			searchbar_enabled = false;
			linear4.setVisibility(View.VISIBLE);
			searchbar.setVisibility(View.GONE);
		}
		else {
			edittext1.setText("");
		}
	}
	
	
	public void _byn_back() {
		searchbar_enabled = false;
		linear4.setVisibility(View.VISIBLE);
		searchbar.setVisibility(View.GONE);
		edittext1.setText("");
	}
	
	
	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");
		
		return result;
	}
	
	
	public void _json_search(final String _charSeq) {
		listmap = new Gson().fromJson(search, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = listmap.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = listmap.get((int)r).get("title").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			}
			else {
				listmap.remove((int)(r));
			}
			r--;
		}
		listview.setAdapter(new ListviewAdapter(listmap));
		((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
	}
	
	
	public void _statusbarcolor(final String _color) {
		try {
			if(Build.VERSION.SDK_INT >= 21) {
				Window w = this.getWindow();
				w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				w.setStatusBarColor(Color.parseColor(_color));
			}
		} catch (Exception e) {}
	}
	
	
	public void _json(final String _charSeq) {
		listmap = new Gson().fromJson(search, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = listmap.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = listmap.get((int)r).get("status").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			}
			else {
				listmap.remove((int)(r));
			}
			r--;
		}
		listview.setAdapter(new ListviewAdapter(listmap));
		((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
	}
	
	
	public void _list(final String _charSeq) {
		lisy = new Gson().fromJson(sub, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = lisy.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = lisy.get((int)r).get("title").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			}
			else {
				lisy.remove((int)(r));
			}
			r--;
		}
		listview1.setAdapter(new Listview1Adapter(lisy));
		((BaseAdapter)listview1.getAdapter()).notifyDataSetChanged();
	}
	
	
	public void _status_bar_color(final String _colour1, final String _colour2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) { 
			   Window w = this.getWindow(); w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			   w.setStatusBarColor(Color.parseColor(_colour1)); w.setNavigationBarColor(Color.parseColor(_colour2));
		}
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
				_view = _inflater.inflate(R.layout.dua123, null);
			}
			
			final LinearLayout linear = _view.findViewById(R.id.linear);
			final LinearLayout linear5 = _view.findViewById(R.id.linear5);
			final TextView tv_surah_name = _view.findViewById(R.id.tv_surah_name);
			final TextView textview8 = _view.findViewById(R.id.textview8);
			final TextView textview7 = _view.findViewById(R.id.textview7);
			
			if (_data.get((int)_position).containsKey("title")) {
				tv_surah_name.setText(_data.get((int)_position).get("title").toString().trim());
			}
			if ("".equals(_data.get((int)_position).get("title").toString())) {
				tv_surah_name.setText("দুয়া");
			}
			else {
				tv_surah_name.setText(_data.get((int)_position).get("title").toString().trim());
			}
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*14);
				SketchUi.setStroke(d*2,0xFF01837A);
				linear.setElevation(d*8);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF80CBC4}), SketchUi, null);
				linear.setBackground(SketchUi_RD);
			}
			textview7.setText(_replaceArabicNumber(String.valueOf((long)(_position))));
			if (_data.get((int)_position).containsKey("status")) {
				textview8.setText(_data.get((int)_position).get("status").toString());
			}
			textview8.setVisibility(View.GONE);
			if (_data.get((int)_position).get("title").toString().trim().equals(_data.get((int)_position).get("title").toString().trim())) {
				tv_surah_name.setTextColor(0xFF009688);
			}
			linear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					i.setClass(getApplicationContext(), Dua1Activity.class);
					i.putExtra("id", listmap.get((int)_position).get("id").toString());
					i.putExtra("title", listmap.get((int)_position).get("title").toString());
					startActivity(i);
				}
			});
			
			return _view;
		}
	}
	
	public class Listview1Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Listview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
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
				_view = _inflater.inflate(R.layout.dua123, null);
			}
			
			final LinearLayout linear = _view.findViewById(R.id.linear);
			final LinearLayout linear5 = _view.findViewById(R.id.linear5);
			final TextView tv_surah_name = _view.findViewById(R.id.tv_surah_name);
			final TextView textview8 = _view.findViewById(R.id.textview8);
			final TextView textview7 = _view.findViewById(R.id.textview7);
			
			if (_data.get((int)_position).containsKey("title")) {
				tv_surah_name.setText(_data.get((int)_position).get("title").toString().trim());
			}
			if ("".equals(_data.get((int)_position).get("title").toString())) {
				tv_surah_name.setText("দুয়া");
			}
			else {
				tv_surah_name.setText(_data.get((int)_position).get("title").toString().trim());
			}
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*14);
				SketchUi.setStroke(d*2,0xFF01837A);
				linear.setElevation(d*8);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF80CBC4}), SketchUi, null);
				linear.setBackground(SketchUi_RD);
			}
			textview7.setText(_replaceArabicNumber(String.valueOf((long)(_position))));
			textview8.setVisibility(View.GONE);
			
			return _view;
		}
	}
}
