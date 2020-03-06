package kl.rest.service.netty.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import kl.rest.service.annotation.ApiBody;
import kl.rest.service.annotation.ApiHeader;
import kl.rest.service.annotation.UriParamter;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class HttpApiServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(HttpApiServerHandler.class);

    private final IBizHandlerContainer container;
    private final IApiHeader apiHeader;
    private FullHttpResponse response;
    private FullHttpRequest request;
    private String resource;
    private String uriSubfix;
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
        Map<String, String> paramterMap = null;
        if (indexOfQuestionMark > -1) {
            uriSubfix = resource.substring(indexOfQuestionMark + 1);
            //得到key=value的形式
            String[] split = uriSubfix.split("&");
            paramterMap = new HashMap<>();
            for (String keyValue : split) {
                //讲key=value拆开，得到的数组如果不为2，那说明参数不合法，跳过
                String[] keyValueArray = keyValue.split("=");
                if (keyValueArray.length != 2) {
                    continue;
                }
                paramterMap.put(keyValueArray[0], keyValueArray[1]);
            }
            resource = resource.substring(0, indexOfQuestionMark);
        }
        ///*************************************************************
        //todo 下面代码为重构部分,当前以实现为主，稍后重构
        //去除请求body
        byte[] modelIn = HandlerHelper.createBisRequest(request);
        Map<String, Object> reqBody = HandlerHelper.handleInData(modelIn);

        ContainerStruct containerStruct = ContainerCache.containers.get(resource);
        String sessionId = null;
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (containerStruct == null) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
        } else {
            Class clazz = containerStruct.getClazz();
            Method method = containerStruct.getMethod();
            // 获取标准头部信息
            Map<String, String> inHeaders = HandlerHelper.getHttpHeaders(request,
                    new String[]{});

            if (cookieString != null && cookieString.length() > 0) {
                Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
                for (Cookie cookie : cookies) {
                    if (apiHeader.getSessionIdKey().equalsIgnoreCase(cookie.name())) {
                        sessionId = cookie.value();
                        inHeaders.put(apiHeader.getSessionIdKey(), sessionId);
                        break;
                    }
                }
            }
            Parameter[] parameters = method.getParameters();
            Object[] objects = new Object[parameters.length];
            int index = 0;
            for (Parameter parameter : parameters) {
                //这里默认每个参数只能有一个注解
                Annotation[] annotations = parameter.getAnnotations();
                if (annotations == null) {
                    objects[index++] = null;
                    continue;
                }
                if (annotations[0] instanceof ApiHeader) {
                    objects[index++] = inHeaders;
                }

                if (annotations[0] instanceof ApiBody) {
                    objects[index++] = reqBody;
                }

                if (annotations[0] instanceof UriParamter) {
                    UriParamter uriParamter = (UriParamter) annotations[0];
                    String value = uriParamter.value();
                    Class<?> type = parameter.getType();
                    //todo 这里的写法很蠢，需要重构
                    if (paramterMap == null) {
                        continue;
                    }
                    if (Integer.class.equals(type)) {
                        objects[index++] = Integer.valueOf(paramterMap.get(value));
                    }
                    if (Long.class.equals(type)) {
                        objects[index++] = Long.valueOf(paramterMap.get(value));
                    }
                    if (String.class.equals(type)) {
                        objects[index++] = paramterMap.get(value);
                    }

                }

            }
            Object invoke = method.invoke(clazz.newInstance(), objects);
            byte[] rspbytes = HandlerHelper.convertRspDataToByte(invoke);
            response = HandlerHelper.createBisResponse(response, HttpResponseStatus.OK, DataModelType.JSON, rspbytes);

        }
//        while (!ctx.channel().isWritable()) {
//            sleep(100);
//        }
        ///**************************************************************

//        // 从http头中获取标准的 header 信息
//        inHeaders.putAll(HandlerHelper.getHttpHeaders(request, handler.httpHeaders()));
//        // 从url中获取参数信息，并且放入 headers
//        inHeaders.putAll(HandlerHelper.getHttpHeadersFromUrl(request, handler.httpHeaders()));
//
//        byte[] modelIn = HandlerHelper.createBisRequest(request);
        Map<String, String> outHeaders = new LinkedHashMap<>();
//
//        // 设置头部信息和cookie
        parseHttpHeaders(outHeaders, sessionId);
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
}
