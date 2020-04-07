package com.bwie.mqtt.consumer;

import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

public class SubscribeTopicsManager {

    private MqttPahoMessageDrivenChannelAdapter adapter;
    public SubscribeTopicsManager(MqttPahoMessageDrivenChannelAdapter adapter){
        this.adapter = adapter;
    }

    public void addTopic(String topic, int qos) {
        this.adapter.addTopic(topic,qos);
    }

    public String[] getTopic() {
        return this.adapter.getTopic();
    }

    public void removeTopic(String topic) {
        this.adapter.removeTopic(topic);
    }
}
