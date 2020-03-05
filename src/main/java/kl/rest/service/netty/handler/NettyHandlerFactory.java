package kl.rest.service.netty.handler;

import kl.rest.service.netty.APIProtocol;
import kl.rest.service.netty.apibiz.IBizHandlerContainer;
import kl.rest.service.netty.cfg.IApiHeader;
import kl.rest.service.netty.cfg.NettyApiCfg;

public class NettyHandlerFactory {

    public static HttpApiServerHandler genServerHandler(APIProtocol apiProtocol,
                                                        IApiHeader apiHeader,
                                                        IBizHandlerContainer bizHandlerContainer, NettyApiCfg nettyApiCfg) {
        switch (apiProtocol) {
            case HTTP:
            case HTTPS:
            default:
                return new HttpApiServerHandler(bizHandlerContainer, apiHeader,nettyApiCfg);
        }
    }
}
