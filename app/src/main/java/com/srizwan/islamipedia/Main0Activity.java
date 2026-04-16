package com.srizwan.islamipedia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class Main0Activity extends AppCompatActivity {

    private Toolbar _toolbar;
    private AppBarLayout _app_bar;
    private CoordinatorLayout _coordinator;
    private DrawerLayout _drawer;
    private HashMap<String, Object> list_menu = new HashMap<>();
    private String a = "";
    private String b = "";
    private String hadis_1 = "";
    private String hadis_2 = "";

    private ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> updateme = new ArrayList<>();

    private LinearLayout Main;
    private LinearLayout boxofimage;
    private RecyclerView recyclerview1;
    private TextView textview6;
    private TextView version;
    private ImageView imageview1;
    private ScrollView _drawer_ScrollView1;
    private LinearLayout _drawer_LinearLayout1;
    private LinearLayout _drawer_LinearLayout11;
    private LinearLayout _drawer_LinearLayout10;
    private LinearLayout _drawer_LinearLayout9;
    private LinearLayout _drawer_LinearLayout8;
    private LinearLayout _drawer_LinearLayout7;
    private LinearLayout _drawer_LinearLayout6;
    private LinearLayout _drawer_LinearLayout5;
    private LinearLayout _drawer_LinearLayout4;
    private LinearLayout _drawer_LinearLayout2;
    private LinearLayout _drawer_LinearLayout3;
    private LinearLayout _drawer_Share;
    private LinearLayout _drawer_Rate;
    private LinearLayout _drawer_privacypolicy;
    private LinearLayout _drawer_report;
    private LinearLayout _drawer_developer;

    private LinearLayout _drawer_admin;

    private LinearLayout _drawer_admin0;

    private LinearLayout _drawer_admin1;


    private LinearLayout _drawer_other0;

    private LinearLayout _drawer_other1;

    private LinearLayout _drawer_other2;
    private LinearLayout _drawer_update;
    private LinearLayout _drawer_settings;

    private LinearLayout _drawer_aboutus;
    private TextView _drawer_textview24;
    private ImageView _drawer_ImageView1;
    private ImageView _drawer_imageview13;
    private TextView _drawer_textview23;
    private ImageView _drawer_imageview12;
    private TextView _drawer_textview22;
    private ImageView _drawer_imageview11;
    private TextView _drawer_textview21;
    private ImageView _drawer_imageview14;
    private TextView _drawer_textview25;
    private ImageView _drawer_imageview15;
    private TextView _drawer_textview26;

    private AlertDialog.Builder exit;
    private Intent in = new Intent();
    private Intent setting = new Intent();
    private Intent me = new Intent();
    private Intent dev = new Intent();
    private Intent update = new Intent();

    private Intent intent_map = new Intent();
    private AlertDialog.Builder policy;

    private RequestNetwork send;
    private RequestNetwork.RequestListener _send_request_listener;
    private RequestNetwork hadis;
    private RequestNetwork.RequestListener _hadis_request_listener;
    private RequestNetwork updateNow;
    private RequestNetwork.RequestListener _updateNow_request_listener;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.main0);
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
        _drawer = findViewById(R.id._drawer);
        ActionBarDrawerToggle _toggle = new ActionBarDrawerToggle(Main0Activity.this, _drawer, _toolbar, R.string.app_name, R.string.app_name);
        _drawer.addDrawerListener(_toggle);
        _toggle.syncState();

        LinearLayout _nav_view = findViewById(R.id._nav_view);

        Main = findViewById(R.id.Main);
        boxofimage = findViewById(R.id.boxofimage);
        recyclerview1 = findViewById(R.id.recyclerview1);
        textview6 = findViewById(R.id.textview6);
        version = findViewById(R.id.version);
        imageview1 = findViewById(R.id.imageview1);
        _drawer_ScrollView1 = _nav_view.findViewById(R.id.ScrollView1);
        _drawer_LinearLayout1 = _nav_view.findViewById(R.id.LinearLayout1);
        _drawer_LinearLayout11 = _nav_view.findViewById(R.id.LinearLayout11);
        _drawer_LinearLayout10 = _nav_view.findViewById(R.id.LinearLayout10);
        _drawer_LinearLayout9 = _nav_view.findViewById(R.id.LinearLayout9);
        _drawer_LinearLayout8 = _nav_view.findViewById(R.id.LinearLayout8);
        _drawer_LinearLayout7 = _nav_view.findViewById(R.id.LinearLayout7);
        _drawer_LinearLayout6 = _nav_view.findViewById(R.id.LinearLayout6);
        _drawer_LinearLayout5 = _nav_view.findViewById(R.id.LinearLayout5);
        _drawer_LinearLayout4 = _nav_view.findViewById(R.id.LinearLayout4);
        _drawer_LinearLayout2 = _nav_view.findViewById(R.id.LinearLayout2);
        _drawer_LinearLayout3 = _nav_view.findViewById(R.id.LinearLayout3);

        _drawer_Share = _nav_view.findViewById(R.id.Share);
        _drawer_Rate = _nav_view.findViewById(R.id.Rate);
        _drawer_privacypolicy = _nav_view.findViewById(R.id.privacypolicy);
        _drawer_report = _nav_view.findViewById(R.id.report);
        _drawer_developer = _nav_view.findViewById(R.id.developer);

        _drawer_admin = _nav_view.findViewById(R.id.admin);
        _drawer_admin0 = _nav_view.findViewById(R.id.admin0);
        _drawer_admin1 = _nav_view.findViewById(R.id.admin1);

        _drawer_other0 = _nav_view.findViewById(R.id.other0);
        _drawer_other1 = _nav_view.findViewById(R.id.other1);
        _drawer_other2 = _nav_view.findViewById(R.id.other2);
        _drawer_aboutus = _nav_view.findViewById(R.id.aboutus);
        _drawer_update = _nav_view.findViewById(R.id.update);
        _drawer_settings = _nav_view.findViewById(R.id.settings);
        _drawer_textview24 = _nav_view.findViewById(R.id.textview24);
        _drawer_ImageView1 = _nav_view.findViewById(R.id.ImageView1);
        _drawer_imageview13 = _nav_view.findViewById(R.id.imageview13);
        _drawer_textview23 = _nav_view.findViewById(R.id.textview23);
        _drawer_imageview12 = _nav_view.findViewById(R.id.imageview12);
        _drawer_textview22 = _nav_view.findViewById(R.id.textview22);
        _drawer_imageview11 = _nav_view.findViewById(R.id.imageview11);
        _drawer_textview21 = _nav_view.findViewById(R.id.textview21);
        _drawer_imageview14 = _nav_view.findViewById(R.id.imageview14);
        _drawer_textview25 = _nav_view.findViewById(R.id.textview25);
        _drawer_imageview15 = _nav_view.findViewById(R.id.imageview15);
        _drawer_textview26 = _nav_view.findViewById(R.id.textview26);
        exit = new AlertDialog.Builder(this);
        policy = new AlertDialog.Builder(this);
        send = new RequestNetwork(this);
        hadis = new RequestNetwork(this);
        updateNow = new RequestNetwork(this);

        _send_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
                final String _tag = _param1;
                final String _response = _param2;
                final HashMap<String, Object> _responseHeaders = _param3;

            }

            @Override
            public void onErrorResponse(String _param1, String _param2) {
                final String _tag = _param1;
                final String _message = _param2;

            }
        };

        _hadis_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
                final String _tag = _param1;
                final String _response = _param2;
                final HashMap<String, Object> _responseHeaders = _param3;
                map = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                if (!Main0Activity.this.isFinishing()) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(Main0Activity.this).create();
                    View inflate = getLayoutInflater().inflate(R.layout.hadis, null);
                    dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog1.setView(inflate);
                    TextView now = (TextView) inflate.findViewById(R.id.now);

                    TextView version = (TextView) inflate.findViewById(R.id.version);

                    TextView whats_new = (TextView) inflate.findViewById(R.id.whats_new);

                    LinearLayout bg = (LinearLayout) inflate.findViewById(R.id.bg);
                    version.setText(map.get((int) 0).get("title").toString());
                    whats_new.setText(map.get((int) 0).get("hadis").toString());
                    _rippleRoundStroke(bg, "#FFFFFF", "#000000", 15, 0, "#000000");
                    _rippleRoundStroke(now, "#FF01837A", "#40FFFFFF", 15, 0, "#000000");

                    whats_new.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog1.hide();
                        }
                    });
                    bg.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog1.hide();
                        }
                    });
                    now.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog1.hide();
                        }
                    });
                    dialog1.setCancelable(true);
                    dialog1.show();
                }
            }

            @Override
            public void onErrorResponse(String _param1, String _param2) {
                final String _tag = _param1;
                final String _message = _param2;

            }
        };

        _updateNow_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
                final String _tag = _param1;
                final String _response = _param2;
                final HashMap<String, Object> _responseHeaders = _param3;
                updateme = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                if (Double.parseDouble(version.getText().toString()) < Double.parseDouble(updateme.get((int)0).get("version").toString())) {
                    if (!Main0Activity.this.isFinishing()) {
                        final AlertDialog dialog1 = new AlertDialog.Builder(Main0Activity.this).create();
                        View inflate = getLayoutInflater().inflate(R.layout.update, null);
                        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog1.setView(inflate);
                        TextView now = (TextView) inflate.findViewById(R.id.now);

                        TextView version = (TextView) inflate.findViewById(R.id.version);

                        TextView whats_new = (TextView) inflate.findViewById(R.id.whats_new);

                        LinearLayout bg = (LinearLayout) inflate.findViewById(R.id.bg);
                        version.setText(updateme.get((int) 0).get("title").toString());
                        whats_new.setText(updateme.get((int) 0).get("new").toString());
                        _rippleRoundStroke(bg, "#FFFFFF", "#000000", 15, 0, "#000000");
                        _rippleRoundStroke(now, "#FF9800", "#40FFFFFF", 15, 0, "#000000");
                        now.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                update.setAction(Intent.ACTION_VIEW);
                                update.setData(Uri.parse(updateme.get((int) 0).get("link").toString()));
                                startActivity(update);
                                dialog1.hide();
                                finish();
                            }
                        });
                        dialog1.setCancelable(false);
                        dialog1.show();
                    }
                } else {

                    }

            }


            @Override
            public void onErrorResponse(String _param1, String _param2) {
                final String _tag = _param1;
                final String _message = _param2;

            }
        };
        _drawer_developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                // Create an AlertDialog.Builder instance
                AlertDialog.Builder protarok = new AlertDialog.Builder(Main0Activity.this);
                protarok.setTitle("About me");
                protarok.setMessage("ইসলামী বিশ্বকোষ ও আল হাদিস S2, ইসলামী বিশ্বকোষ ও আল হাদিস, শহিদুল্লাহ বাহাদুর গ্রন্থ সমগ্র, হাফেজ মাওলানা ওসমান গণি গ্রন্থ সমগ্র, ইসলামী তাবলীগ, উসীলা-ইস্তিগাসা ও আহকামুল মাযার এপ্স সমূহের ডেভেলপার আব্দুল মুস্তফা রাহিম রিজওয়ান");

                // Positive button for Facebook ID
                protarok.setPositiveButton("Facebook ID", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent dev = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Abdulmustofarahimramjan0"));
                        startActivity(dev);
                    }
                });

                // Negative button for Facebook Page
                protarok.setNegativeButton("Facebook Page", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent dev = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=61570134066496"));
                        startActivity(dev);
                    }
                });

                // Neutral button for WhatsApp
                protarok.setNeutralButton("Whatsapp", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent me = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801714656343"));
                        startActivity(me);
                    }
                });

                // Show the dialog
                protarok.create().show();
            }
        });

        _drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                setting.setClass(getApplicationContext(), SettingsActivity.class);
                startActivity(setting);
            }
        });


        _drawer_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                // Save the name and author to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", "About us");
                editor.putString("author", "বিশেষ সতর্কীকরণঃ \n" +
                        "আসসালামু আলাইকুম। এই এপ্সটি করতে সাইবার টিমের সদস্যগণ বহুবছর যাবৎ কঠোর পরিশ্রম করেছেন। আমাদের একমাত্র উদ্দেশ্য হল- \"আল্লাহ ও তাঁর রাসূল (ﷺ) এর সন্তুষ্টি এবং দ্বীনের দাওয়াতে কাজ করা।\"\n" +
                        "আমাদের শত চেষ্টার পরও এই এপ্সের অনেক জায়গায় টাইপিং Mistake থাকতেই পারে। কারণ ৭৩০টি বই টাইপিং আকারে নির্ভুলভাবে করা কোন সহজ কথা নয়। তাই মারাত্মক কোন ভুল ধরা পরলে এডমিন বরাবর যোগাযোগ করবেন। এতে করে আমরা তা সংশোধন করার চেষ্টা করব। আর সাইবার টিমের অনিচ্ছাকৃত ভুলের জন্য কোন লেখক দায়ী থাকবেন না। অথবা লেখকের কোন ভুলের জন্য সাইবার টিম দায়ী থাকবে না। মানুষ মরণশীল। একদিন আমরাও থাকব না। ভুল-ত্রুটি গুলো আল্লাহ পাক ক্ষমা করুন আর এর সাথে সম্পৃক্ত সকলকে সদকায়ে জারিয়া কিয়ামত পর্যন্ত নসীব করুন। এর উসীলায় আল্লাহ পাক আমাদের নাজাতের ব্যবস্থা করে দিন। আমিন।\n" +
                        "\n" +
                        "সাইবার টিম\n" +
                        "____________________\n" +
                        "পরিচিতিঃ\n" +
                        " ➖➖➖➖➖➖\n" +
                        "\uD83C\uDF37এপ্সঃ ইসলামী বিশ্বকোষ ও আল হাদিস S2\n" +
                        "\uD83C\uDF00 এপ্স ডেভেলপারঃ রাতের ভ্রমণকারীনি ও আব্দুল মুস্তফা রাহিম রমজান (রিজওয়ান)।\n" +
                        "\uD83C\uDF00 এপ্স প্রকাশনায় সার্বিক তত্ত্বাবধানেঃ \n" +
                        "\uD83D\uDC49 ডা. মাসুম বিল্লাহ সানি ও সুমন মাহমুদ\n" +
                        "\uD83C\uDF00 পরিচালকঃ সাইবার টিম\n" +
                        "\n" +
                        "\uD83C\uDF00 সাইবার টিম এর সদস্যবৃন্দঃ\n" +
                        "➖➖➖➖➖➖\n" +
                        "▪ ডা. মাসুম বিল্লাহ সানি \n" +
                        "▪ সুমন মাহমুদ\n" +
                        "▪ মাহবুবুর রহমান শাওন \n" +
                        "▪ সিরাজুম মুনির তানভির\n" +
                        "▪ মাহমুদ হাসান\n" +
                        "▪ ফয়সাল আহমদ\n" +
                        "▪ আব্দুল মুস্তফা রাহিম রমজান (রিজওয়ান)\n" +
                        "▪ সরকার জিলানী\n" +
                        "▪ সৈয়দ মোস্তুফা সাকিব \n" +
                        "▪ মুহাম্মদ জাহিদুল বাশার\n" +
                        "▪ ফাতেমাতুজ জোহরা শাকিলা\n" +
                        "▪ কার্নিস কাদরিয়া\n" +
                        "▪ মুহাম্মদ আহসানুল হক\n" +
                        "▪ মুহাম্মাদ আবদুল কাদির মাহী\n" +
                        "▪ মোহাম্মদ মারুফ\n" +
                        "▪ মোহাম্মদ জোবায়েদ হোসেন আত্তারী\n" +
                        "▪ মোহাম্মদ সাকিব মাহমুদ আত্তারী\n" +
                        "▪ জাহিদুল বাশার\n" +
                        "▪ মিজানুর রহমান আত্তারী\n" +
                        "▪ আব্দুল্লাহ আল কাফি\n" +
                        "▪ আব্দুল ওহাব রিজভী\n" +
                        "▪ তানভীর বিন শাব্বির\n" +
                        "\n" +
                        "কৃতজ্ঞতা স্বীকারঃ\n" +
                        "\n" +
                        "▪ নাঈমা হক\n" +
                        "▪ সাদি মুক্তাদীর শিউর\n" +
                        "▪ মুহাম্মদ নাহিদ\n" +
                        "▪ মুহাম্মদ রায়হান\n" +
                        "\n" +
                        "সহ আরো অনেকে। আল্লাহ পাক সংশ্লিষ্ট  সকলকে কবুল করুন। আমিন।");
                editor.apply();

                // Launch ViewActivity to show the data saved in SharedPreferences
                Intent dev = new Intent(getApplicationContext(), ViewActivity.class);
                startActivity(dev);
            }
        });


        _drawer_Share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                _share1();
            }
        });

        _drawer_Rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                _rate1();
            }
        });

        _drawer_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin00 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/masumbillah2000 "));
                        startActivity(admin00);
            }
        });

        _drawer_admin0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin01 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/itsumon9"));
                startActivity(admin01);
            }
        });

        _drawer_admin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin02 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sirajum.munir.tanvir86"));
                startActivity(admin02);
            }
        });

        _drawer_other0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin01 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/itsumon9"));
                startActivity(admin01);
            }
        });
        _drawer_other1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin03 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/mahmud.hasan.ag"));
                startActivity(admin03);
            }
        });
        _drawer_other2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                Intent admin04 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/biz.s.islam"));
                startActivity(admin04);
            }
        });
        _drawer_privacypolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                policy.setTitle("Privacy Policy");
                policy.setMessage("Privacy Policy for ইসলামী বিশ্বকোষ ও আল হাদিস S2\n\nEffective Date: 14 March 2025\n\n\n---\n\n1. Introduction\n\nWelcome to ইসলামী বিশ্বকোষ ও আল হাদিস S2. We are committed to protecting your privacy and ensuring that your personal information is handled securely and responsibly.\n\n\n---\n\n2. Information Collection\n\nOur app does not directly collect personal information from users. However, we may gather non-personal information from your device, including but not limited to:\n\nDevice Information: Model, operating system version.\n\nUsage Data: How you interact with the app.\n\n\n\n---\n\n3. Use of Information\n\nThe information collected is used for the following purposes:\n\nTo provide and maintain our app.\n\nTo improve user experience.\n\nTo monitor app usage and trends.\n\n4. Cookies\n\nOur app does not use cookies or similar tracking technologies. However, third-party services, such as Google AdMob, may use cookies to collect data.\n\n\n---\n\n5. Data Security\n\nWe take the security of your information seriously. While we strive to protect your data, please note that no method of transmission over the internet or electronic storage is 100% secure.\n\n\n---\n\n6. Downloaded Content\n\nPDF Files: The app downloads PDF files and saves them in the public directory of your device under the folder:\nDownload/ইসলামী বিশ্বকোষ ফোল্ডার/পিডিএফ.\n\nThe app does not access or modify any other files on your device.\n\n\n\n---\n\n7. Changes to This Privacy Policy\n\nWe may update our Privacy Policy periodically. Any changes will be notified by posting the updated Privacy Policy in the app. You are encouraged to review this Privacy Policy periodically for updates.\n\n\n---\n\n8. Contact Us\n\nIf you have any questions or concerns about this Privacy Policy, please contact us at:\n\nEmail: muhammodrizwan01@gmail.com");
                policy.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface _dialog, int _which) {

                    }
                });
                policy.create().show();
            }
        });

        _drawer_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                // কাস্টম ডায়ালগ তৈরি
                AlertDialog.Builder builder = new AlertDialog.Builder(Main0Activity.this);

                // LayoutInflater ব্যবহার করে কাস্টম লেআউট যোগ করা
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_report, null);
                builder.setView(dialogView);

                final EditText editText = dialogView.findViewById(R.id.editText);
                Button sendButton = dialogView.findViewById(R.id.sendButton);
                Button emailButton = dialogView.findViewById(R.id.emailButton);
                Button whatsapp = dialogView.findViewById(R.id.whatsapp);
                // ডায়ালগ তৈরি করার আগে
                final AlertDialog dialog = builder.create();

                // Send বাটনের জন্য ক্লিক লিসনার
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message = editText.getText().toString();
                        if (!message.isEmpty()) {
                            String url = "https://api.telegram.org/bot8513295796:AAEfeaGo-O29kSk-4zCxcUiB3eU-GRPbtGw/sendMessage?chat_id=7619923490&text=" + message;
                            send.startRequestNetwork(RequestNetworkController.POST, url, "Rizwan", _send_request_listener);
                            dialog.dismiss(); // ডায়ালগ বন্ধ করুন
                            Toast.makeText(getApplicationContext(),"মেসেজ পাঠানো হয়েছে", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Main0Activity.this, "দয়া করে কিছু লিখুন", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                whatsapp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        me.setAction(Intent.ACTION_VIEW);
                        me.setData(Uri.parse("https://wa.me/8801714656343"));
                        startActivity(me);
                    }
                });
                // Gmail বাটনের জন্য ক্লিক লিসনার
                emailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"muhammodrizwan01@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Issue or Suggestion");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "বইয়ের নাম বা অ্যাপের সমস্যা লিখুন...");

                        try {
                            startActivity(Intent.createChooser(emailIntent, "Select Email App"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(Main0Activity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss(); // ডায়ালগ বন্ধ করুন
                    }
                });

                // ডায়ালগ শো করুন
                dialog.show();
            }
        });


        _drawer_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                _rate1();
            }
        });
    }

    private void initializeLogic() {
        setTitle("ইসলামী বিশ্বকোষ ও আল হাদিস S2");
        _status_bar_color("#FF01837A", "#FF01837A");
        _contentlist("আল কুরআন", "quranp");
        _contentlist("তাফসীর সমগ্র", "tafsir");
        _contentlist("হাদিস সমগ্র", "picture_11");
        _contentlist("ইসলামী বিশ্বকোষ", "blog");
        _contentlist("ইসলামী বই", "picture_10");
        //_contentlist("ইসলামী বই ২", "picture_10");
        _contentlist("লেখকভিত্তিক বই", "author");
        _contentlist("বিষয়ভিত্তিক বই", "subject");
        _contentlist("অনলাইন বই ১", "picture_10");
        _contentlist("অনলাইন বই ২", "notebook");
        _contentlist("আসমাউল হুসনা", "names_99");
        _contentlist("আসমাউল নভবী", "sunnah0");
        _contentlist("নাতে মুস্তফা (ﷺ)", "sunnah0");
        _contentlist("নামাযের সময়", "salat");
        _contentlist("সেহরি ও ইফতার", "ic_1_1");
        _contentlist("মাসনুন দু'আ", "picture_14");
        _contentlist("তাসবিহ", "picture_16");
        _contentlist("প্রবন্ধ", "article");
        _contentlist("অনলাইন প্রবন্ধ", "blog");
        _contentlist("বয়ান", "boyan");
        //_contentlist("কুরআন শিক্ষা", "learn_quran");
        _contentlist("নামায শিক্ষা", "namaz_shikkha");
        _contentlist("রোযা", "ramadan");
        _contentlist("যাকাত", "zakat");
        _contentlist("হজ্জ ও উমরা", "hajj");
        _contentlist("সুন্নাহ", "sunnah");
        _contentlist("নির্বাচিত পোষ্ট", "article1");
        _contentlist("মাসায়েল", "interview");
        _contentlist("ইসলামিক নাম", "islamic_name");
        _contentlist("লাইভ", "islamic_live");
        _contentlist("মসজিদ খুঁজি", "mosque");
        _contentlist("ক্বিবলা কম্পাস", "qibla");
        //_contentlist("গুরুত্বপূর্ণ দিন", "important_days");
        _contentlist("সাপোর্ট করুন", "icon_report_round");
        
        //_contentlist("মজার তথ্য", "noti_icon");
        //_contentlist("ইসলামীক এপ্স", "noti_icon");
        _contentlist("অভিযোগ", "noti_icon");
		_contentlist("রেটিং অ্যাপ্স", "icon_star");
        //_contentlist("কমিউনিটি", "noti_icon");
		//_contentlist("দান করুন", "donate");
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch (Exception e) {
            version.setText("Version: Unknown");
        }
        if (Rizwan.isConnected(getApplicationContext())) {
            hadis.startRequestNetwork(RequestNetworkController.GET, "https://www.dropbox.com/scl/fi/yvaikq5ttzupwjgvpg3la/hadismessage.json?rlkey=l8fqf5mz5ddkn0nz4hj8abttu&st=fpc5qi17&dl=1", "", _hadis_request_listener);
            updateNow.startRequestNetwork(RequestNetworkController.GET, "https://www.dropbox.com/scl/fi/grio1blrr5pyin3t941e2/update.json?rlkey=rlwggtmopb5xh81l4b1pydzok&st=kjjsotc4&dl=1", "", _updateNow_request_listener);
        } else {

        }



        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (_drawer.isDrawerOpen(GravityCompat.START)) {
                    _drawer.closeDrawer(GravityCompat.START);
                } else {

// AlertDialog.Builder ব্যবহার করে ডায়ালগ তৈরি
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main0Activity.this);

                    // LayoutInflater ব্যবহার করে কাস্টম লেআউট যোগ করা
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_exit, null);
                    builder.setView(dialogView);

                    // CardView, Button ইত্যাদি সেট করা
                    CardView cardView = dialogView.findViewById(R.id.cardView);
                    Button rateButton = dialogView.findViewById(R.id.rate1);
                    Button shareButton = dialogView.findViewById(R.id.share1);
                    Button finishButton = dialogView.findViewById(R.id.finishaffinity);

                    // রেটিং বাটনের জন্য ক্লিক লিসনার
                    rateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // রেটিং দেওয়ার জন্য প্লে স্টোরে নিয়ে যাওয়ার কোড
                            _rate1();
                        }
                    });

                    // শেয়ার বাটনের জন্য ক্লিক লিসনার
                    shareButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _share1();

                        }
                    });

                    // হ্যাঁ বাটনের জন্য ক্লিক লিসনার
                    finishButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // অ্যাপ বন্ধ করার কোড
                            finishAffinity();  // অ্যাপ্লিকেশন বন্ধ করে দিবে
                        }
                    });

                    // ডায়ালগ শো করা
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    public void _status_bar_color(final String _colour1, final String _colour2) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window w = this.getWindow(); w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(Color.parseColor(_colour1)); w.setNavigationBarColor(Color.parseColor(_colour2));
        }
    }


    public void _contentlist(final String _add, final String _add1) {
        list_menu = new HashMap<>();
        list_menu.put("1", _add);
        list_menu.put("2", _add1);
        list.add(list_menu);
        recyclerview1.setAdapter(new Recyclerview1Adapter(list));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerview1.setLayoutManager(gridLayoutManager);
    }


    public void _exit1() {
        exit.setTitle("আপনি কি বের হতে চান?");
        exit.setPositiveButton("হ্যাঁ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface _dialog, int _which) {
                finishAffinity();
            }
        });
        exit.setNegativeButton("না", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface _dialog, int _which) {

            }
        });
        exit.create().show();
    }


    public void _share1() {
        if (Rizwan.isConnected(getApplicationContext())) {
            try {
                a = "এপ্সটি শেয়ার করুন";
                b = updateme.get((int) 0).get("new").toString().concat("\n".concat(updateme.get((int) 0).get("link").toString()));
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT, a);
                i.putExtra(android.content.Intent.EXTRA_TEXT, b);
                startActivity(Intent.createChooser(i, "এপ্সটি শেয়ার করুন"));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "লিংক আপডেট হয়নি অপেক্ষা করুন", Toast.LENGTH_SHORT).show();
            }
        } else {
            a = "এপ্সটি শেয়ার করুন";
            b = "আসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 তে আল কুরআন, আল হাদিস ও ৪০০+ কিতাব রয়েছে। নিজে ইনষ্টল করুন ও সদকায়ে জারিয়া নিয়্যতে শেয়ার করুন।\nhttps://play.google.com/store/apps/details?id=com.srizwan.islamipedia\nযাজাকাল্লাহু খাইরান।।";
            Intent i = new Intent(android.content.Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(android.content.Intent.EXTRA_SUBJECT, a); i.putExtra(android.content.Intent.EXTRA_TEXT, b); startActivity(Intent.createChooser(i,"এপ্সটি শেয়ার করুন"));
        }
    }


    public void _rate1() {
        if (Rizwan.isConnected(getApplicationContext())) {
            try {
                update.setAction(Intent.ACTION_VIEW);
                update.setData(Uri.parse(updateme.get((int) 0).get("link").toString()));
                startActivity(update);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "লিংক আপডেট হয়নি একটু অপেক্ষা করুন", Toast.LENGTH_SHORT).show();
            }
        } else {
            update.setAction(Intent.ACTION_VIEW);
            update.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"));
            startActivity(update);
        }
    }


    public void _rippleRoundStroke(final View _view, final String _focus, final String _pressed, final double _round, final double _stroke, final String _strokeclr) {
        android.graphics.drawable.GradientDrawable GG = new android.graphics.drawable.GradientDrawable();
        GG.setColor(Color.parseColor(_focus));
        GG.setCornerRadius((float)_round);
        GG.setStroke((int) _stroke,
                Color.parseColor("#" + _strokeclr.replace("#", "")));
        android.graphics.drawable.RippleDrawable RE = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor(_pressed)}), GG, null);
        _view.setBackground(RE);
    }


    public void _hadis_0(final String _childKey, final HashMap<String, Object> _childValue) {

    }

    public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {

        ArrayList<HashMap<String, Object>> _data;

        public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater _inflater = getLayoutInflater();
            View _v = _inflater.inflate(R.layout.listiteam, null);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            _v.setLayoutParams(_lp);
            return new ViewHolder(_v);
        }

        @Override
        public void onBindViewHolder(ViewHolder _holder, final int _position) {
            View _view = _holder.itemView;

            final LinearLayout book1 = _view.findViewById(R.id.book1);
            final ImageView icon = _view.findViewById(R.id.icon);
            final TextView name = _view.findViewById(R.id.name);

            name.setText(list.get((int)_position).get("1").toString());
            icon.setImageResource(getResources().getIdentifier(list.get((int)_position).get("2").toString(), "drawable", getPackageName()));
            book1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    if (name.getText().toString().equals("আল কুরআন")) {
                        in.setClass(getApplicationContext(), QuranActivity.class);
                        in.putExtra("sub", "আল কুরআন");
                        in.putExtra("booklist", "sura.json");
                        startActivity(in);
                    }
                    else {
                        if (name.getText().toString().equals("হাদিস সমগ্র")) {
    in.setClass(getApplicationContext(), HadithMeActivity.class);
    startActivity(in);
} else {
                            if (name.getText().toString().equals("ইসলামী বই")) {
                                in.setClass(getApplicationContext(), Main4Activity.class);
                                in.putExtra("sub", "ইসলামী বই সমাহার");
                                in.putExtra("get", "");
                                in.putExtra("booklist", "file.json");
                                startActivity(in);
                            }
                            else {
                                if (name.getText().toString().equals("ইসলামী বই ২")) {
                                    in.setClass(getApplicationContext(), Main4Activity.class);
                                    in.putExtra("sub", "মুফতী আলাউদ্দিন জেহাদী গ্রন্থ সমগ্র");
                                    in.putExtra("get", "");
                                    in.putExtra("booklist", "book.json");
                                    startActivity(in);
                                }
                                else {
                                    if (name.getText().toString().equals("অনলাইন বই ১")) {
                                            in.setClass(getApplicationContext(), Pdfonline14Activity.class);
                                            startActivity(in);
                                    }
                                    else {
                                        if (name.getText().toString().equals("অনলাইন বই ২")) {
                                            in.setClass(getApplicationContext(), BookonlineActivity.class);
                                            startActivity(in);
                                        }
                                        else {
                                            if (name.getText().toString().equals("মাসনুন দু'আ")) {
                                                // Create an AlertDialog.Builder instance
                                                                                                    AlertDialog.Builder duame = new AlertDialog.Builder(Main0Activity.this);
                                                                                                    duame.setMessage("নির্বাচন করুন");

                                                                                                    // Positive button for Facebook ID
                                                                                                    duame.setPositiveButton("অনলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                            in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                            in.putExtra("url", "https://islamipedia.vercel.app/dua/");
                                                                                                            startActivity(in);
                                                                                                        }
                                                                                                    });

                                                                                                    // Negative button for Facebook Page
                                                                                                    duame.setNegativeButton("অফলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                                                                                                                                                                                                in.setClass(getApplicationContext(), DuaActivity.class);
                                                startActivity(in);
                                                                                                        }
                                                                                                    });                                                                                                duame.create().show();
                                            }
                                            else {
                                                if (name.getText().toString().equals("তাসবিহ")) {
                                                    in.setClass(getApplicationContext(), TasbihActivity.class);
                                                    startActivity(in);
                                                }
                                                else {
                                                    if (name.getText().toString().equals("মজার তথ্য")) {
                                                        in.setClass(getApplicationContext(), ViewActivity.class);
                                                        in.putExtra("name", "মজার তথ্য");
                                                        in.putExtra("author", "মজার তথ্যঃ\n১। আগে এই এপ্সে কেন এড যুক্ত করা হয়েছে?\nএপ্সটা একজন সম্মানীত ব্যক্তির পুরাতন প্লে কনসোলে পাবলিশ করা হয়েছে। অথচ একটা পুরাতন একাউন্টে এপ্স পাবলিশ করতে ৪/৫ হাজার+ টাকা লাগে আর প্রতি সপ্তাহ ভাড়া দেওয়া লাগে ৩/৪ হাজার টাকা+ বিশ্বাস না হলে রিসার্চ করুন তথ্য পেয়ে যাবেন অনেকে এর থেকেও বেশী বা কম রাখে এটা যার যেমন ইচ্ছে। ইনশাআল্লাহ নিজস্ব প্লে কনসোল একাউন্ট নিয়ে এপ্স থেকে সকল ধরনের বিজ্ঞাপন সরিয়ে ফেলা হবে। এপ্সে থাকা সকল কন্টেন্ট কপিরাইট মুক্ত যত ইচ্ছে প্রচার করবেন/এপ্স ডেভেলপ করে সদকায়ে জারিয়ার নিয়্যতে প্রচার করবেন । \n\n* নতুন একাউন্ট আর পুরাতন একাউন্টে পার্থক্য কোথায়? নতুন হলেই তো ভালো ফ্রেশ?\nপার্থক্য হলো নতুন একাউন্টে এপ্স পাবলিশ করতে এখন ১৪ দিন ক্লোজ টেষ্টিং করা লাগে ২০ জন ব্যবহারকারীকে দিয়ে ঠিকমতন ক্লোজ টেষ্টিং না হলে এপ্স পাবলিশ হয় না। অনেক ক্ষেত্রে একাউন্ট ও টারমিনেট হয়ে যাই।\n২। কেন ইসলামী বিশ্বকোষ এপ্সের ডেভেলপার মুছে গেলো?\nযখন ইসলামী বিশ্বকোষ এর ডেভেলপার এপ্স ডেভেলপ করা শুরু করল ইসলামী বিশ্বকোষ হয়ে উনি ১ টাকাও হাদিয়া নেননি। ইসলামী বিশ্বকোষ ও আল হাদিস পুরাতনটা যখন পাবলিশ হল একজন খুশি হয়ে উনাকে ১ হাজার টাকা দিয়েছিলো এটাই ছিলো তার হাদিয়া। ইসলামী বিশ্বকোষ ও আল হাদিস ডেভেলপ করে ডেভেলপার অনেক বড় ভুল করেছে? কি ভুল করেছে ডেভেলপার নাকি ডোনেট আর এড নামে অপশন নিজে যুক্ত করেছে যার কারনে এপ্সটা প্লে ষ্টোর থেকে নাকি রিজেক্ট করে দিয়েছে এটা বলেছে সম্মানীত ইসলামী বিশ্বকোষ এর ব্যবসায়ী গন। অথচ এগুলোর কারনে রিজেক্ট করা হয়নি করা হয়েছে Webview and Affiliate Marketing policy : গুগল প্লে কনসোলের নীতিমালা অনুযায়ী যদি কোন এপ্সে ওয়েবসাইট শো করানো হয় তাহলে সেটার জন্যেই তাদেরকে প্রুফ দেখাতে হয় যে ওয়েবসাইটটি আমাদের। এই বিষয়টা ব্যবসায়ীরা সমাধান করতে পারে নাই অথচ যিনি একাউন্ট ক্রয় করেছিলেন সে সমাধান করে ইসলামী বিশ্বকোষ ও আল হাদিস পুরাতন এপ্সটি ফেরত আনেন।\n\n৩। কেন ব্যবসায়ী?\nতারা ডেভেলপারকে দিয়ে ফ্রীতে এপ্স ডেভেলপ করে সেই পুরাতন প্লে কনসোলে একাউন্ট বিক্রি করেই দেই (৪ লাখ ২০ হাজার মার্কেট প্রাইজ তাদের বক্তব্য ৪২/৫০ হাজার) আর এই বিষয়ে কোন ধরনের পোষ্ট বা নোটিশ দেইনি উলটো ডেভেলপারকে বলা হয় কাউকে জানাতে না। তারা কিন্তু খাই না খাই না আবার বড় কুপ মারছে একাউন্ট বিক্রি করে।\n\n৪। ডেভেলপার কি ডোনেট আর এড অপশন নিজের বাড়ীঘর করার জন্যেই যুক্ত করেছিলো?\nনা। ইসলামী বিশ্বকোষ এর যে ব্যবসায়ীরা বলেছিলো। তাই যুক্ত করা হয়েছিলো এব্যাপারে তারায় বলেছে তাদের কেন দান ভিক্ষা দিতে হবে।\n\n৫। ডেভেলপার কেন মুছে গেল?\nযখন সেই পুরাতন প্লে কনসোল একাউন্টটি বিক্রি করে দিলো আর যিনি এই একাউন্টটি ক্রয় করেছেন সে ইসলামী বিশ্বকোষ ও আল হাদিস এপ্স (পুরাতন) ফেরত আনে। প্রতিদিন ডেভেলপার প্লে ষ্টোরে সার্চ দিত ইসলামী বিশ্বকোষ ও আল হাদিস, ইসলামী তাবলীগ, শহিদুল্লাহ বাহাদুর গ্রন্থ সমগ্র, হাফেজ মাওলানা ওসমান গনি গ্রন্থ সমগ্র, উসীলা-ইস্তিগাসা ও আহকামুল মাযার। আগে ৪ টা এপ্স ডেভেলপার এর সার্চ দিলেই আসতো আর সে নিজের আত্মাকে শান্তি দিত যে ইসলামের জন্যেই কিছু করতে পেরেছি। এরপরে যখন সে দেখলো যারা একাউন্ট ক্রয় করেছিলো তারা পুরাতন এপ্সটি ফেরত আনে উনি সেই ইসলামী বিশ্বকোষ ব্যবসায়ীদের জানাই আর বাকী তারা কি করে তারা ওই লোককে বলে এপ্সটি যেন সরিয়ে ফেলে। যাই হোক এরপরে যখন দেখা গেলো এপ্সটি সহ উপরের এপ্স গুলোও নাই। ইসলামী বিশ্বকোষ এর ডেভেলপার মুছে গেলো।\n\n৬। তাদেরকে এপ্স ডেভেলপ করে দিব আর তারা সে একাউন্ট বিক্রি করে দিবে। সে অনুযায়ী আর তাদের সাথে এপ্সের কোন বিষয়ে আমি যুক্ত থাকবো না। এই এপ্সের পিছনে সময় দিয়ে আব্বা আম্মা থেকে তেজ্জোসন্তান পর্যন্ত হয়েছি। আর কি বাকী আছে?\n\nঅনেক কিছুই কইতে মন চাইতাছিলো আজ আর নইঃ\nইতি আপনাদের ডেভেলপার পক্ষ থেকে\nডেভেলপার প্রতারক, ভন্ড। ডেভেলপারকে ভুলে যাবেন। (তারা কিন্তু তাকে অলী আউলিয়া বলেও প্রশংসা করেছিলো কি কইতাম এত সস্তা সব)\nআর ইসলামী বিশ্বকোষ এর ব্যবসায়ীরা থানায় কেছ করেন মাথা ঠান্ডা করেন। (হাসির ইমুজি)");
                                                        startActivity(in);
                                                    }
                                                    else {

                                                    }
                                                    if (name.getText().toString().equals("নামাযের সময়")) {
                                                        in.setClass(getApplicationContext(), MainActivity2.class);
                                                        startActivity(in);
                                                    }
                                                    else {

                                                    }
                                                    if (name.getText().toString().equals("পরামর্শ লিখুন")) {
                                                        // কাস্টম ডায়ালগ তৈরি
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Main0Activity.this);

                                                        // LayoutInflater ব্যবহার করে কাস্টম লেআউট যোগ করা
                                                        LayoutInflater inflater = getLayoutInflater();
                                                        View dialogView = inflater.inflate(R.layout.dialog_report, null);
                                                        builder.setView(dialogView);

                                                        final EditText editText = dialogView.findViewById(R.id.editText);
                                                        Button sendButton = dialogView.findViewById(R.id.sendButton);
                                                        Button emailButton = dialogView.findViewById(R.id.emailButton);
                                                        Button whatsapp = dialogView.findViewById(R.id.whatsapp);
                                                        // ডায়ালগ তৈরি করার আগে
                                                        final AlertDialog dialog = builder.create();

                                                        // Send বাটনের জন্য ক্লিক লিসনার
                                                        sendButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                String message = editText.getText().toString();
                                                                if (!message.isEmpty()) {
                                                                    String url = "https://api.telegram.org/bot8513295796:AAEfeaGo-O29kSk-4zCxcUiB3eU-GRPbtGw/sendMessage?chat_id=7619923490&text=" + message;
                                                                    send.startRequestNetwork(RequestNetworkController.POST, url, "Rizwan", _send_request_listener);
                                                                    dialog.dismiss(); // ডায়ালগ বন্ধ করুন
                                                                    Toast.makeText(getApplicationContext(),"মেসেজ পাঠানো হয়েছে", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(Main0Activity.this, "দয়া করে কিছু লিখুন", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        whatsapp.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                me.setAction(Intent.ACTION_VIEW);
                                                                me.setData(Uri.parse("https://wa.me/8801714656343"));
                                                                startActivity(me);
                                                            }
                                                        });
                                                        // Gmail বাটনের জন্য ক্লিক লিসনার
                                                        emailButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                                                emailIntent.setType("message/rfc822");
                                                                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"muhammodrizwan01@gmail.com"});
                                                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Issue or Suggestion");
                                                                emailIntent.putExtra(Intent.EXTRA_TEXT, "বইয়ের নাম বা অ্যাপের সমস্যা লিখুন...");

                                                                try {
                                                                    startActivity(Intent.createChooser(emailIntent, "Select Email App"));
                                                                } catch (android.content.ActivityNotFoundException ex) {
                                                                    Toast.makeText(Main0Activity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                                                                }
                                                                dialog.dismiss(); // ডায়ালগ বন্ধ করুন
                                                            }
                                                        });

                                                        // ডায়ালগ শো করুন
                                                        dialog.show();
                                                    }
                                                    else {

                                                    }
                                                    if (name.getText().toString().equals("রেটিং অ্যাপ্স")) {
                                                        _rate1();
                                                    }
                                                    else {
                                                        if (name.getText().toString().equals("তাফসীর সমগ্র")) {
                                                            in.setClass(getApplicationContext(), TafsirOnlineMeActivity.class);
                                                            startActivity(in);
                                                        }
                                                        else {
                                                            if (name.getText().toString().equals("ইসলামীক এপ্স")) {
                                                                in.setClass(getApplicationContext(), ViewActivity.class);
                                                                in.putExtra("name", "ইসলামীক এপ্স");
                                                                in.putExtra("author", "<h1>আসসালামু আলাইকুম</h1>\n    <p>আমার ডেভেলপ করা ইসলামীক অ্যাপস সমূহ:</p>\n\n    <div class=\"app\">\n        <h2>১. ইসলামী বিশ্বকোষ ও আল হাদিস S2</h2>\n        <p>একটি সম্পূর্ণ অফলাইন অ্যাপ, যা আল কুরআন (কানযুল ঈমান ও তাফসিরে খাযাইনুল ইরফান), সহিহ হাদিস এবং ৩৪৫টিরও বেশি ইসলামী বইয়ের সংগ্রহ দিয়ে সমৃদ্ধ।</p>\n        <ul>\n            <li>নামাজের সময়সূচি</li>\n            <li>মাসনুন দুআ</li>\n            <li>তাসবিহ কাউন্টার</li>\n        </ul>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.srizwan.islamipedia\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>২. ইসলামী বিশ্বকোষ ও আল হাদিস</h2>\n        <p>(সম্মানী: বিনামূল্যে, উদ্দেশ্য: সদকায়ে জারিয়া)</p>\n        <ul>\n            <li>২৫০ বই (অফলাইন)</li>\n            <li>৫০০০ বই (অনলাইন)</li>\n            <li>৬৬০০ পোস্ট</li>\n            <li>৭০০ ভিডিও</li>\n            <li>৮০০ ছবি</li>\n            <li>২৫০০ মাস'আলা</li>\n        </ul>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.islamboi.rizwan\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/com.islamboi.rizwan\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>৩. শহিদুল্লাহ বাহাদুর গ্রন্থ সমগ্র</h2>\n        <p>(সম্মানী: বিনামূল্যে, উদ্দেশ্য: সদকায়ে জারিয়া)</p>\n        <p>মাওলানা মুহাম্মদ শহীদুল্লাহ বাহাদুর এর রচিত ও সম্পাদিত সমগ্র গ্রন্থসমূহ।</p>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.sohidullahbahadur.abdulrahim\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/com.sohidullahbahadur.abdulrahim\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>৪. হাফেজ মাওলানা ওসমান গনি গ্রন্থ সমগ্র</h2>\n        <p>(সম্মানী: বিনামূল্যে, উদ্দেশ্য: সদকায়ে জারিয়া)</p>\n        <p>হাফেজ মাওলানা ওসমান গনি রচিত ও সম্পাদিত ৩টি কিতাব।</p>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=sunniencyclopedia.blogspot.com.usmangoniall\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/sunniencyclopedia.blogspot.com.usmangoniall\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>৫. ইসলামী তাবলীগ</h2>\n        <p>(সম্মানী: বিনামূল্যে, উদ্দেশ্য: সদকায়ে জারিয়া)</p>\n        <p>ইসলামের সঠিক আকিদা ও তাবলিগ জামাতের বাতিল আকিদা তুলে ধরা হয়েছে।</p>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.ahsan.rizwan\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/com.ahsan.rizwan\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>৬. উসীলা-ইস্তিগাসা ও আহকামুল মাযার</h2>\n        <p>(সম্মানী: বিনামূল্যে, উদ্দেশ্য: সদকায়ে জারিয়া)</p>\n        <p>কুরআন সুন্নাহ থেকে উসীলা, ইস্তিগাসা ও মাযার এর বিধান বর্ণিত হয়েছে।</p>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.sunnienclypedia.osila\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/com.sunnienclypedia.osila\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <div class=\"app\">\n        <h2>৭. মুফতী আলাউদ্দিন জেহাদী গ্রন্থ সমগ্র</h2>\n        <p>মুফতী আলাউদ্দিন জেহাদী সাহেবের ১৪টি মহামূল্যবান কিতাব।</p>\n        <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.srizwan.bookofhozur140\" target=\"_blank\">Download</a></p>\n <p><strong>প্লে ষ্টোর লিংক:</strong> <a href=\"https://play.google.com/store/apps/details?id=com.srizwan.islamipedia\" target=\"_blank\">Download</a></p>\n        <p><strong>Apk Pure:</strong> <a href=\"https://apkpure.com/p/com.srizwan.book\" target=\"_blank\">Download</a></p>\n    </div>\n\n    <p>যেমন ভাবে আমার সদকায়ে জারিয়া গুলো বিক্রি করা হয়েছে, তেমনি ভাবে আমি আমার সদকায়ে জারিয়াকে আবার নতুন ভাবে ফিরে এনেছি।</p>\n");
                                                                startActivity(in);
                                                            } else {
                                                                if (name.getText().toString().equals("নামায শিক্ষা")) {
                                                                    in.setClass(getApplicationContext(), Main4Activity.class);
                                                                    in.putExtra("sub", "নামায শিক্ষা");
                                                                    in.putExtra("get", "নামায");
                                                                    in.putExtra("booklist", "file.json");
                                                                    startActivity(in);
                                                                }
                                                                else {
                                                                    if (name.getText().toString().equals("রোযা")) {
                                                                        in.setClass(getApplicationContext(), Main4Activity.class);
                                                                        in.putExtra("sub", "রোযা");
                                                                        in.putExtra("get", "রোযা");
                                                                        in.putExtra("booklist", "file.json");
                                                                        startActivity(in);
                                                                    } else {
                                                                        if (name.getText().toString().equals("যাকাত")) {
                                                                            in.setClass(getApplicationContext(), Main4Activity.class);
                                                                            in.putExtra("sub", "যাকাত");
                                                                            in.putExtra("get", "যাকাত");
                                                                            in.putExtra("booklist", "file.json");
                                                                            startActivity(in);
                                                                        }
                                                                        else {
                                                                            if (name.getText().toString().equals("হজ্জ ও উমরা")) {
                                                                                in.setClass(getApplicationContext(), Main4Activity.class);
                                                                                in.putExtra("sub", "হজ্জ ও উমরা");
                                                                                in.putExtra("get", "হজ্জ");
                                                                                in.putExtra("booklist", "file.json");
                                                                                startActivity(in);
                                                                            } else {
                                                                                if (name.getText().toString().equals("আসমাউল হুসনা")) {
                                                                                    in.setClass(getApplicationContext(), ReadingActivity.class);
                                                                                    in.putExtra("name", "আসমাউল হুসনা");
                                                                                    in.putExtra("author", "সংকলক : আব্দুল মুস্তফা রাহিম রমজান");
                                                                                    in.putExtra("bookname", "asmaulhusna");
                                                                                    startActivity(in);
                                                                                }
                                                                                else {
                                                                                    if (name.getText().toString().equals("আসমাউল নভবী")) {
                                                                                        in.setClass(getApplicationContext(), ReadingActivity.class);
                                                                                        in.putExtra("name", "আসমাউল নভবী");
                                                                                        in.putExtra("author", "সংকলক : আব্দুল মুস্তফা রাহিম রমজান");
                                                                                        in.putExtra("bookname", "asmaulnobovi");
                                                                                        startActivity(in);
                                                                                    } else {
                                                                                        if (name.getText().toString().equals("সুন্নাহ")) {
                                                                                            in.setClass(getApplicationContext(), ReadingActivity.class);
                                                                                            in.putExtra("name", "সুন্নাহ");
                                                                                            in.putExtra("author", "সংকলক : আব্দুল মুস্তফা রাহিম রমজান");
                                                                                            in.putExtra("bookname", "sunnah");
                                                                                            startActivity(in);
                                                                                        } else {
                                                                                            if (name.getText().toString().equals("নাতে মুস্তফা (ﷺ)")) {
                                                                                                in.setClass(getApplicationContext(), Main4Activity.class);
                                                                                                in.putExtra("sub", "নাতে মুস্তফা (ﷺ)");
                                                                                                in.putExtra("get", "নাতে মুস্তফা");
                                                                                                in.putExtra("booklist", "file.json");
                                                                                                startActivity(in);
                                                                                            } else {
                                                                                                if (name.getText().toString().equals("লেখকভিত্তিক বই")) {
                                                                                                    // Create an AlertDialog.Builder instance
                                                                                                    AlertDialog.Builder writer = new AlertDialog.Builder(Main0Activity.this);
                                                                                                    writer.setMessage("নির্বাচন করুন");

                                                                                                    // Positive button for Facebook ID
                                                                                                    writer.setPositiveButton("অনলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                            in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                            in.putExtra("url", "https://www.sunni-encyclopedia.com/p/pdf-founded-by-masum-billah-sunny-click.html?m=1&_gl=1*2760ff*_ga*MjgzMjg2MjI4LjE3Mzg3MjM2MjQ.*_ga_1PRTC57BE2*MTczOTA0MjM5Ni40LjEuMTczOTA0ODM4NS4yLjAuMA..");
                                                                                                            startActivity(in);
                                                                                                        }
                                                                                                    });

                                                                                                    // Negative button for Facebook Page
                                                                                                    writer.setNegativeButton("অফলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                            in.setClass(getApplicationContext(), GetallActivity.class);
                                                                                                            in.putExtra("heading", "সকল লেখক");
                                                                                                            in.putExtra("get", "[\n  {\n    \"get\": \"ইমামে আজম ইমাম নু'মান বিন সাবিত (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম আবূ বকর আহমদ খোরাসানী বায়হাক্বী ইবনে হোসাঈন ইবনে আলী ইবনে মূসা (رحمة الله)\"\n  },\n  {\n    \"get\": \"হযরত সায়্যিদুনা ইমাম আবু কাসিম সুলাইমান বিন আহমদ তাবারানী (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম হিন্দী (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম ইবনে আবিদ দুনইয়া (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম শারানী(رحمة الله)\"\n  },\n  {\n    \"get\": \"শাহ নেয়ামাতুল্লাহ (رحمة الله)\"\n  },\n  {\n    \"get\": \"নুয়াইম বিন হাম্মাদ(رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম হাকীম(رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম জালাল উদ্দীন সুয়ূতী (رحمة الله)\"\n  },\n  {\n    \"get\": \"শায়েখ আব্দুল হক মোহাদ্দেছে দেহলভী (رحمة الله)\"\n  },\n  {\n    \"get\": \"আ'লা হযরত ইমাম আহমদ রেযা খান বেরলভী (رحمة الله)\"\n  },\n  {\n    \"get\": \"হাকিমুল উম্মাহ মুফতি ইয়ার খান নঈমী (رحمة الله)\"\n  },\n  {\n    \"get\": \"আল্লামা আবুল হামেদ মুহাম্মদ জিয়াউল্লাহ কাদেরী আশরাফী (رحمة الله)\"\n  },\n  {\n    \"get\": \"ঈমাম নূরুদ্দীন মুল্লা আলী কারী আল হারুবী (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম নূরুদ্দীন আলী ইবনে আহমদ সামহূদী (رحمة الله)\"\n  },\n  {\n    \"get\": \"ইমাম ইবনে জাওজী (رحمة الله)\"\n  },\n  {\n    \"get\": \"আল্লামা মুহাম্মদ আজিজুল হক আল্-কাদেরী (رحمة الله)\"\n  },\n  {\n    \"get\": \"হাফেজ মাওলানা মুহাম্মদ ওসমান গণি\"\n  },\n  {\n    \"get\": \"মাওলানা মুহাম্মদ শহিদুল্লাহ বাহাদুর\"\n  },\n  {\n    \"get\": \"আমীরে আহলে সুন্নাত হযরত মাওলানা ইলিয়াস আত্তার কাদেরী রযভী\"\n  },\n  {\n    \"get\": \"মুফতি মুহাম্মদ আলমগীর হোসাইন আন-নাজিরী\"\n  },\n  {\n    \"get\": \"ড. মুহাম্মদ তাহিরুল কাদেরী\"\n  },\n  {\n    \"get\": \"অধ্যক্ষ শেখ মোহাম্মদ আব্দুল করিম সিরাজনগরী\"\n  },\n  {\n    \"get\": \"মুফতী মাওলানা মুহাম্মদ আলাউদ্দিন জেহাদী\"\n  },\n  {\n    \"get\": \"কাজী সাইফুদ্দীন হোসেন\"\n  },\n  {\n    \"get\": \"মোহাম্মদ মামুনুর রশীদ\"\n  },\n  {\n    \"get\": \"ব্রিগেডিয়ার জেনারেল খুরশীদ আলম (অবঃ)\"\n  },\n  {\n    \"get\": \"হাফিয মাওলানা মুফতি মুহাম্মদ ইকরাম উদ্দিন\"\n  },\n  {\n    \"get\": \"হযরত মাওলানা সৈয়দ জিয়াউদ্দিন আহমদ গীলানী (ভারত)\"\n  },\n  {\n    \"get\": \"ড. মুহাম্মদ আব্দুর রশীদ\"\n  },\n  {\n    \"get\": \"আবু আইয়্যুব কাদেরী\"\n  },\n  {\n    \"get\": \"ড. মোহাম্মদ আবদুল হালিম\"\n  },\n  {\n    \"get\": \"সৈয়দ মুহাম্মদ আবু তাহের রেজা\"\n  },\n  {\n    \"get\": \"মুফতি আবুল হাসান মুহাম্মদ ওমাইর রজভী\"\n  },\n  {\n    \"get\": \"মুফতী মাওলানা এস, এম, সাকীউল কাউছার\"\n  },\n  {\n    \"get\": \"মুফতী দেলোয়ার বিন গাজী\"\n  },\n  {\n    \"get\": \"মুহাম্মদ শাহীদ রিজভী\"\n  },\n  {\n    \"get\": \"মুহাম্মাদ হাসিব হাসেমী\"\n  },\n  {\n    \"get\": \"অধ্যক্ষ মাওলানা মুহাম্মদ বদিউল আলম রিজভি\"\n  },\n  {\n    \"get\": \"ইজহারুল ইসলাম\"\n  },\n  {\n    \"get\": \"আমীরুল ইসলাম\"\n  },\n  {\n    \"get\": \"মুফতী সৈয়দ মুহাম্মাদ সাইফুল বারী\"\n  },\n  {\n    \"get\": \"মুহাম্মদ আখতারুজ্জামান\"\n  },\n  {\n    \"get\": \"মুহাম্মদ হোসাইন আহমেদ আলকাদেরী\"\n  },\n  {\n    \"get\": \"আব্দে রাসূল মুফ্তী নাজিরুল আমিন রেজভী হানাফী ক্বাদেরী\"\n  },\n  {\n    \"get\": \"সাইয়্যিদ আব্দুল ক্বাইয়ুম আল হোসাইনী\"\n  },\n  {\n    \"get\": \"ইমরান বিন বদরী\"\n  },\n  {\n    \"get\": \"ডাক্তার মাসুম বিল্লাহ সানি\"\n  },\n  {\n    \"get\": \"সিফাত সুলতান আলিফ\"\n  },\n  {\n    \"get\": \"আব্দুল মুস্তফা রাহিম রমজান\"\n  }\n]");
                                                                                                            in.putExtra("hint", "লেখকের নাম লিখে সার্চ করুন");
                                                                                                            startActivity(in);
                                                                                                        }
                                                                                                    });                                                                                                writer.create().show();

                                                                                                } else {
                                                                                                    if (name.getText().toString().equals("বিষয়ভিত্তিক বই")) {
                                                                                                        // Create an AlertDialog.Builder instance
                                                                                                        AlertDialog.Builder subject = new AlertDialog.Builder(Main0Activity.this);
                                                                                                        subject.setMessage("নির্বাচন করুন");

                                                                                                        // Positive button for Facebook ID
                                                                                                        subject.setPositiveButton("অনলাইন", new DialogInterface.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                                                in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                                in.putExtra("url", "https://www.sunni-encyclopedia.com/p/blog-page_70.html?m=1&_gl=1*1jg8dsz*_ga*MjgzMjg2MjI4LjE3Mzg3MjM2MjQ.*_ga_1PRTC57BE2*MTczOTA0MjM5Ni40LjEuMTczOTA0ODM2NC4yMy4wLjA.");
                                                                                                                startActivity(in);
                                                                                                            }
                                                                                                        });

                                                                                                        // Negative button for Facebook Page
                                                                                                        subject.setNegativeButton("অফলাইন", new DialogInterface.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                                                in.setClass(getApplicationContext(), GetallActivity.class);
                                                                                                                in.putExtra("heading", "সকল বিষয়");
                                                                                                                in.putExtra("get", "[\n  {\n    \"get\": \"আকাঈদ\"\n  },\n  {\n    \"get\": \"আল্লাহ সম্পর্কিত\"\n  },\n  {\n    \"get\": \"নূরনবী (ﷺ)\"\n  },\n  {\n    \"get\": \"হায়াতুন্নবী (ﷺ)\"\n  },\n  {\n    \"get\": \"মিরাজুন্নবী (ﷺ)\"\n  },\n  {\n    \"get\": \"মিলাদুন্নবী (ﷺ) ও দুরূদ\"\n  },\n  {\n    \"get\": \"শানে মােস্তফা (ﷺ)\"\n  },\n  {\n    \"get\": \"নবী আল উম্মী (ﷺ)\"\n  },\n  {\n    \"get\": \"মুজিযাতুর রাসূল (ﷺ)\"\n  },\n  {\n    \"get\": \"ইলমে গায়েব\"\n  },\n  {\n    \"get\": \"শাফা'আত\"\n  },\n  {\n    \"get\": \"হাজির-নাজির\"\n  },\n  {\n    \"get\": \"দুরূদ-সালাম\"\n  },\n  {\n    \"get\": \"কাসীদা\"\n  },\n  {\n    \"get\": \"হাদিসগ্রন্থ\"\n  },\n  {\n    \"get\": \"আমলে নাজাত\"\n  },\n  {\n    \"get\": \"শানে আহলে বাইত\"\n  },\n  {\n    \"get\": \"আমিরে মুয়াবিয়া (رضي الله عنه)\"\n  },\n  {\n    \"get\": \"সাহাবায়ে কেরাম\"\n  },\n  {\n    \"get\": \"ইয়াজিদ বিন মুয়াবিয়া\"\n  },\n  {\n    \"get\": \"শানে আউলিয়া\"\n  },\n  {\n    \"get\": \"আলা হযরত (رحمة الله)\"\n  },\n  {\n    \"get\": \"আহলুল বিদআত\"\n  },\n  {\n    \"get\": \"উসীলা-আহকামুল মাযার\"\n  },\n  {\n    \"get\": \"তাসাউফ\"\n  },\n  {\n    \"get\": \"মাযহাব\"\n  },\n  {\n    \"get\": \"ফতোয়া-মাস'য়ালা\"\n  },\n  {\n    \"get\": \"নারীদের সম্পর্কে\"\n  },\n  {\n    \"get\": \"আহলুল বিদআত ওয়াল ফুরকা\"\n  },\n  {\n    \"get\": \"কুরবানি\"\n  },\n  {\n    \"get\": \"জিকির-সালাত\"\n  },\n  {\n    \"get\": \"আহকামুল মাযার\"\n  },\n  {\n    \"get\": \"শবে বরাত\"\n  },\n  {\n    \"get\": \"উপদেশবলী\"\n  },\n  {\n    \"get\": \"দাওয়াতে ইসলামী\"\n  }\n]");
                                                                                                                in.putExtra("hint", "বিষয় লিখে সার্চ করুন");
                                                                                                                startActivity(in);
                                                                                                            }
                                                                                                        });                                                                                                subject.create().show();

                                                                                                    } else {
                                                                                                        if (name.getText().toString().equals("বয়ান")) {
                                                                                                            in.setClass(getApplicationContext(), BlogActivity1.class);
																											in.putExtra("url", "https://islamipedia.vercel.app/boyan.html");
                                                                                                            startActivity(in);
                                                                                                        } else {
                                                                                                            if (name.getText().toString().equals("প্রবন্ধ")) {
                                                                                                                in.setClass(getApplicationContext(), Main4Activity.class);
                                                                                                                in.putExtra("sub", "প্রবন্ধ");
                                                                                                                in.putExtra("get", "প্রবন্ধ");
                                                                                                                in.putExtra("booklist", "file.json");
                                                                                                                startActivity(in);
                                                                                                            }
                                                                                                            else {
                                                                                                                if (name.getText().toString().equals("অভিযোগ")) {
    // প্রথমে একটি ডায়ালগ দেখাবে দুইটি অপশন সহ
    AlertDialog.Builder optionDialog = new AlertDialog.Builder(Main0Activity.this);
    optionDialog.setMessage("নির্বাচন করুন");
    optionDialog.setPositiveButton("মেসেজ পাঠান", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // মেসেজ পাঠান এর জন্য আপনার পুরনো AlertDialog কোড
            AlertDialog.Builder builder = new AlertDialog.Builder(Main0Activity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_report, null);
            builder.setView(dialogView);
            
            final EditText editText = dialogView.findViewById(R.id.editText);
            Button sendButton = dialogView.findViewById(R.id.sendButton);
            Button emailButton = dialogView.findViewById(R.id.emailButton);
            Button whatsapp = dialogView.findViewById(R.id.whatsapp);
            
            final AlertDialog dialogme = builder.create();
            
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = editText.getText().toString();
                    if (!message.isEmpty()) {
                        String url = "https://api.telegram.org/bot8513295796:AAEfeaGo-O29kSk-4zCxcUiB3eU-GRPbtGw/sendMessage?chat_id=7619923490&text=" + message;
                        send.startRequestNetwork(RequestNetworkController.POST, url, "Rizwan", _send_request_listener);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "মেসেজ পাঠানো হয়েছে", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Main0Activity.this, "দয়া করে কিছু লিখুন", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent me = new Intent(Intent.ACTION_VIEW);
                    me.setData(Uri.parse("https://wa.me/8801714656343"));
                    startActivity(me);
                }
            });
            
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"muhammodrizwan01@gmail.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Issue or Suggestion");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "বইয়ের নাম বা অ্যাপের সমস্যা লিখুন...");
                    
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Select Email App"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(Main0Activity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                    dialogme.dismiss();
                }
            });
            
            dialogme.show();
        }
    });
    
    optionDialog.setNegativeButton("অনলাইন সাপোর্ট", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // অভিযোগ করুন এ ক্লিক করলে ReportActivity তে যাবে
            Intent in = new Intent(getApplicationContext(), ReportActivity.class);
            startActivity(in);
        }
    });
    
    optionDialog.show();
		}
                                                                                                                
                                                                                                                else {
                                                                                                                    if (name.getText().toString().equals("অনলাইন প্রবন্ধ")) {
                                                                                                                        in.setClass(getApplicationContext(), BlogonlineActivity.class);
                                                                                                                        startActivity(in);
                                                                                                                    }
                                                                                                                    else {
                                                                                                                        if (name.getText().toString().equals("সাপোর্ট করুন")) {
                                                                                                                            in.setClass(getApplicationContext(), DonateActivity.class);
                                                                                                                            startActivity(in);
                                                                                                                        }
                                                                                                                        else {
                                                                                                                            if (name.getText().toString().equals("সেহরি ও ইফতার")) {
                                                                                                                                in.setClass(getApplicationContext(), SehriIftarActivity.class);
                                                                                                                                startActivity(in);
                                                                                                                            }
                                                                                                                            else {
                                                                                                                                if (name.getText().toString().equals("ক্বিবলা কম্পাস")) {
                                                                                                                                    in.setClass(getApplicationContext(), CompassActivity.class);
                                                                                                                                    startActivity(in);
                                                                                                                                }
                                                                                                                                else {
                                                                                                                                    if (name.getText().toString().equals("ইসলামিক নাম")) {
// Create an AlertDialog.Builder instance
                                                                                                    AlertDialog.Builder islamicnameme = new AlertDialog.Builder(Main0Activity.this);
                                                                                                    islamicnameme.setMessage("নির্বাচন করুন");

                                                                                                    // Positive button for Facebook ID
                                                                                                    islamicnameme.setPositiveButton("অনলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                            in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                            in.putExtra("url", "https://islamipedia.vercel.app/islamicname.html");
                                                                                                            startActivity(in);
                                                                                                        }
                                                                                                    });

                                                                                                    // Negative button for Facebook Page
                                                                                                    islamicnameme.setNegativeButton("অফলাইন", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                                                                                                                                                                                                in.setClass(getApplicationContext(), Main4Activity.class);
                                                                                                                                        in.putExtra("sub", "ইসলামিক নাম");
                                                                                                                                        in.putExtra("get", "ইসলামিক নাম");
                                                                                                                                        in.putExtra("booklist", "file.json");
                                                                                                                                        startActivity(in);
                                                                                                        }
                                                                                                    });                                                                                                islamicnameme.create().show();
                                                                                                                                    }
                                                                                                                                    else {
                                                                                                                                        if (name.getText().toString().equals("কমিউনিটি")) {
                                                                                                                                            // Create an AlertDialog.Builder instance
                                                                                                                                            AlertDialog.Builder community0 = new AlertDialog.Builder(Main0Activity.this);
                                                                                                                                            community0.setTitle("Community");
                                                                                                                                            community0.setMessage("আসসালামু আলাইকুম, কোন কারন বশত যদি এপ্সটি প্লে ষ্টোরে না থাকে তারপরেও এপ্সটি সব সময়ে আপডেট ও তথ্য পেতে কমিউনিটিতে যুক্ত থাকতে পারেন।");

                                                                                                                                            // Positive button for Facebook ID
                                                                                                                                            community0.setPositiveButton("টেলিগ্রাম", new DialogInterface.OnClickListener() {
                                                                                                                                                @Override
                                                                                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                                                                                    Intent community1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+Rzqb4mcCnvU1NmQ1"));
                                                                                                                                                    startActivity(community1);
                                                                                                                                                }
                                                                                                                                            });

                                                                                                                                            // Negative button for Facebook Page
                                                                                                                                            community0.setNegativeButton("Facebook Page", new DialogInterface.OnClickListener() {
                                                                                                                                                @Override
                                                                                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                                                                                    Intent community1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=61570134066496"));
                                                                                                                                                    startActivity(community1);
                                                                                                                                                }
                                                                                                                                            });

                                                                                                                                            // Neutral button for WhatsApp
                                                                                                                                            community0.setNeutralButton("Whatsapp", new DialogInterface.OnClickListener() {
                                                                                                                                                @Override
                                                                                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                                                                                    Intent me = new Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/GShUo22Wu5o4F6v75zXf8o"));
                                                                                                                                                    startActivity(me);
                                                                                                                                                }
                                                                                                                                            });

                                                                                                                                            // Show the dialog
                                                                                                                                            community0.create().show();
                                                                                                                                        }
                                                                                                                                        else {
    if (name.getText().toString().equals("মসজিদ খুঁজি")) {
        try {
            Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=mosque");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            startActivity(Intent.createChooser(intent, "Open with"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error occurred while opening link.", Toast.LENGTH_SHORT).show();
        }
    } else {                                                                                                                                                if (name.getText().toString().equals("লাইভ")) {
                                                                                                                                                   in.setClass(getApplicationContext(), BlogActivity1.class);
																																				   in.putExtra("url", "https://islamipedia.vercel.app/live.html");
                                                                                                                                                   startActivity(in);
                                                                                                                                                }
                                                                                                                                                else {
                                                                                                                                                    if (name.getText().toString().equals("ইসলামী বিশ্বকোষ")) {
                                                                                                                                                        in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                                                                        in.putExtra("url", "https://www.sunni-encyclopedia.com/");
                                                                                                                                                        startActivity(in);
                                                                                                                                                    }
                                                                                                                                                    else {
                                                                                                                                                        if (name.getText().toString().equals("নির্বাচিত পোষ্ট")) {
                                                                                                                                                            in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                                                                            in.putExtra("url", "https://sunni-encyclopedia.com/p/blog-page_18.html?m=1");
                                                                                                                                                            startActivity(in);
                                                                                                                                                        }
                                                                                                                                                        else {
                                                                                                                                                            if (name.getText().toString().equals("মাসায়েল")) {
                                                                                                                                                                in.setClass(getApplicationContext(), BlogActivity1.class);
                                                                                                                                                                in.putExtra("url", "https://www.sunni-encyclopedia.com/p/blog-page_23.html?m=0");
                                                                                                                                                                startActivity(in);
                                                                                                                                                            }
                                                                                                                                                            else {

                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
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
