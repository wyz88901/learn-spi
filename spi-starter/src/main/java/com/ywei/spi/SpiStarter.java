package com.ywei.spi;

import com.ywei.spi.service.ILogService;

import java.util.ServiceLoader;

/**
 *
 *  此项目通过log来演示java spi的使用.
 * @author yingzhou.wei
 * @Description 测试java spi
 * @date 2019/6/20
 */
public class SpiStarter {

    /**
     *  1. 当引入sf4j-log.jar ,会调用 Sf4jLogServiceImpl
     *  2. 当引入logback-log.jar, 会调用 LogBackServiceImpl.java
     *  3. 当同时引入后, 两者都会调用
     *
     *  注意事项:
     *  META-INFO/services下的文件名命名规范:
     *   包名+"." + 接口名(接口全路径名)
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        ServiceLoader<ILogService> serviceLoader = ServiceLoader.load(ILogService.class);
        for(ILogService logService: serviceLoader){
            logService.debug(" test spi");
        }
    }
}
