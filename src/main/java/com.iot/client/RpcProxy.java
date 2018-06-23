package com.iot.client;

import com.iot.model.RpcRequest;
import com.iot.model.RpcResponse;
import com.iot.utils.PropertiesUtil;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import org.apache.zookeeper.KeeperException;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:21:24
 */
public class RpcProxy {

	//服务提供方地址，由ServiceDiscover组件中获得。
	private String serverAddress;
	private ServiceDiscovery serviceDiscovery;

	{
		Properties properties = PropertiesUtil.loadProps("client-config.properties");
		try {
			serviceDiscovery = new ServiceDiscovery(properties.getProperty("discovery.address"));
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass) {
		return (T) Proxy.newProxyInstance(
				interfaceClass.getClassLoader(),
				new Class<?>[]{interfaceClass},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
						request.setRequestId(UUID.randomUUID().toString());
						request.setInterfaceName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);

						if (serviceDiscovery != null) {
							serverAddress = serviceDiscovery.discover();
						}

						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);

						RpcClient client = new RpcClient(host, port);
						RpcResponse response = client.send(request);
						if (response.hasException()) {
							throw response.getException();
						} else {
							return response.getResult();
						}
					}
				}
		);
	}
}