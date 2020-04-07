package com.bwie.mqtt.consumer;

//@Service
public class MqttMessageHandlerDev{

    public void receiveMessage(String topic, String payload) {
        System.out.println(topic + "收到消息:" + payload);
        //自己的业务逻辑

    }
}
