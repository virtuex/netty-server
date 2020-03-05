package kl.rest.service.netty.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gaodq on 2017/1/17.
 */
public class HttpObjectAggregatorHandler extends HttpObjectAggregator {

    private static final Logger logger = LoggerFactory.getLogger(HttpObjectAggregatorHandler.class);

    public static final String TOO_LARGE_RESOURCE = "TOO_LARGE";

    private static FullHttpResponse TOO_LARGE;

    public HttpObjectAggregatorHandler(int maxContentLength) {
        super(maxContentLength);
    }

    @Override
    protected void handleOversizedMessage(final ChannelHandlerContext ctx, HttpMessage oversized) throws Exception {
        if (oversized instanceof HttpRequest) {

            if (TOO_LARGE == null) {
                TOO_LARGE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
                HandlerHelper.createErrorResponse(TOO_LARGE, TOO_LARGE_RESOURCE, new Exception("数据长度不合法"));

            }

            // send back a 413 and close the connection
            ChannelFuture future = ctx.writeAndFlush(TOO_LARGE.retainedDuplicate())
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                logger.error("Failed to send a 413 Request Entity Too Large.", future.cause());
                                ctx.close();
                            }
                        }
                    });

            // If the client started to send data already, close because it's
            // impossible to recover.
            // If keep-alive is off and 'Expect: 100-continue' is missing, no
            // need to leave the connection open.
            if (oversized instanceof FullHttpMessage
                    || !HttpUtil.is100ContinueExpected(oversized) && !HttpUtil.isKeepAlive(oversized)) {
                future.addListener(ChannelFutureListener.CLOSE);
            }

            // If an oversized request was handled properly and the connection
            // is still alive
            // (i.e. rejected 100-continue). the decoder should prepare to
            // handle a new message.
            HttpObjectDecoder decoder = ctx.pipeline().get(HttpObjectDecoder.class);
            if (decoder != null) {
                decoder.reset();
            }
        } else if (oversized instanceof HttpResponse) {
            ctx.close();
            throw new TooLongFrameException("Response entity too large: " + oversized);
        } else {
            throw new IllegalStateException();
        }
    }
}
