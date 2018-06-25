package com.iot.server;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:17:23
 */
public interface Constant {


	int ZK_SESSION_TIMEOUT = 20000;

	String ZK_REGISTRY_PATH = "/root";
//	String ZK_REGISTRY_PATH = "/registry";
	String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
//	String ZK_DATA_PATH ="/*";

}
