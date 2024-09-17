package com.gl.rapid;


/**
 * 启动类
 */
public class Bootstrap {

    // 加载网关配置
    private RapidConfig rapidConfig = RapidConfigLoader.getInstance().load(new String[]{});

    // 插件初始化

    // 初始化服务注册管理中心 监听动态配置的新增 修改 删除


    // 启动容器
    RapidContainer rapidContainer = new RapidContainer(rapidConfig);
    rapidContainer.start();


}
