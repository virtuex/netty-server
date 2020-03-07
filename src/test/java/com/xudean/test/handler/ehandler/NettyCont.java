package com.xudean.test.handler.ehandler;

import com.xudean.server.annotation.ApiBody;
import com.xudean.server.annotation.ApiHeader;
import com.xudean.server.annotation.NtRequestMapping;
import com.xudean.server.annotation.UriParamter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NtRequestMapping(uri = "/test1")
public class NettyCont {
    @NtRequestMapping(uri = "/map")
    public Map<String, Object> map(@ApiHeader Map<String,Object> header,
                                   @ApiBody Map<String,Object> body,
                                   @UriParamter(value = "name") String name,
                                   @UriParamter(value = "age" )Integer age) {
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("testkey1", "testvalue1");
        respMap.put("testkey2", "testvalue2");
        return respMap;
    }


    @NtRequestMapping(uri = "/list")
    public List<String> string() {
        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        return list;
    }

    @NtRequestMapping(uri = "/string")
    public String list() {
        return "hello";
    }
}
