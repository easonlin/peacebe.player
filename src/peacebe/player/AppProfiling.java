package peacebe.player;

import org.json.JSONArray;
import org.json.JSONException;

import peacebe.player.PlayerActivity.SrvState;
import peacebe.player.PlayerActivity.UiState;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.FrameLayout;

public class AppProfiling implements IApp {
	ProfileView profilingView;
	TeamView teamView;
	JSONArray mTeams=null;
	public void init(FrameLayout paintFrame) {
		// TODO Auto-generated method stub
		profilingView = new ProfileView(paintFrame.getContext());
		teamView = new TeamView(paintFrame.getContext());
	}

	public void srv(String app, String state, SrvState srvState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			if (isTeaming){
				mTeams = srvState.srv.getOpenedTeams();
				teamView.setTeams(mTeams);
			}
			else {
				
			}
		} else if  (app.equals("profiling") && state.equals("v_profiling")){
			
		} else if  (app.equals("profiling") && state.equals("w_profiling")){
			
		}
	}
	private boolean isTeaming = false;
	public void ui(String app, String state, UiState uiState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			if (isTeaming){
				uiTeaming(uiState);
			} else {
				uiProfiling(uiState);
			}
		} else if  (app.equals("profiling") && state.equals("v_profiling")){
			uiState.isMain=true;
		} else if  (app.equals("profiling") && state.equals("w_profiling")){
			uiState.isMain=true;
		} else {
			uiState.isMain=true;
		}
	}

	public void send(String app, String state, SrvState srvState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			if (isTeaming){
				int selectedId = teamView.getSelectedId();
				String tid;
				try {
					tid = mTeams.getJSONObject(selectedId).getString("id");
					srvState.srv.sendJoin(tid);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Bitmap profilePhoto = profilingView.getPhoto();
				String name = profilingView.getName();
				String male = profilingView.getMale();
				srvState.srv.sendProfile(profilePhoto, male, name);
				isTeaming = true;
				srvState.redo=true;
			}
		}
	}

	private void uiProfiling(UiState uiState) {
		Log.i("ui","uiProfiling view");
		uiState.view = profilingView;
	}
	private void uiTeaming(UiState uiState){
		teamView.flush();
		uiState.view = teamView;
	}
}
