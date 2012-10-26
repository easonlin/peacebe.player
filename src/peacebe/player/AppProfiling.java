package peacebe.player;

import peacebe.player.PlayerActivity.SrvState;
import peacebe.player.PlayerActivity.UiState;
import android.graphics.Bitmap;
import android.widget.FrameLayout;

public class AppProfiling implements IApp {
	private PhotoView profilingView;
	public void init(FrameLayout paintFrame) {
		// TODO Auto-generated method stub
		profilingView = new PhotoView(paintFrame.getContext());
	}

	public void srv(String app, String state, SrvState srvState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
		} else if  (app.equals("profiling") && state.equals("v_profiling")){
			
		} else if  (app.equals("profiling") && state.equals("w_profiling")){
			
		}
	}
	private boolean isTeaming;
	public void ui(String app, String state, UiState uiState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			if (isTeaming){
				uiTeaming(uiState);
			} else {
				uiProfiling(uiState);
			}
		} else if  (app.equals("profiling") && state.equals("v_profiling")){
			
		} else if  (app.equals("profiling") && state.equals("w_profiling")){
			
		} else {
			uiState.isMain=true;
		}
	}

	public void send(String app, String state, SrvState srvState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			if (isTeaming){
			Bitmap profilePhoto = profilingView.getPhoto();
			srvState.srv.sendProfile(profilePhoto);
			}
		}
	}

	private void uiProfiling(UiState uiState) {
		uiState.view = profilingView;
		profilingView.pickImage();
	}
	TeamView teamView;
	private void uiTeaming(UiState uiState){
		uiState.view = teamView;
	}
}
