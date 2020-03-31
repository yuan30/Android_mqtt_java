package com.example.mqtt_test;

public interface IMqttResponse {
    void receiveMessage(String topic, String message);
    void connectState(boolean connectState);
}
