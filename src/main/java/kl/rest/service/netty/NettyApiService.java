package kl.rest.service.netty;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import kl.rest.service.netty.cfg.NettyApiCfg;

/**
 * 基于netty的对外服务<br>
 * 使用方式：<br>
 * <blockquote>
 * 
 * <pre>
 * // [可选] 如果采用TLS-PSK，可以定制化PSK密钥
 * SimplePSKKeyManager.getInstance().setPskKeyGen(...);
 * 
 * // [可选] 如果采用KL-PSK，可以定制化DOS TIME
 * KLDosTimeFactory.getInstance().setKlDosTime(...);
 * // [可选] 如果采用KL-PSK，可定制哪些资源不走安全协议
 * SecurityProtocolFilter.getInstance().addBizSkip(...);
 * 
 * // [必填] 设置配置，包含使用协议、绑定IP和端口
 * NettyApiService.getInstance().setNettyApiCfg(...);
 * 
 * // [必填] 设置业务实现，可设置单个实现或者根据包名设置
 * BizHandlerContainer.getInstance().addHandlerContainer(...);
 * 
 * // [必填] 设置业务异常处理
 * ApiBizErrorHandlerFactory.getInstance().setApiBizErrorHandler(...);
 * 
 * // [必填] 启动或停止服务
 * NettyApiService.getInstance().start();
 * </pre>
 * 
 * </blockquote>
 * 
 * @author huangff gaodq
 *
 */
public class NettyApiService {

	private static final Logger logger = LoggerFactory.getLogger(NettyApiService.class);
	private static final NettyApiService INSTANCE = new NettyApiService();
	private static final String  HTTP = "HTTP";
	private static final String HTTPS = "HTTPS";
	private NettyApiCfg nettyApiCfg;
	private Channel server;

	private NettyApiService() {

		// 确保在程序退出的时候关闭 API 服务
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					NettyApiService.this.stop();
				} catch (Exception e) {
					logger.warn("shutdown error:" + NettyApiService.class.getName(), e);
				}
			}
		}));

	}

	public static NettyApiService getInstance() {
		return INSTANCE;
	}

	public NettyApiCfg getNettyApiCfg() {
		return nettyApiCfg;
	}

	public void setNettyApiCfg(NettyApiCfg nettyApiCfg) {
		this.nettyApiCfg = nettyApiCfg;
	}

	public void start() throws Exception {

		String protocol = HTTP;
		SslContext sslContext = null;
		switch (nettyApiCfg.getApiProtocol()) {
		case HTTP:
			break;
		case HTTPS:
			protocol = HTTPS;
			sslContext = getSslContext();
			break;
		case HTTPS_TLS_PSK:
			protocol = HTTPS;
			sslContext = getTlsPskSslContext();
			break;
		default:
		}
		URI baseUri = URI.create(String.format("%s://%s:%d/", protocol, nettyApiCfg.getIp(), nettyApiCfg.getPort()));
		try {
			this.server = new NettyHttpServerBuilder(nettyApiCfg.getApiProtocol(), nettyApiCfg)
					.setSslContext(sslContext).build();
			logger.info(String.format("REST API Service start successfully: {%s}", baseUri));
		} catch (Exception e) {
			try {
				NettyApiService.this.stop();
			} catch (Exception ignored) {
			}
			throw e;
		}

	}

	public SslContext getSslContext() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(nettyApiCfg.getSslStoreFile());
			keyStore.load(fileInputStream, nettyApiCfg.getSslStorePwd().toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

			kmf.init(keyStore, nettyApiCfg.getSslStorePwd().toCharArray());

			return SslContextBuilder.forServer(kmf).build();
		} finally {
			if(fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	public SslContext getTlsPskSslContext() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(nettyApiCfg.getSslStoreFile());
			keyStore.load(fileInputStream, nettyApiCfg.getSslStorePwd().toCharArray());

			// 原生 netty4 框架的限制，必须初始化一个 KeyManagerFactory
			// 虽然 使用 TLS-PSK 的时候并不需要
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keyStore, nettyApiCfg.getSslStorePwd().toCharArray());

			// 指定支持的 PSK 算法
			// TLS_PSK_WITH_AES_128_CBC_SHA,
			// TLS_PSK_WITH_AES_256_CBC_SHA,
			List<String> ciphers = new ArrayList<>(
					Arrays.asList("TLS_PSK_WITH_AES_128_CBC_SHA", "TLS_PSK_WITH_AES_256_CBC_SHA"));

			// 基于 OPENSSL 的 TLS通讯， 原生netty4 不支持 TLS-PSK， 需要定制部分内容
			return SslContextBuilder.forServer(kmf).sslProvider(SslProvider.OPENSSL).ciphers(ciphers).build();
		} finally {
			if(fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	public void stop() throws Exception {

		if (this.server != null) {
			this.server.close().sync();
			this.server = null;
		}
		logger.info("REST API Service stop successfully");

	}

	public boolean isRunning() {
		return this.server != null;
	}
}
