package cn.humorchen.jimu.export.annotation;

import java.lang.annotation.*;

/**
 * @author humorchen
 * date: 2024/1/15
 * description: 该报表接口使用下载任务中心代理掉，完成下载任务
 * 使用要求：
 * 参数中需要有一个参数是 DownloadCenterBaseParam 的子类，方法返回值类型需要是支持泛型的JimuPageDto类，方法上加注@UseDownloadTaskCenter注解
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface UseDownloadTaskCenter {

}
