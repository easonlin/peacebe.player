package peacebe.player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.os.Handler;
import android.os.HandlerThread;

public class TeamHandler{
	private HandlerThread mTeamThread; 
	private int mPort=1100;
	private DatagramSocket mSocket;
	private Handler mTeamHandler;
	private boolean mClose; 
	public TeamHandler(){
		mTeamThread = new HandlerThread("team");
		mTeamThread.start();
		mTeamHandler = new Handler(mTeamThread.getLooper());
	}
	public void start(){
		try {
			mSocket = new DatagramSocket(mPort);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mSocket.setBroadcast(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTeamHandler.postDelayed(teamTimer, 400);
	}
	private Runnable teamTimer = new Runnable() {
		public void run() {
			if (mClose){
				return;
			}
			try {
				String[] params = getPacket();
				String id = "1";
				if(params.length > 2){
					id = params[0];
				}
//				srv.sendJoin(id);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mTeamHandler.postDelayed(teamTimer, 400);
		}
	};
	public String[] getPacket() throws IOException{
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		mSocket.receive(packet);
		String data = new String(buf);
		String[] params = data.split(":");
		return params;
	}
	public void close() {
		// TODO Auto-generated method stub
		mClose = true;
		mTeamHandler.removeCallbacks(teamTimer);
		mSocket.close();
	}
}
