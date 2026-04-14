package com.srizwan.islamipedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.*;
import android.os.*;
import android.os.Bundle;
import android.view.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

public class LiveActivity extends AppCompatActivity {

	private ArrayList<HashMap<String, Object>> live = new ArrayList<>();

	private LinearLayout toolbar;
	private LinearLayout spin;
	private LinearLayout content;
	private ImageView list;
	private LinearLayout box;
	private TextView bookname;
	private ProgressBar spinber;
	private LinearLayout Nointernet;
	private ImageView imageview3;
	private TextView textview1;
	private MaterialButton materialbutton1;
	private RecyclerView recyclerview1;

	private RequestNetwork request;
	private RequestNetwork.RequestListener _request_request_listener;
	private Intent in = new Intent();

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.live);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	private void initialize(Bundle _savedInstanceState) {
		toolbar = findViewById(R.id.toolbar);
		spin = findViewById(R.id.spin);
		content = findViewById(R.id.content);
		list = findViewById(R.id.list);
		box = findViewById(R.id.box);
		bookname = findViewById(R.id.bookname);
		spinber = findViewById(R.id.spinber);
		Nointernet = findViewById(R.id.Nointernet);
		imageview3 = findViewById(R.id.imageview3);
		textview1 = findViewById(R.id.textview1);
		materialbutton1 = findViewById(R.id.materialbutton1);
		recyclerview1 = findViewById(R.id.recyclerview1);
		request = new RequestNetwork(this);
		list.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		materialbutton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				request.startRequestNetwork(RequestNetworkController.GET, "https://www.dropbox.com/scl/fi/ydrnh1pk1ueuh38hcwhq8/live.json?rlkey=egwh3aff6yolzzu2xgluq1twa&st=ae8roj98&dl=1", "Rizwan", _request_request_listener);
			}
		});

		_request_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				live = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType());
				recyclerview1.setAdapter(new Recyclerview1Adapter(live));
				spin.setVisibility(View.GONE);
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				spin.setVisibility(View.VISIBLE);
				Nointernet.setVisibility(View.VISIBLE);
			}
		};
	}

	private void initializeLogic() {
		_status_bar_color("#FF01837A", "#FF01837A");
		recyclerview1.setLayoutManager(new LinearLayoutManager(this));
		if (Rizwan.isConnected(getApplicationContext())) {
			request.startRequestNetwork(RequestNetworkController.GET, "https://www.dropbox.com/scl/fi/ydrnh1pk1ueuh38hcwhq8/live.json?rlkey=egwh3aff6yolzzu2xgluq1twa&st=ae8roj98&dl=1", "Rizwan", _request_request_listener);
		} else {

		}
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});
	}

	public void _status_bar_color(final String _colour1, final String _colour2) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Window w = this.getWindow();
			w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			w.setStatusBarColor(Color.parseColor(_colour1));
			w.setNavigationBarColor(Color.parseColor(_colour2));
		}
	}


	public String _replaceArabicNumber(final String _n) {
		String result = _n.replace("1", "১").replace("2", "২").replace("3", "৩").replace("4", "৪").replace("5", "৫").replace("6", "৬").replace("7", "৭").replace("8", "৮").replace("9", "৯").replace("0", "০");

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

			{
				android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
				int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
				SketchUi.setColor(0xFFFFFFFF);
				SketchUi.setCornerRadius(d * 20);
				SketchUi.setStroke(d * 1, 0xFF01837A);
				linear1.setElevation(d * 5);
				android.graphics.drawable.RippleDrawable SketchUi_RD = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{0xFF01837A}), SketchUi, null);
				linear1.setBackground(SketchUi_RD);
			}
			number.setText(_replaceArabicNumber(String.valueOf((long) (_position + 1))));
			name.setText(live.get((int) _position).get("name").toString());
			linear1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					in.setClass(getApplicationContext(), PlayerActivity.class);
					in.putExtra("name", live.get((int) _position).get("name").toString());
					in.putExtra("author", live.get((int) _position).get("videoid").toString());
					startActivity(in);
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