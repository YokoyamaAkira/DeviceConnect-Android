package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.p2p.WebServer;

import java.util.HashMap;
import java.util.Map;

public class AWSIotWebServerManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    private Map<RemoteDeviceConnectManager, WebServer> mWebServerList = new HashMap<>();

    private AWSIotRemoteManager mIot;
    private Context mContext;

    public AWSIotWebServerManager(final Context context, final AWSIotRemoteManager ctl) {
        mContext = context;
        mIot = ctl;
    }

    public void destroy() {
        for (Map.Entry<RemoteDeviceConnectManager, WebServer> e : mWebServerList.entrySet()) {
            e.getValue().stop();
        }
        mWebServerList.clear();
    }

    public void createWebServer(final RemoteDeviceConnectManager remote, final String address) {
        if (DEBUG) {
            Log.i(TAG, "createWebServer: " + remote);
        }

        // TODO WebServerをどのタイミングで止めるか検討
        WebServer webServer = new WebServer(mContext, address) {
            @Override
            public void onNotifySignaling(final String signaling) {
                mIot.publish(remote, mIot.createP2P(signaling));
            }
        };
        String url = webServer.start();
        mWebServerList.put(remote, webServer);

        if (DEBUG) {
            Log.i(TAG, "url=" + url);
        }
    }

    public void onReceivedSignaling(final RemoteDeviceConnectManager remote, final String message) {
        WebServer webServer = mWebServerList.get(remote);
        if (webServer != null) {
            webServer.onReceivedSignaling(message);
        }
    }
}