package kl.rest.service.netty.apibiz;

public interface IBizHandlerContainer {

	/**
	 * 添加一个实现
	 * 
	 * @param handler
	 */
	public void addHandler(IApiBizHandler handler) throws Exception;

	/**
	 * 根据报名添加实现，会自动搜索包以及子包中所有符合接口的类
	 * 
	 * @param handlePackage
	 */
	public void addHandlerContainer(final String handlePackage);

	/**
	 * 通过资源获取实现
	 * 
	 * @param resource
	 * @return
	 */
	public IApiBizHandler getHandler(String resource);
}
