package com.iot.server;

import com.iot.model.RpcRequest;
import com.iot.model.RpcResponse;
import com.iot.utils.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:17:36
 *
 * 服务端接收消息后，通过反射调用??
 *    好像不是很能理解
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest>{

	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ServerHandler.class);

	//存放接口名与服务Bean之间的映射关系
	private final Map<String, Object> handlerMap;

	public ServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		RpcResponse response=new RpcResponse();
		response.setRequestId(msg.getRequestId());
		try{
			//执行服务器端的任务
			Object result= handle(msg);

			//设置结果
			response.setResult(result);
		}catch (Throwable throwable){
			response.setException((Exception) throwable);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}


	//使用反射或者CGLib
	private Object handle(RpcRequest request) throws Exception {
		// 获取服务对象
		String serviceName = request.getInterfaceName();
		String serviceVersion = request.getServiceVersion();
		if (StringUtil.isNotEmpty(serviceVersion)) {
			serviceName += "-" + serviceVersion;
		}
		Object serviceBean = handlerMap.get(serviceName);
		if (serviceBean == null) {
			throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
		}
		// 获取反射调用所需的参数
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		// 执行反射调用
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);
		// 使用 CGLib 执行反射调用 ??
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("server handler caught exception", cause);
		ctx.close();
	}
}
