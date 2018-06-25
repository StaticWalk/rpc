package com.iot.server;


import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:17:02
 */
public class ServiceRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

	private CountDownLatch latch = new CountDownLatch(1);

	private String registryAddress;

	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public void register(String data) {
		if (data != null) {
			ZooKeeper zk = connectServer();
			if (zk != null) {
				createNode(zk, data);
			}
		}
	}

	//连接zookeeper服务器
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (IOException  e) {
			LOGGER.error("", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return zk;
	}

	//创建节点
	private void createNode(ZooKeeper zk, String data) {
		try {
			String path = zk.create(Constant.ZK_DATA_PATH, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			LOGGER.debug("create zookeeper node ({} => {})", path, data);
		} catch (KeeperException e) {
			LOGGER.error("", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}