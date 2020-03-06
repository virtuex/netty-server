package kl.rest.test.cdsm;

import kl.rest.service.netty.APIProtocol;
import kl.rest.service.netty.NettyApiService;
import kl.rest.service.netty.apibiz.BizHandlerContainer;
import kl.rest.service.netty.cfg.NettyApiCfg;
import kl.rest.test.cdsm.handler.ehandler.NettyCont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CdsmService {
	private static final Logger logger = LoggerFactory.getLogger(CdsmService.class);
	public void start() throws Exception {
		NettyApiService apiService = NettyApiService.getInstance();
		if (apiService.isRunning()) {
			return;
		}
		NettyApiCfg apiCfg = new NettyApiCfg("127.0.0.1", 10668, APIProtocol.HTTP);
		NettyApiService.getInstance().setNettyApiCfg(apiCfg);
		//todo 这里通过add的方式不合理，增加了复杂度，后期需要改成在netty启动阶段进行扫描，扫描路径可自定配置或者就从根包下开始扫描
		BizHandlerContainer.getInstance().addHandlerContainer(NettyCont.class.getPackage().getName());

		NettyApiService.getInstance().start();
		logger.info(" http service started");
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
