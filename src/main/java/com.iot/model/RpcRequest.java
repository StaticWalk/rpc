package com.iot.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:19:49
 */


@Getter
@Setter
public class RpcRequest {
	private String requestId;
	private String interfaceName;
	private String serviceVersion;
	private String methodName;
	private Class<?>[] parameterTypes;
	private Object[] parameters;
}
