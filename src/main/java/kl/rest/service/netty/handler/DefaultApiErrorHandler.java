package kl.rest.service.netty.handler;

import kl.rest.service.constant.ApiHeaderKey;
import kl.rest.service.netty.apibiz.DataModelType;
import kl.rest.service.netty.apibiz.IApiBizErrorHandler;
import kl.rest.service.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 终端服务异常封装
 *
 * @author todo
 */
public class DefaultApiErrorHandler implements IApiBizErrorHandler {
    @Override
    public byte[] errorHandle(String resource, Throwable cause, Map<String, Object> errorHeaders) {
        Map<String, Object> errorBase = new HashMap<>(16);
        errorBase.put("msg",cause.getMessage());
        return JSONUtil.toJSONBytes(errorBase);
    }

    @Override
    public DataModelType errorModelType(String resource) {
        return DataModelType.JSON;
    }
}
