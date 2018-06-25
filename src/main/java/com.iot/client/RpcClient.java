package com.iot.client;

import com.iot.codec.RpcCodec;
import com.iot.model.RpcRequest;
import com.iot.model.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:21:22
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

	private String host;
	private int port;
	private RpcResponse response;

	private final Object obj = new Object();

	public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		this.response = response;

		synchronized (obj) {
			obj.notifyAll(); // 收到响应，唤醒线程
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("client caught exception", cause);
		ctx.close();
	}

	public RpcResponse send(RpcRequest request) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline()
									.addLast(new RpcCodec(RpcRequest.class,RpcResponse.class))
//									.addLast(new Encoder(RpcRequest.class))
//									.addLast(new Decoder(RpcResponse.class))
									.addLast(RpcClient.this);
						}
					})
					.option(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();

			synchronized (obj) {
				obj.wait(); // 未收到响应，使线程等待
			}

			if (response != null) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
	}
}
