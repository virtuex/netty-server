package com.xudean.server.netty.handler;

import com.xudean.server.util.JSONUtil;
import com.xudean.server.netty.apibiz.DataModelType;
import com.xudean.server.netty.apibiz.IApiBizErrorHandler;

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
