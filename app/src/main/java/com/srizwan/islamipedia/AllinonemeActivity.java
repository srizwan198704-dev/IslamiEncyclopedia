package com.srizwan.islamipedia;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class AllinonemeActivity extends AppCompatActivity {
	
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";

	private String value2 = "";
	private double n = 0;
	private HashMap<String, Object> ListMap = new HashMap<>();

	private String a = "";
	private String b = "";
	private ArrayList<HashMap<String, Object>> chapter = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap_cache = new ArrayList<>();
	
	private LinearLayout toolbar;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private LinearLayout LinearLayout1;
	private ImageView searchimg;
	private TextView bookname;
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
		setContentView(R.layout.allinoneme);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		toolbar = findViewById(R.id.toolbar);
		content = findViewById(R.id.content);
		list = findViewById(R.id.list);
		box = findViewById(R.id.box);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		searchimg = findViewById(R.id.searchimg);
		bookname = findViewById(R.id.bookname);
		author = findViewById(R.id.author);
		searxhmain = findViewById(R.id.searxhmain);
		recyclerview1 = findViewById(R.id.recyclerview1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);

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
		bookname.setText(_Arabic(getIntent().getStringExtra("name")));
		_marquue(bookname, getIntent().getStringExtra("name"));
		author.setText(getIntent().getStringExtra("auhtor"));
		try{
			java.io.InputStream input = getAssets().open(getIntent().getStringExtra("file"));
			listmap_cache = new Gson().fromJson(Rizwan.copyFromInputStream(input), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			n = 0;
			for(int _repeat35 = 0; _repeat35 < (int)(listmap_cache.size()); _repeat35++) {
				if (listmap_cache.get((int)n).get("chapter_id").toString().equals(getIntent().getStringExtra("chapter_id"))) {
					ListMap = new HashMap<>();
					ListMap.put("hadith_key", listmap_cache.get((int)n).get("hadith_key").toString());
					ListMap.put("ar", listmap_cache.get((int)n).get("ar").toString());
					ListMap.put("narrator", listmap_cache.get((int)n).get("narrator").toString());
					ListMap.put("grade", listmap_cache.get((int)n).get("grade").toString());
					chapter.add(ListMap);
				}
				n++;
			}
			recyclerview1.setAdapter(new Recyclerview1Adapter(chapter));
			recyclerview1.setLayoutManager(new LinearLayoutManager(this));
		}catch(Exception e){
			 
		}
		search = new Gson().toJson(chapter);
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
			value1 = chapter.get((int)r).get("narrator").toString();
			value2 = chapter.get((int)r).get("hadith_key").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {

			} else {
				if (!(_charSeq.length() > value2.length()) && value2.toLowerCase().contains(_charSeq.toLowerCase())) {

				} else {
					chapter.remove((int) (r));
				}
			}
			r--;
		}
		recyclerview1.setAdapter(new AllinonemeActivity.Recyclerview1Adapter(chapter));
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
		public AllinonemeActivity.Recyclerview1Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.hadisviewmore, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new AllinonemeActivity.Recyclerview1Adapter.ViewHolder(_v);
		}

		@Override
		public void onBindViewHolder(ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
			View _view = _holder.itemView;

			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final TextView book = _view.findViewById(R.id.book);
			final TextView title = _view.findViewById(R.id.title);
			final TextView description_ar = _view.findViewById(R.id.description_ar);
			final TextView description = _view.findViewById(R.id.description);
			final TextView hadith_number = _view.findViewById(R.id.hadith_number);
			final LinearLayout linear3 = _view.findViewById(R.id.linear3);
			final ImageView copy = _view.findViewById(R.id.copy);
			final ImageView share = _view.findViewById(R.id.share);

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
			hadith_number.setText(Html.fromHtml(_Arabic(chapter.get((int)_position).get("hadith_key").toString())));
			book.setText(author.getText().toString());
			title.setText(Html.fromHtml(_Arabic(chapter.get((int)_position).get("ar").toString())));
			description_ar.setText(Html.fromHtml(_Arabic(chapter.get((int)_position).get("narrator").toString())));
			description.setText(Html.fromHtml(_Arabic(chapter.get((int)_position).get("grade").toString())));
			if ("".equals(chapter.get((int)_position).get("ar").toString())) {
				linear1.setVisibility(View.GONE);
			} else {
				linear1.setVisibility(View.VISIBLE);
			}
			copy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE))
							.setPrimaryClip(ClipData.newPlainText("clipboard",
									hadith_number.getText().toString().concat("\n")
											.concat(title.getText().toString().concat("\n")
													.concat(description_ar.getText().toString().concat("\n")
															.concat(description.getText().toString().concat("\n")
																	.concat(book.getText().toString()
																			.concat("\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")))))));

					Toast.makeText(getApplicationContext(), hadith_number.getText().toString().concat(" কপি করা হয়েছে"), Toast.LENGTH_SHORT).show();
				}
			});

			share.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					a = "হাদিসটি শেয়ার করুন";
					b = hadith_number.getText().toString().concat("\n")
							.concat(title.getText().toString().concat("\n")
									.concat(description_ar.getText().toString().concat("\n")
											.concat(description.getText().toString().concat("\n")
													.concat(book.getText().toString()
															.concat("\n\nআসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")))));

					Intent i = new Intent(android.content.Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(android.content.Intent.EXTRA_SUBJECT, a);
					i.putExtra(android.content.Intent.EXTRA_TEXT, b);
					startActivity(Intent.createChooser(i, "হাদিসটি শেয়ার করুন"));

					Toast.makeText(getApplicationContext(), hadith_number.getText().toString().concat(" শেয়ার করা হয়েছে"), Toast.LENGTH_SHORT).show();
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
