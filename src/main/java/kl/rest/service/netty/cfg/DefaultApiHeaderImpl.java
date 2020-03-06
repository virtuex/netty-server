package kl.rest.service.netty.cfg;

import kl.rest.service.constant.ApiHeaderKey;

/**
 * 默认的api头参数，通过reqtype识别接口
 * @author
 *
 */
public class DefaultApiHeaderImpl implements IApiHeader {



	@Override
	public String getSessionIdKey() {
		return ApiHeaderKey.SESSION_ID;
	}


	@Override
	public String getClientIp() {
		return ApiHeaderKey.CDS_CLIENT_IP;
	}
}
