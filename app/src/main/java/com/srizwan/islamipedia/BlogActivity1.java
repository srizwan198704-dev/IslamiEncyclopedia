package com.srizwan.islamipedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.downloader.Error;
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
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class BlogActivity1 extends AppCompatActivity {

	private Timer _timer = new Timer();
	
	private String FilePath = "";
	private double num = 0;
	private String filename = "";
	private String Fileurl = "";
	private HashMap<String, Object> map = new HashMap<>();
	private boolean isTabSelected = false;
	private double currentTab = 0;
	private double r = 0;
	
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
	
	private LinearLayout linear1;
	private TabLayout tablayout1;
	private LinearLayout linear_web;
	private LinearLayout linear_download;
	private ProgressBar progressbar1;
	private Button permissions;
	private SwipeRefreshLayout swiperefreshlayout1;
	private LinearLayout errorView;
	private WebView webview1;
	private TextView textview1;
	private MaterialButton materialbutton3;
	private MaterialButton materialbutton1;
	private MaterialButton materialbutton2;
	private ScrollView vscroll1;
	private LinearLayout linear_list;
	
	private AlertDialog.Builder dialog;
	private SharedPreferences save;
	private Intent intent = new Intent();
	private TimerTask time;
	private TimerTask timer;
	private Intent in = new Intent();
	private TimerTask refresh;
	private AlertDialog.Builder dialogme;
	private Intent open = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.blog);
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
		linear1 = findViewById(R.id.linear1);
		tablayout1 = findViewById(R.id.tablayout1);
		linear_web = findViewById(R.id.linear_web);
		linear_download = findViewById(R.id.linear_download);
		progressbar1 = findViewById(R.id.progressbar1);
		permissions = findViewById(R.id.permissions);
		swiperefreshlayout1 = findViewById(R.id.swiperefreshlayout1);
		errorView = findViewById(R.id.errorView);
		webview1 = findViewById(R.id.webview1);
		webview1.getSettings().setJavaScriptEnabled(true);
		webview1.getSettings().setSupportZoom(true);
		textview1 = findViewById(R.id.textview1);
		materialbutton3 = findViewById(R.id.materialbutton3);
		materialbutton1 = findViewById(R.id.materialbutton1);
		materialbutton2 = findViewById(R.id.materialbutton2);
		vscroll1 = findViewById(R.id.vscroll1);
		linear_list = findViewById(R.id.linear_list);
		dialog = new AlertDialog.Builder(this);
		save = getSharedPreferences("download", Activity.MODE_PRIVATE);
		dialogme = new AlertDialog.Builder(this);
		
		tablayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				final int _position = tab.getPosition();
				if (_position == 0) {
					linear_web.setVisibility(View.VISIBLE);
					linear_download.setVisibility(View.GONE);
				}
				if (_position == 1) {
					linear_web.setVisibility(View.GONE);
					linear_download.setVisibility(View.VISIBLE);
				}
				if (_position == 2) {
					in.setClass(getApplicationContext(), DownloadActivity.class);
					startActivity(in);
					tablayout1.getTabAt(0).select();
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				final int _position = tab.getPosition();
				
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				final int _position = tab.getPosition();
				
			}
		});
		
		swiperefreshlayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				webview1.reload();
				refresh = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								swiperefreshlayout1.setRefreshing(false);
							}
						});
					}
				};
				_timer.schedule(refresh, (int)(1500));
			}
		});
		
		//webviewOnProgressChanged
		webview1.setWebChromeClient(new WebChromeClient() {
				@Override public void onProgressChanged(WebView view, int _newProgress) {
					
				}
		});
		
		//OnDownloadStarted
		webview1.setDownloadListener(new DownloadListener() {
			@SuppressLint("UnspecifiedRegisterReceiverFlag")
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadManager.Request webview1a = new DownloadManager.Request(Uri.parse(url));
				String webview1b = CookieManager.getInstance().getCookie(url);
				webview1a.addRequestHeader("cookie", webview1b);
				webview1a.addRequestHeader("User-Agent", userAgent);
				webview1a.setDescription("Downloading file...");
				webview1a.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
				webview1a.allowScanningByMediaScanner(); webview1a.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); webview1a.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
				
				DownloadManager webview1c = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				webview1c.enqueue(webview1a);
				//showMessage("Downloading File....");
				BroadcastReceiver onComplete = new BroadcastReceiver() {
					public void onReceive(Context ctxt, Intent intent) {
						//showMessage("Download Complete!");
						unregisterReceiver(this);
						
					}};
				registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			}
		});
		
		webview1.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				progressbar1.setVisibility(View.VISIBLE);
				swiperefreshlayout1.setVisibility(View.VISIBLE);
				errorView.setVisibility(View.GONE);
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				progressbar1.setVisibility(View.GONE);
				super.onPageFinished(_param1, _param2);
			}
		});
		
		materialbutton3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (webview1.canGoBack()) {
					webview1.goBack();
				} else {
					finish();
				}
				errorView.setVisibility(View.GONE);
				swiperefreshlayout1.setVisibility(View.VISIBLE);
			}
		});
		
		materialbutton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				webview1.reload();
				errorView.setVisibility(View.GONE);
				swiperefreshlayout1.setVisibility(View.VISIBLE);
			}
		});
		
		materialbutton2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
				startActivity(intent);
			}
		});
	}
	
	private void initializeLogic() {
		permissions.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)10, (int)01, 0xFF607D8B, 0xFFF44336));
		
		webview1.setWebChromeClient(new WebChromeClient() {
			    @Override
			    public void onProgressChanged(WebView view, int newProgress) {
				        // Update the ProgressBar based on the loading progress
				        progressbar1.setProgress(newProgress);
				        if (newProgress == 100) {
					            // Hide ProgressBar when loading is complete
					            progressbar1.setVisibility(View.GONE);
					        } else {
					            // Show ProgressBar while loading
					            progressbar1.setVisibility(View.VISIBLE);
					        }
				    }
		});
		WebSettings webSettings = webview1.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webview1.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		webview1.setWebViewClient(new WebViewClient() {
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
				        if (url.startsWith("http") || url.startsWith("https")) {
					            // সাধারণ ওয়েব লিংক হলে WebView-তে লোড করবো
					            view.loadUrl(url);
					            return true;
					        } else if (url.startsWith("intent://")) {
					            try {
						                // Intent URL ওপেন করার চেষ্টা করবো
						                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
						                if (intent != null) {
							                    view.getContext().startActivity(intent);
							                    return true;
							                }
						            } catch (Exception e) {
						                e.printStackTrace();
						                Toast.makeText(view.getContext(), "লিংক ওপেন করা সম্ভব হয়নি!", Toast.LENGTH_SHORT).show();
						            }
					            return true;
					        } else {
					            try {
						                // অন্য কোনো বিশেষ স্কিম থাকলে সেটিও ওপেন করবো
						                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						                view.getContext().startActivity(intent);
						            } catch (Exception e) {
						                Toast.makeText(view.getContext(), "সমর্থিত নয়!", Toast.LENGTH_SHORT).show();
						            }
					            return true;
					        }
				    }
		});
		/*
if (getSupportActionBar() != null) {
        getSupportActionBar().hide();
    }
*/
		webview1.loadUrl(getIntent().getStringExtra("url"));
		num = 0;
		FilePath = FileUtil.getPackageDataDir(getApplicationContext()).concat("//ইসলামী বিশ্বকোষ ও  আল হাদিস S2/");
		tablayout1.addTab(tablayout1.newTab().setText("ইসলামী বিশ্বকোষ"));
		tablayout1.addTab(tablayout1.newTab().setText("ডাউনলোড"));
		tablayout1.addTab(tablayout1.newTab().setText("অফলাইন"));
		tablayout1.setTabTextColors(0xFF000000, 0xFFFFFFFF);
		tablayout1.setInlineLabel(true);
		tablayout1.setTabRippleColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}}, 
		
		new int[] {0xFF01837A}));
		tablayout1.setSelectedTabIndicatorColor(0xFFFFFFFF);
		tablayout1.setSelectedTabIndicatorHeight(3);
		PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
				.setDatabaseEnabled(true) // Optimize PRDownloader
				.setReadTimeout(30_000)
				.setConnectTimeout(30_000)
				.build();
		PRDownloader.initialize(getApplicationContext(), config);
		webview1.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				
				/*
filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
*/
				filename = URLUtil.guessFileName(url, contentDisposition, mimetype).replaceAll("\\.bin$", ".pdf");
				Fileurl = url;
				dialog.setTitle("Download");
				dialog.setMessage("Download to:\n".concat(filename));
				dialog.setPositiveButton("Download", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						map = new HashMap<>();
						map.put("fileurl", Fileurl);
						map.put("filepath", FilePath);
						map.put("filename", filename);
						map.put("filecode", "000");
						listmap.add(map);
						final View inflate = getLayoutInflater().inflate(R.layout.custom, null);
						inflate.setId((int)num);
						LinearLayout linear_base = inflate.findViewById(R.id.linear_base);
						final LinearLayout load = inflate.findViewById(R.id.load);
						final LinearLayout play = inflate.findViewById(R.id.play);
						TextView textview_title = inflate.findViewById(R.id.textview_title);
						TextView textview_mb = inflate.findViewById(R.id.textview_mb);
						Button buttons1 = inflate.findViewById(R.id.buttons1);
						Button buttons2 = inflate.findViewById(R.id.buttons2);
						TextView textview_persent = inflate.findViewById(R.id.textview_persent);
						ProgressBar progressbar_persent = inflate.findViewById(R.id.progressbar_persent);
						linear_base.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)10, (int)1, 0xFF607D8B, 0xFFFFFFFF));
						buttons1.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)50, (int)1, 0xFF607D8B, 0xFFF44336));
						buttons2.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)50, (int)1, 0xFF607D8B, 0xFFF44336));
						textview_title.setText(listmap.get((int)inflate.getId()).get("filename").toString());
						buttons1.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								Toast.makeText(getApplicationContext(), "ডাউনলোড শুরু হচ্ছে অপেক্ষা করুন", Toast.LENGTH_SHORT).show();
								if (Status.RUNNING == PRDownloader.getStatus(Integer.parseInt(listmap.get((int)inflate.getId()).get("filecode").toString()))) {
									PRDownloader.pause(Integer.parseInt(listmap.get((int)inflate.getId()).get("filecode").toString()));
								} else {
									if (Status.PAUSED == PRDownloader.getStatus(Integer.parseInt(listmap.get((int)inflate.getId()).get("filecode").toString()))) {
										PRDownloader.resume(Integer.parseInt(listmap.get((int)inflate.getId()).get("filecode").toString()));
									} else {
										int downloadId = PRDownloader.download(listmap.get((int)inflate.getId()).get("fileurl").toString(), listmap.get((int)inflate.getId()).get("filepath").toString(), listmap.get((int)inflate.getId()).get("filename").toString())
										                        .build()
										                       
										                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
												                            @Override
												                            public void onStartOrResume() {
														                             Button buttons1 = inflate.findViewById(R.id.buttons1);
												
												buttons1.setText("Pause");
												// Assuming tablayout1 is your TabLayout
												tablayout1.getTabAt(1).select();  // Switch to Tab 2 (index starts from 0)  
														                            }
												                        })
										                        .setOnPauseListener(new OnPauseListener() {
												                            @Override
												                            public void onPause() {
														                            Button buttons1 = inflate.findViewById(R.id.buttons1);
												
												buttons1.setText("Start");   
														                            }
												                        })
										                        .setOnCancelListener(new OnCancelListener() {
												                            @Override
												                            public void onCancel() {
														                             ((ViewGroup)(findViewById((int) inflate.getId())).getParent()).removeView((findViewById((int) inflate.getId())));   
														                            }
												                        })
										                        .setOnProgressListener(new OnProgressListener() {
												                            @Override
												                            public void onProgress(Progress progress) {
												                            long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
														                         TextView textview_persent = inflate.findViewById(R.id.textview_persent);
												ProgressBar progressbar_persent = inflate.findViewById(R.id.progressbar_persent);
												
												TextView textview_mb = inflate.findViewById(R.id.textview_mb);
												textview_persent.setText("" + progressPercent);
												progressbar_persent.setProgress((int)progressPercent);
												textview_mb.setText(_ByteToMagaByte(progress.totalBytes).concat("/".concat(_ByteToMagaByte(progress.currentBytes))));      
														                            }
												                        })
										                        .start(new OnDownloadListener() {
												                            @Override
												                            public void onDownloadComplete() {
														                           Toast.makeText(getApplicationContext(), "আলহামদুলিল্লাহ ডাউনলোড সম্পূর্ণ হয়েছে", Toast.LENGTH_SHORT).show();
												play.setVisibility(View.GONE);
												load.setVisibility(View.GONE);
												// আগের লিস্ট লোড করুন (যদি আগে কিছু থাকে)
												String json = save.getString("download", "[]"); // আগের লিস্ট পেতে (ডিফল্ট হিসেবে খালি লিস্ট সেট)
												ArrayList<HashMap<String, String>> oldList = new Gson().fromJson(json, new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType());
												
												// নতুন HashMap তৈরি করে, সবকিছু String এ কনভার্ট করুন
												HashMap<String, String> newMap = new HashMap<>();
												for (Map.Entry<String, Object> entry : map.entrySet()) {
													    newMap.put(entry.getKey(), String.valueOf(entry.getValue())); // Object থেকে String এ কনভার্ট
												}
												
												// নতুন আপডেট হওয়া লিস্টে নতুন আইটেম যোগ করুন
												oldList.add(newMap);
												
												// নতুন আপডেট হওয়া লিস্ট SharedPreferences-এ সংরক্ষণ করুন
												save.edit().putString("download", new Gson().toJson(oldList)).commit();
												isTabSelected = false;    
														                            }
												                            @Override
												                            public void onError(Error error) {
														                           Toast.makeText(getApplicationContext(), "দুঃখিত কিছু সমস্যা হয়েছে আবার চেষ্টা করুন", Toast.LENGTH_SHORT).show();
												// Assuming tablayout1 is your TabLayout
												tablayout1.getTabAt(0).select();  // Switch to Tab 2 (index starts from 0)
												((ViewGroup)(findViewById((int) inflate.getId())).getParent()).removeView((findViewById((int) inflate.getId())));    
														                            }
												                        });
										listmap.get((int)inflate.getId()).put("filecode", String.valueOf(downloadId));
									}
								}
							}
						});
						buttons2.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								PRDownloader.cancel(Integer.parseInt(listmap.get((int)inflate.getId()).get("filecode").toString()));
							}
						});
						buttons1.performClick();
						linear_list.addView(inflate);
						num++;
						linear_base.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								if (FileUtil.isExistFile(FilePath.concat(filename))) {
									r = inflate.getId();;
									open.setClass(getApplicationContext(), PdfadActivity.class);
									open.putExtra("heading", listmap.get((int)r).get("filename").toString());
									open.putExtra("pdfname", listmap.get((int)r).get("filepath").toString().concat(listmap.get((int)r).get("filename").toString()));
									startActivity(open);
								} else {
									Toast.makeText(getApplicationContext(), "দুঃখিত ফাইলটি পাওয়া যাইনি", Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				});
				dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dialog.create().show();
			}
		});


// Add JavaScript interface for back button in HTML
        webview1.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void backButtonClicked() {
                runOnUiThread(() -> {
                    handleCustomBack(); // Use same back handling logic
                });
            }
        }, "Android");

// Your back press handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCustomBack();
            }
        });

// Common back logic for both Android button & HTML button


        webview1.setWebViewClient(new WebViewClient() {
			
			public void onReceivedError(WebView webview, int errorCode, String description, String failingUrl) {
				
				swiperefreshlayout1.setVisibility(View.GONE);
				errorView.setVisibility(View.VISIBLE);
				
			} });
		registerForContextMenu(webview1);}
	    @Override
	    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo){
		        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
		
		        final WebView.HitTestResult webViewHitTestResult = webview1.getHitTestResult();
		
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
						
													Toast.makeText(BlogActivity1.this,"Image Downloaded Successfully.",Toast.LENGTH_LONG).show();
											}
											else {
													Toast.makeText(BlogActivity1.this,"Sorry.. Something Went Wrong.",Toast.LENGTH_LONG).show();
											}
											return false;
									}
							});
			        }
		isTabSelected = false;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
	}
	public void _CreatePath() {
		if (FileUtil.isDirectory(FilePath)) {
			FileUtil.makeDir(FilePath);
		}
	}
	
	
	public String _ByteToMagaByte(final double _bbb) {
		return String.format(Locale.ENGLISH, "%.2fMB", _bbb / (1024.00 * 1024.00));
	}

    private void handleCustomBack() {
        currentTab = tablayout1.getSelectedTabPosition();

        if (currentTab == 1) {
            tablayout1.getTabAt(0).select();
        } else {
            if (currentTab == 0) {
                if (webview1.canGoBack()) {
                    webview1.goBack();
                } else {
                    finish();
                }
            }
        }
    }
	public void _error() {
		webview1.setVisibility(View.GONE);
		errorView.setVisibility(View.VISIBLE);
	}
}
