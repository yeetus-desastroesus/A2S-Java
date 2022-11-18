package com.valvesoftware.source.query.messages;

import java.net.InetSocketAddress;

import com.valvesoftware.source.query.Query;

import io.netty.buffer.ByteBuf;

public record PlayerQuery(InetSocketAddress remoteAddress, Integer challenge) implements Query {
	public static final byte OP = 0x55;

	public PlayerQuery withChallenge(int challenge) { return new PlayerQuery(remoteAddress(), challenge); }

	public static PlayerQuery read(InetSocketAddress remoteAddress, ByteBuf buffer) {
		Integer challenge = buffer.readIntLE();
		if(challenge==0) challenge = null;
		return new PlayerQuery(remoteAddress, challenge);
	}
	public PlayerQuery write(ByteBuf buffer) {
		buffer.writeByte(OP);
		buffer.writeIntLE(challenge()!=null?challenge():-1);
		return this;
	}
}
