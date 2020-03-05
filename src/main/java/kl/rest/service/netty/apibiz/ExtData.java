package kl.rest.service.netty.apibiz;

import java.util.HashMap;

/**
 * @author gaodq on 2018/11/27
 */
public class ExtData extends HashMap<String, Object> {

    public ExtData() {
        super(2);
    }

    public BaseLoggerHandler getLoggerHandler() {
        return (BaseLoggerHandler) get(ExtNames.getLoggerHandler());
    }

    public void setLoggerHandler(BaseLoggerHandler loggerHandler) {
        put(ExtNames.getLoggerHandler(), loggerHandler);
    }

    public static class ExtNames {

        private static final String LOGGER_HANDLER = "LOGGER";

        public static String getLoggerHandler() {
            return LOGGER_HANDLER;
        }
    }

}
