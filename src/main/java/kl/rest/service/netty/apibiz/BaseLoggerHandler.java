package kl.rest.service.netty.apibiz;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import kl.rest.service.netty.handler.HandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 */
public class BaseLoggerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseLoggerHandler.class);

    private long beginDate = System.currentTimeMillis();
    private long bizSpend;

    private FullHttpRequest request;
    private FullHttpResponse response;
    private String reqBody;
    private Throwable cause;
    private String clientIp;

    public BaseLoggerHandler() {
    }

    public long getBeginDate() {
        return beginDate;
    }

    public long getBizSpend() {
        return bizSpend;
    }

    public FullHttpResponse getResponse() {
        return response;
    }

    public void setRequest(FullHttpRequest request) {
        this.request = request;
        // 提前读取 reqBody， 因为 catchException之前会被释放
        this.reqBody = HandlerHelper.bodyToString(request);
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public boolean isSuccess() {
        return cause == null;
    }

    public String getReqBody() {
        return reqBody;
    }

    public void log() {

        bizSpend = System.currentTimeMillis() - beginDate;

        logToFile();

        logToOther();

    }

    public void logToFile() {

        if (!isSuccess()) {
            LOGGER.error(cause.getMessage(), cause);
        }

        if (!LOGGER.isDebugEnabled()) {
            return;
        }

        String reqUri = request == null ? "httpMessage is null" : request.uri();
        String reqHeader = HandlerHelper.headerToString(request);

        String rspCode = response == null ? "httpMessage is null" : response.status().code() + "";
        String rspHeader = HandlerHelper.headerToString(response);
        String rspBody = HandlerHelper.bodyToString(response);

        LOGGER.debug("\n{" +
                        "reqUri:\n" +
                        "{}\n" +
                        "reqHeader:\n" +
                        "{}\n" +
                        "reqBody:\n" +
                        "{}\n"
                        + "rspCode:{}\n" +
                        "rspHeader:\n" +
                        "{}\n" +
                        "rspBody:\n" +
                        "{}\n" +
                        "bizSpend: {}\n" +
                        "clientIp: {}\n" +
                        "}\n",
                new Object[]{reqUri, reqHeader, reqBody, rspCode, rspHeader, rspBody, bizSpend, clientIp});
    }

    public void logToOther() {

    }
}
