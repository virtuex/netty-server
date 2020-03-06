package kl.rest.service.netty;


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

    public static APIProtocol getProtcal(String name) {
        if(HTTP.name().equals(name.toUpperCase())){
            return HTTP;
        }
        if(HTTPS.name().equals(name.toUpperCase())){
            return HTTPS;
        }
        return HTTP;
    }

}
