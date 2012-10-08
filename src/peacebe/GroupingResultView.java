package peacebe;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MenuItem;
public class GroupingResultView extends ActivityView {
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