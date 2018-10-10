package test;

import publicinterface.HelloService;
import rpcclient.RPCProxy;

public class Client
{
	public static void main(String args[]) throws Exception
	{
		RPCProxy proxy = new RPCProxy();
		HelloService service = (HelloService)proxy.createProxy(HelloService.class, "#");
		for (int i=0; i < 100; i++)
		{
			Thread.sleep(1000);
			System.out.println(service.hello("How are you?"));
		}
	}
}
