package kl.rest.service.netty.apibiz;


/**
 * @author gaodq on 2018/11/26
 */
public class ApiBizLoggerHandlerFactory {

    private static Class<? extends BaseLoggerHandler> handlerClass = BaseLoggerHandler.class;

    public static Class<? extends BaseLoggerHandler> getHandlerClass() {
        return handlerClass;
    }

    public static void setHandlerClass(Class<? extends BaseLoggerHandler> handlerClass) {
        ApiBizLoggerHandlerFactory.handlerClass = handlerClass;
    }

    public static BaseLoggerHandler createLogger() {
        try {
            return handlerClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    handlerClass == null ? "handlerClass = null" : "handlerClass = " + handlerClass.getName(),
                    e);
        }
    }
}
