package com.iot.ZookeeperTest;

import org.apache.zookeeper.*;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/24
 * Time:12:45
 */
public class ZookeeperTest {
	private ZooKeeper zk = null;
	
	private static final String PATH="/root";
//	private static final String PATH="/usr/local/zookeeper-3.4.11/data";

	public ZookeeperTest(){
		try {
			zk = new ZooKeeper("10.24.36.209:2181", 500000,new Watcher() {
				// 监控所有被触发的事件
				public void process(WatchedEvent event) {
					System.out.println(event.getPath());
					System.out.println(event.getType().name());
					System.out.println(event.getState().getIntValue());
				}
			});
			zk.exists(PATH+"/childone", true);//观察这个节点发生的事件

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public  void createNodes() {
		try {

			//创建一个节点root，数据是mydata,不进行ACL权限控制，节点为永久性的(即客户端shutdown了也不会消失)
//			if (null==zk.exists("/root",false))
//			{
//				zk.create("/root", "mydata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//			}

			if (null==zk.exists("/root",false)) {
				zk.create( "/root", "childone".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}


			//在root下面创建一个childone znode,数据为childone,不进行ACL权限控制，节点为永久性的
//			if (null==zk.exists("/root/childone",false)) {
//				zk.create( "/root/childone", "childone".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public  void updateNodes() {
		try {

			//取得/root/childone节点下的数据,返回byte[]
			System.out.println(new String(zk.getData(PATH+"/childone", true, null)));

			//修改节点/root/childone下的数据，第三个参数为版本，如果是-1，那会无视被修改的数据版本，直接改掉
			zk.setData(PATH+"/childone","127.0.0.1:8080".getBytes(), -1);

			System.out.println(new String(zk.getData(PATH+"/childone", true, null)));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public  void deleteNodes() {
		try {

			//第二个参数为版本，－1的话直接删除，无视版本
			zk.delete(PATH+"/childone", -1);
			zk.delete(PATH, -1);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			ZookeeperTest zkTest = new ZookeeperTest();
			zkTest.createNodes();
//			zkTest.updateNodes();
//			zkTest.deleteNodes();

			while(true){
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}