package kl.rest.service.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import kl.rest.service.netty.apibiz.ApiBizErrorHandlerFactory;
import kl.rest.service.netty.apibiz.DataModelType;
import kl.rest.service.netty.apibiz.IApiBizErrorHandler;
import kl.rest.service.util.JSONUtil;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;


public class HandlerHelper {

    private static final String OCTET_STREAM = "application/octet-stream";

    public static String bodyToString(FullHttpMessage httpMessage) {

        if (httpMessage == null) {
            return null;
        }

        CharSequence mimeType = HttpUtil.getMimeType(httpMessage);
        if (mimeType != null && OCTET_STREAM.equalsIgnoreCase(mimeType.toString())) {
            return OCTET_STREAM + " data";
        } else {
            return httpMessage.content().toString(Charset.forName("UTF-8"));
        }
    }

    public static String headerToString(HttpMessage httpMessage) {
        if (httpMessage == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = httpMessage.headers().iteratorAsString();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    /**
     * 输入日志记录
     *
     * @param logger
     * @param request
     * @deprecated use {@link kl.rest.service.netty.apibiz.BaseLoggerHandler} instead
     */
    @Deprecated
    public static void logInputDebug(Logger logger, FullHttpRequest request) {
        if (logger == null || request == null || !logger.isDebugEnabled()) {
            return;
        }

        StringBuilder header = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = request.headers().iteratorAsString();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            header.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
        }
        if (header.length() > 0) {
            header.setLength(header.length() - 2);
        }

        String content;
        content = bodyToString(request);

        logger.debug("\r\nreq_uri:\r\n{}\r\nreq_header:\r\n{}\r\nreq_content:\r\n{}\r\n",
                new Object[]{request.uri(), header.toString(), content});

    }


    /**
     * 输出日志记录
     *
     * @param logger
     * @param request
     * @param response
     * @deprecated use {@link kl.rest.service.netty.apibiz.BaseLoggerHandler} instead
     */
    @Deprecated
    public static void logOutputDebug(Logger logger, FullHttpRequest request, FullHttpResponse response) {
        if (logger == null || response == null || !logger.isDebugEnabled()) {
            return;
        }

        StringBuilder header = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = response.headers().iteratorAsString();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            header.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
        }
        if (header.length() > 0) {
            header.setLength(header.length() - 2);
        }
        String content = bodyToString(response);

        logger.debug("\r\nreq_uri:\r\n{}\r\nrep_header:\r\n{}\r\nrep_content:\r\n{}\r\n",
                new Object[]{request == null ? "" : request.uri(), header.toString(), content});
    }


    /**
     * 数据请求
     *
     * @param request
     * @return
     */
    public static byte[] createBisRequest(FullHttpRequest request) {
        ByteBuf buf = request.content();
        byte[] byteArray = new byte[buf.readableBytes()];
        buf.readBytes(byteArray);
        return byteArray;
    }

    /**
     * 数据响应
     *
     * @param response
     * @param status
     * @param modelType
     * @param modelOut
     * @return
     */
    public static FullHttpResponse createBisResponse(FullHttpResponse response, final HttpResponseStatus status,
                                                     DataModelType modelType, byte[] modelOut) {
        if (response == null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        }
        if (modelOut == null) {
            // 避免空指针
            modelOut = new byte[0];
        }
        ByteBuf res = Unpooled.wrappedBuffer(modelOut);
        switch (modelType) {
            case BYTEARRAY:
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM);
                break;
            case JSON:
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                break;
            case XML:
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/xml;charset=UTF-8");
                break;
            case TEXT:
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
                break;
            default:
        }
        response = response.replace(res);
        response.setStatus(status);

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    /**
     * todo 发生异常时的处理
     * @param response
     * @param resource
     * @param cause
     * @return
     * @throws Exception
     */
    public static FullHttpResponse createErrorResponse(FullHttpResponse response,
                                                       String resource,
                                                       Throwable cause) throws Exception {

        IApiBizErrorHandler apiBizErrorHandler = ApiBizErrorHandlerFactory.getInstance().getApiBizErrorHandler();
        if (apiBizErrorHandler == null) {
            throw new Exception("error.handler.cannot.be.empty");
        }

        Map<String, Object> errorHeaders = new HashMap<>();

        byte[] errorOut = apiBizErrorHandler.errorHandle(resource, cause, errorHeaders);
        DataModelType modelType = apiBizErrorHandler.errorModelType(resource);

        if (response == null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        }

        for (String key : errorHeaders.keySet()) {
            response.headers().set(key, errorHeaders.get(key));
        }

        return HandlerHelper.createBisResponse(response, HttpResponseStatus.OK, modelType, errorOut);
    }

    /**
     * 获取请求头，这里如果httpHeaders为空，那就把所有的头都取出来
     *
     * @param request
     * @param httpHeaders
     * @return
     */
    public static Map<String, String> getHttpHeaders(FullHttpRequest request, String[] httpHeaders) {
        Map<String, String> mapHeaders = new LinkedHashMap<String, String>();
        if (httpHeaders == null || httpHeaders.length == 0) {
            HttpHeaders headers = request.headers();
            Iterator<Map.Entry<String, String>> iterator = headers.entries().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                mapHeaders.put(next.getKey(), next.getValue());
            }
            return mapHeaders;
        }
        for (String header : httpHeaders) {
            mapHeaders.put(header.trim(), request.headers().get(header.trim()));
        }
        return mapHeaders;
    }

    public static Map<String, String> getHttpHeaders(Map<String, String> headers, String[] httpHeaders) {
        Map<String, String> mapHeaders = new LinkedHashMap<String, String>();
        if (httpHeaders == null) {
            return mapHeaders;
        }
        for (String header : httpHeaders) {
            mapHeaders.put(header.trim(), headers.get(header.trim()));
        }
        return mapHeaders;
    }

    public static Map<String, String> getUrlParameters(FullHttpRequest request) {
        Map<String, String> parameters = new HashMap<>();
        String urlParameters = null;
        final String uri = request.uri();
        if (uri != null && uri.contains("?")) {
            urlParameters = uri.substring(uri.indexOf("?") + 1);
            String[] parameterArray = urlParameters.split("&");
            for (String keyValue : parameterArray) {
                String[] keyValueArray = keyValue.split("=");
                String key = keyValueArray[0];
                String value = keyValueArray.length > 1 ? keyValueArray[1] : null;
                try {
                    if (value != null) {
                        value = URLDecoder.decode(value, "UTF-8");
                    }
                } catch (UnsupportedEncodingException ignored) {
                }
                parameters.put(key, value);
            }

        }
        return parameters;
    }

    public static Map<? extends String, ? extends String> getHttpHeadersFromUrl(FullHttpRequest request, String[] httpHeaders) {
        return getHttpHeaders(getUrlParameters(request), httpHeaders);
    }


    public static byte[] convertRspDataToByte(Object object) throws Exception {
        return JSONUtil.toJSONBytes(object);
    }

}
