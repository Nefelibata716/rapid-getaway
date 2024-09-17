package com.gl.rapid;

public interface LifeCycle {
    /**
     * 生命周期初始化
     */
    void init();

    /**
     * 生命周期启动方法
     */
    void start();

    /**
     * 生命周期关闭方法
     */
    void shutdown();
}
