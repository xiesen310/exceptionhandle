package com.zork.exceptionhandle;

import com.zork.exceptionhandle.zk.*;
import junit.framework.TestCase;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReadPropertiesTest extends TestCase {
    private ZkClient zkClient = new ZkClient("192.168.218.100,192.168.218.101,192.168.218.102");
    private String rootNode = "/node20000000015";
    ConfigChangeSubscriber zkConfig = new ZkConfigChangeSubscriberImpl(zkClient,rootNode);

    private DynamicPropertiesHelperFactory helperFactory = new DynamicPropertiesHelperFactory(new ZkConfigChangeSubscriberImpl(zkClient, rootNode));

    public void testA() throws InterruptedException {
        if (!this.zkClient.exists("/node20000000015/test1.properties"))
            this.zkClient.createPersistent("/node20000000015/test1.properties");

        final CountDownLatch latch = new CountDownLatch(1);
        this.zkConfig.subscribe("test1.properties", new ConfigChangeListener() {
            @Override
            public void configChanged(String key, String values) {
                System.out.println("test1接收到数据变更通知: key = " + key + " , values = " + values);
                latch.countDown();
            }
        });
        this.zkClient.writeData("/node20000000015/test1.properties", "aa=1 ");
        boolean notified = latch.await(30L, TimeUnit.SECONDS);
        if(!notified)
            fail("客户端没有收到变更通知");
    }
}
