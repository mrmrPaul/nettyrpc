package rpcserver;

import com.google.protobuf.Message;
import com.google.protobuf.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import publicinterface.RpcProto;

/**
 * 服务端 启动时 将rpc服务暴露
 */
public class RPCServer
{
	private ServerHandler serverHandler;

	public RPCServer()
	{
		serverHandler = new ServerHandler();
	}

	// 注册rpc服务
	public void registerService(Service service)
	{
		serverHandler.registerService(service);
	}

	public void unregisterService(Service service)
	{
		serverHandler.unregisterService(service);
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
						Message defaultInstance = RpcProto.RpcRequest.getDefaultInstance();
						pipeline.addLast("protobufEncoder", new ProtobufEncoder());
						pipeline.addLast("protobufDecoder", new ProtobufDecoder(defaultInstance));
						// 服务端消息处理
						pipeline.addLast(serverHandler);
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
