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

import org.json.JSONException;
import org.json.JSONObject;
import peacebe.player.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import peacebe.common.Setting;
import peacebe.common.ViewOption;

public class PlayerActivity extends Activity {
	private IPeaceBeServer srv = PeaceBeServer.factoryGet();
	private FrameLayout paintFrame;
	private Button nextButton;
	private AlertDialog taskDialog;
	private ProgressBar pgbWaiting;
	private IApp mAppGrouping;
	private IApp mAppProfiling;
	private Handler mTaskHandler;
	private Handler mUiHandler = new Handler();
	private HandlerThread mTaskThread;
	private Setting mSetting;
	private String mState;
	private String mApp;
	private boolean mIsInited = false;
	private boolean mIsTaskClosed = false;
	private int mTaskDelayed=600;
	//private TeamHandler mTeamHandler = new TeamHandler();
	class SrvState {
		public IPeaceBeServer srv;
		public SrvState(IPeaceBeServer isrv) {
			srv = isrv;
		}
	}
	class UiState {
		public boolean isBlock=false;
		public boolean isDialog=false;
		public boolean isMain=false;
		public View view=null;
	};
	private Runnable uiRunner = new Runnable() {
		public void run() {
			Log.i("run", "uiTimer");
			if (!mIsInited) {
				initTaskViews();
				mIsInited = true;
				Log.i("run", "uiTImer inited");
			}
			if (mApp.equals("grouping")){
				UiState uiState = new UiState();
				mAppGrouping.ui(mApp, mState, uiState);
				uiTask(uiState);
			} else if (mApp.equals("profiling")){
				UiState uiState = new UiState();
				mAppGrouping.ui(mApp, mState, uiState);
				uiTask(uiState);
			} else {
				uiMain();
			}
		}
	};
	private Runnable sendRunner = new Runnable() {
		public void run() {
			Log.i("run", "sendRunner");
			if (mState.equals("main")) {
				Log.e(getLocalClassName(),
						"Button should not be clicked in main state");
				return;
			}
			if (mApp.equals("grouping")){
				SrvState srvState = new SrvState(srv);
				mAppGrouping.send(mApp, mState, srvState);
			} else if (mApp.equals("profiling") ){
				SrvState srvState = new SrvState(srv);
				mAppProfiling.send(mApp, mState, srvState);
			}
		}
	};
	private Runnable taskRunner = new Runnable() {
		public void run() {
			if(mIsTaskClosed){
				Log.i("run","taskRunner is closed");
				return;
			}
			Log.i("run", "taskTimer isInited:" + mIsInited);
			if (!mIsInited) {
				if (!isPaintFrameReady()) {
					mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
					return;
				}
			}
			JSONObject result = srv.getState();
			if (result == null) {
				Log.e(getLocalClassName(), "Failed to get state from server.");
				mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
				return;
			}
			String oldapp=null;
			String oldstate=null;
			try {
				oldapp=mApp;
				oldstate=mState;
				mApp = result.getString("app");
				mState = result.getString("state");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.e(getLocalClassName(), "Failed to parse app and state.");
				mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
				return;
			}
			Log.i("STATE", "app:" + mApp + ",state:" + mState);
			if (mState.equals(oldstate)&&mApp.equals(oldapp)) {
				Log.i("STATE", "no state changed");
				mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
				return;
			}
			if (mApp.equals("grouping")) {
				SrvState srvState = new SrvState(srv);
				mAppGrouping.srv(mApp, mState, srvState);
			} else if (mApp.equals("profiling")) {
				SrvState srvState = new SrvState(srv);
				mAppProfiling.srv(mApp, mState, srvState);
			} else if (mApp.equals("main") && mState.equals("stop")){
			}
			mUiHandler.post(uiRunner);
			mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
		}
	};
	public boolean isPaintFrameReady() {
		int vHeight = paintFrame.getHeight();
		int vWidth = paintFrame.getWidth();
		if (vHeight <= 0 || vWidth <= 0) {
			return false;
		} else {
			return true;
		}
	}
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
				mTaskHandler.post(sendRunner);
			}
		});
		taskDialog = new AlertDialog.Builder(paintFrame.getContext())
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
		mSetting = new Setting(this);
		srv.setPlayer(mSetting.getPlayer());
		mAppGrouping = new AppGrouping();
		mAppProfiling = new AppProfiling();
		mTaskThread = new HandlerThread("task");
		mTaskThread.start();
		mTaskHandler = new Handler(mTaskThread.getLooper());
		initMainView();
		// Do the post init by main timer.
		mTaskHandler.postDelayed(taskRunner, mTaskDelayed);
		//mTeamHandler.start();
	}

	public void uiTask(UiState state){
		if(state.isMain){
			uiMain();
			return;
		} 
		// else uiTask()
		paintFrame.removeAllViews();
		paintFrame.addView(state.view);
		if (state.isBlock){
			nextButton.setVisibility(Button.GONE);
			pgbWaiting.setVisibility(ProgressBar.VISIBLE);
		} else {
			nextButton.setVisibility(Button.VISIBLE);
			pgbWaiting.setVisibility(ProgressBar.GONE);
		}
		if(state.isDialog){
			taskDialog.show();
		} else {
			taskDialog.dismiss();
		}
	}
	public void uiMain() {
		taskDialog.dismiss();
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
				srv.setPlayer(Integer.toString(player));
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
		mTaskHandler.removeCallbacks(taskRunner);
		mUiHandler.removeCallbacks(uiRunner);
		mIsTaskClosed = true;
		//mTeamHandler.close();
		finish();
	}
	public void toInit() {
		mIsInited = false;
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
