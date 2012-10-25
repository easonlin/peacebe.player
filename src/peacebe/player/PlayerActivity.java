/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package peacebe.player;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import peacebe.player.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import peacebe.common.IPeaceBeServer;
import peacebe.common.PeaceBeServer;
import peacebe.common.ViewOption;

public class PlayerActivity extends Activity {
	private IPeaceBeServer srv = PeaceBeServer.factoryGet();
	private FrameLayout paintFrame;
	private Button nextButton;
	private AlertDialog mTaskDialog;
	private ProgressBar pgbWaiting;
	private IApp mAppGrouping;
	private Handler mHandler;
	private Handler mUiHandler = new Handler();
	private HandlerThread mTaskThread;
	private String state;
	private String app;
	private boolean isInited = false;
	private TeamHandler mTeamHandler = new TeamHandler();
	public boolean isPaintFrameReady() {
		int vHeight = paintFrame.getHeight();
		int vWidth = paintFrame.getWidth();
		if (vHeight <= 0 || vWidth <= 0) {
			return false;
		} else {
			return true;
		}
	}
	AppProfiling mAppProfiling;
	public void initTaskViews() {
		mAppGrouping.init(paintFrame);
		mAppProfiling.init(paintFrame);
	}
	public void initMainView() {
		setContentView(R.layout.main);
		paintFrame = (FrameLayout) findViewById(R.id.paintFrame);
		pgbWaiting = (ProgressBar) findViewById(R.id.pgbWaiting);
		nextButton = (Button) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				uiMain();
				mHandler.post(onClick);
			}
		});
		mTaskDialog = new AlertDialog.Builder(paintFrame.getContext())
		.setTitle("Task")
		.setMessage("Draw your favorite animal.")
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
					}
				}).create();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("FLOW","onCreate");
		settings = this.getPreferences(MODE_WORLD_WRITEABLE);
		editor = settings.edit();
		mAppGrouping = new AppGrouping();
		mAppProfiling = new AppProfiling();
		mTaskThread = new HandlerThread("task");
		mTaskThread.start();
		mHandler = new Handler(mTaskThread.getLooper());
		initMainView();
		// Do the post init by main timer.
		mHandler.postDelayed(mainTimer, 400);
		mTeamHandler.start();
	}
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	public String getPlayer() {
		String player = UUID.randomUUID().toString();
		try {
			player = settings.getString("player", player);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return player;
	}
	public void setPlayer(String player) {
		editor.putString("player", player);
		editor.commit();
	}
	private Runnable uiTimer = new Runnable() {
		public void run() {
			Log.i("run", "uiTimer");
			if (!isInited) {
				initTaskViews();
				uiMain();
				isInited = true;
				Log.i("run", "uiTImer inited");
			}
			if (app.equals("grouping")){
				UiState uiState = new UiState(paintFrame);
				mAppGrouping.ui(app, state, uiState);
				uiTask(uiState);
			} else if (app.equals("profiling")){
				UiState uiState = new UiState(paintFrame);
				mAppGrouping.ui(app, state, uiState);
				uiTask(uiState);
			} else {
				uiMain();
			}
		}
	};
	private Runnable onClick = new Runnable() {
		public void run() {
			Log.i("run", "onClick");
			if (state.equals("main")) {
				Log.e(getLocalClassName(),
						"Button should not be clicked in main state");
				return;
			}
			if (app.equals("grouping")){
				SrvState srvState = new SrvState(srv);
				mAppGrouping.send(app, state, srvState);
			} else if (app.equals("profiling") ){
				SrvState srvState = new SrvState(srv);
				mAppProfiling.send(app, state, srvState);
			}
		}
	};
	private Runnable mainTimer = new Runnable() {
		public void run() {
			Log.i("run", "mainTimer isInited:" + isInited);
			if (!isInited) {
				if (!isPaintFrameReady()) {
					mHandler.postDelayed(mainTimer, 400);
					return;
				}
				String player = getPlayer();
				srv.setPlayer(player);
			}
			JSONObject result = srv.getState();
			if (result == null) {
				Log.e(getLocalClassName(), "Failed to get state from server.");
				mHandler.postDelayed(mainTimer, 400);
				return;
			}
			String oldapp=null;
			String oldstate=null;
			try {
				oldapp=app;
				oldstate=state;
				app = result.getString("app");
				state = result.getString("state");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.e(getLocalClassName(), "Failed to parse app and state.");
				mHandler.postDelayed(mainTimer, 400);
				return;
			}
			Log.i("STATE", "app:" + app + ",state:" + state);
			if (state.equals(oldstate)&&app.equals(oldapp)) {
				Log.i("STATE", "no state changed");
				mHandler.postDelayed(mainTimer, 400);
				return;
			}
			if (app.equals("grouping")) {
				SrvState srvState = new SrvState(srv);
				mAppGrouping.srv(app, state, srvState);
			} else if (app.equals("profiling")) {
				SrvState srvState = new SrvState(srv);
				mAppProfiling.srv(app, state, srvState);
			} else if (app.equals("main") && state.equals("stop")){
			}
			mUiHandler.post(uiTimer);
			mHandler.postDelayed(mainTimer, 400);
		}
	};
	class SrvState {
		public SrvState(IPeaceBeServer isrv) {
			// TODO Auto-generated constructor stub
			srv = isrv;
		}

		public IPeaceBeServer srv;
	}
	class UiState {
		public UiState(FrameLayout paintFrame) {
			// TODO Auto-generated constructor stub
			mPaintFrame = paintFrame;
		}
		public FrameLayout mPaintFrame;
		public boolean isBlock=false;
		public boolean isDialog=false;
	};
	public void uiTask(UiState state){
		paintFrame.removeAllViews();	
		if (state.isBlock){
			nextButton.setVisibility(Button.GONE);
			pgbWaiting.setVisibility(ProgressBar.VISIBLE);
		} else {
			nextButton.setVisibility(Button.VISIBLE);
			pgbWaiting.setVisibility(ProgressBar.GONE);
		}
		if(state.isDialog){
			mTaskDialog.show();
		} else {
			mTaskDialog.dismiss();
		}
	}


	public void uiMain() {
		mTaskDialog.dismiss();
		paintFrame.removeAllViews();
		pgbWaiting.setVisibility(ProgressBar.VISIBLE);
		nextButton.setVisibility(Button.GONE);
	}

	public boolean resetPlayer() {
		final CharSequence[] items = { "Player1", "Player2", "Player3",
				"Player4", "Player5", "Player6", "Player7", "Player8" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a player");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int item) {
				Toast.makeText(getApplicationContext(), items[item],
						Toast.LENGTH_SHORT).show();
				int player = item + 1;
				setPlayer(Integer.toString(player));
				// Do the post init by mainTimer
				toInit();
				return;
			}
		});
		builder.show();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		/****
		 * Is this the mechanism to extend with filter effects? Intent intent =
		 * new Intent(null, getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions( Menu.ALTERNATIVE, 0, new ComponentName(this,
		 * NotesList.class), null, intent, 0, null);
		 *****/
		return true;
	}
	private static final int LEAVE_MENU_ID = Menu.FIRST + 6;
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		ViewOption view = (ViewOption) paintFrame.getChildAt(0);
		if (view != null) {
			view.onPrepareOptionsMenu(menu);
		}
		menu.add(0, LEAVE_MENU_ID, 0, "Player").setShortcut('6', 'l');
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ViewOption view = (ViewOption) paintFrame.getChildAt(0);
		if (view != null) {
			view.onOptionsItemSelected(item);
		}
		switch (item.getItemId()) {
		case LEAVE_MENU_ID:
			resetPlayer();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onBackPressed() {
		Log.i("FLOW","onBackPressed");
		mHandler.removeCallbacks(mainTimer);
		mUiHandler.removeCallbacks(uiTimer);
		mTeamHandler.close();
		finish();
	}
	public void toInit() {
		isInited = false;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ViewOption view = (ViewOption) paintFrame.getChildAt(0);
		if (view != null) {
			view.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


}
