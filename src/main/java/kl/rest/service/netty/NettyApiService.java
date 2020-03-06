package kl.rest.service.netty;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ResourceBundle;

import javax.net.ssl.KeyManagerFactory;

import kl.rest.service.constant.NettyConfigKey;
import kl.rest.service.netty.apibiz.BizHandlerContainer;
import kl.rest.service.util.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import kl.rest.service.netty.cfg.NettyApiCfg;

/**
 * 基于netty的对外服务<br>
 * 使用方式：<br>
 * <p>
 * <p>
 * // [必填] 设置配置，包含使用协议、绑定IP和端口
 * NettyApiService.getInstance().setNettyApiCfg(...);
 * <p>
 * // [必填] 设置业务实现，可设置单个实现或者根据包名设置
 * BizHandlerContainer.getInstance().addHandlerContainer(...);
 * <p>
 * // [必填] 设置业务异常处理
 * ApiBizErrorHandlerFactory.getInstance().setApiBizErrorHandler(...);
 * <p>
 * // [必填] 启动或停止服务
 * NettyApiService.getInstance().start();
 *
 * @author
 */
public class NettyApiService {

    private static final Logger logger = LoggerFactory.getLogger(NettyApiService.class);
    private static final NettyApiService INSTANCE = new NettyApiService();
    private static final String HTTP = "HTTP";
    private static final String HTTPS = "HTTPS";
    private NettyApiCfg nettyApiCfg;
    private Channel server;

    private NettyApiService() {
        //加载netty配置
        loadNettyConfig();
        //加载handler
        BizHandlerContainer.getInstance().addHandlerContainer(nettyApiCfg.getBaseScannerPkg());
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
            default:
        }
        URI baseUri = URI.create(String.format("%s://%s:%d/", protocol, nettyApiCfg.getIp(), nettyApiCfg.getPort()));
        //执行类扫描，找出符合条件的handler（使用NtMapping注解）
        //todo 名字需要修改
//		ClassLoaderUtil.getClasses()

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
        if (nettyApiCfg.getSslStoreFile() == null) {
            throw new Exception("无法启用https服务，原因：keystore加载失败");
        }
        try {
            fileInputStream = new FileInputStream(nettyApiCfg.getSslStoreFile());
            keyStore.load(fileInputStream, nettyApiCfg.getSslStorePwd().toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            kmf.init(keyStore, nettyApiCfg.getSslStorePwd().toCharArray());

            return SslContextBuilder.forServer(kmf).build();
        } finally {
            if (fileInputStream != null) {
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

    private void loadNettyConfig() {
        ResourceBundle bundle = ResourceBundle.getBundle("nettyserver");
        nettyApiCfg = new NettyApiCfg();
        //IP
        nettyApiCfg.setIp(bundle.getString(NettyConfigKey.IP));
        //端口
        nettyApiCfg.setPort(Integer.valueOf(bundle.getString(NettyConfigKey.PORT)));
        //协议
        String protacl = bundle.getString(NettyConfigKey.APIP_ROTOCOL);
        nettyApiCfg.setApiProtocol(APIProtocol.getProtcal(protacl));
        //keystore路径
        nettyApiCfg.setSslStoreFile(new File(bundle.getString(NettyConfigKey.SSL_STOREFILE_PATH)));

        nettyApiCfg.setSslStorePwd(bundle.getString(NettyConfigKey.SSL_STORE_PWD));

        //设置是否长连接
        String keepAliveStr = bundle.getString(NettyConfigKey.IS_KEEPALIVE);
        if (StringUtils.isNotBlank(keepAliveStr)) {
            nettyApiCfg.setKeepAlive(BooleanUtils.string2Boolean(keepAliveStr));
        }

        /**
         * 设置读超时时间
         */
        String readerTimeStr = bundle.getString(NettyConfigKey.READER_IDLE_TIME);
        if (StringUtils.isNotBlank(readerTimeStr)) {
            nettyApiCfg.setReaderIdleTime(Long.valueOf(readerTimeStr));
        }
        /**
         * 设置写尝试时间
         */
        String writeTimeStr = bundle.getString(NettyConfigKey.WRITE_IDLE_TIME);
        if (StringUtils.isNotBlank(writeTimeStr)) {
            nettyApiCfg.setWriterIdleTime(Long.valueOf(writeTimeStr));
        }

        /**
         * 设置允许的最大长度
         */
        String contentLengthStr = bundle.getString(NettyConfigKey.MAX_CONTENT_LENGTH);
        if (StringUtils.isNotBlank(contentLengthStr)) {
            int contentLength = 0;
            //如果是以M为单位
            if (contentLengthStr.toLowerCase().contains("m")) {
                contentLength = Integer.valueOf(contentLengthStr.toLowerCase().replace("m", "")) * 1024 * 1024;
            }
            //如果是以K为单位
            if (contentLengthStr.toLowerCase().contains("k")) {
                contentLength = Integer.valueOf(contentLengthStr.toLowerCase().replace("k", "")) * 1024;
            }
            if (contentLength == 0) {
                contentLength = Integer.valueOf(contentLengthStr.toLowerCase());
            }
            nettyApiCfg.setMaxContentLength(contentLength);
        }
        /**
         * 设置初始Hanler扫描路径
         */
        String basePackage = bundle.getString(NettyConfigKey.BASE_SCANNER_PACKAGE);
        nettyApiCfg.setBaseScannerPkg(basePackage);
        logger.info("Server properties load success!");

    }

    public NettyApiCfg getNettyApiCfg() {
        return nettyApiCfg;
    }

}
