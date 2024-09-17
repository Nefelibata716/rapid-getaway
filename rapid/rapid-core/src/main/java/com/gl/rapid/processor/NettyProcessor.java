package com.gl.rapid.processor;

import com.gl.rapid.context.HttpRequestWrapper;

public interface NettyProcessor {

    void process(HttpRequestWrapper httpRequestWrapper);

    public void start();

    public void shutdown();
}
