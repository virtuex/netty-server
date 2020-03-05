package kl.rest.service.container;

import java.lang.reflect.Method;

/**
 * 一个HTTP请求对应的容器
 */
public class ContainerStruct {
    /**
     * 请求的URL
     */
    private String url;
    /**
     * 执行改URL逻辑对应的Method所在的class
     */
    private Class clazz;
    /**
     * 执行URL逻辑的Method
     */
    private Method method;

    public ContainerStruct(String url, Class clazz, Method method) {
        this.url = url;
        this.clazz = clazz;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
