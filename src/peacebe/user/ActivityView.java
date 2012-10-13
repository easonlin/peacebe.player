package peacebe.user;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public abstract class ActivityView extends View {

	public ActivityView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public abstract boolean onOptionsItemSelected(MenuItem item);
	public abstract void onPrepareOptionsMenu(Menu menu);
}
