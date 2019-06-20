package com.ywei.spi.service.logback;

import com.ywei.spi.service.ILogService;

/**
 * @author yingzhou.wei
 * @Description
 * @date 2019/6/20
 */
public class LogBackServiceImpl implements ILogService {
    @Override
    public void debug(String message) {
        System.out.println(" logback debug msg: " + message);
    }
    @Override
    public void info(String message) {
        System.out.println(" logback info msg: " + message);
    }
    @Override
    public void error(String message) {
        System.out.println(" logback error msg: " + message);
    }
    @Override
    public void warn(String message) {
        System.out.println(" logback warn msg: " + message);
    }
}
