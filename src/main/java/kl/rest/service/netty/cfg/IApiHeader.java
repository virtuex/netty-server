package kl.rest.service.netty.cfg;

/**
 * API HTTP 头部内容数据定义，默认定义的头部内容，其它内容应该通过接口去定义
 * 
 * @author
 *
 */
public interface IApiHeader {


	/**
	 * 获取会话KEY字段
	 * 
	 * @return
	 */
	public String getSessionIdKey();


	/**
	 * 获取客户端ip字段
	 * @return
	 */
	public String getClientIp();
}
