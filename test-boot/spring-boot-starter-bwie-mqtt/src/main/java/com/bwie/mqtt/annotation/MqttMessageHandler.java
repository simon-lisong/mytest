package com.bwie.mqtt.annotation;

import org.springframework.stereotype.Component;

@Component("MqttMessageHandler")
public class MqttMessageHandler {

    @MqttListener(topics={"yjs/1705C/#"})
    public void receiveMessage0(String topic, String payload) {
        System.out.println(topic + ": 处理消息(0) " + payload);
    }

    @MqttListener(topics={"yjs/1705C/aaa","yjs/1705C/bbb"})
    public void receiveMessage1(String topic, String payload) {
        System.out.println(topic + ": 处理消息(1) " + payload);
    }

    @MqttListener(topics={"yjs/1705C/aaa"})
    public void receiveMessage2(String topic, String payload) {
        System.out.println(topic + ": 处理消息(2) " + payload);
    }
}
