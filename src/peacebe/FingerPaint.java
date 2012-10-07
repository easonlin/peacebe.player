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
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import peacebe.PeaceBeServer;

public class FingerPaint extends Activity
        implements ColorPickerDialog.OnColorChangedListener {    
	private FrameLayout paintFrame;
	private Button nextButton;
	private PaintView paintView;
	private VoteView voteView;
	private GroupingResultView groupingResultView;
	private String state;
	private String app;
	private String clientState="main";
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
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }
    
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;
    
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class GroupingResultView extends View {
    	JSONObject mResult;
    	public GroupingResultView(Context c,  int vHeight, int vWidth){
    		super(c);
    	}

		public void setResult(JSONObject m) {
			// TODO Auto-generated method stub
			mResult = m;
		}
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFFFFFAA);
            Bitmap bitmap=null;
			try {
				bitmap = PeaceBeServer.getBitmapFromString(mResult.getString("photo"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG));
        }
    }
    public class VoteView extends View {
    	private  JSONArray mCandidate;
    	private int mVote=0;
    	public VoteView(Context c,  int vHeight, int vWidth){
    		super(c);
    	}

		public int getVote() {
			// TODO Auto-generated method stub
			try {
				return  mCandidate.getJSONObject(mVote).getInt("id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}

		public void setCandidate(JSONArray q) {
			// TODO Auto-generated method stub
			mCandidate = q;
		}
	    private void drawPict(Canvas canvas, int x, int y, int w, int h,
                int location) {
			canvas.save();
			canvas.translate(x, y);
			canvas.clipRect(0, 0, w, h);
			canvas.scale(0.5f, 0.5f);
			canvas.scale(1, 1, w, h);
			Bitmap bitmap = null;
			try {
				bitmap = PeaceBeServer.getBitmapFromString(mCandidate.getJSONObject(location).getString("paint"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(location == mVote){
				canvas.drawColor(0xFFFFFFAA);
			}
			canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG));
			canvas.restore();
		}
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
            }
            return true;
        }
        private void touch_start(float x, float y) {
        	setVote(x, y);
        }
        protected void setVote(float x, float y){
            int mX = getWidth()/2;
            int mY = getHeight()/2;
            if (x < mX && y < mY){
            	mVote = 0;
            } else if (x > mX && y < mY){
            	mVote = 1;
            } else if (x < mX && y > mY){
            	mVote = 2;
            } else if (x > mX && y > mY){
            	mVote = 3;
            }
        }
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFFFAAAA);
            int x = getWidth()/2;
            int y = getHeight()/2;
            drawPict(canvas, 0, 0, x, y,  0);
            drawPict(canvas, x, 0, x, y, 1);
            drawPict(canvas, 0, y, x, y,  2);
            drawPict(canvas, x, y, x, y, 3);
        }
    }
    public class PaintView extends View {
        
        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;
        
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;
        
        public PaintView(Context c, int vHeight, int vWidth) {
            super(c);
            //mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
            mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }
        public Bitmap getBitmap()
        {
        	return mBitmap;
        }
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);
            
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            
            canvas.drawPath(mPath, mPaint);
        }
        
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;
        
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
    
    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

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
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                        PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                    PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
