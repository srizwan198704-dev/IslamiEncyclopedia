package com.srizwan.islamipedia;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
public class ViewActivity1 extends AppCompatActivity {
	private Timer _timer = new Timer();
	
	private String fontName = "";
	private String typeace = "";
	private HashMap<String, Object> viewmap = new HashMap<>();
	private String htmlurl = "";
	private String endStr = "";
	private double i = 0;
	
	private LinearLayout viewblog;
	private LinearLayout toolbar;
	private ProgressBar progressbar1;
	private ScrollView vscroll1;
	private ImageView btn_search;
	private LinearLayout linear4;
	private ImageView btn_menu;
	private TextView textview1;
	private LinearLayout linear2;
	private LinearLayout pl;
	private WebView webview2;
	private TextView textview2;
	private WebView tu;
	private TextView textview3;
	
	private Intent in = new Intent();
	private TimerTask timer;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.view);
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
		viewblog = findViewById(R.id.viewblog);
		toolbar = findViewById(R.id.toolbar);
		progressbar1 = findViewById(R.id.progressbar1);
		vscroll1 = findViewById(R.id.vscroll1);
		btn_search = findViewById(R.id.btn_search);
		linear4 = findViewById(R.id.linear4);
		btn_menu = findViewById(R.id.btn_menu);
		textview1 = findViewById(R.id.textview1);
		linear2 = findViewById(R.id.linear2);
		pl = findViewById(R.id.pl);
		webview2 = findViewById(R.id.webview2);
		webview2.getSettings().setJavaScriptEnabled(true);
		webview2.getSettings().setSupportZoom(true);
		textview2 = findViewById(R.id.textview2);
		tu = findViewById(R.id.tu);
		tu.getSettings().setJavaScriptEnabled(true);
		tu.getSettings().setSupportZoom(true);
		textview3 = findViewById(R.id.textview3);
		
		btn_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
		
		//webviewOnProgressChanged
		webview2.setWebChromeClient(new WebChromeClient() {
				@Override public void onProgressChanged(WebView view, int _newProgress) {
					
				}
		});
		
		//OnDownloadStarted
		webview2.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadManager.Request webview2a = new DownloadManager.Request(Uri.parse(url));
				String webview2b = CookieManager.getInstance().getCookie(url);
				webview2a.addRequestHeader("cookie", webview2b);
				webview2a.addRequestHeader("User-Agent", userAgent);
				webview2a.setDescription("Downloading file...");
				webview2a.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
				webview2a.allowScanningByMediaScanner(); webview2a.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); webview2a.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
				
				DownloadManager webview2c = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				webview2c.enqueue(webview2a);

				BroadcastReceiver onComplete = new BroadcastReceiver() {
					public void onReceive(Context ctxt, Intent intent) {

						unregisterReceiver(this);
						
					}};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
                }
            }
		});
		
		webview2.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				pl.setVisibility(View.VISIBLE);
				progressbar1.setVisibility(View.VISIBLE);
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				pl.setVisibility(View.GONE);
				progressbar1.setVisibility(View.GONE);
				super.onPageFinished(_param1, _param2);
			}
		});
		
		//webviewOnProgressChanged
		tu.setWebChromeClient(new WebChromeClient() {
				@Override public void onProgressChanged(WebView view, int _newProgress) {
					
				}
		});
		
		//OnDownloadStarted
		tu.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadManager.Request tua = new DownloadManager.Request(Uri.parse(url));
				String tub = CookieManager.getInstance().getCookie(url);
				tua.addRequestHeader("cookie", tub);
				tua.addRequestHeader("User-Agent", userAgent);
				tua.setDescription("Downloading file...");
				tua.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
				tua.allowScanningByMediaScanner(); tua.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); tua.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
				
				DownloadManager tuc = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				tuc.enqueue(tua);

				BroadcastReceiver onComplete = new BroadcastReceiver() {
					public void onReceive(Context ctxt, Intent intent) {

						unregisterReceiver(this);
						
					}};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
                }
            }
		});
		
		tu.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				
				super.onPageFinished(_param1, _param2);
			}
		});
	}
	
	private void initializeLogic() {
		_oncreatvirw1();
		_statusbarcolor("#01837A");


		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				_back();
			}
		});
	}

	public void _Webview_() {
		WebSettings webSettings = webview2.getSettings();
		webSettings.setJavaScriptEnabled(true); webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webview2.setVerticalScrollBarEnabled(false);
		webview2.setHorizontalScrollBarEnabled(false);
		webview2.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
				String cookies = CookieManager.getInstance().getCookie(url);
				request.addRequestHeader("cookie", cookies);
				request.addRequestHeader("User-Agent", userAgent);
				request.setDescription("Downloading file to crn folder...");
				request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
				request.allowScanningByMediaScanner(); request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				java.io.File aatv = new java.io.File(Environment.getExternalStorageDirectory().getPath() + "/ইসলামী বিশ্বকোষ ও আল হাদিস");
				
				if(!aatv.exists()){if (!aatv.mkdirs()){ Log.e("TravellerLog ::","Problem creating Image folder");}} request.setDestinationInExternalPublicDir("/crn", URLUtil.guessFileName(url, contentDisposition, mimetype));
				
				DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				
				manager.enqueue(request);
				

				
				//Notif if success
				
				BroadcastReceiver onComplete = new BroadcastReceiver() {
					
					public void onReceive(Context ctxt, Intent intent) {
						

						
						unregisterReceiver(this);
						
					}};

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
                }

            }
			
		});
	}
	
	
	public void _back() {
		if (webview2.canGoBack()) {
			webview2.goBack();
		}
		else {
			finish();
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


	public void _oncreatvirw1() {
		// Intent থেকে Title নেওয়া
		String title = getIntent().getStringExtra("name");

		// SharedPreferences থেকে Content রিসিভ করা
		SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
		String content = prefs.getString("content", "Error: Content not found");

		try {
			textview1.setText(title);
			textview1.setSelected(true);
			_marquue(textview1, title);
			_type_effect(title, textview1, 20);
		} catch (Exception e) {
			textview1.setText("Error: Can't get post title");
		}

		try {
			String htmlurl = "<!DOCTYPE html>\n" +
					"<html>\n" +
					"<head>\n" +
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
					"<meta content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0\" name=\"viewport\">\n" +
					"<style>\n" +
					"@font-face {\n" +
					"    font-family: 'CustomFont';\n" +
					"    src: url('file:///android_res/font/solaimanlipi.ttf');\n" +
					"}\n" +
					"body {\n" +
					"    font-family: 'CustomFont', sans-serif;\n" +
					"    font-size: 16px;\n" +
					"    line-height: 1.6;\n" +
					"    width: 95%;\n" +
					"    overflow-wrap: break-word;\n" +
					"}\n" +
					"</style>\n" +
					"</head>\n" +
					"<body>".concat(content.concat("</body>\n</html>"));

			webview2.loadDataWithBaseURL(null, htmlurl, "text/html", "UTF-8", null);
		} catch (Exception e) {
			// যদি কোনো সমস্যা হয় তাহলে কিছুই করবে না
		}
		{

		};
		registerForContextMenu(webview2);}
	    @Override
	    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo){
		        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
		
		        final WebView.HitTestResult webViewHitTestResult = webview2.getHitTestResult();
		
		        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
					webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
			
			            contextMenu.setHeaderTitle("Download Image From Below");
			
			            contextMenu.add(0, 1, 0, "Save - Download Image")
							.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
									@Override
									public boolean onMenuItemClick(MenuItem menuItem) {
					
											String DownloadImageURL = webViewHitTestResult.getExtra();
					
											if(URLUtil.isValidUrl(DownloadImageURL)){
						
													DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
													request.allowScanningByMediaScanner();
													request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
													DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
													downloadManager.enqueue(request);
						
													Toast.makeText(ViewActivity1.this,"Image Downloaded Successfully.",Toast.LENGTH_LONG).show();
											}
											else {
													Toast.makeText(ViewActivity1.this,"Sorry.. Something Went Wrong.",Toast.LENGTH_LONG).show();
											}
											return false;
									}
							});
			        }
		webview2.setVerticalScrollBarEnabled(false);
		webview2.setHorizontalScrollBarEnabled(false);
		webview2.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
				String cookies = CookieManager.getInstance().getCookie(url);
				request.addRequestHeader("cookie", cookies);
				request.addRequestHeader("User-Agent", userAgent);
				request.setDescription("Downloading file...");
				request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
				request.allowScanningByMediaScanner(); request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				java.io.File aatv = new java.io.File(Environment.getExternalStorageDirectory().getPath() + "/vpn/Ak2 vpn folder");
				
				if(!aatv.exists()){if (!aatv.mkdirs()){ Log.e("TravellerLog ::","Problem creating Image folder");}} request.setDestinationInExternalPublicDir("/vpn/Ak2 vpn folder", URLUtil.guessFileName(url, contentDisposition, mimetype));
				DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				manager.enqueue(request);

				//Notif if success
				BroadcastReceiver onComplete = new BroadcastReceiver() {
					public void onReceive(Context ctxt, Intent intent) {

						unregisterReceiver(this);
					}};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
                }
            }
		});
		_Webview_();
		textview1.setVisibility(View.VISIBLE);
		textview2.setVisibility(View.GONE);
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFF01837A);
			toolbar.setElevation(d*5);
			toolbar.setBackground(SketchUi);
		}
	}
	
	
	public void _type_effect(final String _Text, final TextView _textview, final double _deley) {
		endStr = "";
		i = 0;
		timer = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!(i == _Text.length())) {
							String jg=""+_Text;
							
							char ug = jg.charAt((int) i);
							
							endStr = endStr+""+ug;
							
							_textview.setText(endStr);
							i++;
						}
						else {
							timer.cancel();
						}
					}
				});
			}
		};
		_timer.scheduleAtFixedRate(timer, (int)(_deley), (int)(_deley));
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
	}

