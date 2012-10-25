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
		}
	}

	public void ui(String app, String state, UiState uiState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			uiProfiling(uiState);
		}
	}

	public void send(String app, String state, SrvState srvState) {
		// TODO Auto-generated method stub
		if (app.equals("profiling") && state.equals("profiling")) {
			Bitmap profilePhoto = profilingView.getPhoto();
			srvState.srv.sendProfile(profilePhoto);
		}
	}

	private void uiProfiling(UiState uiState) {
		uiState.mPaintFrame.addView(profilingView);
		profilingView.pickImage();
	}
}
