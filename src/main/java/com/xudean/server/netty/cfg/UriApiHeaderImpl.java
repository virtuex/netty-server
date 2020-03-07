package com.xudean.server.netty.cfg;

import com.xudean.server.constant.ApiHeaderKey;

/**
 * 通过uri的方式识别接口
 * @author
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
