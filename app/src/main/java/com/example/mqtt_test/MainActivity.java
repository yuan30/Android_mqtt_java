package com.example.mqtt_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IMqttResponse {

    private MqttAsyncHelper mMqttAsyncHelper;

    private static TextView mTxtConnectState;
    private static TextView mTxtReceive;
    private static EditText mTxtSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtConnectState = findViewById(R.id.mTxtConnectState);
        mTxtReceive = findViewById(R.id.mTxtReceive);
        mTxtSend = findViewById(R.id.mTxtSend);
        

    }

    public void ButtonClickListener(View view){

        int BtnId = view.getId();
        if(BtnId == R.id.mBtnConnect){
            if (mMqttAsyncHelper == null) {
                mMqttAsyncHelper = new MqttAsyncHelper(MainActivity.this, MainActivity.this);
                mMqttAsyncHelper.setUsername("1")
                        .setPassword("")
                        .setSubscriptionTopic("test123")
                        .setPublishTopic("test456")
                        .setQos(new int[]{1})
                        .build();
            } else {
                mMqttAsyncHelper.setUsername("1")
                        .setPassword("")
                        .setSubscriptionTopic("test123")
                        .setPublishTopic("test456")
                        .setQos(new int[]{1})
                        .build();
            }
        }else if(BtnId == R.id.mBtnSend){
            if(mMqttAsyncHelper != null)
                mMqttAsyncHelper.publishData(mTxtSend.getText().toString());
        }else{
            if(mMqttAsyncHelper != null)
                mMqttAsyncHelper.disconnect();
        }
    }

    @Override
    public void receiveMessage(String topic, String response) {

        Message msg = new Message();
        msg.arg1 = 2;
        msg.obj = response;
        handler.sendMessage(msg);
    }

    @Override
    public void connectState(boolean connectState) {

        final String connectStateString;
        if (connectState) {
            connectStateString = "Connect";
        } else {
            connectStateString = "Disconnect";
        }
        Message msg = new Message();
        msg.arg1 = 1;
        msg.obj = connectStateString;
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 1)
            {
                mTxtConnectState.setText(msg.obj.toString());
            }else if(msg.arg1 == 2){
                mTxtReceive.setText(mTxtReceive.getText().toString() + "\n" +msg.obj.toString());
            }else if(msg.arg1 == 3){
                mTxtSend.setText(msg.obj.toString());
            }
        }
    };
}
