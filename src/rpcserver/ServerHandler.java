package rpcserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import publicinterface.HelloServiceImpl;

public class ServerHandler extends ChannelInboundHandlerAdapter
{
	@Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		String message = (String) msg;
		if (message.startsWith("#"))
		{
			String response = new HelloServiceImpl().hello(message);
			ctx.writeAndFlush(response);
		}
	}
}
