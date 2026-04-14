package com.srizwan.islamipedia;

import android.animation.*;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.*;
import android.media.SoundPool;
import android.os.*;
import android.os.Vibrator;
import android.view.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TasbihActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private double s1 = 0;
	private double clicklist = 0;
	
	private ArrayList<HashMap<String, Object>> map = new ArrayList<>();
	
	private ScrollView vscroll1;
	private LinearLayout linear1;
	private LinearLayout linear9;
	private LinearLayout linear2;
	private ViewPager viewpager1;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private LinearLayout linear5;
	private LinearLayout linear7;
	private TextView textview1;
	private TextView textview2;
	private LinearLayout linear10;
	private LinearLayout linear6;
	private TextView textview5;
	private TextView textview4;
	private ImageView imageview1;
	
	private SharedPreferences save;
	private Vibrator vid;
	private TimerTask timer;
	private SoundPool mp;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.tasbih);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		vscroll1 = findViewById(R.id.vscroll1);
		linear1 = findViewById(R.id.linear1);
		linear9 = findViewById(R.id.linear9);
		linear2 = findViewById(R.id.linear2);
		viewpager1 = findViewById(R.id.viewpager1);
		linear3 = findViewById(R.id.linear3);
		linear4 = findViewById(R.id.linear4);
		linear5 = findViewById(R.id.linear5);
		linear7 = findViewById(R.id.linear7);
		textview1 = findViewById(R.id.textview1);
		textview2 = findViewById(R.id.textview2);
		linear10 = findViewById(R.id.linear10);
		linear6 = findViewById(R.id.linear6);
		textview5 = findViewById(R.id.textview5);
		textview4 = findViewById(R.id.textview4);
		imageview1 = findViewById(R.id.imageview1);
		save = getSharedPreferences("save", Activity.MODE_PRIVATE);
		vid = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		linear1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				imageview1.performClick();
			}
		});
		
		linear7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				imageview1.performClick();
			}
		});

		linear10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if ("সাউন্ড অন".equals(textview5.getText().toString())) {
					textview5.setText("সাউন্ড অফ");
					save.edit().putString("sound", "সাউন্ড অফ").apply(); // Save state
				} else {
					textview5.setText("সাউন্ড অন");
					save.edit().putString("sound", "সাউন্ড অন").apply(); // Save state
				}
			}
		});


		linear6.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				
				return true;
			}
		});
		
		linear6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				save.edit().remove("count").commit();
				textview2.setText("0");
				vid.vibrate((long)(100));
			}
		});

		imageview1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// Convert Bangla number to English before incrementing
				String currentCount = replaceBanglaNumberToEnglish(textview2.getText().toString().replace(",", ""));
				double newCount = Double.parseDouble(currentCount) + 1;

				// Set the new count in Bangla
				textview2.setText(replaceArabicNumber(
						new DecimalFormat("#,##,##,###.##").format(newCount)
				));

				// Save the new count
				save.edit().putString("count", textview2.getText().toString()).apply();

				// Play sound if enabled
				if (textview5.getText().toString().equals("সাউন্ড অন")) {
					s1 = mp.play(1, 1.0f, 1.0f, 1, 0, 1.0f);
				}
			}
		});


	}
	
	private void initializeLogic() {
		_NavStatusBarColor("#FF01837A", "#FF01837A");
		{
			android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
			int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
			SketchUi.setColor(0xFF01837A);SketchUi.setCornerRadii(new float[]{
				d*15,d*15,d*15 ,d*15,d*0,d*0 ,d*0,d*0});
			linear3.setBackground(SketchUi);
		}
		_DARK_ICONS();
		_ClickAnimation(linear6);
		_Animator(linear6, "elevation", 10, 0);
		_ClickAnimation(linear10);
		_Animator(linear10, "elevation", 10, 0);
		_ClickAnimation(imageview1);
		_Animator(imageview1, "elevation", 10, 0);
		_Shadow(5, 30, "#FFFFFF", linear2);
		android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
		int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
		SketchUi.setColor(0xFFFFFFFF);SketchUi.setCornerRadii(new float[]{
			d*7,d*7,d*0 ,d*0,d*0,d*0 ,d*7,d*7});
		linear6.setElevation(d*0);
		android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF4CAF50}), SketchUi, null);
		linear6.setBackground(SketchUi_RD);
		android.graphics.drawable.GradientDrawable SketchUo = new android.graphics.drawable.GradientDrawable();
		int p = (int) getApplicationContext().getResources().getDisplayMetrics().density;
		SketchUo.setColor(0xFFFFFFFF);SketchUo.setCornerRadii(new float[]{
			p*7,p*7,p*0 ,p*0,p*0,p*0 ,p*7,p*7});
		linear10.setElevation(p*0);
		android.graphics.drawable.RippleDrawable SketchUo_RP = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF4CAF50}), SketchUo, null);
		linear10.setBackground(SketchUo_RP);
		try{
			map = new Gson().fromJson("[\n  {\n    \"dua\": \"(১) \n\nلاَ اِلَهَ اِلاَّ اللهُ مُحَمَّدُ رَّسُوْ لُ الله\n\nউচ্চারণঃ লা-ইলাহা ইল্লাল্লাহু মুহাম্মাদুর রাসূলুল্লাহ (ﷺ)\n\nঅর্থঃ আল্লাহ ছাড়া আর কোন মা'বুদ নাই। হযরত মুহাম্মদ (ﷺ) আল্লাহর প্রেরিত রাসূল।\n\"\n  },\n  {\n    \"dua\": \"(২)\n\nسبحان الله وبحمده سبحان الله العظيم\n\nউচ্চারণ : সুবহানাল্লাহি ওয়া বিহামদিহি, সুবহানাল্লাহিল আজিম।\n\nঅর্থঃ আল্লাহ সুমহান এবং সকল প্রশংসা তারই। আল্লাহ সুমহান যিনি সর্বোচ্চ মর্যাদার অধিকারী।\n\"\n  },\n  {\n    \"dua\": \"(৩) سُبْحَانَ ٱللَّٰهِ\n\nউচ্চারণঃ সুবহানাল্লাহ \nঅর্থঃ আল্লাহ মহিমান্বিত।\n\"\n  },\n  {\n    \"dua\": \"(৪) ٱلْحَمْدُ لِلَّٰهِ\n\nউচ্চারণঃ আলহামদুলিল্লাহ। \nঅর্থঃ সমস্ত প্রশংসা মহান আল্লাহর।\n\"\n  },\n  {\n    \"dua\": \"(৫)  ٱللَّٰهُ أَكْبَرُ \nউচ্চারণঃ আল্লাহু আকবার।\nঅর্থঃ আল্লাহ সর্বশ্রেষ্ঠ। \n\"\n  },\n  {\n    \"dua\": \"(৬)\nأَسْتَغْفِرُ اللهَ\nউচ্চারণঃ আস্তাগফিরুল্লাহ\nঅর্থঃ আমি আল্লাহর নিকট ক্ষমা প্রার্থনা করছি।\n\"\n  },\n  {\n    \"dua\": \"(৭) জাহান্নাম থেকে মুক্তির দোয়াঃ\n اللَّهُمَّ أَجِرْنِى مِنَ النَّارِ\nউচ্চারণ: বাংলা উচ্চারণ : আল্লাহুম্মা আজিরনি মিনান নার।\n\"\n  },\n  {\n    \"dua\": \"(৮) কেয়ামতের দিন হিসাব সহজ হওয়ার দোয়াঃ\nاللَّهُمَّ حَاسِبْنِي حِسَابًا يَسِيرًا\nউচ্চারণ : ‘আল্লাহুম্মা হাসিবনি হিসাবাই ইয়াসিরা’।\nঅর্থ : হে আল্লাহ! আপনি আমার হিসাবকে সহজ করে দিন।\n\"\n  },\n  {\n    \"dua\": \"(৯) জান্নাতের গুপ্তধনঃ\n لاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللهِ \n(লা হাওলা অলা ক্বুওয়াতা ইল্লা বিল্লাহ)\n\nঅর্থঃ আল্লাহ ব্যতীত অনিষ্ট দূর করার এবং কল্যাণ লাভের কোনো শক্তি কারো নেই।\n\"\n  },\n  {\n    \"dua\": \"(১০) বালা মুসীবত থেকে মুক্তির দোয়াঃ\n\nحَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ، نِعْمَ الْمَوْلَىٰ وَنِعْمَ النَّصِيرُ\n\nউচ্চারণঃ হাসবুনাল্লাহু ওয়া নি’মাল ওয়াকিল, নি’মাল মাওলা ওয়া নি’মান-নাসির।’\n\nঅর্থঃ আল্লাহ তাআলাই আমাদের জন্য যথেষ্ট, তিনিই হলেন উত্তম কর্মবিধায়ক; আল্লাহ তাআলাই হচ্ছে উত্তম অভিভাবক এবং উত্তম সাহায্যকারী।\n\n(সূরা আলে ইমরান ১৭৩ নম্বর আয়াত এবং সুরা আনফাল ৪০, সুরা হজ্জ্ব ৭৮)\n\"\n  },\n  {\n    \"dua\": \"(১১) মাগফিরাত লাভের দোয়া ও লাইলাতুল কদরের দোয়াঃ\n\nاللَّهمَّ إنَّك عفُوٌّ كريمٌ تُحِبُّ العفْوَ، فاعْفُ عنِّي\n \nউচ্চারণ: \n‘আল্লাহুম্মা ইন্নাকা আফুউন কারিমুন তুহিব্বুল আফওয়া ফা'ফু আন্নি।’\n\nঅর্থ: \n‘হে আল্লাহ, আপনি মহানুভব ক্ষমাশীল এবং ক্ষমা করতে পছন্দ করেন, অতএব আমাকে ক্ষমা করুন।’ \n\"\n  },\n  {\n    \"dua\": \"(১২) প্রিয় নবীর নৈকট্য লাভের উপায়:-\n\nاَللّٰهُمَّ صَلِّ عَليٰ مُحَمَّدٍ كَاَ تُحِبُّ وَتَرْضٰى لَهٗ\n\nআল্লাহুম্মা ছাল্লি আলা মুহাম্মাদিন কামা তুহিব্বু ওয়া ত্বারদ্বা লাহু।\n\"\n  },\n  {\n    \"dua\": \"(১৩) কাদেরীয়া তরীকার শ্রেষ্ঠ দুরূদ শরীফ:-\n\nاَللَّهُمَّ صَلِّ عَليٰ سَيِّدِنَا مَوْلَانَا مُحَمَّدٍ وَعَليٰ اٰلِ سَيِّدِنَا مَوْلَانَا مُحَمَّدٍ وَبَارِكْ وَسَلِّمْ-\n\nআল্লাহুম্মা ছাল্লি আলা সয়্যিদিনা মাওলানা মুহাম্মাদিন ওয়ালা আ-লি সয়্যিদিনা মাওলানা মুহাম্মাদিন ওয়া বারিক ওয়া সাল্লিম।\n\nফযিলত: এই দুরূদ শরীফ সকাল ও সন্ধ্যা একশত বার পাঠ করলে বালা মুছিবত দূর হয়।\n\"\n  },\n  {\n    \"dua\": \"(১৪) জিয়ারতে রাসুল (ﷺ):-\n\nاَللّٰهُمَّ صَلِّ عَليٰ رُوْحِ مُحَمَّدٍ فِي اَلْاَرْوَاحِ وَعَليٰ جَسَدِهِ فِي اَلْاَجْسَادِ وَعَليٰ قَبَرِهِ فِي الْقُبُوْرِ-\n\n(আল্লাহুম্মা ছাল্লি আলা রুহি মুহাম্মাদিন্ ফিল আর ওয়াহি ওয়ালা জাসাদিহি ফিল আজসা-দি ওয়ালা ক্ববরিহি ফিল ক্বুবুর)\n\"\n  },\n  {\n    \"dua\": \"(১৫) যেকোন সমস্যা সমাধানের জন্য পড়ুন:-\n\nقَلَّتْ حِيْلَتِيْ اَنْتَ وَسِيْلَتِيْ اَدْرِكْنِي يَا رَسُولَ اللهِ صَلَّي اللهُ عَلَيْهِ وَسَلَّمَ\n\nক্বল্লাত হিলাতী আন্তা ওয়াসিলাতি আদ্ রিকনি ইয়া রাসুলাল্লাহ (ﷺ)।\n\nফযিলত: যেকোন সমস্যার সমাধানের জন্য পড়ুন ইনশাআল্লাহ বৃথা যাবে না।\n\"\n  },\n  {\n    \"dua\": \"(১৬) দরুদে গাউসিয়া:\n\nاَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ مَّعْدَنِ الْجُوْدِ وَالْكَرَمِ وَاٰلِهٖ وَبَارِكْ وَسَلِّمْ-\n\nআল্লাহুম্মা ছাল্লি আলা সয়্যিদিনা ওয়া মাওলানা মুহাম্মাদিম মাআদিনিল যুদি ওয়াল কারামি ওয়া আলিহি ওয়া বারিক ওয়া সাল্লিম।\n\nফযিলত: এ দুরূদ শরীফ পাঠ করলে- ১.জীবিকায় বরকত হবে ২.সমস্ত কাজ সহজ হবে ৩.মৃত্যুকালে কলেমা নসীব হবে ৪.প্রাণ সহজে বের হবে ৫.কবর প্রশস্ত হবে ৬.কারো মুখাপেক্ষী থাকবেনা ৭.আল্লাহর সৃষ্টি তাকে ভালোবাসবে।\n\"\n  },\n  {\n    \"dua\": \"(১৭) মুখের দুর্গন্ধ দূর করার উপায়:-\n\nاَللّٰهُمَّ صَلِّ وَسَلِّمْ عَلىٰ النَّبِيِّ الطَّاهِرِ\n\nআল্লাহুম্মা ছাল্লি ওয়া সাল্লিম আলান নবিয়্যিত ত্বাহিরি।\n\nফযিলত: এক নিশ্বাসে ১১বার এই দুরূদ শরীফ পাঠ করলে মুখের দুর্গন্ধ দূর হয়।\n\"\n  },\n  {\n    \"dua\": \"(১৮) বৃষ্টির সময় এই দুরূদ শরীফ পড়া উত্তম:-\n\nاَللّٰهُمَّ صَلِّ وَّسَلِّمْ عَلىٰ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ وَّ علىٰ اٰلِهٖ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ بِعَدَدِ قَطْرَاتِ الْأَمْطَارِ\n\nআল্লাহুম্মা ছাল্লি ওয়া সাল্লিম আলা সাইয়্যিদিনা ওয়া মাওলানা মুহাম্মাদিও ওয়া আলা আলিহি সাইয়্যিদিনা ওয়া মাওলানা মুহাম্মাদিন্ বি আদাদি ক্বাতরাতিল আমতার। \n\nফযিলত: বৃষ্টি আসার সময় এই দুরূদ শরীফ পাঠ করলে যতগুলো ফোটা মাটিতে পড়ে ততগুলো ছাওয়াব পাওয়া যাবে।\n\"\n  },\n  {\n    \"dua\": \"(১৯) উভয় জাহানের নেয়ামত অর্জন:-\n\nاَللّٰهُمَّ صَلِّ وَّسَلِّمْ وَبَارِكْ عَليٰ سَيِّدِنَا مُحَمَّدٍوَّعَليٰ اٰلِهٖ عَدَدَ اِنْعَامِ اللهِ وَاَفْضَالِهٖ\n\nআল্লাহুম্মা ছাল্লি ওয়া সাল্লিম ওয়া বারিক আলা সায়্যিদিনা মুহাম্মাদিও ওয়া আলা আলিহি আদাদা ইনআমিল্লাহি ওয়া আফদ্বালিহি।\n\nফযিলত: এই দুরূদ শরীফ পড়লে অগণিত নেয়ামত অর্জিত হয়।\n\"\n  },\n  {\n    \"dua\": \"(২০) আশি বছরের গুনাহ মাফ:\n\nاَللّٰهُمَّ صَلِّ عَلىٰ سَيِّدِنَا مُحَمَّدِنِ النَّبِيِّ الْأُمِّيِّ وَعَلىٰ اٰلِهٖ وَسَلِّمْ\n\nআল্লাহুম্মা ছাল্লি আলা সাইয়্যিদিনা মুহাম্মাদিনিন নাবিয়্যিল উম্মিই ওয়া আলা আলিহি ওয়া সাল্লিম।\n\nফযিলত: রাসূলে পাক ইরশাদ ফরমান যে ব্যক্তি জুমার দিন আছরের পর ৮০বার এই দুরূদ শরীফ পড়বে আল্লাহ তার ৮০ বছরের গুনাহ মাফ করে দিবেন।\n\"\n  },\n  {\n    \"dua\": \"(২১) মাগফিরাতের দুরূদ শরীফ:\n\nاَللّٰهُمَّ صَلِّ عَلىٰ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ وَعَلىٰ اٰلِهٖ وَسَلِّمْ\n\nআল্লাহুম্মা ছাল্লি আলা সাইয়্যিদিনা ওয়া মাওলানা মুহাম্মাদিও ওয়া আলা আলিহি ওয়া সাল্লিম।\n\"\n  },\n  {\n    \"dua\": \"(২২) সারাদিন দুরূদ শরীফ পড়ার ছাওয়াব:\n\nاَللّٰهُمَّ صَلِّ عَليٰ سَيِّدَنَا مُحَمَّدٍ فِي اَوَّلِ كَاَيمِنَا اَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا مُحَمَّدٍ فِي اَوْسَطِ كَاَ مِنَا اَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا مُحَمَّدٍ فِي اۤخِرِ كَاَ مِنَا\n\nআল্লাহুম্মা ছাল্লি আলা সইয়্যিদিনা মুহাম্মাদিন ফি আউয়ালি কালামিনা। আল্লাহুম্মা ছাল্লি আলা সইয়্যিদিনা মুহাম্মাদিন ফি আওসাতি কালামিনা। আল্লাহুম্মা ছাল্লি আলা সইয়্যিদিনা মুহাম্মাদিন ফি আখিরি কালামিনা।\n\"\n  },\n  {\n    \"dua\": \"(২৩) দরুদে রযভিয়্যাহ:\n\nصَلَّي اللهُ عَليٰ النَّبِيِّ الْاُمِّيِّ وَاٰلِهٖ صَلَّي اللهُ عَلَيْهِ وَسَلِّمْ صَلوٰةً وَّسَلاماً عَلَيْكَ يَا رَسُوْلَ الله-\n\nছাল্লাল্লাহু আলান নাবিয়্যিল উম্মিয়্যি ওয়া আলিহি ছাল্লাল্লাহু আলাইহি ওয়াসাল্লাম ছালাতাও ওয়া সালামান আলাইকা ইয়া রাসুলাল্লাহ।\n\nফযিলত: এ দুরূদ শরীফ প্রত্যেক নামায ও জুমার নামাযের পর খাস করে মদীনা মনোয়ারার দিকে মুখ করে ১০০ বার পাঠ করলে অগণিত ফযিলত অর্জন হয়।\n\"\n  },\n  {\n    \"dua\": \"(২৪) দরূদে মাহীঃ \n\nاَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا مُحَمَّدٍ خَيْرِ الْخَلَائِقِ اَفْضَلِ الْبَشَرِ شَفِيْعِ اْلاُمَّةِ يَوْمِ الْحَشَرِ وَالنَّشَرِسَيِّدِنَا مُحَمَّدٍ بِعَدَدِ كُلِّ مَعْلُوْمِ لَّكَ وَصَلِّ عَليٰ جَمِيْعِ اْلاَنْۭبِيَاءِ وَالْمُرْسَلِيْنَ وَالْمَلۤائِكَةِ الْمُقَرَّبِيْنَ وَعَليٰ عِبَادِ اللهِ الصَّالِحِيْنَ وَارْحَمْنَا مَعَهُمْ بِرَحْمَتِكَ يَا اَرْحَمَ الرَّحِمِيْنَ-\n\nআল্লাহুম্মা সাল্লি আলা মুহাম্মাদিন খাইরিল খালায়িক্বি আফদ্বালিল বাশারি শাফীয়িল উম্মাতি ইয়াওমিল হাশারি ওয়ান্নাশরি সইয়্যিদিনা মুহাম্মাদিম্ বিআদাদি কুল্লি মালুমিল্লাকা ওয়া সাল্লি আলা জমীয়িল আম্বিয়ায়ি ওয়াল মুরসালীনা ওয়াল মালায়িকাতিল মুক্বাররাবীনা ওয়া আলা ইবাদিল্লাহিস্ সালিহীনা ওয়ারহামনা মাআ'হুম বিরহমাতিকা ইয়া আরহামার রহিমীনা।\n\nফযিলত: খুব কঠিন বিপদে কিংবা দুরারোগ্য রোগে আক্রান্ত হলে ক্রমবৃদ্ধি করে ২১ দিন বা ৪১ দিনে সোয়া লক্ষ বার এই দুরূদ শরীফ পড়িলে সাথে সাথে ফল পাওয়া যায়।\n\"\n  },\n  {\n    \"dua\": \"(২৫) দরূদে খাইর:\n\nاَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا وَنَبِيِّنَا وَشَفِيْعِنَا وَمَوْلَآنَا مُحَمَّدٍ صَلَّي اللهَ عَلَيْهِ وَعَليٰ اٰلِهٖ وَاَصْحَابِهِ وَاَزْوَاجِهِ وَبَارِكْ وَسَلِّمْ\n\nআল্লাহুম্মা সাল্লি আলা সইয়্যিদিনা ওয়া নাবিয়্যিনা ওয়া শাফীয়িনা ওয়া মাওলানা মুহাম্মাদিন সাল্লাল্লাহু আলাইহি ওয়া আলা আলিহী ওয়া আসহাবিহী ওয়া আযওয়াজিহী ওয়া বারিক ওয়া সাল্লিম।\n\nফযিলত: যিনি সর্বদা এই দুরূদ শরীফ আমল করবেন- তিনি অবশ্যই দেশের সর্দার হবেন। যদি তা না হয়, তবে অন্তত স্বীয় বংশের সর্দার রূপে বা শ্রেষ্ঠ ধনী রূপে ইজ্জত পাবেন। প্রত্যহ চাশ্ত নামাযের পর ২১বার পড়লে ইন্শাআল্লাহ ধনী হয়ে যাবে।\n\"\n  },\n  {\n    \"dua\": \"(২৬) দরূদে রুইয়াতে নবী (ﷺ):\n\nاَللّٰهُمَّ صَلِّ عَليٰ سَيِّدِنَا مُحَمَّدٍنِ النَّبِيِّ اْلاُمِيِّ\n\nআল্লাহুম্মা সাল্লি আলা সাইয়্যিদিনা মুহাম্মাদিন নিন্নাবিয়্যিল উম্মিয়্যি।\n\nফযিলত: হযরত শেখ আব্দুল কাদের জিলানী (রা:) বা বড় পীর (রা:) গুনিয়াতুত্তালিবীন এ লিখেছেন, রাসূল সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম বলেছেন, যে ব্যক্তি জুমার রাতে দুই রাকাত নফল নামাজ এই নিয়্যতে পড়ে যে, প্রত্যেক রাকাতে সূরা ফাতিহার পর ১বার আয়াতুল কুরসী ও ১৫বার সূরা ইখলাস এবং নামাজ শেষে এই দুরূদ শরীফ ১০০০ বার পড়বে অবশ্যই সে ব্যক্তি আমাকে স্বপ্নে দেখতে পাবে। যদি ঐ রাতে না দেখে তবে ২য় শুক্রবার আসার পূর্বে দেখতে পাবে। এবং তার সমস্ত গুনাহ মাফ হয়ে যাবে।\n\"\n  },\n  {\n    \"dua\": \"(২৭) সাইয়্যেদা ফাতিমা (রা:) রচিত দুরূদ শরীফ:-\n\nاَللّٰهُمَّ صَلِّ عَليٰ مَنْ رُوْحُهُ مِحْرَابُ الْأَرْوَاحِ وَالْمَلٰئِكَةِ وَالْكَوْنِ- اَللّٰهُمَّ صَلِّ عَليٰ مَنْ هُوَ اِمَامُ اْلاَنْبِيَاءِ وَالُمُرْسَلِيْنَ- اَللّٰهُمَّ صَلِّ عَليٰ مَنْ هُوَ اِمَامُ اَهْلِ الْجَنَّةِ عِبَادَ اللهِ الْمُؤْمِنِيْنَ\n\n(আল্লাহুম্মা ছাল্লি আলা মান রুহুহু মিহরাবুল আরওয়াহি ওয়াল মলাইকাতি ওয়াল কাউনি। আল্লাহুম্মা ছাল্লি আলা মানহুয়া ইমামুল আম্বিয়ায়ি ওয়াল মুরসালীনা। আল্লাহুম্মা ছাল্লি আলা মানহুয়া ইমামু আহলিল জান্নাতি ইবাদিল্লাহিল মুমিনীন।)\n\"\n  },\n  {\n    \"dua\": \"(২৮) দুনিয়াতে জান্নাত দেখার দুরূদ শরীফ:\n\nصَلَّى اللهُ عَليٰ حَبِيْبِهٖ مُحَمَّدٍ وَاٰلِهٖ وَسلَّمَ\n\nছাল্লাল্লাহু আলা হাবীবিহী মুহাম্মাদিন ওয়া আলিহী ওয়াসাল্লিম।\n\nফযিলত: যে ব্যক্তি এই দুরূদ শরীফ একাধারে এক হাজার বার পাঠ করবে, তার মৃত্যুর পূর্বে সে অবশ্যই জান্নাতে তার স্থান ও জান্নাতের ঘর দেখতে পাবে।\n\"\n  },\n  {\n    \"dua\": \"(২৯) দরূদে তাজ:\n\nاَللّٰهُمَّ صَلِّ عَلىٰ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ صَاحِبِ التَّاجِ وَالْمِعْرَاجِ وَالْبُرَاقِ وَالْعَلَمِ دَافِعِ الْبَلَاءِ وَالْوَبَاءِ وَالْقَحَطِ وَالْمَرَضِ وَالاَلَمِ اِسْمُهُ مَكْتُوْبٌ مَرْفُوْعٌ مَشْفُوْعٌ مَنْقُوْشٌ فِي اللَّوْحِ وَالْقَلَمِ سَيِّدِ الْعَرَبِ وَالْعَجَمِ جِسْمُهُ مُقَدَّسٌ مُعَطَّرٌ مُطَهَّرٌ مُنَوَّرٌ فِي الْبَيْتِ وَالْحَرَمِ شَمْسِ الضُّحىٰ بَدْرِ الدَّجىٰ صَدْرِ الْعُلىٰ نُوْرِ الْهُدٰى كَهْفِ الْوَرٰى مِصْبَاحِ الظُّلَمِ جَمِيْلِ الشِّيَمِ شَفِيْعِ الْاُمَمِ صَاحِبِ الْجُوْدِ وَالْكَرَمِ وَاللهُ عَاصِمُهُ وَجِبْرِيْلُ خَادِمُهُ وَالْبُرَاقُ مَرْكَبُهُ وَالْمِعْرَاجُ سَفَرُهُ وَسِدْرَةُ الْمُنْتَهٰى مَقَامُهُ وَقَابَ قَوْسَيْنِ مَطْلُوْبُهُ وَالْمَطْلُوْبُ مَقْصُوْدُهُ وَالْمَقْصُوْدُ مَوْجُوْدُهُ سَيِّدِ الْمُرْسَلِيْنَ خَاتَمِ النَّبِيّيْنَ شَفِيْعِ الْمُذْنَبِيْنَ اَنِيْسِ الْغَرِيْبِيْنَ رَحْمَةً لِلْعٰلَمِيْنَ رَاحَةً الْعَاشِقِيْنَ مُرَادِ الْمُشْتَاقِيْنَ شَمْسِ الْعَارِفِيْنَ سِرَاجِ السَّالِكِيْنَ مِصْبَاحِ الْمُقَرَّبِيْنَ مُحِبِّ الْفُقَرَاءِ وَالْمَسَاكِيْنَ سَيِّدِ الثَّقْلَيْنِ نَبِيِّ الْحَرَمَيْنِ اِمَامِ الْقِبْلَتَيْنِ وَسِيْلَتِنَا فِي الدَّارَيْنِ صَاحِبِ قَابَ قَوْسَيْنِ مَحْبُوْبِ رَبِّ الْمَشْرِقَيْنِ وَالْمَغْرِبَيْنِ جَدِّ الْحَسَنِ وَالْحُسَيْنِ مَوْلَانَا وَمَوْلىٰ الثَّقْلَيْنِ اَبِى الْقَاسِمِ مُحَمَّدِ بْنِ عَبْدِ اللهِ نُوْرٍ مِنْ نُوْرِ اللهِ يَا اَيُّهَا الْمُشْتَاقُوْنَ بِنُوْرِ جَمَالِهٖ صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيْمًا\n\nআল্লাহুম্মা সাল্লি আলা সায়্যিদিনা ওয়া মাওলানা মুহাম্মাদিন, সাহিবিত্ তাজি ওয়াল মিরাজি ওয়াল বুরাক্বি ওয়াল আলাম। দা-ফিয়িল বালা–য়ি, ওয়াল ওবা–য়ি, ওয়াল ক্বাহাতি, ওয়াল মারাদ্বি, ওয়াল আলাম। ইসমুহু মাক্বতুবুন, মারফুউন, মাশফুউন, মানকূশুন, ফিল-লাওহি ওয়াল ক্বালাম। সায়্যিদিল আরাবি ওয়াল আজম। জিসমুহু মুক্বাদ্দাসুন, মুয়াত্তারুন, মতাহ্হারুন, মুনাও-ওয়ারুন, ফিল বাইতি ওয়াল হারাম। শাসছিদ্দুহা, বদরিদ্দুজা, সাদরিল-উলা, নু-রিল হুদা, কাহফিল ওয়ারা, মিসবাহিয্ যুলাম। জামীলিশ্ শিয়ামি শাফিয়িল উমামি, সা-হিবিল জু-দি ওয়াল কারাম। ওয়াল্লাহু আছিমুহু, ওয়া জিব্রীলু খাদিমুহু, ওয়াল বুরাক্বু মারকাবুহু, ওয়াল মিরাজু ছাফারুহু, ওয়া সিদরাতুল মুন্তাহা মাক্বামুহু ওয়া ক্বাবা ক্বাওসাইনি, মাতলুবুহু ওয়াল মাতলুবু, মাক্বসুদহু ওয়াল মাক্বসুদু মাওজুদুহু, সায়্যিদিল মুরসালীনা, খা-তামিন নাবিইয়্যীনা, শাফিয়িল মুযনিবীনা, আনীছিল গারীবীনা রাহমাতাল্লিল আলামীনা, রাহাতিল আ-শিক্বীনা, মুরাদিল মুশ্তাক্বীনা, শামছিল আ-রিফীনা, সিরাজিছ্ ছা-লিকিনা, মিছবাহিল্ মুকাররাবীনা, মুহিব্বিল্ ফোক্বারায়ি– ওয়াল গোরাবায়ি, ওয়াল মাছাকীনা, সয়্যিদিছ্ ছাক্বলাইনি, নাবিয়্যিল হারামায়নি, ইমামিল ক্বিবলাতাইনি, ওয়াসীলাতিনা ফিদ্দারায়নি, ছাহিবি ক্বা-বা ক্বাওছাইনি, মাহ্বূবি রাব্বিল মাশরিকায়নি ওয়াল মাগরিবাইনি, জাদ্দিল হাসানি ওয়াল হুসাইনি (রাদ্বিআল্লাহু আন্হুমা) মাওলানা ওয়া মাওলাছ্ সাক্বলাইনি, আবিল ক্বাছিম মুহাম্মদ বিন আব্দিল্লাহি নূরিম মিন নূরিল্লাহ। ইয়া আয়য়ূহাল মুশতাকুনা বিনূরি জামালিহী সাল্লু আলায়হি ওয়া সাল্লিমূ তাসলীমা।\"\n  }\n]", new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			viewpager1.setAdapter(new Viewpager1Adapter(map));
			((PagerAdapter)viewpager1.getAdapter()).notifyDataSetChanged();
		}catch(Exception e){
			 
		}
		clicklist = 0;
		mp = new SoundPool((int)(2), AudioManager.STREAM_MUSIC, 0);
		s1 = mp.load(getApplicationContext(), R.raw.tasbih_click, 1);

	}

	private void setWindowFlag(final int bits, boolean on) {
		    Window win = getWindow();
		    WindowManager.LayoutParams winParams = win.getAttributes();
		    if (on) {
					        winParams.flags |= bits;
					    } else {
					        winParams.flags &= ~bits;
					    }
		    win.setAttributes(winParams);
	}
	{
	}
	private String replaceArabicNumber(String n) {
		return n.replace("0", "০")
				.replace("1", "১")
				.replace("2", "২")
				.replace("3", "৩")
				.replace("4", "৪")
				.replace("5", "৫")
				.replace("6", "৬")
				.replace("7", "৭")
				.replace("8", "৮")
				.replace("9", "৯");
	}
	private String replaceBanglaNumberToEnglish(String n) {
		return n.replace("০", "0")
				.replace("১", "1")
				.replace("২", "2")
				.replace("৩", "3")
				.replace("৪", "4")
				.replace("৫", "5")
				.replace("৬", "6")
				.replace("৭", "7")
				.replace("৮", "8")
				.replace("৯", "9");
	}

	@Override
	public void onStart() {
		super.onStart();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		// Set initial count in Bangla
		if (save.getString("count", "").equals("")) {
			textview2.setText(replaceArabicNumber("0"));
		} else {
			textview2.setText(replaceArabicNumber(save.getString("count", "")));
		}

		// Set initial sound state
		if (save.getString("sound", "").equals("")) {
			textview5.setText("সাউন্ড অন");
		} else {
			textview5.setText(save.getString("sound", ""));
		}
	}


	public void _DARK_ICONS() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
	}
	
	
	public void _NavStatusBarColor(final String _color1, final String _color2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Window w = this.getWindow();	w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);	w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			w.setStatusBarColor(Color.parseColor("#" + _color1.replace("#", "")));	w.setNavigationBarColor(Color.parseColor("#" + _color2.replace("#", "")));
		}
	}
	
	
	public void _Animator(final View _view, final String _propertyName, final double _value, final double _duration) {
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget(_view);
		anim.setPropertyName(_propertyName);
		anim.setFloatValues((float)_value);
		anim.setDuration((long)_duration);
		anim.start();
	}
	
	
	public void _ClickAnimation(final View _view) {
		_view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:{
						_Animator(_view, "elevation", 5, 70);
						_Animator(_view, "scaleX", 0.95d, 70);
						_Animator(_view, "scaleY", 0.95d, 70);
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										_Animator(_view, "elevation", 1, 100);
										_Animator(_view, "scaleX", 0.9d, 100);
										_Animator(_view, "scaleY", 0.9d, 100);
									}
								});
							}
						};
						_timer.schedule(timer, (int)(70));
						break; }
					case MotionEvent.ACTION_UP:{ timer.cancel();
						_Animator(_view, "elevation", 10, 100);
						_Animator(_view, "scaleX", 1, 100);
						_Animator(_view, "scaleY", 1, 100);
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										_Animator(_view, "elevation", 5, 100);
										_Animator(_view, "scaleX", 1, 100);
										_Animator(_view, "scaleY", 1, 100);
									}
								});
							}
						};
						_timer.schedule(timer, (int)(100));
						break; } } return false; } });
	}
	
	
	public void _Shadow(final double _sadw, final double _cru, final String _wc, final View _widgets) {
		android.graphics.drawable.GradientDrawable wd = new android.graphics.drawable.GradientDrawable();
		wd.setColor(Color.parseColor(_wc));
		wd.setCornerRadius((int)_cru);
		_widgets.setElevation((int)_sadw);
		_widgets.setBackground(wd);
	}
	
	public class Viewpager1Adapter extends PagerAdapter {
		
		Context _context;
		ArrayList<HashMap<String, Object>> _data;
		
		public Viewpager1Adapter(Context _ctx, ArrayList<HashMap<String, Object>> _arr) {
			_context = _ctx;
			_data = _arr;
		}
		
		public Viewpager1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_context = getApplicationContext();
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public boolean isViewFromObject(View _view, Object _object) {
			return _view == _object;
		}
		
		@Override
		public void destroyItem(ViewGroup _container, int _position, Object _object) {
			_container.removeView((View) _object);
		}
		
		@Override
		public int getItemPosition(Object _object) {
			return super.getItemPosition(_object);
		}
		
		@Override
		public CharSequence getPageTitle(int pos) {
			// Use the Activity Event (onTabLayoutNewTabAdded) in order to use this method
			return "page " + String.valueOf(pos);
		}
		
		@Override
		public Object instantiateItem(ViewGroup _container,  final int _position) {
			View _view = LayoutInflater.from(_context).inflate(R.layout.just, _container, false);
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final ScrollView vscroll2 = _view.findViewById(R.id.vscroll2);
			final LinearLayout book = _view.findViewById(R.id.book);
			final LinearLayout linear21 = _view.findViewById(R.id.linear21);
			final TextView textview3 = _view.findViewById(R.id.textview3);
			final LinearLayout bookback = _view.findViewById(R.id.bookback);
			final LinearLayout linear20 = _view.findViewById(R.id.linear20);
			final LinearLayout booknext0 = _view.findViewById(R.id.booknext0);
			final ImageView imageview9 = _view.findViewById(R.id.imageview9);
			final ImageView imageview14 = _view.findViewById(R.id.imageview14);
			
			textview3.setText(map.get((int)_position).get("dua").toString());
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d*15);
				SketchUi.setStroke(d*4,0xFF01837A);
				linear1.setElevation(d*7);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFFE0E0E0}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
			}
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFF01837A);
				SketchUi.setCornerRadius(d*360);
				bookback.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFFE0E0E0}), SketchUi, null);
				bookback.setBackground(SketchUi_RD);
			}
			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFF01837A);
				SketchUi.setCornerRadius(d*360);
				booknext0.setElevation(d*5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFFE0E0E0}), SketchUi, null);
				booknext0.setBackground(SketchUi_RD);
			}
			bookback.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					viewpager1.setCurrentItem((int)_position - 1);
				}
			});
			booknext0.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					viewpager1.setCurrentItem((int)_position + 1);
				}
			});
			viewpager1.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
							@Override
							public void onPageScrolled(int _position, float _positionOffset, int _positionOffsetPixels) {
									if (_position == 0) {
											bookback.setVisibility(View.GONE);
									}
									else {
											bookback.setVisibility(View.VISIBLE);
									}
									if (_position == 28) {
											booknext0.setVisibility(View.GONE);
									}
									else {
											booknext0.setVisibility(View.VISIBLE);
									}
							}
							
							@Override
							public void onPageSelected(int _position) {
									if (_position == 0) {
											bookback.setVisibility(View.GONE);
									}
									else {
											bookback.setVisibility(View.VISIBLE);
									}
									if (_position == 28) {
											booknext0.setVisibility(View.GONE);
									}
									else {
											booknext0.setVisibility(View.VISIBLE);
									}
							}
							
							@Override
							public void onPageScrollStateChanged(int _scrollState) {
									if (_position == 0) {
											bookback.setVisibility(View.GONE);
									}
									else {
											bookback.setVisibility(View.VISIBLE);
									}
									if (_position == 28) {
											booknext0.setVisibility(View.GONE);
									}
									else {
											booknext0.setVisibility(View.VISIBLE);
									}
							}
					});
			
			_container.addView(_view);
			return _view;
		}
	}
}
