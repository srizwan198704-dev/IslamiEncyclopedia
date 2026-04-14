package com.srizwan.islamipedia;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class Dua1Activity extends AppCompatActivity {
	
	private double n = 0;
	private HashMap<String, Object> map = new HashMap<>();
	
	private ArrayList<HashMap<String, Object>> listmap_cache = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
	
	private LinearLayout linear1;
	private LinearLayout linear4;
	private ListView listview;
	private ImageView imageview2;
	private LinearLayout linear5;
	private TextView title;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.dua1);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		linear4 = findViewById(R.id.linear4);
		listview = findViewById(R.id.listview);
		imageview2 = findViewById(R.id.imageview2);
		linear5 = findViewById(R.id.linear5);
		title = findViewById(R.id.title);
		
		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
		
		linear5.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				
				return true;
			}
		});
	}
	
	private void initializeLogic() {
		_marquue(title, getIntent().getStringExtra("title"));
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFF01837A);
			linear4.setElevation(d*7);
			linear4.setBackground(SketchUi);
		}
		_status_bar_color("#FF01837A", "#FF01837A");
		try{
			java.io.InputStream inputstream1 = getAssets().open("dua");
			listmap_cache = new Gson().fromJson(Rizwan.copyFromInputStream(inputstream1), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			n = 0;
			for(int _repeat23 = 0; _repeat23 < (int)(listmap_cache.size()); _repeat23++) {
				if (listmap_cache.get((int)n).get("id").toString().equals(getIntent().getStringExtra("id"))) {
					map = new HashMap<>();
					map.put("dua", listmap_cache.get((int)n).get("dua").toString());
					map.put("transliteration", listmap_cache.get((int)n).get("transliteration").toString());
					map.put("meaning", listmap_cache.get((int)n).get("meaning").toString());
					map.put("source", listmap_cache.get((int)n).get("source").toString());
					map.put("benefit", listmap_cache.get((int)n).get("benefit").toString());
					listmap.add(map);
				}
				n++;
			}
			listview.setAdapter(new ListviewAdapter(listmap));
			((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
		}catch(Exception e){
			 
		}
		listview.setSelector(android.R.color.transparent);
		listview.setFastScrollEnabled(true);
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
	
	
	public void _enable_copy_textview(final TextView _tv) {
		_tv.setTextIsSelectable(true);
	}
	
	
	public void _status_bar_color(final String _colour1, final String _colour2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) { 
			   Window w = this.getWindow(); w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			   w.setStatusBarColor(Color.parseColor(_colour1)); w.setNavigationBarColor(Color.parseColor(_colour2));
		}
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
				_view = _inflater.inflate(R.layout.dua1234, null);
			}
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final TextView textview1 = _view.findViewById(R.id.textview1);
			final TextView textview2 = _view.findViewById(R.id.textview2);
			final TextView textview3 = _view.findViewById(R.id.textview3);
			final TextView textview5 = _view.findViewById(R.id.textview5);
			final TextView textview4 = _view.findViewById(R.id.textview4);
			final TextView textview6 = _view.findViewById(R.id.textview6);
			
			if (_data.get((int)_position).containsKey("dua")) {
				textview1.setText(_data.get((int)_position).get("dua").toString());
			}
			if (_data.get((int)_position).containsKey("transliteration")) {
				textview2.setText(_data.get((int)_position).get("transliteration").toString());
			}
			if (_data.get((int)_position).containsKey("meaning")) {
				textview3.setText(_data.get((int)_position).get("meaning").toString());
			}
			if (_data.get((int)_position).containsKey("source")) {
				textview5.setText(_data.get((int)_position).get("source").toString());
			}
			if (_data.get((int)_position).containsKey("benefit")) {
				textview6.setText(_data.get((int)_position).get("benefit").toString());
			}
			if ("not".equals(_data.get((int)_position).get("dua").toString())) {
				textview1.setVisibility(View.GONE);
			}
			else {
				
			}
			if ("not".equals(_data.get((int)_position).get("transliteration").toString())) {
				textview2.setVisibility(View.GONE);
			}
			else {
				
			}
			if ("not".equals(_data.get((int)_position).get("meaning").toString())) {
				textview3.setVisibility(View.GONE);
			}
			else {
				
			}
			if ("not".equals(_data.get((int)_position).get("source").toString())) {
				textview5.setVisibility(View.GONE);
			}
			else {
				
			}
			if ("not".equals(_data.get((int)_position).get("benefit").toString())) {
				textview4.setVisibility(View.GONE);
				textview6.setVisibility(View.GONE);
			}
			else {
				
			}
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*14);
				SketchUi.setStroke(d*2,0xFF01837A);
				linear1.setElevation(d*8);
				linear1.setBackground(SketchUi);
			}
			linear1.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View _view) {
					((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", textview1.getText().toString().concat("\n	".concat(textview2.getText().toString().concat("\n	".concat(textview3.getText().toString().concat("\n	".concat(textview5.getText().toString().concat("\n	".concat(textview4.getText().toString().concat("🫢	".concat(textview6.getText().toString()))))))))))));
					Toast.makeText(
							getApplicationContext(),
							title.getText().toString().concat(" কপি করা হয়েছে"),
							Toast.LENGTH_SHORT
					).show();

					return true;
				}
			});
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", textview1.getText().toString().concat("\n	".concat(textview2.getText().toString().concat("\n	".concat(textview3.getText().toString().concat("\n	".concat(textview5.getText().toString().concat("\n	".concat(textview4.getText().toString().concat("🫢	".concat(textview6.getText().toString()))))))))))));
					Toast.makeText(
							getApplicationContext(),
							title.getText().toString() + " কপি করা হয়েছে",
							Toast.LENGTH_SHORT
					).show();

				}
			});
			
			return _view;
		}
	}
}
