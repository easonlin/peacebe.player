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

package peacebe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import peacebe.user.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import peacebe.PeaceBeServer;

public class PlayerActivity extends Activity {    
	private FrameLayout paintFrame;
	private Button nextButton;
	private PaintView paintView;
	private VoteView voteView;
	private GroupingResultView groupingResultView;
	private ProgressBar pgbWaiting;
	private Handler handler = new Handler();
	private String state;
	private String app;
	private String clientState="init";
	public boolean initTaskViews() {
        int vHeight = paintFrame.getHeight();
        int vWidth = paintFrame.getWidth();
        if(vHeight <= 0 || vWidth <= 0) {
        	return false; 
        }
        paintView = new PaintView(paintFrame.getContext(), vHeight, vWidth);
        voteView = new VoteView(paintFrame.getContext(), vHeight, vWidth);
        groupingResultView = new GroupingResultView(paintFrame.getContext(), vHeight, vWidth);
        return true;
	}
	public void initMainView() {
        setContentView(R.layout.fingerpaint);
        paintFrame = (FrameLayout) findViewById(R.id.paintFrame);
        pgbWaiting = (ProgressBar) findViewById(R.id.pgbWaiting);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v) {
        		Log.i(getLocalClassName(), "1 app[ " + app + "] clientState [" + clientState + "]");
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
        		}	
        		toMain();
        	}	
        });	
	}
	//private PeaceBeServer.FakePeaceBeServer srv = new PeaceBeServer().getFake();
	private PeaceBeServer srv = new PeaceBeServer();   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		settings = this.getPreferences(MODE_WORLD_WRITEABLE);
		editor = settings.edit();
		initMainView();
		// Do the post init by main timer.
		toInit();
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
    private Runnable mainTimer = new Runnable() {
    	public void run() {
    		if (clientState.equals("init")) {
				if (! initTaskViews()){
					toInit();
					return;
				}
				int player = getPlayer();
				srv.setPlayer(player);
			    toMain(); 
			    return;
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
    		if (app.equals("grouping") && state.equals("painting")){
    			toPainting();
    		} else if (app.equals("grouping") && state.equals("voting")) {
    			toVoting();
    		} else if (app.equals("grouping") && state.equals("result")) {
    			toResult();
    		} else { /* Still in main state */
    			handler.postDelayed(mainTimer, 200);
    		}
    	}
    }; 
    public void toInit(){
        clientState="init";
        handler.postDelayed(mainTimer, 1);
    }
	public void toMain() {
		paintFrame.removeAllViews();
		pgbWaiting.setVisibility(ProgressBar.VISIBLE);
		nextButton.setVisibility(Button.GONE);
		handler.postDelayed(mainTimer, 1);
		clientState="main";
	}
    public void toPainting(){
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
        handler.removeCallbacks(mainTimer);
		clientState="painting";
    }
    public void toVoting(){
		JSONArray q = srv.getCandidate();
		voteView.setCandidate(q);
		paintFrame.addView(voteView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
        handler.removeCallbacks(mainTimer);
		clientState="voting";
    }
    public void toResult(){
		JSONObject m = srv.getGroupingResult();
		groupingResultView.setResult(m);
		paintFrame.addView(groupingResultView);
		nextButton.setVisibility(Button.VISIBLE);
		pgbWaiting.setVisibility(ProgressBar.GONE);
        handler.removeCallbacks(mainTimer);
		clientState="result";
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
    
    private static final int LEAVE_MENU_ID = Menu.FIRST+6;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	ActivityView view = (ActivityView) paintFrame.getChildAt(0);
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
}
