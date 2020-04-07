package com.bwie.mqtt.controller;

import com.bwie.mqtt.consumer.SubscribeTopicsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscribeController {
    /**
     * 注入发送MQTT的Bean
     */
    @Autowired
    private SubscribeTopicsManager subscribeTopicsManager;

    // 发送自定义消息内容（使用默认主题）
    @GetMapping("/subscribe/addTopic")
    public String addTopic(@RequestParam("topic") String topic,@RequestParam("qos")int qos) {
        subscribeTopicsManager.addTopic(topic,qos);

        return "Success";
    }

    // 发送自定义消息内容，且指定主题
    @GetMapping("/subscribe/getTopics")
    public String[] getTopics() {
       return subscribeTopicsManager.getTopic();
    }

    // 发送自定义消息内容，且指定主题
    @GetMapping("/subscribe/removeTopic")
    public String removeTopic(@RequestParam("topic") String topic) {
        subscribeTopicsManager.removeTopic(topic);
        return "Success";
    }
}
