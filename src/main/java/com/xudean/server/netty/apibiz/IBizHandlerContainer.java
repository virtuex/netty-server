package com.xudean.server.netty.apibiz;

public interface IBizHandlerContainer {



	/**
	 * 根据报名添加实现，会自动搜索包以及子包中所有符合接口的类
	 * 
	 * @param handlePackage
	 */
	public void addHandlerContainer(final String handlePackage);

}
