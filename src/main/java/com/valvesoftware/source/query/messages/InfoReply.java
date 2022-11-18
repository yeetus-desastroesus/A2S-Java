package com.valvesoftware.source.query.messages;

import java.net.InetSocketAddress;

import com.valvesoftware.source.query.ServerInfo;
import com.valvesoftware.source.query.Reply;

import io.netty.buffer.ByteBuf;

public record InfoReply(InetSocketAddress remoteAddress, ServerInfo payload) implements Reply {
	public static final byte OP = 0x49;

	public static InfoReply read(InetSocketAddress remoteAddress, ByteBuf buffer) {
		ServerInfo payload = ServerInfo.read(remoteAddress, buffer);
		return new InfoReply(remoteAddress, payload);
	}
	public InfoReply write(ByteBuf buffer) {
		buffer.writeByte(OP);
		payload().write(buffer);
		return this;
	}
}
