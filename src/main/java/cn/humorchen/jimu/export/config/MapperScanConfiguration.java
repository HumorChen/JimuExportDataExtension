package cn.humorchen.jimu.export.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author humorchen
 * date: 2024/6/25
 * description: 配置扫描mapper包路径
 **/
@Configuration
@MapperScan(basePackages = "cn.humorchen.jimu.export.mapper")
public class MapperScanConfiguration {
}
