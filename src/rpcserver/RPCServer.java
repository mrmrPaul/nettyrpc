package rpcserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 服务端 启动时 将rpc服务暴露
 */
public class RPCServer
{
	public static void main(String args[])
	{
		RPCServer server = new RPCServer();
		server.start("localhost", 54321);
	}

	public void start(String host, int port)
	{
		ServerBootstrap bootstrap = new ServerBootstrap(); // 服务端引导
		// 两个独立的线程 IO分离
		NioEventLoopGroup boss = new NioEventLoopGroup();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		bootstrap.group(boss, worker)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>()
				{
					@Override protected void initChannel(SocketChannel socketChannel) throws Exception
					{
						ChannelPipeline pipeline = socketChannel.pipeline();
						// 设置编解码器
						pipeline.addLast(new StringDecoder());
						pipeline.addLast(new StringEncoder());
						// 服务端消息处理
						pipeline.addLast(new ServerHandler());
					}
				});

		try
		{
			ChannelFuture future = bootstrap.bind(host, port).sync();
			future.addListener(future1 -> {
				if (future1.isSuccess())
					System.out.printf("Server start succeeded on port: %d host:%s\n", port, host);
			});

			future.channel().closeFuture().sync();
		}
		catch (Exception e)
		{
			boss.shutdownGracefully();
			worker.shutdownGracefully();
			e.printStackTrace();
		}
	}
}
