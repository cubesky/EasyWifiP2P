# Easy Wifi P2P

A Library written in Kotlin and Java to make your WifiDirect Easiler

## Install 
You can use `CubeSky Repo` ([https://cubesky-mvn.github.io](https://cubesky-mvn.github.io)) to use this library.

## API
**1. you need a `WifiP2PHelper` instance.**

```java
WifiP2PHelper helper = new WifiP2PHelper(context);
```

**2. start `EasyWifiP2P` Service**
```
helper.easyStart()
```

EasyWifiP2P will automatically startup a Service for Wifi Direct.  


**3. Request Location Permission for Oreo**
```java
WifiP2PHelper.requestPermission(Activity)
```

This will show a permission request dialog by user's system.

**4. Setup Listeners**

**4.1. PeerListener**

If you act as host. Ignore this.

```java
helper.setPeerListener(new WifiP2PHelper.PeerListener(){
    void foundPeers(ArrayList<WifiP2pDevice> peers){
        //Your code
    }
})
```

**4.2. ConnectListener**


```java
helper.setConnectListener(new WifiP2PHelper.ConnectListener(){
    void connectDone(boolean state){
        //Your code
    }
    void connectState(boolean state){
        //Your code
    }
})
```

**4.3. ConnectInfoListener**


```java
helper.setConnectInfoListener(new WifiP2PHelper.ConnectInfoListener(){
    void connectInfo(String address,boolean isGroupOwner,boolean groupFormed){
        //Your code
    }
})
```

**5. Start to discover Peer**

This will also make your device visible on other device.

```java
helper.startDiscovery()
```

**5.1. Stop Discover Peer**

```java
helper.stopDiscovery()
```

**6. Connect To Peer**

```java
helper.connectToPeer(WifiP2pDevice)
```

**6.1. Disconnect Peer**

```java
helper.disconnectAll()
```

**7. Stop `EasyWifiP2P` Service**

```java
helper.easyStop()
```

## EasyWifiP2P WifiLock

`EasyWifiP2P` will automatically request a `WifiLock` to avoid wifi interrupt. This will release when Service is stop.

## Dependency

| Name     | URL                                 |
|:--       |:--                                  |
| KAndroid | https://github.com/pawegio/KAndroid |

## LICENSE
MIT License