package kl.rest.test.cdsm.handler.ehandler;

import kl.rest.service.annotation.NtRequestMapping;

@NtRequestMapping(uri = "/test1")
public class NettyCont {
    @NtRequestMapping(uri = "/method")
    public String test(){
        return "hello";
    }
}
