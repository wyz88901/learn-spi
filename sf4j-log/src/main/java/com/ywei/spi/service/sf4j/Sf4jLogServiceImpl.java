package com.ywei.spi.service.sf4j;

import com.ywei.spi.service.ILogService;

/**
 * @author yingzhou.wei
 * @Description TODO
 * @date 2019/6/20
 */
public class Sf4jLogServiceImpl implements ILogService {
    @Override
    public void debug(String message) {
        System.out.println(" sf4j debug msg: " + message);
    }
    @Override
    public void info(String message) {
        System.out.println(" sf4j info msg: " + message);
    }
    @Override
    public void error(String message) {
        System.out.println(" sf4j error msg: " + message);
    }
    @Override
    public void warn(String message) {
        System.out.println(" sf4j warn msg: " + message);
    }
}
