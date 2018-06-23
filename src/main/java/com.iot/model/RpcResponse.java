package com.iot.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:19:50
 */

@Setter
@Getter
public class RpcResponse {

	private String requestId;
	private Exception exception;
	private Object result;

	public boolean hasException() {
		return exception != null;
	}

}
