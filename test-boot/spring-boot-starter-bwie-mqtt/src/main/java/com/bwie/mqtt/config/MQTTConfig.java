package com.bwie.mqtt.config;

import com.bwie.mqtt.annotation.MqttListener;
import com.bwie.mqtt.consumer.MqttMessageHandlerDev;
import com.bwie.mqtt.consumer.SubscribeTopicsManager;
import com.bwie.mqtt.context.SpringApplicationContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

@Configuration
public class MQTTConfig {

    /**
     * 发布的bean名称
     */
    public static final String CHANNEL_NAME_OUT = "mqttOutboundChannel";
    public static final String CHANNEL_NAME_IN = "mqttInboundChannel";

    // 客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息
    private static final byte[] WILL_DATA;
    static {
        WILL_DATA = "offline".getBytes();
    }

    @Value("${mqtt.client.username}")
    private String username;

    @Value("${mqtt.client.password}")
    private String password;

    @Value("${mqtt.client.brokerURIs}")
    private String brokeURIs;

    @Value("${mqtt.client.keepAliveInterval}")
    private Integer keepAliveInterval;

    @Value("${mqtt.client.connectionTimeout}")
    private Integer connectionTimeout;

    @Value("${mqtt.producer.clientId}")
    private String producerClientId;

    @Value("${mqtt.producer.defaultQos}")
    private Integer producerDefaultQos;

    @Value("${mqtt.producer.defaultTopic}")
    private String producerDefaultTopic;

    @Value("${mqtt.producer.defaultRetained}")
    private boolean producerDefaultRetained;

    @Value("${mqtt.consumer.clientId}")
    private String consumerClientId;

    @Value("${mqtt.consumer.defaultQos}")
    private Integer consumerDefaultQos;

    @Value("${mqtt.consumer.completionTimeout}")
    private Integer consumerCompletionTimeout;

    @Value("${mqtt.consumer.topics}")
    private String consumerTopics;

    /**
     * MQTT客户端
     */
    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        // 设置连接的用户名
        if(!username.trim().equals("")){
            factory.setUserName(username);
            // 设置连接的密码
            factory.setPassword(password);
        }
        // 设置连接的地址
        if(brokeURIs.indexOf(",")!=-1){
            String[] urls= StringUtils.split(brokeURIs, ",");
            factory.setServerURIs(urls);
        }else{
            factory.setServerURIs(brokeURIs);
        }

        // 设置超时时间 单位为秒
        factory.setConnectionTimeout(connectionTimeout);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线
        // 但这个方法并没有重连的机制
        factory.setKeepAliveInterval(keepAliveInterval);
        // 设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息。
        //options.setWill("willTopic", WILL_DATA, 2, false);
        return factory;
    }

    /**
     * MQTT信息通道（生产者）
     */
    @Bean(name = CHANNEL_NAME_OUT)
    public MessageChannel outboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT消息处理器（生产者）
     */
    @Bean
    @ServiceActivator(inputChannel = CHANNEL_NAME_OUT)
    public MessageHandler outbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                producerClientId,
                mqttPahoClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(producerDefaultQos);
        messageHandler.setDefaultRetained(producerDefaultRetained);
        messageHandler.setDefaultTopic(producerDefaultTopic);
        return messageHandler;
    }

    /**
     * MQTT信息通道（消费者）
     */
    @Bean(name = CHANNEL_NAME_IN)
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT消息处理器（消费者）
     */
    @Bean
    public MessageProducer inbound() {
        String[] topics;
        if(consumerTopics.indexOf(",")!=-1){
            topics= StringUtils.split(consumerTopics, ",");
        }else{
            topics = new String[]{consumerTopics};
        }
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(consumerClientId,
                mqttPahoClientFactory(), topics);
        adapter.setCompletionTimeout(consumerCompletionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(consumerDefaultQos);
        adapter.setOutputChannel(inboundChannel());
        return adapter;
    }

    @Bean
    public SubscribeTopicsManager subscribeTopicsManager() {
        MqttPahoMessageDrivenChannelAdapter adapter = (MqttPahoMessageDrivenChannelAdapter)inbound();
        SubscribeTopicsManager subscribeTopicsManager = new SubscribeTopicsManager(adapter);
        return subscribeTopicsManager;
    }

    @Autowired
    MqttMessageHandlerDev mqttMessageHandlerDev;

    @Bean
    public MqttMessageHandlerDev mqttMessageHandler() {
        MqttMessageHandlerDev mqttMessageHandlerDev = new MqttMessageHandlerDev();
        return mqttMessageHandlerDev;
    }

    @Bean
    //ServiceActivator注解表明当前方法用于处理MQTT消息，inputChannel参数指定了用于接收消息信息的channel。
    @ServiceActivator(inputChannel = MQTTConfig.CHANNEL_NAME_IN)
    public MessageHandler handler() {
        return message -> {
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
            String payload = message.getPayload().toString();
            // 根据topic分别进行消息处理。
            //具体的业务逻辑
            //handlerAnnotation(topic,payload);
            mqttMessageHandler().receiveMessage(topic,payload);
        };
    }

    /**
     *
     * @param topic
     * @param payload
     */
    private void handlerAnnotation(String topic,String payload){
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages("com.bwie").addScanners(
                        new SubTypesScanner()).addScanners(new MethodAnnotationsScanner()));

        //获取带MqttListener注解的类
        Set<Method> methodList = reflections.getMethodsAnnotatedWith(MqttListener.class);
        for (Method method : methodList) {
            Class<?> declaringClass = method.getDeclaringClass();
            Object bean = SpringApplicationContextUtils.getBean("MqttMessageHandler");
            MqttListener mqttListenerAnnotation = (MqttListener) method.getAnnotation(MqttListener.class);
            String[] topicsAnnotation = mqttListenerAnnotation.topics();
            for(String topicAnnotation : topicsAnnotation){
                String topicForRegex = topicAnnotation
                        .replaceAll("/", "\\\\/")
                        .replaceAll("\\+", "[^/]+")
                        .replaceAll("#", "(.+)") + "$";
                if(topic.matches(topicForRegex)){
                    try {
                        method.invoke(bean,topic,payload);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
