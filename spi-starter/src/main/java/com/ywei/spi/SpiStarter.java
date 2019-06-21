package com.ywei.spi;

import com.ywei.spi.dubbo.ExtensionLoader;
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


    public static void main(String[] args) {
      dubboSpi();

        //  jdkSpi();
    }

    /**
     * jdkspi缺点:
     * 1. 无法根据key来动态获取spi实现类
     * 2. 只能循环迭代，根据if来获取指定的spi实现类
     * 3. 没有默认值
     * 功能实现:
     *  1. 当引入sf4j-log.jar ,会调用 Sf4jLogServiceImpl
     *  2. 当引入logback-log.jar, 会调用 LogBackServiceImpl.java
     *  3. 当同时引入后, 两者都会调用
     *  注意事项:
     *    META-INFO/services下的文件名命名规范: 包名+"." + 接口名(接口全路径名)
     *
     */
    public static void jdkSpi(){
        ServiceLoader<ILogService> serviceLoader = ServiceLoader.load(ILogService.class);
        for(ILogService logService: serviceLoader){
            logService.debug(" test spi");
        }
    }


    /**
     * dubbo spi
     * 1. 可以根据key来动态获取spi实现类
     * 2. 有默认值
     */
    public static void dubboSpi(){

        // default spi
/*        ILogService logService = ExtensionLoader.getExtensionLoader(ILogService.class).getDefaultExtension();
        logService.debug(" dubbo spi test");*/


        ILogService  logService = ExtensionLoader.getExtensionLoader(ILogService.class).getExtension("sf4j");
        logService.debug(" dubbo spi test");

    }

}
