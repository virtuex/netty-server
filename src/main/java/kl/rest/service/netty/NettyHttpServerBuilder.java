package kl.rest.service.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import kl.rest.service.netty.apibiz.BizHandlerContainer;
import kl.rest.service.netty.apibiz.IBizHandlerContainer;
import kl.rest.service.netty.cfg.NettyApiCfg;
import kl.rest.service.netty.handler.HttpObjectAggregatorHandler;
import kl.rest.service.netty.handler.NettyHandlerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Reapsn on 2016/9/29.
 */
public class NettyHttpServerBuilder {

    private NettyApiCfg nettyApiCfg;
    private SslContext sslContext;
    /*
     * when {@code true}, this method will block until the server is stopped.
     * When {@code false}, the execution will end immediately after the server
     * is started.
     */
    private boolean block;
    private APIProtocol apiProtocol;

    public NettyHttpServerBuilder(APIProtocol apiProtocol, NettyApiCfg nettyApiCfg) {
        this.apiProtocol = apiProtocol;
        this.nettyApiCfg = nettyApiCfg;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public NettyHttpServerBuilder setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public boolean isBlock() {
        return block;
    }

    public NettyHttpServerBuilder setBlock(boolean block) {
        this.block = block;
        return this;
    }

    public APIProtocol getApiProtocol() {
        return apiProtocol;
    }

    public NettyHttpServerBuilder setAPIProtocol(APIProtocol apiProtocol) {
        this.apiProtocol = apiProtocol;
        return this;
    }



    /**
     * Create and start Netty server.
     *
     * @return Netty channel instance.
     */
    public Channel build(final IBizHandlerContainer bizHandlerContainer) throws InterruptedException {

        // Configure the server.
        final EventLoopGroup bossGroup = new NioEventLoopGroup(0, newThreadFactory("boss"));
        final EventLoopGroup workerGroup = new NioEventLoopGroup(0, newThreadFactory("worker"));

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                        ChannelPipeline p = ch.pipeline();
                        if (sslContext != null) {
                            p.addLast(sslContext.newHandler(ch.alloc()));
                        }
                        if (nettyApiCfg.isKeepAlive()) {
                            p.addLast(new IdleStateHandler(nettyApiCfg.getReaderIdleTime(), nettyApiCfg.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS));
                        }
                        p.addLast(new HttpServerCodec());
                        p.addLast(new ChunkedWriteHandler());
                        // 最大上传数量
                        p.addLast(new HttpObjectAggregatorHandler(nettyApiCfg.getMaxContentLength()));
                        p.addLast(new HttpContentCompressor());
                        p.addLast(NettyHandlerFactory.genServerHandler(getApiProtocol(), nettyApiCfg.getApiHeaderKey(), bizHandlerContainer, nettyApiCfg));

                    }
                });

        Channel ch = b.bind(nettyApiCfg.getIp(), nettyApiCfg.getPort()).sync().channel();

        ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                bossGroup.shutdownGracefully(0, 60, TimeUnit.SECONDS);
                workerGroup.shutdownGracefully(0, 60, TimeUnit.SECONDS);
            }
        });

        if (block) {
            ch.closeFuture().sync();
            return ch;
        } else {
            return ch;
        }
    }

    private ThreadFactory newThreadFactory(final String poolId) {
        return new ThreadFactory() {

            private final AtomicInteger threadId = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                String name = String.format("api-netty-%s-%s-%d-%s-%d",
                        apiProtocol.isHTTPS() ? "https" : "http",
                        nettyApiCfg.getIp(),
                        nettyApiCfg.getPort(),
                        poolId,
                        threadId.getAndIncrement());
                return new Thread(r, name);
            }
        };
    }


    /**
     * 默认build方法，默认使用NettyHandlerFactory.genServerHandler构造
     */
    public Channel build() throws InterruptedException {
        return build(BizHandlerContainer.getInstance());
    }


}
