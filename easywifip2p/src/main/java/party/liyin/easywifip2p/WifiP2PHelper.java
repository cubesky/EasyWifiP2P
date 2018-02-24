package party.liyin.easywifip2p;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WifiP2PHelper {
    public static final String ACTION_SEND_DATA_TO_SERVICE = "party.liyin.easywifip2p.send";
    public static final String ACTION_RECIEVE_DATA = "party.liyin.easywifip2p.recieve";

    public static class State {
        public static final String ACTION = "party.liyin.easywifip2p.state";
        public static final String ENABLED = "party.liyin.easywifip2p.state.enabled";
        public static final String DISABLED = "party.liyin.easywifip2p.state.disabled";
    }

    public static class Peer {
        public static final String ACTION = "party.liyin.easywifip2p.peer";
    }

    public static class Connect {
        public static final String ACTION_CONNECT = "party.liyin.easywifip2p.connect";
        public static final String ACTION_STATE = "party.liyin.easywifip2p.connect.state";
        public static final String CONNECTED = "party.liyin.easywifip2p.connect.connected";
        public static final String DISCONNECTED = "party.liyin.easywifip2p.connect.disconnected";
    }

    public static class P2PInfo {
        public static final String ACTION = "party.liyin.easywifip2p.p2pinfo";
    }

    public static class Request {
        public static final String STATE = "party.liyin.easywifip2p.request.state";
        public static final String START_PEER_DISCOVERY = "party.liyin.easywifip2p.request.start_peer_discovery";
        public static final String STOP_PEER_DISCOVERY = "party.liyin.easywifip2p.request.stop_peer_discovery";
        public static final String PEER_LIST = "party.liyin.easywifip2p.request.peerlist";
        public static final String CONNECT = "party.liyin.easywifip2p.request.connect";
        public static final String DISCONNECT = "party.liyin.easywifip2p.request.disconnect";
        public static final String CONNINFO = "party.liyin.easywifip2p.request.conninfo";
    }

    public static class ERROR {
        public static final String ACTION = "party.liyin.easywifip2p.error";
        public static final String PEER_DISCOVER = "party.liyin.easywifip2p.error.peer_discovery";
        public static final String CONNECT = "party.liyin.easywifip2p.error.connect";
    }

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 120;

    public interface PeerListener {
        /**
         * Callback when Peer discovered
         * @param peers Peer array
         */
        void foundPeers(ArrayList<WifiP2pDevice> peers);
    }
    public interface ConnectListener {
        /**
         * Callback when connect is done
         * @param state True for success (Usually ignore) / False for failed
         */
        void connectDone(boolean state);

        /**
         * Callback when connect state changed
         * @param state True for Peer is connected / False for Peer disconnect
         */
        void connectState(boolean state);
    }
    public interface ConnectInfoListener {
        /**
         * Callback when coonect state is connected. This will return a network info for this connection
         * @param address Group Owner Address
         * @param isGroupOwner is Group Owner
         * @param groupFormed is Group Formed
         */
        void connectInfo(String address,boolean isGroupOwner,boolean groupFormed);
    }

    private EasyReceiver easyReceiver = new EasyReceiver();
    private Context context;
    private PeerListener peerListener = null;
    private ConnectListener connectListener = null;
    private ConnectInfoListener connectInfoListener = null;

    /**
     * Request Permission on Android Oreo, if you need get peer list, you need call this method to get location permission.
     * @param activity Activity in your app
     */
    public static void requestPermission(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    public WifiP2PHelper(Context context) {
        this.context = context;
    }

    /**
     * Start Service
     */
    public void easyStart(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECIEVE_DATA);
        context.getApplicationContext().registerReceiver(easyReceiver, filter);
        context.startService(new Intent(context,WifiP2PService.class));
    }

    /**
     * Stop Service
     */
    public void easyStop(){
        stopDiscovery();
        disconnectAll();
        context.stopService(new Intent(context,WifiP2PService.class));
        context.getApplicationContext().unregisterReceiver(easyReceiver);
    }

    /**
     * Set PeerListener
     * @param peerListener PeerListener instance
     */
    public void setPeerListener(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    /**
     * Set Connect Listener
     * @param connectListener ConnectListener instance
     */
    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    /**
     * Set Connect Info Listener
     * @param connectInfoListener ConnectInfoListener instance
     */
    public void setConnectInfoListener(ConnectInfoListener connectInfoListener) {
        this.connectInfoListener = connectInfoListener;
    }

    /**
     * Start Peer Discovery
     */
    public void startDiscovery() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_DATA_TO_SERVICE);
        intent.putExtra("Request",Request.START_PEER_DISCOVERY);
        context.sendBroadcast(intent);
    }

    /**
     * Stop Peer Discovery
     */
    public void stopDiscovery() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_DATA_TO_SERVICE);
        intent.putExtra("Request",Request.STOP_PEER_DISCOVERY);
        context.sendBroadcast(intent);
    }

    /**
     * Connect to Peer
     * @param device WifiP2pDevice Peer
     */
    public void connectToPeer(WifiP2pDevice device) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_DATA_TO_SERVICE);
        intent.putExtra("Request",Request.CONNECT);
        intent.putExtra("Data", device);
        context.sendBroadcast(intent);
    }

    /**
     * Disconnect All Peer
     */
    public void disconnectAll() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_DATA_TO_SERVICE);
        intent.putExtra("Request",Request.DISCONNECT);
        context.sendBroadcast(intent);
    }

    /**
     * replay connect info
     */
    public void requestConnInfo() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_DATA_TO_SERVICE);
        intent.putExtra("Request",Request.CONNINFO);
        context.sendBroadcast(intent);
    }

    class EasyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("Action")) {
                case Peer.ACTION:
                    if (peerListener != null) peerListener.foundPeers(((ArrayList<WifiP2pDevice>) (intent.getExtras().get("Data"))));
                    break;
                case Connect.ACTION_CONNECT:
                    if (connectListener != null) connectListener.connectDone(true);
                    break;
                case Connect.ACTION_STATE:
                    if (connectListener != null){
                        connectListener.connectState(intent.getStringExtra("Data").equals(Connect.CONNECTED));
                    }
                    break;
                case P2PInfo.ACTION:
                    if (connectInfoListener != null) {
                        try {
                            JSONObject json = new JSONObject(intent.getStringExtra("Data"));
                            if (json.has("groupOwnerHostAddress")){
                                connectInfoListener.connectInfo(json.getString("groupOwnerHostAddress"),json.getBoolean("isGroupOwner"),json.getBoolean("groupFormed"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ERROR.ACTION:
                    switch (intent.getStringExtra("Data")) {
                        case ERROR.PEER_DISCOVER:
                            if (peerListener != null) peerListener.foundPeers(null);
                            break;
                        case ERROR.CONNECT:
                            if (connectListener != null) connectListener.connectDone(false);
                            break;
                    }
                    break;
            }
        }

    }
}
