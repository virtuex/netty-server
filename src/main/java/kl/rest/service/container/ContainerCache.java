package kl.rest.service.container;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个key为String,value为method的缓存。
 * todo 缓存机制需要重写
 */
public class ContainerCache {
    public static Map<String, ContainerStruct> containers = new HashMap<>();
}
