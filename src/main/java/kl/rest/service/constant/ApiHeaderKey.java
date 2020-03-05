package kl.rest.service.constant;

public class ApiHeaderKey {

	private ApiHeaderKey() {
		throw new IllegalStateException("Utility class");
	}

	// 会话ID
	public static final String SESSION_ID = "SESSION-ID";

	// 客户端ip
	public static final String CDS_CLIENT_IP = "CDS-CLIENT-IP";

}
