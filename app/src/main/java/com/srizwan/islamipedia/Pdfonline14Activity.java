package com.srizwan.islamipedia;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.Status;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class Pdfonline14Activity extends AppCompatActivity {
	private boolean isOnlineMode = true;

    private double click = 0;
    private String b = "";
	private String search = "";
	private double length = 0;
	private double r = 0;

	private double lngth = 0;
	private double r0 = 0;
	private String value1 = "";
	private String value2 = "";
	private String value3 = "";
	private String getsearch = "";

	private String downloaded = "";
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
	
	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private TextView bookname;
	private ImageView searchimg;
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
	private SharedPreferences save;
	private SharedPreferences mysecret;
	private Intent in = new Intent();
	private AlertDialog.Builder deleted;
	private AlertDialog.Builder onlineoffline;
	private Switch onlineOfflineSwitch;

	private SharedPreferences data;


	@Override
protected void onCreate(Bundle _savedInstanceState) {
    super.onCreate(_savedInstanceState);
    setContentView(R.layout.pdfonline);
    initialize(_savedInstanceState);

    // Check and request permissions
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
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
		bookname = findViewById(R.id.bookname);
		searchimg = findViewById(R.id.searchimg);
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
		save = getSharedPreferences("json", Activity.MODE_PRIVATE);
		mysecret = getSharedPreferences("mysecret", Activity.MODE_PRIVATE);
		deleted = new AlertDialog.Builder(this);
		onlineoffline = new AlertDialog.Builder(this);
		onlineOfflineSwitch = findViewById(R.id.onlineOfflineSwitch);
		data = getSharedPreferences("data", Activity.MODE_PRIVATE);

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
				book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.pdf, "", _book_request_listener);
				spinber.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.GONE);
			}
		});
		
		imageview2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (searchbox.getText().toString().equals("")) {
					searxhmain.setVisibility(View.GONE);
					click = 0;
				} else {
					searchbox.setText("");
				}
			}
		});
		onlineOfflineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					isOnlineMode = true;
					onlineOfflineSwitch.setText("অনলাইন");
					Toast.makeText(getApplicationContext(), "অনলাইন মোড চালু", Toast.LENGTH_SHORT).show();
					book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.pdf, "", _book_request_listener);
					spinber.setVisibility(View.VISIBLE);
					((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
					Nointernet.setVisibility(View.GONE);
				spin.setVisibility(View.VISIBLE);
				} else {
					isOnlineMode = false;
					onlineOfflineSwitch.setText("অফলাইন");
					((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
					spin.setVisibility(View.VISIBLE);
					ListView1.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "অফলাইন মোড চালু (শুধু ডাউনলোড হওয়া বই)", Toast.LENGTH_SHORT).show();
_offlinebook();
				}
			}
		});



		searchbox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();

				if (_charSeq.isEmpty()) {
					// সার্চ ফাঁকা হলে পুরনো লিস্ট ফেরত আনবো
					if (isOnlineMode) {
						map = new Gson().fromJson(getsearch, new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
					} else {
						_offlinebook(); // অফলাইনে থাকলে আবার অফলাইন লোড করবো
					}
					ListView1.setAdapter(new ListView1Adapter(map));
					((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
					nores.setVisibility(View.GONE);
					ListView1.setVisibility(View.VISIBLE);
				} else {
					_json_search(_charSeq); // নরমাল সার্চ
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
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
                map = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                save.edit().putString("অনলাইন বই সমাহার", _response).commit();
                if (map != null && !map.isEmpty()) {
                    ListView1.setAdapter(new ListView1Adapter(map));
                    ((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
                    mysecret.edit().putString("mysecret", map.get(0).get("api").toString()).commit();
                } else {
                    map = new ArrayList<>();
                    Toast.makeText(getApplicationContext(), "কোনো বই পাওয়া যায়নি।", Toast.LENGTH_SHORT).show();
                }
                getsearch = new Gson().toJson(map);
				downloaded = new Gson().toJson(map);
				onlineOfflineSwitch.setVisibility(View.VISIBLE);
				nores.setVisibility(View.GONE);
				ListView1.setVisibility(View.VISIBLE);
				try {
					if (map.size() == 0) {
						spin.setVisibility(View.VISIBLE);
						content.setVisibility(View.GONE);
						searchimg.setVisibility(View.GONE);
						onlineOfflineSwitch.setVisibility(View.GONE);
					} else {
						spin.setVisibility(View.GONE);
						content.setVisibility(View.VISIBLE);
						searchimg.setVisibility(View.VISIBLE);
						onlineOfflineSwitch.setVisibility(View.VISIBLE);
					}
				} catch (NullPointerException e) {

				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
				if (save.contains("অনলাইন বই সমাহার")) {
                    map = new Gson().fromJson(save.getString("অনলাইন বই সমাহার", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                    if (map != null && !map.isEmpty()) {
                        ListView1.setAdapter(new Pdfonline14Activity.ListView1Adapter(map));
                        ((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
                        //mysecret.edit().putString("mysecret", map.get(0).get("api").toString()).commit();
                    } else {
                        map = new ArrayList<>();
                        Toast.makeText(getApplicationContext(), "কোনো বই পাওয়া যায়নি।", Toast.LENGTH_SHORT).show();
                    }
                    getsearch = save.getString("অনলাইন বই সমাহার", "");
					downloaded = save.getString("অনলাইন বই সমহার", "");
					mysecret.edit().putString("mysecret", map.get((int) 0).get("api").toString()).commit();
					spin.setVisibility(View.GONE);
					content.setVisibility(View.VISIBLE);
					Nointernet.setVisibility(View.GONE);
					onlineOfflineSwitch.setVisibility(View.VISIBLE);
					searchimg.setVisibility(View.VISIBLE);
					onlineOfflineSwitch.setVisibility(View.VISIBLE);
				}
				else {
					spinber.setVisibility(View.GONE);
					Nointernet.setVisibility(View.VISIBLE);
				}
			}
		};
	}
	
private void initializeLogic() {
    // Default UI setup
    _status_bar_color("#FF01837A", "#FF01837A");
    bookname.setText("অনলাইন বই সমাহার");
    _marquue(bookname, "অনলাইন বই সমাহার");
    downloadDirectory = FileUtil.getPackageDataDir(getApplicationContext());
    FileUtil.makeDir(downloadDirectory);


    PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true) // Optimize PRDownloader
			.setReadTimeout(30_000)
			.setConnectTimeout(30_000)
            .build();
    PRDownloader.initialize(getApplicationContext(), config);

    boxofsearch.setBoxCornerRadii(100, 100, 100, 100);
    boxofsearch.setBoxBackgroundColor(0xFFFFFFFF);
    Nointernet.setVisibility(View.GONE);
    searxhmain.setVisibility(View.GONE);
    searchimg.setVisibility(View.GONE);
    nores.setVisibility(View.GONE);

	if (data.getString("data", "").isEmpty()) {
		// সুইচটি GONE করা
		onlineOfflineSwitch.setVisibility(View.GONE);
	} else {
		// যদি ডাটা থাকে, সুইচটি VISIBLE রাখা
		onlineOfflineSwitch.setVisibility(View.VISIBLE);
	}
    // FastScroll Listener
    ListView1.setOnScrollListener(new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                ListView1.setFastScrollEnabled(true);
            } else {
                ListView1.setFastScrollEnabled(true);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // No extra work needed
        }
    });

    // Handle connectivity
    if (Rizwan.isConnected(getApplicationContext())) {
		onlineOfflineSwitch.setChecked(true);
        book.startRequestNetwork(RequestNetworkController.GET, BuildConfig.pdf, "", _book_request_listener);
    } else {
        loadOfflineData();
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


	private void _offlinebook() {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				ArrayList<HashMap<String, Object>> offlineList = new ArrayList<>();

				if (save.contains("অনলাইন বই সমাহার")) {
					ArrayList<HashMap<String, Object>> allBooks = new Gson().fromJson(
							save.getString("অনলাইন বই সমাহার", ""),
							new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType()
					);

					for (HashMap<String, Object> book : allBooks) {
						String bookName = book.get("name").toString();
						String filePath = FileUtil.getPackageDataDir(getApplicationContext()) + "/" + bookName;

						if (FileUtil.isExistFile(filePath)) {
							offlineList.add(book);
						}
					}
				}

				map = offlineList;
				if (map.isEmpty()) {
					nores.setVisibility(View.VISIBLE);
					ListView1.setVisibility(View.GONE);
				} else {
					nores.setVisibility(View.GONE);
					ListView1.setVisibility(View.VISIBLE);
				}

				ListView1.setAdapter(new ListView1Adapter(map));
				((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();

				spin.setVisibility(View.GONE);  // লোডিং অফ করবো
				ListView1.setVisibility(View.VISIBLE); // লিস্ট দেখাবো
			}
		}, 300); // 300 মিলিসেকেন্ড ডিলে, চাইলেই কম বেশি করতে পারো
	}



	// NullPointerException fixes
	private void _json_search(final String _charSeq) {
		ArrayList<HashMap<String, Object>> tempList;

		if (isOnlineMode) {
			// যদি অনলাইনে থাকি, তখন getsearch থেকে
			if (getsearch != null && !getsearch.isEmpty()) {
				tempList = new Gson().fromJson(getsearch, new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
			} else {
				tempList = new ArrayList<>();
			}
		} else {
			// যদি অফলাইনে থাকি, map থেকেই কাজ করবো
			tempList = new ArrayList<>(map);
		}

		ArrayList<HashMap<String, Object>> filteredList = new ArrayList<>();
		for (HashMap<String, Object> book : tempList) {
			String name = book.get("name").toString().toLowerCase();
			String author = book.get("author").toString().toLowerCase();
			String searchLower = _charSeq.toLowerCase();

			if (name.contains(searchLower) || author.contains(searchLower)) {
				filteredList.add(book);
			}
		}

		map = filteredList;

		if (map.isEmpty()) {
			nores.setVisibility(View.VISIBLE);
			ListView1.setVisibility(View.GONE);
		} else {
			nores.setVisibility(View.GONE);
			ListView1.setVisibility(View.VISIBLE);
		}

		ListView1.setAdapter(new ListView1Adapter(map));
		((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
	}


// Handle offline data
private void loadOfflineData() {
    if (save.contains("অনলাইন বই সমাহার")) {
        map = new Gson().fromJson(save.getString("অনলাইন বই সমাহার", ""), new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
        if (map == null) map = new ArrayList<>();
        ListView1.setAdapter(new ListView1Adapter(map));
        ((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();
        getsearch = save.getString("অনলাইন বই সমাহার", "");
		downloaded = save.getString("অনলাইন বই সমাহার", "");
        spin.setVisibility(View.GONE);
		onlineOfflineSwitch.setChecked(false);
        content.setVisibility(View.VISIBLE);
        Nointernet.setVisibility(View.GONE);
		onlineOfflineSwitch.setVisibility(View.VISIBLE);
		searchimg.setVisibility(View.VISIBLE);
    } else {
        Toast.makeText(getApplicationContext(), "ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
        spin.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        Nointernet.setVisibility(View.VISIBLE);
    }
}

// PRDownloader improvements
private void setupDownloader() {
    PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build();
    PRDownloader.initialize(getApplicationContext(), config);
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

	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");
		
		return result;
	}
	
	public void _extra1() {
	}
	int downloadID;
	public String _SizeFormat(double size) {
			String string = "0 B";
			if (size < 1000) {
					string = (long)size + " B" + "  (" + (long)size + ")";
			}
 else if (size < 1000000) {
					string = new DecimalFormat("0.00").format(size / 1000) + " KB" + "  (" + (long)size + " B)";
			} else if (size < 1.0E9) {
					string = new DecimalFormat("0.00").format(size / 1000000) + " MB" + "  (" + (long)size + " B)";
			}
 else {
					string = new DecimalFormat("0.00").format(size / 1.0E9) + " GB" + "  (" + (long)size + " B)";
			}
			return string;
	}
	
	{
	}
	
	
	public void _Task(final String _type) {
		if (_type.equals("start")) {
			
		} else {
			if (_type.equals("end")) {
				
			} else {
				if (_type.equals("reset")) {
					
				}
			}
		}
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
				_view = _inflater.inflate(R.layout.book, null);
			}
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final ImageView img = _view.findViewById(R.id.img);
			final LinearLayout boxofcontent = _view.findViewById(R.id.boxofcontent);
			final LinearLayout LinearLayout3 = _view.findViewById(R.id.LinearLayout3);
			final TextView name = _view.findViewById(R.id.name);
			final TextView author = _view.findViewById(R.id.author);
			final LinearLayout boxofdownload = _view.findViewById(R.id.boxofdownload);
			final TextView percentage = _view.findViewById(R.id.percentage);
			final ProgressBar downloadprogressbar = _view.findViewById(R.id.downloadprogressbar);
			final ImageView download = _view.findViewById(R.id.download);
			final ImageView delete = _view.findViewById(R.id.delete);
			final TextView number = _view.findViewById(R.id.number);
			final LinearLayout bookpic = _view.findViewById(R.id.bookpic);
			number.setText(_replaceArabicNumber(String.valueOf((long)(1 + _position))));
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*20);
				SketchUi.setStroke(d*1,0xFF01837A);
				linear1.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
				linear1.setClickable(true);
			}
			downloadprogressbar.setProgressDrawable(getDrawable(R.drawable.circular_progress));

			if (map.get((int)_position).containsKey("pdflink")) {
				name.setText(map.get((int)_position).get("name").toString());
				author.setText("লেখক : ".concat(map.get((int)_position).get("author").toString()));
				Glide.with(getApplicationContext()).load(Uri.parse(map.get((int)_position).get("img").toString())).into(img);
			} else {
				
			}

			if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat(map.get((int)_position).get("name").toString())))) {
				downloadprogressbar.setVisibility(View.GONE);
				percentage.setVisibility(View.GONE);
				delete.setVisibility(View.VISIBLE);
				download.setVisibility(View.VISIBLE);
				download.setImageResource(R.drawable.checked);
			} else {
				downloadprogressbar.setVisibility(View.GONE);
				percentage.setVisibility(View.GONE);
				delete.setVisibility(View.GONE);
				download.setVisibility(View.VISIBLE);
				download.setImageResource(R.drawable.download);
			}
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (map.get((int)_position).get("pdflink").toString().equals("link")) {
						Toast.makeText(getApplicationContext(), "লিংক যুক্ত করা হয়নি", Toast.LENGTH_SHORT).show();
					} else {
						if (FileUtil.isExistFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat(map.get((int)_position).get("name").toString())))) {
							in.setClass(getApplicationContext(), PdfadActivity.class);
							in.putExtra("heading", map.get((int)_position).get("name").toString());
							in.putExtra("pdfname", FileUtil.getPackageDataDir(getApplicationContext()).concat("/".concat(map.get((int)_position).get("name").toString())));
							startActivity(in);
						} else {
							if (tap) {
								Toast.makeText(getApplicationContext(), "আপনার আরেকটি ফাইল ডাউনলোড না হওয়া পর্যন্ত অপেক্ষা করুন", Toast.LENGTH_SHORT).show();
							} else {
								tap = true;
								if (Rizwan.isConnected(getApplicationContext())) {
									download.setVisibility(View.GONE);
									downloadprogressbar.setVisibility(View.VISIBLE);
									percentage.setVisibility(View.VISIBLE);
									percentage.setText("০%");
									Toast.makeText(getApplicationContext(), "ডাউনলোড শুরু হচ্ছে অপেক্ষা করুন", Toast.LENGTH_SHORT).show();
									if (Status.RUNNING == PRDownloader.getStatus(downloadID) || Status.PAUSED == PRDownloader.getStatus(downloadID)) {
										return;
									}
									vUrl = map.get((int)_position).get("pdflink").toString().trim();
									vFilename = map.get((int)_position).get("name").toString();
									try {
										new java.net.URL(vUrl); // Check if url is valid
										//Loading..
										_Task("reset");
										_Task("start");
										downloadID = PRDownloader.download(vUrl, downloadDirectory, vFilename)
										.build()
										.setOnStartOrResumeListener(new OnStartOrResumeListener() {
											@Override
											public void onStartOrResume() {
												vResumePause = false;
												//On Click Resume
											}
										})
										.setOnPauseListener(new OnPauseListener() {
											@Override
											public void onPause() {
												vResumePause = false;
												//On Click Pause
											}
										})
										.setOnCancelListener(new OnCancelListener() {
											@Override
											public void onCancel() {
												_Task("end");
												//On Click Cancel
											}
										})
										.setOnProgressListener(new OnProgressListener() {
											@Override
											public void onProgress(Progress _progress) {
												long currentBytes = _progress.currentBytes;
												long totalBytes = _progress.totalBytes;
												if (totalBytes != -1) {
													long progressPercent = currentBytes * 100 / totalBytes;
													int progress = (int)progressPercent;
													download_progress = progress;
													percentage.setText(_replaceArabicNumber(String.valueOf((long)(download_progress)).concat("%")));
													downloadprogressbar.setProgress((int)download_progress);
													download.setVisibility(View.GONE);
													downloadprogressbar.setVisibility(View.VISIBLE);
													percentage.setVisibility(View.VISIBLE);
												} else {
													
												}
												Current_Size = _SizeFormat(currentBytes);
												Total_Size = _SizeFormat(totalBytes);
											}
										})
                                                .start(new OnDownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        _Task("end");
												//on download complete
												downloadprogressbar.setProgress((int)0);
												percentage.setText("০%");
												((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
												percentage.setVisibility(View.GONE);
												downloadprogressbar.setVisibility(View.GONE);
												download.setVisibility(View.VISIBLE);
												download.setImageResource(R.drawable.checked);
												delete.setVisibility(View.VISIBLE);
												Toast.makeText(getApplicationContext(), "ডাউনলোড সম্পূর্ণ হয়েছে", Toast.LENGTH_SHORT).show();
												tap = false;
												data.edit().putString(_data.get((int)_position).get("name").toString(), _data.get((int)_position).get("name").toString()).commit();
											}
                                                    @Override
                                                    public void onError(com.downloader.Error error) {
                                                        _Task("end");
                                                        String string = "Unknown error";
                                                        if (error.isServerError()) {
                                                            string = "Server Error: " + error.getServerErrorMessage();
                                                            Toast.makeText(
                                                                    getApplicationContext(),
                                                                    "সার্ভারে সমস্যা দিচ্ছে",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
													downloadprogressbar.setProgress((int)0);
													percentage.setText("০%");
													((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
													download.setImageResource(R.drawable.download);
													download.setVisibility(View.VISIBLE);
													percentage.setVisibility(View.GONE);
													downloadprogressbar.setVisibility(View.GONE);
													tap = false;
												} else {
													if (error.isConnectionError()) {
														string = "Connection Error: " + error.getConnectionException().getMessage();
														Toast.makeText(getApplicationContext(), "নেটওয়ার্ক সমস্যা হচ্ছে ইন্টারনেট সেটিং চেক করুন এবং আবার ডাউনলোড করার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
														downloadprogressbar.setProgress((int)0);
														percentage.setText("০%");
														download.setImageResource(R.drawable.download);
														download.setVisibility(View.VISIBLE);
														percentage.setVisibility(View.GONE);
														downloadprogressbar.setVisibility(View.GONE);
														tap = false;
														((BaseAdapter)ListView1.getAdapter()).notifyDataSetChanged();
													}
												}
												debug_string = string;
											}
										});
									}
									catch (java.net.MalformedURLException e) {
									}
									catch (Exception | OutOfMemoryError e) {
										Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
										tap = false;
									}
								} else {
									download.setVisibility(View.VISIBLE);
									downloadprogressbar.setVisibility(View.GONE);
									percentage.setVisibility(View.GONE);
									percentage.setText("০%");
									Toast.makeText(getApplicationContext(), "ইন্টারনেট সেটিং চেক করুন", Toast.LENGTH_SHORT).show();
								}
							}
						}
					}
				}
			});
			delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					try {
						// Inflate the custom layout
						LayoutInflater inflater = LayoutInflater.from(_view.getContext());
						View customView = inflater.inflate(R.layout.custom_dialog, null);

						// Find views in the custom layout
						TextView dialogTitle = customView.findViewById(R.id.dialog_title);
						Button btnYes = customView.findViewById(R.id.btn_yes);
						Button btnNo = customView.findViewById(R.id.btn_no);

						// Set title text with custom font
						Typeface solaimanLipiFont = ResourcesCompat.getFont(_view.getContext(), R.font.solaimanlipi);
						if (solaimanLipiFont != null) {
							String titleText = map.get((int) _position).get("name").toString().concat(" কিতাবটি ডিলিট করবেন?");
							dialogTitle.setText(titleText);
							dialogTitle.setTypeface(solaimanLipiFont);
						} else {
							dialogTitle.setText("Custom Font Not Found");
						}

						// Create the dialog
						AlertDialog dialog = new AlertDialog.Builder(_view.getContext())
								.setView(customView)
								.setCancelable(false) // Prevent dismiss by tapping outside
								.create();

						// Handle button clicks
						btnYes.setOnClickListener(v -> {
							try {
								// Perform delete action
								String filePath = FileUtil.getPackageDataDir(getApplicationContext()).concat("/"
										.concat(map.get((int) _position).get("name").toString()));
								FileUtil.deleteFile(filePath);
								_offlinebook(); // লিস্ট রিফ্রেশ করবো
								data.edit().remove(_data.get((int)_position).get("name").toString()).commit();
								// Show a toast
								Toast.makeText(
										_view.getContext(),
										map.get((int) _position).get("name").toString() + " ডিলিট করা হয়েছে।",
										Toast.LENGTH_SHORT
								).show();

								// Update ListView
								((BaseAdapter) ListView1.getAdapter()).notifyDataSetChanged();

								// Dismiss the dialog
								dialog.dismiss();
							} catch (Exception e) {
								Toast.makeText(_view.getContext(), "ফাইল ডিলিট করতে সমস্যা হয়েছে।", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
						});

						btnNo.setOnClickListener(v -> {
							// Dismiss the dialog
							dialog.dismiss();
						});

						// Show the dialog
						dialog.show();
					} catch (Exception e) {
						// Handle exceptions gracefully
						Toast.makeText(_view.getContext(), "ডায়ালগ লোড করতে সমস্যা হয়েছে।", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			});


			return _view;
		}
	}
}
