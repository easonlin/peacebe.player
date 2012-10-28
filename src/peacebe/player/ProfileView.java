package peacebe.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import peacebe.common.ViewOption;

//public class ProfileView extends ActivityViewGroup {
public class ProfileView extends FrameLayout implements ViewOption {
	ImageView mPhotoView;
	RadioGroup mMaleView;
	EditText mNameView;
	View profilingView;
	private static final int REQUEST_CODE = 1;
	private Bitmap mProfilePhoto=null;
	public ProfileView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		profilingView = inflater.inflate(R.layout.profiling, this, false);
		mPhotoView = (ImageView) profilingView.findViewById(R.id.profilingPhoto);
		mPhotoView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pickImage();
			}
		});
		mMaleView = (RadioGroup) profilingView.findViewById(R.id.radioGroupMale);
		mNameView = (EditText) profilingView.findViewById(R.id.editName);
		this.addView(profilingView);
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
	            int sampleWidth = picWidth / viewWidth * 2;
	            int sampleHeight = picHeight / viewHeight * 2;
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
				mPhotoView.setImageBitmap(mProfilePhoto);
				// profileingView.setImageBitmap(mProfilePhoto);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
		}
	}

	public Bitmap getPhoto() {
		// TODO Auto-generated method stub
		return mProfilePhoto;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return mNameView.getText().toString();
	}

	public String getMale() {
		// TODO Auto-generated method stub
		int id = mMaleView.getCheckedRadioButtonId();
		Log.i("ui", "getChecked Radio Button id is " + id);
		if (id==R.id.radioMale){
			return "boy";
		}
		else if (id==R.id.radioFemale){
			return "girl";
		} else {
			return "";
		}
	}
}
