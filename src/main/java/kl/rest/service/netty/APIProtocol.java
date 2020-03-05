package kl.rest.service.netty;

/**
 * Created by gaodq on 2017/4/1.
 */
public enum APIProtocol {
    /**
     *
     */
    HTTP,

    /**
     *
     */
    HTTPS,

    GMVPN,
    /**
     * 基于 TLS-PSK 的 HTTPS
     */
    HTTPS_TLS_PSK;

    public boolean isHTTPS() {
        return this.name().toUpperCase().contains("HTTPS");
    }

    public boolean isTLSPSK() {
        return HTTPS_TLS_PSK.equals(this);
    }

}
