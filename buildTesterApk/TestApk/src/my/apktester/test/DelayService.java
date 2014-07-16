package my.apktester.test;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public class DelayService extends Service {
   
    public static final int MSG_END_TESTING = -1;
    public static final int MSG_START_TESTING = 0;
    public static final int MSG_NUMDELAYS_RESPONSE = 1;
    public static final int MSG_REPEAT_TEST = 5;
    public static final int MSG_REGISTER_TESTER = 2;
    public static final int MSG_UNREGISTER_TESTER = 3;
	
	public static final int NUM_DELAYS = 1;
	public static final int NUM_SEGMENTS = 8;
	
    private Messenger msg = new Messenger(new DelayHandler());
    private Messenger toTester;
    
    private static boolean isRunning = false;
    
  
    public IBinder onBind(Intent arg0) {               
        return msg.getBinder();
    }
    
    public static boolean isRunning(){
		return isRunning;
	}

    @Override
	public void onCreate() {
		super.onCreate();
		Log.i("MyScIPC", "Delay Service Created.");
		isRunning = true;
	}

    
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("MyScIPC", "Delay Service Destroyed.");
		isRunning = false;
	}
	
 
    class DelayHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
		    // This is the action
		    int msgType = msg.what;

		    switch(msgType) {
		    case MSG_START_TESTING:
		        Log.i("MyScIPC", "Scheduler starts the test, sending delay parameters.");
		        Message resp = Message.obtain(null, MSG_NUMDELAYS_RESPONSE);
                Bundle bResp = new Bundle();
                bResp.putInt("numDelays", NUM_DELAYS);
                bResp.putInt("numSegments", NUM_SEGMENTS);
                resp.setData(bResp);
		        try {
		            msg.replyTo.send(resp); 
		            Log.i("MyScIPC", "Service has sent delays: " + NUM_DELAYS + " segments: " + NUM_SEGMENTS);
		        } catch (RemoteException e) {       
		            e.printStackTrace();
		        }
		        break;
		    case MSG_REGISTER_TESTER:
                Log.i("MyScIPC", "Tester is registered to the DelayService..");
                toTester = msg.replyTo;
                break;
		    case MSG_UNREGISTER_TESTER:
                Log.i("MyScIPC", "Tester is unregistered to the DelayService..");
                toTester = null;
                break;
		    case MSG_REPEAT_TEST:
		        Log.i("MyScIPC", "Scheduler requests to repeat tests");
		        if(toTester == null){
		            Log.i("MyScIPC", "No registered testers to repeat tests");
		            break;
		        }
		        try {
                    toTester.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
		        break;
	        case MSG_END_TESTING:
	            Log.i("MyScIPC", "Scheduler requests to end testing");
	            if(toTester == null){
                    Log.i("MyScIPC", "No registered testers to repeat tests");
                    break;
                }
                try {
                    toTester.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
		    default:
		    	Log.i("MyScIPC", "Unidentified message received by the DelayService.");
		    }
		}
    }
    
      
}