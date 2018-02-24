package party.liyin.easywifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import com.pawegio.kandroid.i
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class WifiP2PReceiver(val manager: WifiP2pManager, val channel: WifiP2pManager.Channel) : BroadcastReceiver() {

    var wifi_state = "Unknown"
    val peers = mutableListOf<WifiP2pDevice>()
    var connjson : JSONObject? = null

    override fun onReceive(context: Context, intent: Intent) {
        i("ACTION: " + intent.action)
        i("Extra-Request: " + (intent.getStringExtra("Request") ?: ""))
        val sendIntent = Intent()
        sendIntent.action = WifiP2PHelper.ACTION_RECIEVE_DATA
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                if (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    wifi_state = WifiP2PHelper.State.ENABLED
                } else {
                    wifi_state = WifiP2PHelper.State.DISABLED
                }
                sendIntent.putExtra("Action", WifiP2PHelper.State.ACTION)
                sendIntent.putExtra("Data", wifi_state)
                context.sendBroadcast(sendIntent)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { p0 ->
                    peers.clear()
                    p0.deviceList.forEach { println("${it.deviceName} on ${it.deviceAddress}") }
                    if(p0.deviceList.isNotEmpty()) {
                        peers.addAll(p0.deviceList.asIterable())
                    }
                    sendIntent.putExtra("Action", WifiP2PHelper.Peer.ACTION)
                    sendIntent.putExtra("Data",peers.toArrayList())
                    context.sendBroadcast(sendIntent)
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                sendIntent.putExtra("Action", WifiP2PHelper.Connect.ACTION_STATE)
                if (networkInfo.isConnected) {
                    sendIntent.putExtra("Data",WifiP2PHelper.Connect.CONNECTED)
                    manager.requestConnectionInfo(channel, {
                        val p2pInfoIntent = Intent()
                        p2pInfoIntent.action = WifiP2PHelper.ACTION_RECIEVE_DATA
                        p2pInfoIntent.putExtra("Action", WifiP2PHelper.P2PInfo.ACTION)
                        connjson = JSONObject()
                        connjson?.put("isGroupOwner", it.isGroupOwner)
                        connjson?.put("groupFormed", it.groupFormed)
                        connjson?.put("groupOwnerHostAddress",it.groupOwnerAddress.hostAddress)
                        p2pInfoIntent.putExtra("Data",(if (connjson == null) JSONObject() else connjson).toString())
                        context.sendBroadcast(p2pInfoIntent)
                    })
                } else {
                    sendIntent.putExtra("Data",WifiP2PHelper.Connect.DISCONNECTED)
                }
                context.sendBroadcast(sendIntent)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

            }
            WifiP2PHelper.ACTION_SEND_DATA_TO_SERVICE -> {
                when (intent.getStringExtra("Request")) {
                    WifiP2PHelper.Request.STATE -> {
                        sendIntent.putExtra("Action", WifiP2PHelper.State.ACTION)
                        sendIntent.putExtra("Data", wifi_state)
                        context.sendBroadcast(sendIntent)
                    }
                    WifiP2PHelper.Request.START_PEER_DISCOVERY -> {
                        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {  }

                            override fun onFailure(p0: Int) {
                                sendIntent.putExtra("Action", WifiP2PHelper.ERROR.ACTION)
                                sendIntent.putExtra("Data", WifiP2PHelper.ERROR.PEER_DISCOVER)
                                context.sendBroadcast(sendIntent)
                            }
                        })
                    }
                    WifiP2PHelper.Request.PEER_LIST -> {
                        sendIntent.putExtra("Action", WifiP2PHelper.Peer.ACTION)
                        sendIntent.putExtra("Data",peers.toArrayList())
                        context.sendBroadcast(sendIntent)
                    }
                    WifiP2PHelper.Request.CONNECT -> {
                        val device = intent.extras.get("Data") as WifiP2pDevice
                        val config = WifiP2pConfig()
                        config.deviceAddress = device.deviceAddress
                        config.wps.setup = WpsInfo.PBC
                        manager.connect(channel,config,object:WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                sendIntent.putExtra("Action", WifiP2PHelper.Connect.ACTION_CONNECT)
                                context.sendBroadcast(sendIntent)
                            }

                            override fun onFailure(p0: Int) {
                                sendIntent.putExtra("Action", WifiP2PHelper.ERROR.ACTION)
                                sendIntent.putExtra("Data", WifiP2PHelper.ERROR.CONNECT)
                                context.sendBroadcast(sendIntent)
                            }
                        })
                    }
                    WifiP2PHelper.Request.CONNINFO -> {

                    }
                    WifiP2PHelper.Request.DISCONNECT -> {
                        manager.cancelConnect(channel,null)
                    }
                    WifiP2PHelper.Request.STOP_PEER_DISCOVERY -> {
                        manager.stopPeerDiscovery(channel,null)
                    }
                }
            }
        }
    }

    fun MutableCollection<*>.toArrayList() = ArrayList(this)
}
