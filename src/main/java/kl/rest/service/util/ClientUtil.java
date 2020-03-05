package kl.rest.service.util;

import java.net.InetSocketAddress;

import org.apache.commons.lang3.StringUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class ClientUtil {

	public static String getClientIP(ChannelHandlerContext ctx, FullHttpRequest request) {
		// 获取客户端IP，记录日志使用
		String clientIP = request.headers().get("X-Forwarded-For");
		if (StringUtils.isBlank(clientIP) || "unknown".equalsIgnoreCase(clientIP)) {
			clientIP = request.headers().get("Proxy-Client-IP");
		}
		if (StringUtils.isBlank(clientIP) || "unknown".equalsIgnoreCase(clientIP)) {
			clientIP = request.headers().get("WL-Proxy-Client-IP");
		}
		if (StringUtils.isBlank(clientIP) || "unknown".equalsIgnoreCase(clientIP)) {
			InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
			clientIP = insocket.getAddress().getHostAddress();
		}
		return clientIP;
	}

}
