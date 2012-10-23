package peacebe.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import peacebe.common.Helper;
import peacebe.common.ViewOption;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class PhotoView extends ImageView implements ViewOption {
	private static final int REQUEST_CODE = 1;
	private Bitmap mProfilePhoto;
	public PhotoView(Context context) {
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
	public void pickImage() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		Activity activity = (Activity) getContext();
		activity.startActivityForResult(intent, REQUEST_CODE);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
			try {
				// We need to recyle unused bitmaps
				if (mProfilePhoto != null) {
					mProfilePhoto.recycle();
				}
				
				InputStream stream = getContext().getContentResolver().openInputStream(data.getData());
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
	            BitmapFactory.decodeStream(stream, null, options);
	            stream.close();
	            int viewWidth = getWidth();
	            int viewHeight = getHeight();
	            int picWidth = options.outWidth;
	            int picHeight = options.outHeight;
	            int sampleWidth = picWidth / viewWidth;
	            int sampleHeight = picHeight / viewHeight;
	            int sampleSize = Math.max(Math.max(sampleWidth, sampleHeight),1); 
	            Log.i("BITMAP","sampleSize="+sampleSize);
	            Log.i("BITMAP","picWidth="+picWidth);
	            Log.i("BITMAP","viewWidth="+viewWidth);
	            options = new BitmapFactory.Options();
				options.inSampleSize = sampleSize;
				options.inPurgeable = true;
				options.inInputShareable = true;
				stream = getContext().getContentResolver().openInputStream(data.getData());
				mProfilePhoto = BitmapFactory.decodeStream(stream, null, options);
				stream.close();
				String a = Helper.getStringFromBitmap(mProfilePhoto);
				Bitmap b = Helper.getBitmapFromString(a);
				setImageBitmap(b);
				// profileingView.setImageBitmap(mProfilePhoto);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			pickImage();
		}
	}

	public Bitmap getPhoto() {
		// TODO Auto-generated method stub
		return mProfilePhoto;
	}
};