package peacebe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse; 
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException; 
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.util.EntityUtils; 

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PeaceBeServer {
	class URLPair {
		public String url;
		public JSONObject content;
	}
	private class HTTPPutTask extends AsyncTask<URLPair, Integer, Integer> {
		@Override 
		protected Integer doInBackground(URLPair... urls) {
			URLPair urlPair = urls[0];
			String url = urlPair.url;
			JSONObject content = urlPair.content;
			Log.i("http", "httpPut " + url);
			// TODO Auto-generated method stub			
			HttpClient httpClient = new DefaultHttpClient();    
	        HttpPut httpPut = new HttpPut(url);
			try {
				httpPut.setEntity(new ByteArrayEntity(content.toString().getBytes("UTF8")));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpPut.setHeader("Content-Type", "application/json");
	        HttpResponse response = null;
			try {
				response = httpClient.execute(httpPut);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("http", Integer.toString(response.getStatusLine().getStatusCode()));
			return 0;
	     }
	}
	private class HTTPGetTask extends AsyncTask<String, Integer, JSONObject> {
		@Override 
		protected JSONObject doInBackground(String... urls) {
	 		String url = urls[0];
	 		Log.i("http", "httpGet " + url);
			//HttpGet httpRequest = new HttpGet("http://ec2-175-41-156-14.ap-southeast-1.compute.amazonaws.com/app/main/player/1/state");
			HttpGet httpRequest = new HttpGet(url); 
	    	HttpResponse httpResponse = null;
			JSONObject result = null;
			try {
				httpResponse = new DefaultHttpClient().execute(httpRequest);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    	if(httpResponse.getStatusLine().getStatusCode() == 200)  
	    	{ 
	    		String strResult = null;
				try {
					strResult = EntityUtils.toString(httpResponse.getEntity());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		        	 		

				try {
					result = new JSONObject(strResult);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	     
	    	} 
	    	else 
	    	{ 
	    		Log.d("http", "Error Response: " + httpResponse.getStatusLine().toString()); 
	    	}
	    	return result;
	     }
	}
	public FakePeaceBeServer getFake(){
		return new FakePeaceBeServer();
	}
	private int mPlayer = 5;
	//private String mBaseURL = "http://ec2-175-41-156-14.ap-southeast-1.compute.amazonaws.com";
	private String mBaseURL = "http://175.41.156.14";

	public JSONObject httpGet(String url)  {
		JSONObject result = null;
		try {
			 result =  new HTTPGetTask().execute(url).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		/*
		Log.i("http", "httpGet " + url);
		//HttpGet httpRequest = new HttpGet("http://ec2-175-41-156-14.ap-southeast-1.compute.amazonaws.com/app/main/player/1/state"); 
		HttpGet httpRequest = new HttpGet(url); 
    	HttpResponse httpResponse = null;
		JSONObject result = null;
		try {
			httpResponse = new DefaultHttpClient().execute(httpRequest);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	if(httpResponse.getStatusLine().getStatusCode() == 200)  
    	{ 
    		String strResult = null;
			try {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		        	 		

			try {
				result = new JSONObject(strResult);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	     
    	} 
    	else 
    	{ 
    		Log.d("http", "Error Response: " + httpResponse.getStatusLine().toString()); 
    	}
    	return result;
    	*/
	}
	public void httpPut(String url, JSONObject content){
		URLPair urlPair = new URLPair();
		urlPair.url = url;
		urlPair.content = content;
		try {
			int i = new HTTPPutTask().execute(urlPair).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		Log.i("http", "httpPut " + url);
		// TODO Auto-generated method stub			
		HttpClient httpClient = new DefaultHttpClient();    
        HttpPut httpPut = new HttpPut(url);
		try {
			httpPut.setEntity(new ByteArrayEntity(content.toString().getBytes("UTF8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpPut.setHeader("Content-Type", "application/json");
        HttpResponse response = null;
		try {
			response = httpClient.execute(httpPut);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("http", Integer.toString(response.getStatusLine().getStatusCode()));
		*/
	}
    public class FakePeaceBeServer{
    	private JSONArray mProcesses;
    	private int mIndex=0;
    	private String mStringBitmap;
    	private int mVote;
    	public FakePeaceBeServer(){
    		mProcesses = new JSONArray();
    		JSONObject map = new JSONObject();
    		try {
				map.put("app", "main");
	    		map.put("state", "stop");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "grouping");
	    		map.put("state", "painting");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "grouping");
	    		map.put("state", "w_painting");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "grouping");
	    		map.put("state", "voting");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "grouping");
	    		map.put("state", "w_voting");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "grouping");
	    		map.put("state", "result");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "main");
	    		map.put("state", "stop");
	    		mProcesses.put(map);
	    		map = new JSONObject();
	    		map.put("app", "main");
	    		map.put("state", "stop");
	    		mProcesses.put(map);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	public JSONObject getGroupingResult() {
			// TODO Auto-generated method stub
    		JSONObject m = new JSONObject();
    		try {
				m.put("id", mVote);
	    		m.put("name", "jack");
	    		m.put("photo", getBitmapFromString(mStringBitmap));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return m;
		}
		public void sendVote(int id) {
			// TODO Auto-generated method stub
			mVote = id;
			
		}
		public JSONArray getCandidate(){
			// TODO Auto-generated method stub
			//LinkedList<HashMap> candidate = new LinkedList();
			//HashMap map= new HashMap();
			JSONObject map = new JSONObject();
			JSONArray candidate = new JSONArray();
			try {
				map.put("paint", mStringBitmap);
				map.put("id", 5);
				candidate.put(map);
				map= new JSONObject();
				map.put("paint", mStringBitmap);
				map.put("id", 6);
				candidate.put(map);
				map= new JSONObject();
				map.put("paint", mStringBitmap);
				map.put("id", 7);
				candidate.put(map);
				map= new JSONObject();
				map.put("paint", mStringBitmap);
				map.put("id", 8);
				candidate.put(map);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return candidate;
		}
		public void sendPaint(Bitmap bitmap) {
			// TODO Auto-generated method stub
			mStringBitmap = getStringFromBitmap(bitmap);
		}
		public void setProcess(JSONArray processes){
    		mProcesses = processes;
    	}
    	public JSONObject getState(){
    		try {
    			JSONObject result = mProcesses.getJSONObject(mIndex);
    			mIndex ++;
				return result;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
    	}
    }
	public JSONObject getState() {
		// TODO Auto-generated method stub
		return httpGet(mBaseURL+"/app/main/player/"+mPlayer+"/state");
	}
	public JSONArray getCandidate() {
		// TODO Auto-generated method stub
		try {
			return httpGet(mBaseURL+"/app/grouping/player/"+mPlayer+"/candidate").getJSONArray("players");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public JSONObject getGroupingResult() {
		// TODO Auto-generated method stub
		return httpGet(mBaseURL+"/app/grouping/player/"+mPlayer+"/result");
	}
	public void sendPaint(Bitmap bitmap) {
		// TODO Auto-generated method stub
		String stringBitmap = getStringFromBitmap(bitmap);
		JSONObject content = new JSONObject();
		try {
			content.put("paint", stringBitmap);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpPut(mBaseURL+"/app/grouping/player/"+mPlayer+"/paint", content);

	}
	public void sendVote(int id) {
		// TODO Auto-generated method stub
		JSONObject content = new JSONObject();
		try {
			content.put("player", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpPut(mBaseURL+"/app/grouping/player/"+mPlayer+"/vote", content);		
	}
	public static String getStringFromBitmap(Bitmap bitmapPicture) {
		 /*
		 * This functions converts Bitmap picture to a string which can be
		 * JSONified.
		 * */
		 final int COMPRESSION_QUALITY = 100;
		 String encodedImage;
		 ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
		 bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, byteArrayBitmapStream);
		 byte[] b = byteArrayBitmapStream.toByteArray();
		 encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
		 return encodedImage;
	}
	public static Bitmap getBitmapFromString(String stringPicture) {
		/*
		* This Function converts the String back to Bitmap
		* */
		byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedByte;
	}

}
