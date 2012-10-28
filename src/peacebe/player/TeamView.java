package peacebe.player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import peacebe.common.ActivityViewGroup;
import peacebe.common.ViewOption;

public class TeamView extends FrameLayout implements ViewOption {
	ListView teamView;
	String [] mTeams;
	int mSelected = 0;
	public void flush(){
		ArrayAdapter<String> teamingAdapter = new ArrayAdapter<String>(teamView.getContext(), R.layout.profiling_item, mTeams);	
		teamView.setAdapter(teamingAdapter);
	}
	public TeamView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		teamView = new ListView(this.getContext());
		teamView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				String [] selectTeams = mTeams.clone();
				selectTeams[position] = mTeams[position] + " Selected";
				mSelected = position;
				ArrayAdapter<String> teamingAdapter = new ArrayAdapter<String>(teamView.getContext(), R.layout.profiling_item, selectTeams);	
				teamView.setAdapter(teamingAdapter);
			}
		});
		this.addView(teamView);
	}
	public int getSelectedId(){
		return mSelected;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

	}
	public void setTeams(JSONArray mJSONTeams) {
		// TODO Auto-generated method stub
		//{"teams": [{"id":"1","name": "Tina", "photo":XYZ, "seat":2},
		// {"id":"2","name": "Jan", "photo":XYZ, "seat":1}]}
		int len = mJSONTeams.length();
		mTeams = new String[len];
		for(int i=0; i<len;i++){
			JSONObject team;
			try {
				team = mJSONTeams.getJSONObject(i);
				String seat = team.getString("seat");
				String id = team.getString("id");
				mTeams[i]="seat: " + seat + ", id: " + id;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

}
