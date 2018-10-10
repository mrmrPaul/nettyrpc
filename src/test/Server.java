package test;

import rpcserver.RPCServer;

public class Server
{
	public static void main(String args[])
	{
		RPCServer server = new RPCServer();
		loadService(server);
		server.start("localhost", 54321);
	}

	// 加载rpc服务
	private static void loadService(RPCServer server)
	{
//		server.registerService();
	}
}
