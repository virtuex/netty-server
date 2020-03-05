package kl.rest.service.netty.cfg;

import kl.rest.service.constant.ApiHeaderKey;

/**
 * 通过uri的方式识别接口
 * @author wumc
 *
 */
public class UriApiHeaderImpl implements IApiHeader {


	@Override
	public String getSessionIdKey() {
		return ApiHeaderKey.SESSION_ID;
	}


	@Override
	public String getClientIp() {
		return ApiHeaderKey.CDS_CLIENT_IP;
	}

}
