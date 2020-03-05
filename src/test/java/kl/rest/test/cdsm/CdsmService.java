package kl.rest.test.cdsm;

import kl.rest.service.netty.APIProtocol;
import kl.rest.service.netty.NettyApiService;
import kl.rest.service.netty.apibiz.ApiBizErrorHandlerFactory;
import kl.rest.service.netty.apibiz.BizHandlerContainer;
import kl.rest.service.netty.cfg.NettyApiCfg;
import kl.rest.test.cdsm.handler.TestHandler;
import kl.rest.test.cdsm.handler.ehandler.CDSMResponseErrorHandler;


public class CdsmService {
	
	public void start() throws Exception {
		NettyApiService apiService = NettyApiService.getInstance();
		if (apiService.isRunning()) {
			return;
		}
		NettyApiCfg apiCfg = new NettyApiCfg("127.0.0.1", 10668, APIProtocol.HTTP);
		NettyApiService.getInstance().setNettyApiCfg(apiCfg);

		BizHandlerContainer.getInstance().addHandlerContainer(TestHandler.packagePath);

		ApiBizErrorHandlerFactory.getInstance().setApiBizErrorHandler(new CDSMResponseErrorHandler());
		NettyApiService.getInstance().start();
		System.out.println("http server has started");
	}

	public void stop() throws Exception {
		NettyApiService apiService = NettyApiService.getInstance();
		if (!apiService.isRunning()) {
			return;
		}
		apiService.stop();
	}
	
	public static void main(String[] args) {
		try {
			new CdsmService().start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}
