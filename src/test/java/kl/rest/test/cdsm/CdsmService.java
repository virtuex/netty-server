package kl.rest.test.cdsm;


import kl.rest.service.netty.NettyApiService;


public class CdsmService {
	public static void main(String[] args) throws Exception {
		NettyApiService apiService = NettyApiService.getInstance();
		apiService.start();
	}
	
}
