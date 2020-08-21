package com.zhaofujun.nest.context.event;

import com.zhaofujun.nest.NestApplication;
import com.zhaofujun.nest.configuration.ConfigurationManager;
import com.zhaofujun.nest.context.event.channel.MessageChannelProvider;
import com.zhaofujun.nest.context.event.channel.MessageConsumer;
import com.zhaofujun.nest.context.event.channel.MessageProducer;
import com.zhaofujun.nest.context.event.message.MessageInfo;
import com.zhaofujun.nest.standard.EventBus;
import com.zhaofujun.nest.standard.EventData;
import com.zhaofujun.nest.standard.EventHandler;

import java.util.Date;
import java.util.UUID;

public class DefaultEventBus implements EventBus {
    private ConfigurationManager configurationManager;

    public DefaultEventBus(NestApplication nestApplication) {

        this.configurationManager = nestApplication.getConfigurationManager();
    }

    public void publish(EventData eventData) {
        publish(eventData, "?");
    }

    public void publish(EventData eventData, String eventSource) {
        EventConfiguration eventConfiguration = configurationManager.getEventConfigurationByEventCode(eventData.getEventCode());


        MessageChannelProvider messageChannel = NestApplication.current().getProviderManage().getMessageChannel(eventConfiguration.getMessageChannelCode());
        MessageProducer messageProducer = messageChannel.getMessageProducer();


        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(UUID.randomUUID().toString());
        messageInfo.setData(eventData);
        messageInfo.setEventSource(eventSource);
        messageInfo.setSendTime(new Date());

        messageProducer.send(eventData.getEventCode(), messageInfo);

    }


    public void registerHandler(EventHandler eventHandler) {

        EventConfiguration eventConfiguration = configurationManager.getEventConfigurationByEventCode(eventHandler.getEventCode());

        MessageChannelProvider messageChannel = NestApplication.current().getProviderManage().getMessageChannel(eventConfiguration.getMessageChannelCode());

        MessageConsumer messageConsumer = messageChannel.getMessageConsumer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                messageConsumer.subscribe(eventHandler);
            }
        });
        thread.start();

    }

}
