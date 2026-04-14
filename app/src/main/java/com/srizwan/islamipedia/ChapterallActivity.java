package com.srizwan.islamipedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class ChapterallActivity extends AppCompatActivity {
	
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	
	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> chapter = new ArrayList<>();
	
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
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.chapterall);
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
	}
	
	private void initializeLogic() {
		heading.setText(getIntent().getStringExtra("name"));
		author.setText(getIntent().getStringExtra("author"));
		try{
			java.io.InputStream imput = getAssets().open("onlinehadis");
			map = new Gson().fromJson(Rizwan.copyFromInputStream(imput), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			chapter = new Gson().fromJson(new Gson().toJson((ArrayList<HashMap<String,Object>>)map.get((int)(Double.parseDouble(getIntent().getStringExtra("id")))).get("chapter")), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
			recyclerview1.setLayoutManager(new LinearLayoutManager(this));
			search = new Gson().toJson(chapter);
		}catch(Exception e){
			 
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
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০").replace("-", "");
		
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
	
	
	public String _Arabic(final String _n) {
		String result = _n.replace("<b>", " ").replace("</b>"," ").replace("(রহঃ)", "(رحمة الله)").replace("(রাঃ)", "(رضي الله عنه)").replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)").replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace("(‘আঃ)", "(عليه السلام)").replace("(সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)", "(ﷺ)").replace("(রাঃ)", "(رضي الله عنه)").replace("[১]", "").replace("[২]", "").replace("[৩]", "").replace("(রহ)", "(رحمة الله)").replace("(রা)", "(رضي الله عنه)").replace("(সা)", "(ﷺ)").replace(" (সাল্লাল্লাহু ‘আলাইহি ওয়া সাল্লাম)","(ﷺ)").replace("(‘আ)", "(عليه السلام)").replace("(সাঃ)", "(ﷺ)").replace("(রাঃ)", "(رضي الله عنه)").replace("(স)", "(ﷺ)").replace("বিবিন্‌ত", "বিন্‌ত").replace("বিন্ত", "বিন্‌ত").replace("(সা.)", "(ﷺ)").replace("(স.)", "(ﷺ)");
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
			View _v = _inflater.inflate(R.layout.onlinebookk, null);
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
			final TextView suraArabic = _view.findViewById(R.id.suraArabic);
			final TextView number = _view.findViewById(R.id.number);
			final TextView suraName = _view.findViewById(R.id.suraName);
			final TextView verses = _view.findViewById(R.id.verses);

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d * 24);
				SketchUi.setStroke(d * 1, 0xFF01837A);
				linear1.setElevation(d * 7);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
			}
			suraArabic.setVisibility(View.GONE);
			if (chapter.get((int) _position).containsKey("title")) {
				suraName.setText(_Arabic(chapter.get((int) _position).get("title").toString()));
			}
			if (chapter.get((int) _position).containsKey("number")) {
				number.setText(_replaceArabicNumber(chapter.get((int) _position).get("number").toString()));
			}
			if (chapter.get((int) _position).containsKey("hadis_range")) {
				verses.setText("হাদিসের রেঞ্জ : ".concat(chapter.get((int) _position).get("hadis_range").toString()));
				verses.setVisibility(View.VISIBLE);
			} else {
				verses.setVisibility(View.GONE);
			}
			if (suraName.getText().toString().contains("বিবিধ ১")) {
				linear1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						in.setClass(getApplicationContext(), DirecthadisActivity.class);
						in.putExtra("name", chapter.get((int) _position).get("title").toString());
						in.putExtra("author", "হাদিসের রেঞ্জ : ".concat(chapter.get((int) _position).get("hadis_range").toString()));
						in.putExtra("bookname", "00");
						startActivity(in);
					}
				});
			} else {

				if (suraName.getText().toString().contains("সালাত")) {
					linear1.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							in.setClass(getApplicationContext(), DirecthadisActivity.class);
							in.putExtra("name", chapter.get((int) _position).get("title").toString());
							in.putExtra("author", "হাদিসের রেঞ্জ : ".concat(chapter.get((int) _position).get("hadis_range").toString()));
							in.putExtra("bookname", "সালাত");
							startActivity(in);
						}
					});
				} else {
					if (suraName.getText().toString().contains("দুগ্ধপান")) {
						linear1.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								in.setClass(getApplicationContext(), DirecthadisActivity.class);
								in.putExtra("name", chapter.get((int) _position).get("title").toString());
								in.putExtra("author", "হাদিসের রেঞ্জ : ".concat(chapter.get((int) _position).get("hadis_range").toString()));
								in.putExtra("bookname", "দুগ্ধপান");
								startActivity(in);
							}
						});
					} else {
						if (chapter.get((int) _position).containsKey("hadis_range")) {
							linear1.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View _view) {
									in.setClass(getApplicationContext(), AllinonemeActivity.class);
									in.putExtra("name", chapter.get((int) _position).get("title").toString());
									in.putExtra("author", "হাদিসের রেঞ্জ : ".concat(chapter.get((int) _position).get("hadis_range").toString()));
									in.putExtra("file", chapter.get((int) _position).get("book_name").toString());
									in.putExtra("chapter_id", chapter.get((int) _position).get("chapter_id").toString());
									startActivity(in);
								}
							});
						} else {
							linear1.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View _view) {
									in.setClass(getApplicationContext(), AllinonemeActivity.class);
									in.putExtra("name", chapter.get((int) _position).get("title").toString());
									in.putExtra("author", _replaceArabicNumber(chapter.get((int) _position).get("number").toString()));
									in.putExtra("file", chapter.get((int) _position).get("book_name").toString());
									in.putExtra("chapter_id", chapter.get((int) _position).get("chapter_id").toString());
									startActivity(in);
								}
							});
						}
					}
				}
			}
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
