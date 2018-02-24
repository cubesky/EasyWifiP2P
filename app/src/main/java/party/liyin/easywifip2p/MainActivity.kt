package party.liyin.easywifip2p

import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val wifiP2P = WifiP2PHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.setOnItemClickListener { parent,_,position,_ ->
            val item = parent.getItemAtPosition(position) as WifiP2pDevice
            wifiP2P.connectToPeer(item)
        }
        wifiP2P.setConnectInfoListener { address, isGroupOwner, groupFormed ->
            println("========\nAddress:$address\nisGroupOwner:$isGroupOwner\ngroupFormed:$groupFormed\n========")
        }
        wifiP2P.setConnectListener(object : WifiP2PHelper.ConnectListener{
            override fun connectState(state: Boolean) {
                println("Connect State: $state")
            }

            override fun connectDone(state: Boolean) {
                println("Connect Done: $state")
            }

        })
        wifiP2P.setPeerListener {
            list.adapter = ArrayAdapter<WifiP2pDevice>(this@MainActivity,android.R.layout.simple_expandable_list_item_1, it)
        }
        btn_start.setOnClickListener {
            wifiP2P.easyStart()
        }
        btn_stop.setOnClickListener {
            wifiP2P.easyStop()
        }
        btn_start_search.setOnClickListener {
            wifiP2P.startDiscovery()
        }
        btn_reqperm.setOnClickListener {
            WifiP2PHelper.requestPermission(this@MainActivity)
        }
    }
}
