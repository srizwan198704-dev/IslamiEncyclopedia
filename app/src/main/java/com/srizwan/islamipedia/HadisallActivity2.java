package com.srizwan.islamipedia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;


public class HadisallActivity2 extends AppCompatActivity {
	
	private HashMap<String, Object> list_menu = new HashMap<>();
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	
	private ArrayList<HashMap<String, Object>> list = new ArrayList<>();
	
	private LinearLayout toolbar;
	private LinearLayout content;
	private ImageView list0;
	private LinearLayout box;
	private LinearLayout LinearLayout1;
	private ImageView searchimg;
	private TextView heading;
	private TextView author;
	private LinearLayout searxhmain;
	private RecyclerView recyclerview1;
	private LinearLayout nores;
	private TextInputLayout boxofsearch;
	private ImageView imageview2;
	private EditText searchbox;
	private ImageView noresult;
	private TextView no_result;
	
	private Intent in = new Intent();
	private SharedPreferences book;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.hadisall2);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		toolbar = findViewById(R.id.toolbar);
		content = findViewById(R.id.content);
		list0 = findViewById(R.id.list0);
		box = findViewById(R.id.box);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		searchimg = findViewById(R.id.searchimg);
		heading = findViewById(R.id.heading);
		author = findViewById(R.id.author);
		searxhmain = findViewById(R.id.searxhmain);
		recyclerview1 = findViewById(R.id.recyclerview1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);
		book = getSharedPreferences("hadis", Activity.MODE_PRIVATE);
		
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
				if (list.size() == 0) {
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
	}
	
	private void initializeLogic() {
		try{
			java.io.InputStream input = getAssets().open("onlinehadis");
			list = new Gson().fromJson(Rizwan.copyFromInputStream(input), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			recyclerview1.setAdapter(new Recyclerview1Adapter(list));
			recyclerview1.setLayoutManager(new LinearLayoutManager(this));
		}catch(Exception e){
			 
		}
		search = new Gson().toJson(list);
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
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০").replace("-", "");
		
		return result;
	}
	
	
	public void _json_search(final String _charSeq) {
		list = new Gson().fromJson(search, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = list.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = list.get((int)r).get("title_en").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			} else {
				list.remove((int)(r));
			}
			r--;
		}
		recyclerview1.setAdapter(new Recyclerview1Adapter(list));
		recyclerview1.setLayoutManager(new LinearLayoutManager(this));
	}
	
	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.onlinebokk2, null);
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
			final TextView suraName = _view.findViewById(R.id.suraName);
			final TextView verses = _view.findViewById(R.id.verses);
			final TextView suraArabic = _view.findViewById(R.id.suraArabic);
			
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
			number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
			if (list.get((int)_position).containsKey("title_ar")) {
				suraName.setText(list.get((int)_position).get("title_ar").toString());
				suraName.setVisibility(View.VISIBLE);
			} else {
				suraName.setVisibility(View.GONE);
			}
			if (list.get((int)_position).containsKey("title_en")) {
				verses.setText(list.get((int)_position).get("title_en").toString());
				verses.setVisibility(View.VISIBLE);
			} else {
				verses.setVisibility(View.GONE);
			}
			if (list.get((int)_position).containsKey("total_hadith")) {
				suraArabic.setText(_replaceArabicNumber(list.get((int)_position).get("total_hadith").toString()).concat(" টি হাদিস"));
				suraArabic.setVisibility(View.VISIBLE);
			} else {
				suraArabic.setVisibility(View.GONE);
			}
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (list.get((int)_position).get("title").toString().equals("রিয়াদুস সলেহিন") || (list.get((int)_position).get("title").toString().equals("বুলুগুল মারাম") || list.get((int)_position).get("title").toString().equals("আল লু'লু ওয়াল মারজান"))) {
						in.setClass(getApplicationContext(), ChapterallActivity.class);
						in.putExtra("name", list.get((int)_position).get("title").toString());
						in.putExtra("author", _replaceArabicNumber(list.get((int)_position).get("total_section").toString()).concat(" টি অধ্যায়, ".concat(_replaceArabicNumber(list.get((int)_position).get("total_hadith").toString()).concat(" টি হাদিস"))));
						in.putExtra("id", list.get((int)_position).get("id").toString());
						startActivity(in);
					} else {
						if (list.get((int)_position).get("title").toString().equals("শরহে মুসনাদে ইমাম আ‘যম আবু হানিফা")) {
							in.setClass(getApplicationContext(), ReadingActivity.class);
							in.putExtra("name", "শরহে মুসনাদে ইমাম আ‘যম আবু হানিফা");
							in.putExtra("author", "ব্যাখ্যাকার: হাফেজ মাওলানা মুহাম্মদ ওসমান গণি");
							in.putExtra("bookname", "026");
							startActivity(in);
						} else {
							if (list.get((int)_position).get("title").toString().equals("আল-মুস্তাদরাক আলা আস-সহীহাইন")) {
								in.setClass(getApplicationContext(), ReadingActivity.class);
								in.putExtra("name", "আল-মুস্তাদরাক আলা আস-সহীহাইন");
								in.putExtra("author", "ইমাম হাকীম (رحمة الله)");
								in.putExtra("bookname", "0137");
								startActivity(in);
							} else {
								if (list.get((int)_position).get("title").toString().equals("কানযুল উম্মাল")) {
									in.setClass(getApplicationContext(), ReadingActivity.class);
									in.putExtra("name", "কানযুল উম্মাল");
									in.putExtra("author", "ইমাম হিন্দী (رحمة الله)");
									in.putExtra("bookname", "0108");
									startActivity(in);
								} else {
									if (list.get((int)_position).get("title").toString().equals("মিরআত শরহে মিশকাত (অসম্পূর্ণ)")) {
										in.setClass(getApplicationContext(), ReadingActivity.class);
										in.putExtra("name", "মিরআত শরহে মিশকাত (অসম্পূর্ণ)");
										in.putExtra("author", "মূল: হাকিমুল উম্মাহ মুফতি ইয়ার খান নঈমী (رحمة الله)");
										in.putExtra("bookname", "043");
										startActivity(in);
									} else {
										book.edit().putString("book", list.get((int)_position).get("title").toString()).commit();
										in.setClass(getApplicationContext(), ChapterallActivity2.class);
										in.putExtra("name", list.get((int)_position).get("title").toString());
										in.putExtra("author", _replaceArabicNumber(list.get((int)_position).get("total_section").toString()).concat(" টি অধ্যায়, ".concat(_replaceArabicNumber(list.get((int)_position).get("total_hadith").toString()).concat(" টি হাদিস"))));
										in.putExtra("id", list.get((int)_position).get("id").toString());
										startActivity(in);
									}
								}
							}
						}
					}
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
