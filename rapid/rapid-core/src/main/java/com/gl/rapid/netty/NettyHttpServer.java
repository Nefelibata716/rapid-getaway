package com.gl.rapid.netty;

import com.gl.rapid.LifeCycle;
import com.gl.rapid.RapidConfig;
import com.gl.rapid.common.util.RemotingUtil;
import com.gl.rapid.netty.handler.NettyHttpServerHandler;
import com.gl.rapid.netty.handler.NettyServerConnectManagerHandler;
import com.gl.rapid.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 承接所有网络请求的核心类
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final RapidConfig rapidConfig;

    private int port = 8888;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private NettyProcessor nettyProcessor;


    public NettyHttpServer(RapidConfig rapidConfig, NettyProcessor nettyProcessor) {
        this.rapidConfig = rapidConfig;
        this.nettyProcessor = nettyProcessor;
        int port1 = rapidConfig.getPort();
        if (port1 > 0 && port1 < 65535) {
            port = port1;
        }
        init();
    }


    @Override
    public void init() {
        // 初始化 方法
        this.serverBootstrap = new ServerBootstrap();
        if (useEPoll()) {
            bossGroup = new EpollEventLoopGroup(rapidConfig.getEventLoopGroupBossNum(),new DefaultThreadFactory("NettyBossEpoll"));
            workerGroup = new EpollEventLoopGroup(rapidConfig.getEventLoopGroupWorkNum(), new DefaultThreadFactory("NettyWorkerEpoll"));
        }else {
            bossGroup = new NioEventLoopGroup(rapidConfig.getEventLoopGroupBossNum(), new DefaultThreadFactory("NettyBossNio"));
            workerGroup = new NioEventLoopGroup(rapidConfig.getEventLoopGroupWorkNum(), new DefaultThreadFactory("NettyWorkerNio"));
        }


    }

    public boolean useEPoll() {
        return rapidConfig.isUseEPoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        ServerBootstrap handler = this.serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(useEPoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)			//	sync + accept = backlog
                .option(ChannelOption.SO_REUSEADDR, true)   	//	tcp端口重绑定
                .option(ChannelOption.SO_KEEPALIVE, false)  	//  如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.TCP_NODELAY, true)   //	该参数的左右就是禁用Nagle算法，使用小数据传输时合并
                .childOption(ChannelOption.SO_SNDBUF, 65535)	//	设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)	//	设置接收数据缓冲区大小
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(rapidConfig.getMaxContentLength()),
                                new HttpServerExpectContinueHandler(),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler(nettyProcessor)
                        );
                    }
                });

        if(rapidConfig.isNettyAllocator()) {
            handler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            this.serverBootstrap.bind().sync();
            log.info("< ============= Rapid Server StartUp On Port: " + this.port + "================ >");
        } catch (Exception e) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() fail!", e);
        }
    }

    @Override
    public void shutdown() {
        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
