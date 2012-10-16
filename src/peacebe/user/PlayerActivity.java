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

package peacebe.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import peacebe.user.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import peacebe.common.ActivityView;
import peacebe.common.Helper;
import peacebe.common.PeaceBeServer;
import peacebe.common.ViewOption;

public class PlayerActivity extends Activity {    
	private PeaceBeServer srv = PeaceBeServer.factoryGet();   
	private FrameLayout paintFrame;
	private Button nextButton;
	private PaintView paintView;
	private VoteView voteView;
	private ImageView groupingResultView;
	private ProgressBar pgbWaiting;
	private Handler mHandler;
	private Handler mUiHandler = new Handler();
	private HandlerThread mTaskThread;
	private String state;
	private String app;
	private String clientState="main";
	private boolean isInited=false;
    private JSONArray mCandidate;
    public boolean isPaintFrameReady(){
        int vHeight = paintFrame.getHeight();
        int vWidth = paintFrame.getWidth();
        if(vHeight <= 0 || vWidth <= 0) {
        	return false; 
        }  else {
        	return true;
        }
    }
	public void initTaskViews() {
        int vHeight = paintFrame.getHeight();
        int vWidth = paintFrame.getWidth();
        paintView = new PaintView(paintFrame.getContext(), vHeight, vWidth);
        voteView = new VoteView(paintFrame.getContext(), vHeight, vWidth);
        profileingView = new ImageView(paintFrame.getContext());
        groupingResultView = new ImageView(paintFrame.getContext());
	}
	public void initMainView() {
        setContentView(R.layout.main);
        paintFrame = (FrameLayout) findViewById(R.id.paintFrame);
        pgbWaiting = (ProgressBar) findViewById(R.id.pgbWaiting);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v) {
        		Log.i(getLocalClassName(), "1 app[ " + app + "] clientState [" + clientState + "]");
        		uiMain();
        		mHandler.post(onClick);
        	}	
        });	
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		settings = this.getPreferences(MODE_WORLD_WRITEABLE);
		editor = settings.edit();
		mTaskThread = new HandlerThread("task");
		mTaskThread.start();
		mHandler = new Handler(mTaskThread.getLooper());
		initMainView();
		// Do the post init by main timer.
    	mHandler.postDelayed(mainTimer, 200);
    }
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
    public int getPlayer(){
		int player = settings.getInt("player", 1);
    	return player;
    }
    public void setPlayer(int player){
        editor.putInt("player", player);
        editor.commit();
    }
    private Runnable uiTimer = new Runnable(){
    	public void run(){
    		Log.i("run","uiTimer");
    		if (! isInited){
    			initTaskViews();
    			uiMain();
    			isInited=true;
    			Log.i("run","uiTImer inited");
    		}
    		if(clientState.equals("main")){
    			Log.e(getLocalClassName(),"Button should not be clicked in main state");
    			return;
    		}
    		if (app.equals("grouping") && state.equals("painting")){
    			uiPainting();
    		} else if (app.equals("grouping") && state.equals("voting")) {
    			uiVoting();
    		} else if (app.equals("grouping") && state.equals("result")) {
    			uiResult();
    		} else if (app.equals("profiling") && state.equals("profiling")) {
    			uiProfiling();
    		} 
    	}
    };
    private Runnable onClick = new Runnable(){
    	public void run(){
    		Log.i("run","onClick");
    		if (clientState.equals("main")) {
    			Log.e(getLocalClassName(),"Button should not be clicked in main state");
    			return;
    		}
    		if (app.equals("grouping") && clientState.equals("painting")){
    			Bitmap bitmap = paintView.getBitmap();
    			srv.sendPaint(bitmap);
    		} else if (app.equals("grouping") && clientState.equals("voting")) {
    			int id = voteView.getVote();
    			srv.sendVote(id);
    		} else if (app.equals("grouping") && clientState.equals("result")) {
    		} else if (app.equals("profiling") && clientState.equals("profiling")) {
    			srv.sendProfile(mProfilePhoto);
    		}    		
    		clientState="main";
        	mHandler.postDelayed(mainTimer, 1);
    	}
    };
    private Runnable mainTimer = new Runnable() {
    	public void run() {
    		Log.i("run","mainTimer isInited:"+ isInited);
    		if (! isInited) {
				if (! isPaintFrameReady()){
					mHandler.postDelayed(mainTimer, 100);
					return;
				}
				int player = getPlayer();
				srv.setPlayer(player);
    		}
    		if (! clientState.equals("main")) {
        		Log.e(getLocalClassName(),"mainTimer should not be triger in task state.");
        		return;
    		}
			JSONObject result = srv.getState();
			if (result == null) {
				Log.e(getLocalClassName(),"Failed to get state from server.");
				return;
			}
			try {
				app = result.getString("app");
				state = result.getString("state");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("STATE", "app:" + app + ",state:" + state);
    		if (app.equals("grouping") && state.equals("painting")){
    			clientState="painting";
    			toPainting();
    		} else if (app.equals("grouping") && state.equals("voting")) {
    			mCandidate = srv.getCandidate();
    			clientState="voting";
    			toVoting();
    		} else if (app.equals("grouping") && state.equals("result")) {
				JSONObject m = srv.getGroupingResult();
				setResultBitmap(m);
				clientState="result";
    			toResult();
    		} else if (app.equals("profiling") && state.equals("profiling")) {
    			clientState="profiling";
    			toProfiling();
    		} else { /* Still in main state */
    			mHandler.postDelayed(mainTimer, 200);
    		}
    	}
    }; 
    public void toProfiling(){
        mHandler.removeCallbacks(mainTimer);
        mUiHandler.post(uiTimer);
    }
    public void uiProfiling(){
    	pickImage();
		paintFrame.addView(profileingView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
    }
	public void uiMain(){
		paintFrame.removeAllViews();
		pgbWaiting.setVisibility(ProgressBar.VISIBLE);
		nextButton.setVisibility(Button.GONE);
	}
    public void uiPainting(){
		new AlertDialog.Builder(paintFrame.getContext())
        .setTitle("Task")
        .setMessage("Draw your favorite animal.")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
            }
         })
     	.show();
		paintFrame.addView(paintView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
    }
    public void toPainting(){
        mHandler.removeCallbacks(mainTimer);
        mUiHandler.post(uiTimer);
    }
    public void uiVoting(){
		voteView.setCandidate(mCandidate);
		paintFrame.addView(voteView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
    }
    public void toVoting(){
        mHandler.removeCallbacks(mainTimer);
        mUiHandler.post(uiTimer);
    }
    Bitmap mBitmap=null;
    public void setResultBitmap(JSONObject mResult) {
		try {
			mBitmap = Helper.getBitmapFromString(mResult.getString("photo"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void toResult(){
        mHandler.removeCallbacks(mainTimer);
        mUiHandler.post(uiTimer);
    }
    public void uiResult(){
		paintFrame.addView(groupingResultView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
		groupingResultView.setImageBitmap(mBitmap);
    }
    public boolean resetPlayer() {
        final CharSequence[] items = {"Player1", "Player2", "Player3","Player4","Player5","Player6","Player7","Player8"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a player");
        builder.setItems(items, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int item) {
                Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                int player = item+1;
                setPlayer(player);
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

        /****   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        *****/
        return true;
    }
    public class MyImageView extends ImageView implements ViewOption {

		public MyImageView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onPrepareOptionsMenu(Menu menu) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    private static final int LEAVE_MENU_ID = Menu.FIRST+6;
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
    	ActivityView view = (ActivityView) paintFrame.getChildAt(0);
    	if (view != null){
    		view.onOptionsItemSelected(item);
    	}
        switch (item.getItemId()) {
        	case LEAVE_MENU_ID:
        		resetPlayer();
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private static final int REQUEST_CODE = 1;
    private Bitmap mProfilePhoto;
    private ImageView profileingView;
    public void pickImage() {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      startActivityForResult(intent, REQUEST_CODE);
    }
    public void toInit(){
    	isInited = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        try {
          // We need to recyle unused bitmaps
          if (mProfilePhoto != null) {
            mProfilePhoto.recycle();
          }
          InputStream stream = getContentResolver().openInputStream(data.getData());
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inPurgeable = true;
          options.inInputShareable = true;
          options.inSampleSize = 8000;
          mProfilePhoto = BitmapFactory.decodeStream(stream,null,options);
          stream.close();
          String a = Helper.getStringFromBitmap(mProfilePhoto);
          Bitmap b = Helper.getBitmapFromString(a);
          profileingView.setImageBitmap(b);
          //profileingView.setImageBitmap(mProfilePhoto);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      super.onActivityResult(requestCode, resultCode, data);
    }
}
