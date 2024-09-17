package com.gl.rapid;

import com.gl.rapid.netty.NettyHttpClient;
import com.gl.rapid.netty.NettyHttpServer;
import com.gl.rapid.processor.NettyCoreProcessor;
import com.gl.rapid.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 生命周期容器方法
 */
@Slf4j
public class RapidContainer implements LifeCycle {

    private final RapidConfig rapidConfig;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public RapidContainer(RapidConfig rapidConfig) {
        this.rapidConfig = rapidConfig;
        init();
    }


    @Override
    public void init() {
        nettyProcessor = new NettyCoreProcessor();


        nettyHttpServer = new NettyHttpServer(rapidConfig,nettyProcessor);
        nettyHttpServer.init();


    }

    @Override
    public void start() {
        nettyHttpServer.start();

    }

    @Override
    public void shutdown() {
        nettyHttpServer.shutdown();
    }
}
