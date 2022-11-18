package com.valvesoftware.source.query;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.valvesoftware.source.query.messages.ChallengeReply;
import com.valvesoftware.source.query.messages.InfoQuery;
import com.valvesoftware.source.query.messages.PlayerQuery;
import com.valvesoftware.source.query.messages.RulesQuery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class QueryClient {
	private static final String SRC = "Source Engine Query";

	private DatagramChannel channel;
	private EventLoopGroup worker;
	private Map<InetSocketAddress, Map.Entry<Query, CompletableFuture<?>>> requests = new HashMap<InetSocketAddress, Map.Entry<Query, CompletableFuture<?>>>();

	public QueryClient() {
		worker = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap()
			.group(worker)
			.channel(NioDatagramChannel.class)
			.handler(new ChannelInitializer<NioDatagramChannel>() {
				protected void initChannel(NioDatagramChannel ch) throws Exception {
					ch.pipeline().addLast(
						new MessageCodec(),
						new SimpleChannelInboundHandler<Reply>() {
							protected void channelRead0(ChannelHandlerContext ctx, Reply msg) throws Exception {
								Map.Entry<Query, CompletableFuture<?>> request = requests.get(msg.remoteAddress());
								if(request != null) {
									@SuppressWarnings("unchecked")
									CompletableFuture<Object> future = (CompletableFuture<Object>) request.getValue();
									future.complete(msg.payload());
									clearRequest(msg.remoteAddress());
								}
							}
						},
						new SimpleChannelInboundHandler<ChallengeReply>() {
							protected void channelRead0(ChannelHandlerContext ctx, ChallengeReply msg) throws Exception {
								Map.Entry<Query, CompletableFuture<?>> request = requests.get(msg.remoteAddress());
								if(request != null) {
									ctx.writeAndFlush(request.getKey().withChallenge(msg.payload()));
								}
							}
						}
					);
				}
			});
		channel = (DatagramChannel) bootstrap.bind(0).syncUninterruptibly().channel();
	}

	private void writeRequest(Query request, CompletableFuture<?> future) {
		clearRequest(request.remoteAddress());
		requests.put(request.remoteAddress(), Map.entry(request, future));
		channel.pipeline().writeAndFlush(request);
	}

	private void clearRequest(InetSocketAddress remoteAddress) {
		if(requests.get(remoteAddress) != null) {
			requests.get(remoteAddress).getValue().cancel(true);
			requests.put(remoteAddress, null);
		}
	}

	public CompletableFuture<ServerInfo> queryServer(InetSocketAddress serverAddress) {
		CompletableFuture<ServerInfo> future = new CompletableFuture<ServerInfo>();
		writeRequest(new InfoQuery(serverAddress, SRC, null), future);
		return future;
	}
	public CompletableFuture<List<PlayerInfo>> queryPlayers(InetSocketAddress serverAddress) {
		CompletableFuture<List<PlayerInfo>> future = new CompletableFuture<List<PlayerInfo>>();
		writeRequest(new PlayerQuery(serverAddress, null), future);
		return future;
	}
	public CompletableFuture<Map<String, String>> queryRules(InetSocketAddress serverAddress) {
		CompletableFuture<Map<String, String>> future = new CompletableFuture<Map<String, String>>();
		writeRequest(new RulesQuery(serverAddress, null), future);
		return future;
	}

	public void shutdown() {
		channel.close();
		worker.shutdownGracefully();
	}
}
