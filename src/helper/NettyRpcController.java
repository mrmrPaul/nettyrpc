package helper;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class NettyRpcController implements RpcController
{
	private String reason;
	private boolean failed;
	private boolean canceled;

	private RpcCallback<Object> callback;


	@Override public void reset()
	{
		reason = null;
		failed = false;
		canceled = false;
		callback = null;
	}

	@Override public boolean failed()
	{
		return failed;
	}

	@Override public String errorText()
	{
		return reason;
	}

	@Override public void startCancel()
	{
		canceled = true;
	}

	@Override public void setFailed(String reason)
	{
		this.failed = true;
		this.reason = reason;
	}

	@Override public boolean isCanceled()
	{
		return canceled;
	}

	@Override public void notifyOnCancel(RpcCallback<Object> rpcCallback)
	{
		this.callback = rpcCallback;
	}
}
