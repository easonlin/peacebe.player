package peacebe.user;

import org.json.JSONArray;
import org.json.JSONException;

import peacebe.common.PeaceBeServer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
public class VoteView extends ActivityView {
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
		//canvas.scale(0.5f, 0.5f);
		canvas.scale(1, 1, w, h);
		Bitmap bitmap = null;
		try {
			bitmap = PeaceBeServer.getBitmapFromString(mCandidate.getJSONObject(location).getString("paint"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap resizedBitmap = PeaceBeServer.getResizedBitmap(bitmap, w, h);
		if(location == mVote){
			canvas.drawColor(0xFFFFFFAA);
		}
		canvas.drawBitmap(resizedBitmap, 0, 0, new Paint(Paint.DITHER_FLAG));
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}
}