package com.srizwan.islamipedia;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class PlayerActivity extends AppCompatActivity {

	private LinearLayout toolbar;
	private ScrollView vscroll1;
	private ImageView list;
	private LinearLayout box;
	private TextView bookname;
	private LinearLayout linear2;
	private YouTubePlayerView youtube1;
	private LinearLayout linear3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		// Initializing views
		initialize(savedInstanceState);

		// Adding lifecycle observer for YouTubePlayerView
		getLifecycle().addObserver(youtube1);

		// Initialize logic
		initializeLogic();
	}

	private void initialize(Bundle savedInstanceState) {
		toolbar = findViewById(R.id.toolbar);
		vscroll1 = findViewById(R.id.vscroll1);
		list = findViewById(R.id.list);
		box = findViewById(R.id.box);
		bookname = findViewById(R.id.bookname);
		linear2 = findViewById(R.id.linear2);
		youtube1 = findViewById(R.id.youtube1);
		linear3 = findViewById(R.id.linear3);

		// Back button click listener
		list.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
	}

	private void initializeLogic() {
		// Set the book name from the intent extra
		String bookTitle = getIntent().getStringExtra("name");
		_status_bar_color("#FF01837A", "#FF01837A");
		_marquue(bookname, getIntent().getStringExtra("name"));
		if (bookTitle != null) {
			_marquue(bookname, getIntent().getStringExtra("name"));
		} else {
			bookname.setText("No Title");
		}

		// Add YouTube player listener
		youtube1.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
			@Override
			public void onReady(@NonNull YouTubePlayer youTubePlayer) {
				// Get video ID from intent extras
				String videoId = getIntent().getStringExtra("author");
				if (videoId != null && !videoId.isEmpty()) {
					youTubePlayer.cueVideo(videoId, 0);
				} else {
					// Log or show a message for missing video ID
					bookname.setText("Video ID is missing");
				}
			}
		});
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
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Clean up YouTubePlayerView
		youtube1.release();
	}
}
