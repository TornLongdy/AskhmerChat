package com.askhmer.chat.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.askhmer.chat.network.GsonObjectRequest;
import com.askhmer.chat.network.MySingleton;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Lach Phalleak on 9/24/2016.
 */
public class MyService  extends Service{
    private static WebSocketClient client;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    public void onCreate() {
        connectivityReceiver=new ConnectivityReceiver();
        super.onCreate();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(connectivityReceiver, filter);
        }catch (Exception e){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(client != null){
            client.close();
        }
        try {
                unregisterReceiver(connectivityReceiver);
        }catch (Exception e) {

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initailizeWebsocketClient();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyIBinder extends Binder {

        public MyService getMyService(){
            return MyService.this;
        }

    }
    public class ConnectivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    Log.i("WIFI","HAS");
                    if(client== null){
                          SharedPreferencesFile  mSharedPrefer = SharedPreferencesFile.newInstance(context.getApplicationContext(), SharedPreferencesFile.PREFER_FILE_NAME);
                          final String user_id = mSharedPrefer.getStringSharedPreference(SharedPreferencesFile.USERIDKEY);
                          if(user_id != null){
                              listNotSeenMessage(user_id,context);
                              URI uri=null;
                              try {
                                  uri=new URI(WsConfig.URL_WEBSOCKET);
                              } catch (URISyntaxException e) {
                                  e.printStackTrace();
                              }
                              client=new WebSocketClient(uri,new Draft_17(), null, 10000) {
                                  @Override
                                  public void onOpen(ServerHandshake serverHandshake) {
                                      Log.d("MyConnection", "Connected");
                                      MessageGenerator.sendMessageToServer("", user_id, "",client);
                                  }

                                  @Override
                                  public void onMessage(String s) {
                                      int group_id= getMessagGroupId(s);
                                      if(MySocket.getMessageListener() != null && MySocket.getCurrent_group_id()==group_id){
                                          MySocket.getMessageListener().getMessageFromServer(s);
                                      }else{
                                          myNotify(s);
                                      }
                                  }

                                  @Override
                                  public void onClose(int i, String s, boolean b) {
                                      Log.d("MyConnection", "Closed");
                                  }

                                  @Override
                                  public void onError(Exception e) {
                                         Log.e("MySocket","Error Connection");
                                  }
                              };
                              client.connect();
                              MySocket.setWebSocketClient(client);
                          }
                      }
                }else{
                    Log.i("WIFI","NO");
                    if(client != null) {
                        client.close();
                    }
                    client=null;
                }
            }
        }
    }
    public void myNotify(String msg){
        JSONObject jsonObject=null;
        String message="";
        int userid=0;
        int groupid=0;
        String username="";
        String image_url="";
        try {
            jsonObject=new JSONObject(msg);
            message=jsonObject.getString("message");
            userid=jsonObject.getInt("userid");
            groupid=jsonObject.getInt("groupid");
            username=jsonObject.getString("username");
            image_url=jsonObject.getString("img_profile");
        }catch (Exception e)
        {

        }
        String []param={image_url};
        new NotificationGenerator(this,message,username,groupid,userid,image_url).execute(param);
    }

    public void initailizeWebsocketClient(){
       // Log.d("Initial Socket","Initialize Socket");
        client=null;
        if(client==null){
            SharedPreferencesFile  mSharedPrefer = SharedPreferencesFile.newInstance(getApplicationContext(), SharedPreferencesFile.PREFER_FILE_NAME);
            final String user_id = mSharedPrefer.getStringSharedPreference(SharedPreferencesFile.USERIDKEY);
            if(user_id != null) {
                Log.d("User Id",user_id);
                URI uri = null;
                try {
                    uri = new URI(WsConfig.URL_WEBSOCKET);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                try {
                    client = new WebSocketClient(uri, new Draft_17(), null, 10000) {
                        @Override
                        public void onOpen(ServerHandshake serverHandshake) {
                            Log.d("MyConnection", "Connected");
                            MessageGenerator.sendMessageToServer("", user_id, "", client);
                        }

                        @Override
                        public void onMessage(String s) {
                            int group_id = getMessagGroupId(s);
                            if (MySocket.getMessageListener() != null && MySocket.getCurrent_group_id() == group_id) {
                                MySocket.getMessageListener().getMessageFromServer(s);
                            } else {
                                myNotify(s);
                            }
                        }

                        @Override
                        public void onClose(int i, String s, boolean b) {

                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("MySocket", "Error Connection");
                        }
                    };
                    client.connect();
                    MySocket.setWebSocketClient(client);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public int getMessagGroupId(String msg){
        JSONObject jsonObject=null;
        int group_id=0;
        try {
            jsonObject=new JSONObject(msg);
            group_id=jsonObject.getInt("groupid");
        }catch (Exception e)
        {
            return 0;
        }
        return group_id;
    }



    private void listNotSeenMessage(String userId, final Context context) {

        String url = "http://chat.askhmer.com/api/message/notiftynotseen/"+userId;
        GsonObjectRequest jsonRequest = new GsonObjectRequest(Request.Method.GET,url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String imageResource="http://chat.askhmer.com/resources/upload/file/";
                String []param={};
                try {
                    if (response.has("DATA")) {
                        JSONArray jsonArray = response.getJSONArray("DATA");
                        for (int i = 0; i < jsonArray.length(); i++) {
                                    int userid=jsonArray.getJSONObject(i).getInt("userId");
                                    String username=jsonArray.getJSONObject(i).getString("userName");
                                    String image_url=jsonArray.getJSONObject(i).getString("userProfile");
                                    String message=jsonArray.getJSONObject(i).getString("message");
                                    int groupid= jsonArray.getJSONObject(i).getInt("roomId");
                            if(image_url.contains("facebook")){
                                param[0]=image_url;
                            }else{
                                param[0]=imageResource+image_url;
                            }
                            new NotificationGenerator(context,message,username,groupid,userid,param[0]).execute(param);
                        }
                    }else{
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // CustomDialog.hideProgressDialog();
//                Toast.makeText(getContext(),"Error", Toast.LENGTH_LONG).show();
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }
}
