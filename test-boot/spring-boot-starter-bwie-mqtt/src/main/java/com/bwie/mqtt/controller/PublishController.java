package com.bwie.mqtt.controller;

import com.bwie.mqtt.producer.MqttPahoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublishController {
    /**
     * 注入发送MQTT的Bean
     */
    @Autowired
    private MqttPahoTemplate mqttProducer;

    // 发送自定义消息内容（使用默认主题）
    @GetMapping("/publish/{data}")
    public void publish(@PathVariable("data") String data) {
        mqttProducer.publish(data);
    }

    // 发送自定义消息内容，且指定主题
    @GetMapping("/publish/{topic}/{data}")
    public void publish(@PathVariable("topic") String topic, @PathVariable("data") String data) {
        mqttProducer.publish(topic, data);
    }

    // 发送自定义消息内容，且指定主题
    @GetMapping("/publish/{topic}/{qos}/{data}")
    public void publish(@PathVariable("topic") String topic, @PathVariable("qos") int qos,@PathVariable("data") String data) {
        mqttProducer.publish(topic, qos,data);
    }
}
