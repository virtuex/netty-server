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
    HTTPS;


    public boolean isHTTPS() {
        return this.name().toUpperCase().contains("HTTPS");
    }

}
