package peacebe.player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import peacebe.common.Helper;
import peacebe.player.PlayerActivity.SrvState;
import peacebe.player.PlayerActivity.UiState;
import android.graphics.Bitmap;
import android.widget.FrameLayout;

public class AppGrouping implements IApp {
	private PaintView paintView;
	private VoteView voteView;
	private PhotoView groupingResultView;
	private JSONArray mCandidate;
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#init(android.widget.FrameLayout)
	 */
	public void init(FrameLayout paintFrame){
		int vHeight = paintFrame.getHeight();
		int vWidth = paintFrame.getWidth();
		paintView = new PaintView(paintFrame.getContext(), vHeight, vWidth);
		voteView = new VoteView(paintFrame.getContext(), vHeight, vWidth);
		groupingResultView = new PhotoView(paintFrame.getContext());
	}
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#srv(java.lang.String, java.lang.String, peacebe.player.PlayerActivity.SrvState)
	 */
	public void srv(String app, String state, SrvState srvState) {
		if (app.equals("grouping") && state.equals("painting")) {
		} else if (app.equals("grouping") && state.equals("voting")) {
			mCandidate = srvState.srv.getCandidate();
		} else if (app.equals("grouping") && state.equals("result")) {
			JSONObject m = srvState.srv.getGroupingResult();
			setResultBitmap(m);
		}
	}
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#ui(java.lang.String, java.lang.String, peacebe.player.PlayerActivity.UiState)
	 */
	public void ui(String app, String state, UiState uiState){

		if (app.equals("grouping") && state.equals("painting")) {
			uiPainting(uiState);
		} else if (app.equals("grouping") && state.equals("voting")) {
			uiVoting(uiState);
		} else if (app.equals("grouping") && state.equals("result")) {
			uiResult(uiState);
		}	
	}
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#send(java.lang.String, java.lang.String, peacebe.player.PlayerActivity.SrvState)
	 */
	public void send(String app, String state, SrvState srvState){
		if (app.equals("grouping") && state.equals("painting")) {
			Bitmap bitmap = paintView.getBitmap();
			srvState.srv.sendPaint(bitmap);
		} else if (app.equals("grouping") && state.equals("voting")) {
			String id = voteView.getVote();
			srvState.srv.sendVote(id);
		} else if (app.equals("grouping") && state.equals("result")) {
		}		
	}
	Bitmap mBitmap = null;
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#setResultBitmap(org.json.JSONObject)
	 */
	private void setResultBitmap(JSONObject mResult) {
		try {
			mBitmap = Helper.getBitmapFromString(mResult.getString("photo"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#uiPainting(peacebe.player.PlayerActivity.UiState)
	 */
	private void uiPainting(UiState uiState) {
		uiState.mPaintFrame.addView(paintView);
		uiState.isDialog = true;
	}

	/* (non-Javadoc)
	 * @see peacebe.player.IApp#uiVoting(peacebe.player.PlayerActivity.UiState)
	 */
	private void uiVoting(UiState uiState) {
		voteView.setCandidate(mCandidate);
		uiState.mPaintFrame.addView(voteView);
	}
	/* (non-Javadoc)
	 * @see peacebe.player.IApp#uiResult(peacebe.player.PlayerActivity.UiState)
	 */
	private void uiResult(UiState uiState) {
		uiState.mPaintFrame.addView(groupingResultView);
		groupingResultView.setImageBitmap(mBitmap);
		uiState.isBlock = true;
	}
}
