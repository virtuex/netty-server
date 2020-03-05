package kl.rest.service.netty.cfg;

import java.io.File;

import kl.rest.service.netty.APIProtocol;

/**
 * 服务配置文件
 *
 * @author huangff
 */
public class NettyApiCfg {

    private String ip;
    private int port;
    private APIProtocol apiProtocol;

    private File sslStoreFile;
    private String sslStorePwd;
    private boolean isKeepAlive;
    //读写超时设定
    private long readerIdleTime;
    private long writerIdleTime;
    /**
     * netty服务的单次最大接受内容长度，单位字节byte，默认5M
     */
    private int maxContentLength = 5 * 1024 * 1024;

    private IApiHeader apiHeaderKey = new DefaultApiHeaderImpl();

    public NettyApiCfg() {
        super();
    }

    public NettyApiCfg(String ip, int port, APIProtocol apiProtocol) {
        this(ip, port, apiProtocol, null, null);
    }

    public NettyApiCfg(String ip, int port, APIProtocol apiProtocol, File sslStoreFile, String sslStorePwd) {
        this(ip, port, apiProtocol, sslStoreFile, sslStorePwd, 5 * 1024 * 1024, false);
    }

    public NettyApiCfg(String ip, int port, APIProtocol apiProtocol, File sslStoreFile, String sslStorePwd,
                       int maxContentLength) {
        this(ip, port, apiProtocol, sslStoreFile, sslStorePwd, maxContentLength, false);
    }

    public NettyApiCfg(String ip, int port, APIProtocol apiProtocol, File sslStoreFile, String sslStorePwd,
                       int maxContentLength, boolean isKeepAlive) {
        super();
        this.ip = ip;
        this.port = port;
        this.apiProtocol = apiProtocol;
        this.sslStoreFile = sslStoreFile;
        this.sslStorePwd = sslStorePwd;
        this.maxContentLength = maxContentLength;
        this.isKeepAlive = isKeepAlive;
    }



    public NettyApiCfg(String ip, int port, APIProtocol apiProtocol, File sslStoreFile, String sslStorePwd,
                       int maxContentLength, boolean isKeepAlive, long readerIdleTime, long writerIdleTime) {
        super();
        this.ip = ip;
        this.port = port;
        this.apiProtocol = apiProtocol;
        this.sslStoreFile = sslStoreFile;
        this.sslStorePwd = sslStorePwd;
        this.maxContentLength = maxContentLength;
        this.isKeepAlive = isKeepAlive;
        this.readerIdleTime = readerIdleTime;
        this.writerIdleTime = writerIdleTime;
    }


    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        isKeepAlive = keepAlive;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public APIProtocol getApiProtocol() {
        return apiProtocol;
    }

    public void setApiProtocol(APIProtocol apiProtocol) {
        this.apiProtocol = apiProtocol;
    }

    public File getSslStoreFile() {
        return sslStoreFile;
    }

    public void setSslStoreFile(File sslStoreFile) {
        this.sslStoreFile = sslStoreFile;
    }

    public String getSslStorePwd() {
        return sslStorePwd;
    }

    public void setSslStorePwd(String sslStorePwd) {
        this.sslStorePwd = sslStorePwd;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public IApiHeader getApiHeaderKey() {
        return apiHeaderKey;
    }

    public NettyApiCfg setApiHeaderKey(IApiHeader apiHeaderKey) {
        this.apiHeaderKey = apiHeaderKey;
        return this;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }

    @Override
    public String toString() {
        return "NettyApiCfg [ip=" + ip + ", port=" + port + ", apiProtocol=" + apiProtocol + ", sslStoreFile="
                + sslStoreFile + ", sslStorePwd=" + sslStorePwd + ", maxContentLength=" + maxContentLength + "]";
    }

}
