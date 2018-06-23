package com.iot.codec;

import com.iot.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:19:58
 *
 * 通常使用encode和decode来出来
 *
 */
public class RpcCodec extends ByteToMessageCodec {

	private Class<?> encodeClass,decodeClass;

	public RpcCodec( Class<?> encodeClass, Class<?> decodeClass) {
		this.encodeClass = encodeClass;
		this.decodeClass = decodeClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

		if (encodeClass.isInstance(msg)){
			byte[] data= SerializationUtil.serialize(msg);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		out.add(SerializationUtil.deserialize(data, decodeClass));
	}
}
