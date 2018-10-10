package rpcserver;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import helper.NettyRpcController;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import publicinterface.RpcProto;
import publicinterface.RpcProto.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc的处理均为非阻塞
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcProto.RpcRequest>
{
	private final Map<String, Service> serviceMap = new ConcurrentHashMap<>();

	@Override protected void channelRead0(ChannelHandlerContext ctx, RpcProto.RpcRequest rpcRequest) throws Exception
	{
		String serviceName = rpcRequest.getClassName();
		String methodName = rpcRequest.getMethodName();

		Service service = serviceMap.get(serviceName);
		if (service == null)
		{
			System.out.println("no such service " + serviceName);
			// TODO throw exception
			return;
		}

		MethodDescriptor methodDescriptor = service.getDescriptorForType().findMethodByName(methodName);
		if (methodDescriptor == null)
		{
			System.out.printf("Service %s has no method %s\n", serviceName, methodName);
			// TODO throw Exception
			return;
		}

		Message methodRequest = null;
		try
		{
			methodRequest = buildMessageFromPrototype(service.getRequestPrototype(methodDescriptor),
					rpcRequest.getRequestMessage());
		}
		catch (InvalidProtocolBufferException ex)
		{
			// TODO throw exception
		}

		final Channel channel = ctx.channel();
		final RpcController controller = new NettyRpcController();
		// 该回调用于在需要的时候将rpc调用结果返回给客户端
		RpcCallback<Message> callback = !rpcRequest.hasRequestId() ? null : message -> {
			RpcResponse.Builder responseBuilder = RpcResponse.newBuilder();

			responseBuilder.setResponseId(rpcRequest.getRequestId());
			responseBuilder = message != null
					? responseBuilder.setResponseMessage(message.toByteString())
					: responseBuilder.setErrorCode(RpcProto.ErrorCode.RPC_ERROR).setErrorMessage(controller.errorText());
			channel.writeAndFlush(responseBuilder);
		};

		try
		{
			service.callMethod(methodDescriptor, controller, methodRequest, callback);
		}
		catch (Exception ex)
		{
			// TODO throw exception
		}
	}

	private Message buildMessageFromPrototype(Message prototype, ByteString messageToBuild)
		throws InvalidProtocolBufferException
	{
		return prototype.newBuilderForType().mergeFrom(messageToBuild).build();
	}

	// 注册rpc服务
	synchronized public void registerService(Service service)
	{
		if (serviceMap.containsKey(service.getDescriptorForType().getFullName()))
		{
			System.out.println("Service already registered.");
			return;
		}
		serviceMap.put(service.getDescriptorForType().getFullName(), service);
	}

	synchronized public void unregisterService(Service service)
	{
		serviceMap.remove(service.getDescriptorForType().getFullName());
	}
}
