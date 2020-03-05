package kl.rest.test.cdsm.handler.ehandler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kl.rest.service.netty.apibiz.DataModelType;
import kl.rest.service.netty.apibiz.IApiBizErrorHandler;


public class CDSMResponseErrorHandler implements IApiBizErrorHandler {

	private Logger log = LoggerFactory.getLogger(CDSMResponseErrorHandler.class);


	@Override
	public DataModelType errorModelType(String resource) {
		return DataModelType.JSON;
	}

	@Override
	public byte[] errorHandle(String resource, Throwable cause, Map<String, Object> errorHeaders) {
		log.error("", cause);
		return null;
	}

}
