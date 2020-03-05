package kl.rest.service.netty.apibiz;

import kl.rest.service.util.ClassLoaderUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Reapsn on 2016/9/30.
 */
public class BizHandlerContainer implements IBizHandlerContainer {

    private static final Logger logger = LoggerFactory.getLogger(BizHandlerContainer.class);

    /**
     * 使用构造器，比直接 class.newInstance 效率更高
     */
    private final Map<String, Constructor<? extends IApiBizHandler>> handlers = new HashMap<>();

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
            if (!claz.isInterface() && !Modifier.isAbstract(claz.getModifiers())
                    && IApiBizHandler.class.isAssignableFrom(claz)) {
                try {
                    // 添加时判断实现有无参构造方法
                    addHandler((Class<? extends IApiBizHandler>) claz);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public IApiBizHandler getHandler(String resource) {
        // 保障子类资源可以通过属性共享，每次都是一个新对象
        Constructor<? extends IApiBizHandler> handler = handlers.get(resource);
        if (handler == null) {
            return null;
        }
        try {
            return handler.newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public synchronized void addHandler(IApiBizHandler handler) throws Exception {
        handlers.put(handler.resource(), handler.getClass().getConstructor());
    }

    public synchronized void addHandler(Class<? extends IApiBizHandler> claz) throws Exception {
        addHandler(claz.getConstructor());
    }

    public synchronized void addHandler(Constructor<? extends IApiBizHandler>... constructors) throws Exception {
        for (Constructor<? extends IApiBizHandler> constructor :
                constructors) {
            handlers.put(constructor.newInstance().resource(), constructor);
        }
    }

    public void reset() {
        handlers.clear();
    }
}
