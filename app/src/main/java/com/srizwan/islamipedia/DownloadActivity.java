package com.srizwan.islamipedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;


public class DownloadActivity extends AppCompatActivity {
	
	private String search = "";
	private double length = 0;
	private double r = 0;
	private String value1 = "";
	
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
	
	private LinearLayout toolbar;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private LinearLayout LinearLayout1;
	private ImageView searchimg;
	private TextView bookname;
	private LinearLayout searxhmain;
	private RecyclerView recyclerview1;
	private LinearLayout nores;
	private TextInputLayout boxofsearch;
	private ImageView imageview2;
	private EditText searchbox;
	private ImageView noresult;
	private TextView no_result;
	
	private SharedPreferences save;
	private Intent open = new Intent();
	private Intent in = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.download);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
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
		content = findViewById(R.id.content);
		list = findViewById(R.id.list);
		box = findViewById(R.id.box);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		searchimg = findViewById(R.id.searchimg);
		bookname = findViewById(R.id.bookname);
		searxhmain = findViewById(R.id.searxhmain);
		recyclerview1 = findViewById(R.id.recyclerview1);
		nores = findViewById(R.id.nores);
		boxofsearch = findViewById(R.id.boxofsearch);
		imageview2 = findViewById(R.id.imageview2);
		searchbox = findViewById(R.id.searchbox);
		noresult = findViewById(R.id.noresult);
		no_result = findViewById(R.id.no_result);
		save = getSharedPreferences("download", Activity.MODE_PRIVATE);
		
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
				if (listmap.size() == 0) {
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
		if (save.contains("download")) {
			        // আগের নিয়ম: SharedPreferences থেকে লোড করা
			        listmap = new Gson().fromJson(save.getString("download", "[]"), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			
			        // নতুন লিস্ট তৈরি করব যেখানে শুধু বিদ্যমান ফাইলগুলো থাকবে
			        ArrayList<HashMap<String, Object>> filteredList = new ArrayList<>();
			
			        for (HashMap<String, Object> item : listmap) {
				            String filePath = item.get("filepath").toString() + item.get("filename").toString();
				            if (FileUtil.isExistFile(filePath)) { // যদি ফাইল থাকে, তাহলে লিস্টে রাখব
					                filteredList.add(item);
					            }
				        }
			
			        // নতুন তালিকাটি আপডেট করা হচ্ছে
			        listmap = filteredList;
			        save.edit().putString("download", new Gson().toJson(listmap)).apply();
			
			        // RecyclerView সেটআপ
			        recyclerview1.setLayoutManager(new LinearLayoutManager(this));
			        recyclerview1.setAdapter(new Recyclerview1Adapter(listmap));
			        recyclerview1.getAdapter().notifyDataSetChanged(); // আপডেট করা হয়েছে
			    } else {
			        Toast.makeText(getApplicationContext(), "কোনো ডাউনলোডকৃত ফাইল নেই", Toast.LENGTH_SHORT).show();
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
		listmap = new Gson().fromJson(save.getString("download", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		length = listmap.size();
		r = length - 1;
		for(int _repeat64 = 0; _repeat64 < (int)(length); _repeat64++) {
			value1 = listmap.get((int)r).get("filename").toString();
			if (!(_charSeq.length() > value1.length()) && value1.toLowerCase().contains(_charSeq.toLowerCase())) {
				
			} else {
				listmap.remove((int)(r));
			}
			r--;
		}
		recyclerview1.setAdapter(new Recyclerview1Adapter(listmap));
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
			View _v = _inflater.inflate(R.layout.bookonlinefull, null);
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
			
			name.setText(listmap.get((int)_position).get("filename").toString());
			number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
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
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (FileUtil.isExistFile(listmap.get((int)_position).get("filepath").toString().concat(listmap.get((int)_position).get("filename").toString()))) {
						open.setClass(getApplicationContext(), PdfadActivity.class);
						open.putExtra("heading", listmap.get((int)_position).get("filename").toString());
						open.putExtra("pdfname", listmap.get((int)_position).get("filepath").toString().concat(listmap.get((int)_position).get("filename").toString()));
						startActivity(open);
					} else {
						in.setClass(getApplicationContext(), BlogActivity.class);
						in.putExtra("url", listmap.get((int)_position).get("fileurl").toString());
						startActivity(in);
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
