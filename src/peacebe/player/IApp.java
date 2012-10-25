package peacebe.player;

import peacebe.player.PlayerActivity.SrvState;
import peacebe.player.PlayerActivity.UiState;
import android.widget.FrameLayout;

public interface IApp {

	public abstract void init(FrameLayout paintFrame);

	public abstract void srv(String app, String state, SrvState srvState);

	public abstract void ui(String app, String state, UiState uiState);

	public abstract void send(String app, String state, SrvState srvState);

}