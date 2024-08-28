package com.ericsson.oss.mediation.test.util.queue;

import static com.jayway.awaitility.Awaitility.await;

import com.jayway.awaitility.Duration;
import org.hornetq.api.jms.management.JMSQueueControl;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

/**
 * Created by emaynes on 02/04/2016.
 */
public class ChannelManagement {

    private final String channelName;
    private final String mbeanName;
    private JMSQueueControl queueControl;

    public ChannelManagement(final String channelName) {
        this.channelName = channelName.replace("//global/", "");
        this.mbeanName = String.format(
                            "jboss.as:subsystem=messaging,hornetq-server=default,runtime-queue=\"%1$s\"",
                            this.channelName);
    }

    public void pauseChannel() {
        try {
            getQueueControl().pause();
        } catch (final Exception e) {
            throw new ChannelOperationException("Failed to pause channel: " + channelName, e);
        }
    }

    public void resumeChannel() {
        try {
            clear();
            getQueueControl().resume();
        } catch (final Exception e) {
            throw new ChannelOperationException("Failed to resume channel: " + channelName, e);
        }
    }

    public void resumeAndWaitUtilEmpty() {
        try {
            resumeChannel();
            await().atMost(Duration.ONE_MINUTE).until(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return getQueueControl().countMessages(null) == 0;
                }
            });
        } catch (final Exception e) {
            throw new ChannelOperationException("Failed to get number of messages in channel: " + channelName, e);
        }
    }

    public void clear() {
        try {
            getQueueControl().expireMessages(null);
        } catch (final Exception e) {
            throw new ChannelOperationException("Failed to clear channel: " + channelName, e);
        }
    }

    protected JMSQueueControl getQueueControl() {
        if (queueControl == null) {
            try {
                final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                final ObjectName objectName = ObjectName.getInstance(mbeanName);
                queueControl = MBeanServerInvocationHandler.newProxyInstance(server, objectName, JMSQueueControl.class, false);
            } catch (final MalformedObjectNameException e) {
                throw new ChannelNotFoundException("Could not find management MBean for channel: " + channelName, e);
            }
        }

        return this.queueControl;
    }
}
