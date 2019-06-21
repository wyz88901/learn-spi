package com.ywei.spi.service;

import com.ywei.spi.annotations.SPI;

/**
 * @author yingzhou.wei
 * @Description log 接口定义
 * @date 2019/6/20
 */
@SPI("logback")
public interface ILogService {
    public void debug(String message);
    public void info(String message);
    public void error(String message);
    public void warn(String message);
}
