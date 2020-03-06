package kl.rest.service.netty.apibiz;

/**
 * API异常情况的处理
 *
 * @author
 */
public class ApiBizErrorHandlerFactory {
    private static final ApiBizErrorHandlerFactory INSTANCE = new ApiBizErrorHandlerFactory();

    public static ApiBizErrorHandlerFactory getInstance() {
        return INSTANCE;
    }

    private ApiBizErrorHandlerFactory() {

    }

    private IApiBizErrorHandler apiBizErrorHandler = null;

    public IApiBizErrorHandler getApiBizErrorHandler() {
        return apiBizErrorHandler;
    }

    public void setApiBizErrorHandler(IApiBizErrorHandler apiBizErrorHandler) {
        this.apiBizErrorHandler = apiBizErrorHandler;
    }

}
