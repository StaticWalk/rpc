package com.iot.server;

import com.iot.annotation.RpcService;
import com.iot.codec.RpcCodec;
import com.iot.model.RpcRequest;
import com.iot.model.RpcResponse;
import com.iot.utils.ClassUtil;
import com.iot.utils.PropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:17:36
 */
public class RpcServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

	//服务器地址
	private String serverAddress;
	private ServiceRegistry serviceRegistry;
	private String servicePackage;

	// 存放接口名与service之间的映射关系
	private Map<String, Object> serviceMap = new ConcurrentHashMap();

	{
		Properties properties = PropertiesUtil.loadProps("server-config.properties");
		serverAddress = properties.getProperty("server.address");
		servicePackage = properties.getProperty("server.servicePackage");
		serviceRegistry = new ServiceRegistry(properties.getProperty("registry.address"));
		getRpcService();
	}


	private void getRpcService() {

		List<Class<?>> classList = ClassUtil.getClassList(servicePackage);
		if (classList != null && classList.size() > 0) {
			for (Class c : classList) {
				if (c.isAnnotationPresent(RpcService.class)) {
					String interfaceName = ((RpcService) c.getAnnotation(RpcService.class)).value().getName();
					try {
						serviceMap.put(interfaceName, c.newInstance());
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void addService(Object service) {
		Class<?>[] interfaces = service.getClass().getInterfaces();
		for (Class c : interfaces) {
			serviceMap.put(c.getName(), service);
		}
	}

	public List<String> getActiveServiceName() {
		return new ArrayList(serviceMap.keySet());
	}

	public Object removeService(String serviceName) {
		return serviceMap.remove(serviceName);
	}

	public void start() {
		//启动Rpc服务
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline()
									.addLast(new RpcCodec(RpcRequest.class, RpcResponse.class))
									.addLast(new ServerHandler(serviceMap));
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);

			ChannelFuture future = bootstrap.bind(host, port).sync();
			LOGGER.debug("server started in {} on port {}", host, port);

			if (serviceRegistry != null) {
				// 向注册中心注册服务地址
				serviceRegistry.register(serverAddress);
			}

			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
