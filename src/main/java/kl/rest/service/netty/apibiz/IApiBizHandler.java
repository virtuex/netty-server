package kl.rest.service.netty.apibiz;

import java.util.Map;

/**
 * 业务处理接口
 *
 * @author
 */
public interface IApiBizHandler {

    /**
     * 获取数据类型
     *
     * @return
     */
    DataModelType getModelType();

    /**
     * 获取资源URI或者是http header中的REQ-TYPE<br>
     * 获取URI在某种代理的情况，可能导致变化（有前缀或者后缀），所以有选择的使用REQ-TYPE方式<br>
     * 具体使用情况根据配置文件中的IApiHeader配置决定，默认实现采用HTTP HEADER的方式获取<br>
     *
     * @return
     */
    String resource();

    /**
     * 获取HTTP头中的数据，非公共的头部信息，自定义信息
     *
     * @return
     */
    String[] httpHeaders();

    /**
     * 业务处理
     *
     * @param modelIn    输入
     * @param inHeaders  输入的http头部内容，需要通过HandlerHelper.HTTP_HEADERS，设置获取的内容，多条数据之间通过分号;分割
     * @param outHeaders 输出的http头部内容，和inHeaders有区别，不能直接把inHeaders中数据拷贝到输出，需要获取有用的信息
     * @param clientIp   客户端IP
     * @param ext        转递一些额外的参数，便于后续接口扩展
     * @return 输出
     * @throws Exception
     */
    byte[] handle(final byte[] modelIn, Map<String, String> inHeaders, Map<String, String> outHeaders,
                  String clientIp, ExtData ext) throws Exception;
}
