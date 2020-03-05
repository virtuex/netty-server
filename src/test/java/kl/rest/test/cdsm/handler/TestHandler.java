package kl.rest.test.cdsm.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kl.rest.service.netty.apibiz.DataModelType;
import kl.rest.service.netty.apibiz.ExtData;
import kl.rest.service.netty.apibiz.IApiBizHandler;
import kl.rest.test.cdssr.CdssResoure;

import java.util.HashMap;
import java.util.Map;


public class TestHandler implements IApiBizHandler {

    public static final String packagePath = TestHandler.class.getPackage().getName();

    @Override
    public DataModelType getModelType() {
        return DataModelType.JSON;
    }

    @Override
    public String resource() {
        return CdssResoure.test;
    }

    @Override
    public String[] httpHeaders() {
        return new String[]{"sygj", "tdyx"};
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] handle(byte[] modelIn, Map<String, String> headers, Map<String, String> outHeaders, String clientIp, ExtData ext)
            throws Exception {
        outHeaders.put("CDS_RSP_TYPE", "test_Rsp");
        DataModelType modelType = this.getModelType();

        Gson gson = new GsonBuilder().serializeNulls().create();

        switch (modelType) {
            case JSON:
                Map<Object, Object> readValue = gson.fromJson(new String(modelIn, "UTF-8"), Map.class);
                Map<Object, Object> respM = new HashMap<Object, Object>();
                Map<String, Object> bizBodyMap = new HashMap<String, Object>();
                bizBodyMap.put("result_code", 0);
                bizBodyMap.put("result_msg", "���Գɹ�");
                respM.put("biz_id", readValue.get("biz_id"));
                respM.put("biz_body", bizBodyMap);
                respM.put("biz_type", null);

                String json = gson.toJson(respM);
                System.out.println(readValue);
                System.out.println("headers====" + headers);
                System.out.println("clientIp==============" + clientIp);
                return json.getBytes("UTF-8");
            case BYTEARRAY:
                // TODO ����������
                return null;
            case XML:
            default:
                throw new Exception("��֧�ֵ����ͣ�" + modelType);
        }
    }

}
