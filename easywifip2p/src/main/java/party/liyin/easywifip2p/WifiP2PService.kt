package party.liyin.easywifip2p

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.pawegio.kandroid.fromApi
import com.pawegio.kandroid.notificationManager
import com.pawegio.kandroid.wifiManager
import com.pawegio.kandroid.wifiP2pManager

class WifiP2PService : Service() {
    override fun onBind(intent: Intent): IBinder? = null
    private val EASYWIFIP2P_CHANNEL_ID = "party.liyin.easywifip2p"
    private val EASYWIFIP2P_NOTIFY_ID = 110
    private lateinit var receiver: WifiP2PReceiver
    private lateinit var wifiLock: WifiManager.WifiLock

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        fromApi(Build.VERSION_CODES.O){
            notificationManager?.createNotificationChannel(NotificationChannel(EASYWIFIP2P_CHANNEL_ID,
                    getText(R.string.ForegroundNotification), NotificationManager.IMPORTANCE_MIN).apply {
                this.enableLights(false)
                this.enableVibration(false)
                this.importance = NotificationManager.IMPORTANCE_MIN
                this.lockscreenVisibility = Notification.VISIBILITY_SECRET
            })
        }
        startForeground(EASYWIFIP2P_NOTIFY_ID,NotificationCompat.Builder(applicationContext,EASYWIFIP2P_CHANNEL_ID).apply {
            this.setSmallIcon(R.drawable.ic_stat_name)
            this.setVisibility(Notification.VISIBILITY_SECRET)
            this.priority = NotificationCompat.PRIORITY_MIN
            this.setCategory(Notification.CATEGORY_SERVICE)
            this.setOngoing(true)
            this.setLocalOnly(true)
            this.setSubText(getText(R.string.ForegroundNotification))
        }.build())
        receiver = WifiP2PReceiver(wifiP2pManager!!,wifiP2pManager?.initialize(this,mainLooper,null)!!)
        wifiLock = wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL,"EasyWifiP2P")!!.apply { this@apply.setReferenceCounted(false) }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2PHelper.ACTION_SEND_DATA_TO_SERVICE)
        registerReceiver(receiver,intentFilter)
        wifiLock.acquire()
        super.onCreate()
    }

    override fun onDestroy() {
        wifiLock.release()
        stopForeground(true)
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}
