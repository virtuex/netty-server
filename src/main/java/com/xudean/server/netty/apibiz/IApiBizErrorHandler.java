package com.xudean.server.netty.apibiz;

import java.util.Map;

/**
 * 异常处理接口
 * 
 * @author
 *
 */
public interface IApiBizErrorHandler {

	/**
	 * 错误处理
	 * 
	 * @param resource
	 *            资源URI，可能为空
	 * @param cause
	 *            错误异常，可能为空
	 * @param errorHeaders
	 *            异常头
	 * @return
	 */
	public byte[] errorHandle(String resource, Throwable cause, Map<String, Object> errorHeaders);

	/**
	 * 返回错误信息的ModelType
	 * 
	 * @param resource
	 *            资源URI，可能为空
	 * @return
	 */
	public DataModelType errorModelType(String resource);
}
