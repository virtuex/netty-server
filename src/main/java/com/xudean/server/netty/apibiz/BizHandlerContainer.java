package com.xudean.server.netty.apibiz;

import com.xudean.server.annotation.NtRequestMapping;
import com.xudean.server.model.ContainerStruct;
import com.xudean.server.netty.handler.DefaultApiErrorHandler;
import com.xudean.server.util.ClassLoaderUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BizHandlerContainer implements IBizHandlerContainer {

    private static final Logger logger = LoggerFactory.getLogger(BizHandlerContainer.class);

    /**
     * 使用构造器，比直接 class.newInstance 效率更高
     */
    private static Map<String, ContainerStruct> containers = new HashMap<>();

    private static final BizHandlerContainer INSTANCE = new BizHandlerContainer();

    private BizHandlerContainer() {

    }

    public static BizHandlerContainer getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addHandlerContainer(final String handlePackage) {
        if (StringUtils.isBlank(handlePackage)) {
            return;
        }
        Set<Class<?>> handleClasses = ClassLoaderUtil.getClasses(handlePackage);

        Iterator<Class<?>> it = handleClasses.iterator();
        while (it.hasNext()) {
            Class<?> claz = it.next();
            //*****************************
            //todo 下面是采用新方式匹配container的方法
            //*******************************
            NtRequestMapping annotation = claz.getAnnotation(NtRequestMapping.class);
            if (annotation == null) {
                continue;
            }
            //先获取到类注解中的路径值
            String uri = annotation.uri();
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
            Method[] methods = claz.getMethods();
            for (Method method : methods) {
                NtRequestMapping methodAnnotation = method.getAnnotation(NtRequestMapping.class);
                if (methodAnnotation == null) {
                    continue;
                }
                String methodUri = methodAnnotation.uri();
                if (methodUri.startsWith("/")) {
                    methodUri = methodUri.substring(1, methodUri.length());
                }
                String fullUri = uri + "/" + methodUri;
                ContainerStruct containerStruct = new ContainerStruct(fullUri, claz, method);
                containers.put(fullUri, containerStruct);
            }
            logger.info("Handler load success!");
            //还需要添加默认的错误处理方法
            //todo 这里将来需要支持自定义的错误处理
            ApiBizErrorHandlerFactory.getInstance().setApiBizErrorHandler(new DefaultApiErrorHandler());

            logger.info("Error handler load success!");
        }
    }

    public  Map<String, ContainerStruct> getContainers() {
        return containers;
    }

    public  void setContainers(Map<String, ContainerStruct> containers) {
        BizHandlerContainer.containers = containers;
    }
}
