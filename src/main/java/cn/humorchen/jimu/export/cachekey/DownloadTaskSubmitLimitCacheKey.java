package cn.humorchen.jimu.export.cachekey;

import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @author humorchen
 * date: 2024/3/4
 * description: 下载任务提交限制缓存key
 **/
@AllArgsConstructor
public class DownloadTaskSubmitLimitCacheKey extends CacheKey {
    /**
     * 账号
     */
    private String account;
    /**
     * 请求md5
     */
    private String reqMd5;


    /**
     * @return
     */
    @Override
    public int getExpire() {
        return 10;
    }

    /**
     * @return
     */
    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}
