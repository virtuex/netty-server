package kl.rest.service.netty.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import kl.rest.service.container.ContainerCache;
import kl.rest.service.container.ContainerStruct;
import kl.rest.service.netty.apibiz.ApiBizLoggerHandlerFactory;
import kl.rest.service.netty.apibiz.DataModelType;
import kl.rest.service.netty.apibiz.ExtData;
import kl.rest.service.netty.apibiz.IBizHandlerContainer;
import kl.rest.service.netty.cfg.IApiHeader;
import kl.rest.service.netty.cfg.NettyApiCfg;
import kl.rest.service.util.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;


public class HttpApiServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(HttpApiServerHandler.class);

    private final IBizHandlerContainer container;
    private final IApiHeader apiHeader;
    private FullHttpResponse response;
    private FullHttpRequest request;
    private String resource;
    private NettyApiCfg nettyApiCfg;
    private ExtData ext = new ExtData();

    /**
     * Constructor.
     */
    public HttpApiServerHandler(final IBizHandlerContainer container, final IApiHeader apiHeader, final NettyApiCfg nettyApiCfg) {
        this.container = container;
        this.apiHeader = apiHeader;
        this.nettyApiCfg = nettyApiCfg;
        ext.setLoggerHandler(ApiBizLoggerHandlerFactory.createLogger());

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        request = (FullHttpRequest) msg;

        ext.getLoggerHandler().setRequest(request);

        String clientIp = ClientUtil.getClientIP(ctx, request);
        ext.getLoggerHandler().setClientIp(clientIp);

        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        resource = request.uri();
        int indexOfQuestionMark = resource.indexOf("?");
        if (indexOfQuestionMark > -1) {
            resource = resource.substring(0, indexOfQuestionMark);
        }
        ///*************************************************************
        //todo 下面代码为重构部分
        ContainerStruct containerStruct = ContainerCache.containers.get(resource);
        if (containerStruct == null) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
        }else {
            Class clazz = containerStruct.getClazz();
            Object invoke = containerStruct.getMethod().invoke(clazz.newInstance());
            byte[] rspbytes = HandlerHelper.convertRspDataToByte(invoke);
            // 获取标准头部信息
            Map<String, String> inHeaders = HandlerHelper.getHttpHeaders(request,
                    new String[]{apiHeader.getClientIp()});

             response = HandlerHelper.createBisResponse(response, HttpResponseStatus.OK, DataModelType.JSON, rspbytes);

        }
        //        ext.getLoggerHandler().setResponse(response);
//        ext.getLoggerHandler().log();
//
//        while (!ctx.channel().isWritable()) {
//            sleep(100);
//        }
        ///**************************************************************
//        IApiBizHandler handler = container.getHandler(resource);
//
//        if (handler == null) {
//            // 404
//            throw new Exception("未知错误");
//        }
//        // 获取标准头部信息
////        Map<String, String> inHeaders = HandlerHelper.getHttpHeaders(request,
////                new String[]{ apiHeader.getClientIp()});
//        // 获取session-id信息
//        String sessionId = null;
//        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
//        if (cookieString != null && cookieString.length() > 0) {
//            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
//            for (Cookie cookie : cookies) {
//                if (apiHeader.getSessionIdKey().equalsIgnoreCase(cookie.name())) {
//                    sessionId = cookie.value();
//                    inHeaders.put(apiHeader.getSessionIdKey(), sessionId);
//                    break;
//                }
//            }
//        }
//
//        // 从http头中获取标准的 header 信息
//        inHeaders.putAll(HandlerHelper.getHttpHeaders(request, handler.httpHeaders()));
//        // 从url中获取参数信息，并且放入 headers
//        inHeaders.putAll(HandlerHelper.getHttpHeadersFromUrl(request, handler.httpHeaders()));
//
//        byte[] modelIn = HandlerHelper.createBisRequest(request);
//        Map<String, String> outHeaders = new LinkedHashMap<>();
//        byte[] modelOut = handler.handle(modelIn, inHeaders, outHeaders, clientIp, ext);
//
//        // 设置头部信息和cookie
//        parseHttpHeaders(outHeaders, sessionId);
//
//        response = HandlerHelper.createBisResponse(response, HttpResponseStatus.OK, handler.getModelType(), modelOut);
//        ext.getLoggerHandler().setResponse(response);
//        ext.getLoggerHandler().log();
//
//        while (!ctx.channel().isWritable()) {
//            sleep(100);
//        }
        if (nettyApiCfg.isKeepAlive()) {
            //长连接
            ctx.writeAndFlush(response);
        } else {
            //不使用长连接，用完即关闭
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        try {
            response = HandlerHelper.createErrorResponse(response, resource, cause);

            ext.getLoggerHandler().setResponse(response);
            ext.getLoggerHandler().setCause(cause);
            ext.getLoggerHandler().log();

        } catch (Exception e) {
            logger.error("Create ErrorResponse Error!", e);
        } finally {
            if (response != null) {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR))
                        .addListener(ChannelFutureListener.CLOSE);
            }
        }

    }

    /**
     * 把头部信息添加进协议
     *
     * @param httpHeaders
     * @param sessionId
     */
    public void parseHttpHeaders(Map<String, String> httpHeaders, String sessionId) {
        if (httpHeaders == null) {
            return;
        }
        Object sessionid = httpHeaders.remove(apiHeader.getSessionIdKey());
        if (sessionid == null) {
            sessionid = sessionId;
        }
        if (sessionid != null) {
            // 如果定义了session，并且请求和响应的不相等，则构造cookie中的session-id
            DefaultCookie sessionCookie = new DefaultCookie(apiHeader.getSessionIdKey(), sessionid + "");
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(sessionCookie));
        }
        for (String key : httpHeaders.keySet()) {
            Object value = httpHeaders.get(key);
            response.headers().set(key, value);
        }
    }


    /**
     * 将对象序列化，方便写进响应
     * @param obj
     * @return
     */
    public byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }


}
