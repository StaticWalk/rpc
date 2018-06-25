package com.iot.codec;

import com.iot.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/25
 * Time:19:12
 */
public class Encoder  extends MessageToByteEncoder {
	private Class<?> genericClass;

	public Encoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
		if (genericClass.isInstance(in)) {
//            使用序列化工具序列化in对象
			byte[] data = SerializationUtil.serialize(in);
//            向ByteBuf中写入data的长度
			out.writeInt(data.length);
//            写入data数据
			out.writeBytes(data);
		}
	}
}
