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
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import peacebe.PeaceBeServer;

public class PlayerActivity extends Activity {    
	private FrameLayout paintFrame;
	private Button nextButton;
	private PaintView paintView;
	private VoteView voteView;
	private GroupingResultView groupingResultView;
	private String state;
	private String app;
	private String clientState;
	//private PeaceBeServer.FakePeaceBeServer srv;
	private PeaceBeServer srv;   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerpaint);
        //srv = new PeaceBeServer().getFake();
        srv = new PeaceBeServer();

        paintFrame = (FrameLayout) findViewById(R.id.paintFrame);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener()
        {

        	public void onClick(View v) {
        		if (paintView==null){
                    int vHeight = paintFrame.getHeight();
                    int vWidth = paintFrame.getWidth();
                    paintView = new PaintView(paintFrame.getContext(), vHeight, vWidth);
                    voteView = new VoteView(paintFrame.getContext(), vHeight, vWidth);
                    groupingResultView = new GroupingResultView(paintFrame.getContext(), vHeight, vWidth);
                    clientState="main";
        		}
        		Log.i(getLocalClassName(), "1 app[ " + app + "] clientState [" + clientState + "]");
        		if (clientState=="main") {
        			JSONObject result = srv.getState();
        			if (result == null) {
        				return;
        			}
        			try {
						app = result.getString("app");
						state = result.getString("state");
						Log.i(getLocalClassName(), "1 app [" + app + "] state [" + state + "]");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		if (app.equals("grouping") && state.equals("painting")){
	        			paintFrame.addView(paintView);
	        			clientState="painting";
	        			Log.i(getLocalClassName(), "main painting");
	        		} else if (app.equals("grouping") && state.equals("voting")) {
	        			JSONArray q = srv.getCandidate();
	        			voteView.setCandidate(q);
	        			paintFrame.addView(voteView);
	        			clientState="voting";
	        			Log.i(getLocalClassName(), "main voting");
	        		} else if (app.equals("grouping") && state.equals("result")) {
	        			JSONObject m = srv.getGroupingResult();
	        			groupingResultView.setResult(m);
	        			paintFrame.addView(groupingResultView);
	        			clientState="result";
	        			Log.i(getLocalClassName(), "main result");
	        		}
        		} else {
	        		if (app.equals("grouping") && clientState.equals("painting")){
	        			paintFrame.removeView(paintView);
	        			Bitmap bitmap = paintView.getBitmap();
	        			srv.sendPaint(bitmap);
	        			clientState="main";
	        			Log.i(getLocalClassName(), "grouping painting");
	        		} else if (app.equals("grouping") && clientState.equals("voting")) {
	        			paintFrame.removeView(voteView);
	        			int id =voteView.getVote();
	        			srv.sendVote(id);
	        			clientState="main";
	        			Log.i(getLocalClassName(), "grouping voting");
	        			Log.i(getLocalClassName(), "voted " + id);
	        		} else if (app.equals("grouping") && clientState.equals("result")) {
	        			paintFrame.removeView(groupingResultView);
	        			clientState="main";
	        			Log.i(getLocalClassName(), "grouping stop");
	        		}	
        		}
        		Log.i(getLocalClassName(), "1 app " + app + " clientState " + clientState);
                
			}
        });
     }
    public boolean doLeave() {
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
    	menu.add(0, LEAVE_MENU_ID, 0, "Leave").setShortcut('6', 'l');
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	ActivityView view = (ActivityView) paintFrame.getChildAt(0);
    	view.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        	case LEAVE_MENU_ID:
        		doLeave();
        }
        return super.onOptionsItemSelected(item);
    }
}
