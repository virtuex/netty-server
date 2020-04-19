package com.xudean.test;


import com.xudean.server.netty.NettyApiService;


public class HttpService {
	public static void main(String[] args) throws Exception {
		NettyApiService apiService = NettyApiService.getInstance();
		apiService.start();
	}
	
}
