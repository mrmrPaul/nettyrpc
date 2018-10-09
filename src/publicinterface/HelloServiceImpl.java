package publicinterface;

public class HelloServiceImpl implements HelloService
{
	@Override public String hello(String msg)
	{
		return msg == null ? "I am fine." : msg + "-----> I am fine.";
	}
}
