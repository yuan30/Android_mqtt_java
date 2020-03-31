package com.example.mqtt_test;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

class MqttAsyncHelper {
    private final String TAG = MqttAsyncHelper.class.getSimpleName();

    //MQTT Server
    private final String serverUri = "tcp://140.130.31.183";//192.168.50.50

    //訂閱
    private String subscriptionTopic = null;
    private int[] mQos = null;

    //發佈
    private String publishTopic = null;

    //帳號密碼
    private String username = null;
    private String password = null;

    private MqttAsyncClient mAsyncClient;

    private Context mContext;
    private IMqttResponse mIMqttResponse;


    MqttAsyncHelper(Context context, IMqttResponse iMqttResponse){
        mContext = context;
        mIMqttResponse = iMqttResponse;
    }

    // TODO MQTT連線設定檔
    private MqttConnectOptions initMqttConnectOptions(){
        MqttConnectOptions mOptions = new MqttConnectOptions();
        mOptions.setAutomaticReconnect(true); //離線後，是否自動連線
        mOptions.setCleanSession(true); //是否清空client的連線紀錄。若是，則離線後，broker會自動清除client的連線訊息
        mOptions.setConnectionTimeout(60); //設定超時時間，單位秒
        mOptions.setUserName(username); //設定登入帳號
        mOptions.setPassword(password.toCharArray()); //設定登入密碼
        mOptions.setKeepAliveInterval(60); //多久確認一次client是否在線上
        mOptions.setMaxInflight(10); //允許同時發佈幾條訊息(未收到broker確認訊息)
        mOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1); //選擇MQTT版本
        return mOptions;
    }

    private MqttCallbackExtended mCallbackExtended = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            mIMqttResponse.connectState(true);
            Log.d(TAG, "connectComplete from :" + serverURI);
        }

        @Override
        public void connectionLost(Throwable cause) {
            mIMqttResponse.connectState(false);
            Log.d(TAG, "connect lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.d(TAG, "messageArrived : " + topic + new String(message.getPayload()));
            mIMqttResponse.receiveMessage(topic, message.toString());//topic, message.getPayload().toString()
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }
    };

    private MqttAsyncClient initClient(String serverURI, String clientId, MqttCallback callback, MqttConnectOptions options, String subscribeTopics, int[] qos){
        MqttAsyncClient asyncClient = null;

        try {
            MemoryPersistence persistence = new MemoryPersistence();
            asyncClient = new MqttAsyncClient(serverURI, clientId, persistence);
            asyncClient.setCallback(callback);
            asyncClient.connect(options);
            while (!asyncClient.isConnected()){
                Log.d("Phone", "RUNNING...");
            }

            String[] subscribeTopicsArray = new String[1];
            subscribeTopicsArray[0] = subscribeTopics;
            asyncClient.subscribe(subscribeTopicsArray, qos);

        }catch (MqttException e){
            Log.d("Mqtt Error", e.getMessage());
            e.printStackTrace();
        }

        return asyncClient;
    }

    public MqttAsyncHelper setUsername(String username){
        this.username = username;
        return this; //設定MqttAsyncHelper回傳的話，再呼叫時可以連用(.setUsername()...等)
    }

    public MqttAsyncHelper setPassword(String password){
        this.password = password;
        return this;
    }

    public MqttAsyncHelper setSubscriptionTopic(String subscriptionTopic) {
        this.subscriptionTopic = subscriptionTopic;
        return this;
    }

    public MqttAsyncHelper setPublishTopic(String publishTopic) {
        this.publishTopic = publishTopic;
        return this;
    }

    public MqttAsyncHelper setQos(int[] qos) {
        this.mQos = qos;
        return this;
    }

    public void build(){
        if (username != null && password != null && subscriptionTopic != null && publishTopic != null && mQos != null) {
            if (mAsyncClient == null) {
                mAsyncClient = initClient(serverUri, "0000-1234", mCallbackExtended, initMqttConnectOptions(), subscriptionTopic, mQos);
            }else
                mAsyncClient = initClient(serverUri, "0000-1234", mCallbackExtended, initMqttConnectOptions(), subscriptionTopic, mQos);
        } else {
            throw new NullPointerException();
        }
    }

    // TODO 發佈訊息，後面要參數化動態代入要發佈的資料
    public void publishData(String str) {
        MqttMessage msg = new MqttMessage();
        String msgStr = str;
        msg.setPayload(msgStr.getBytes()); //訊息內容
        msg.setQos(1); //設定Qos等級，0,1,2
        msg.setRetained(false); //是否保存最後一筆訊息，如果保存，client再次上線時，會再次收到上次發佈的最後一條訊息
        try {
            mAsyncClient.publish(publishTopic, msg); //設定訊息的topic，並發佈
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // TODO 斷開連線
    public void disconnect() {
        try {
            if (mAsyncClient != null) {
                mAsyncClient.disconnect();
                mIMqttResponse.connectState(false);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
