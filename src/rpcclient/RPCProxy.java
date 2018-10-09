package rpcclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RPCProxy
{
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static ClientHandler clientHandler;

	public Object createProxy(final Class<?> serviceClass, final String requestID)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InvocationHandler invocationHandler = ((proxy, method, args) -> {
			if (clientHandler == null)
				initCLientHandler();
			clientHandler.setPara(requestID + args[0]);
			return executor.submit(clientHandler).get();
		});

		return Proxy.newProxyInstance(classLoader, new Class<?>[]{ serviceClass }, invocationHandler );
	}


	private void initCLientHandler()
	{
		clientHandler = new ClientHandler();
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>()
				{
					@Override protected void initChannel(SocketChannel socketChannel) throws Exception
					{
						ChannelPipeline pipeline = socketChannel.pipeline();
						pipeline.addLast(new StringDecoder());
						pipeline.addLast(new StringEncoder());
						pipeline.addLast(clientHandler);
					}
				});
		try
		{
			bootstrap.connect("localhost", 54321).sync();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
