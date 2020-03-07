package com.xudean.server.netty.handler;

import com.xudean.server.netty.APIProtocol;
import com.xudean.server.netty.apibiz.IBizHandlerContainer;
import com.xudean.server.netty.cfg.IApiHeader;
import com.xudean.server.netty.cfg.NettyApiCfg;

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
