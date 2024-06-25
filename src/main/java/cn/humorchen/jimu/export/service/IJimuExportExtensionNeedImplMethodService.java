package cn.humorchen.jimu.export.service;

import cn.humorchen.jimu.export.pojo.DownloadTaskJson;
import cn.hutool.core.util.StrUtil;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author humorchen
 * date: 2024/6/25
 * description: 积木报表导出用户获取服务接口
 * --------------------------------------------------------
 * ----------需要自行实现该方法并注入到spring ioc容器中----------
 * --------------------------------------------------------
 **/
public interface IJimuExportExtensionNeedImplMethodService {
    /**
     * token字段名
     */
    Set<String> TOKEN_NAME_SET = Stream.of("Authorization", "authorization", "token", "Token", DownloadTaskJson.Fields.requestToken, "x-access-token", "X-Access-Token").collect(Collectors.toSet());

    /**
     * 获取请求里的登录token
     *
     * @param request 请求对象
     * @return token
     */
    default String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = null;
        for (String key : TOKEN_NAME_SET) {
            if (StrUtil.isNotBlank((token = request.getHeader(key)))) {
                return token;
            }
            if (StrUtil.isNotBlank((token = request.getParameter(key)))) {
                return token;
            }
        }
        return token;
    }

    /**
     * 获取token在请求头里的名称
     */
    default String getTokenHeaderName() {
        return "token";
    }

    /**
     * 获取当前登录的账户
     * 如果不做用户区分的话，那就返回空字符串
     *
     * @return 当前登录的账户
     */
    default String getAccount() {
        return "";
    }


    /**
     * 获取token对应的用户
     *
     * @param token 积木收到的token
     * @return 当前登录的账户
     */
    default String getAccount(String token) {
        return "";
    }


    /**
     * 获取用户对应的角色
     *
     * @param account 用户账户
     * @return 用户对应的角色列表
     */
    default String[] getRoles(String account) {
        return new String[]{"admin"};
    }


    /**
     * 校验token是否有效
     *
     * @param token 积木收到的token
     * @return true表示token有效
     */
    default Boolean verifyToken(String token) {
        return true;
    }

    /**
     * 自定义积木发起api调用的请求头
     *
     * @param headers 请求头
     */
    default void customApiHeader(HttpHeaders headers) {
    }

    /**
     * 判定是否为积木导出请求
     * 用于对导出请求做拦截注册下载任务处理，默认无需修改该实现
     *
     * @param pageNo                 页号
     * @param pageSize               页大小
     * @param jmReportPageSizeNumber 积木报表配置的导出一页数据大小 积木默认5000
     * @return true表示是积木导出请求
     */
    default boolean isExportPageRequest(int pageNo, int pageSize, int jmReportPageSizeNumber) {
        return pageSize == jmReportPageSizeNumber;
    }

    /**
     * 判定是否为积木导出请求
     * 用于对导出请求做拦截注册下载任务处理，默认无需修改该实现
     *
     * @param pageNo                 页号
     * @param pageSize               页大小
     * @param jmReportPageSizeNumber 积木报表配置的导出一页数据大小
     * @return true表示是积木导出请求
     */
    default boolean isExportFirstPageRequest(int pageNo, int pageSize, int jmReportPageSizeNumber) {
        return pageNo == 1 && isExportPageRequest(pageNo, pageSize, jmReportPageSizeNumber);
    }

    /**
     * 获取提交限制时间
     * 同样的报表同样的参数多少秒内不得重复提交导出请求
     *
     * @return 提交限制时间 单位秒
     */
    default Integer getSubmitLimitSec() {
        return null;
    }

    /**
     * 上传excel到oss
     *
     * @param account     下载任务的账户
     * @param taskTitle   下载任务的标题
     * @param inputStream excel文件的输入流，你读取这个流上传到oss，然后上传完关闭这个流
     * @return 返回oss的下载url（https://xxxxx/xxxx），这个地址要直接可以在浏览器打开就下载excel文件的
     * 如果不能直接下载，请前后端对接时约定好。
     */
    String uploadExcelToOss(String account, String taskTitle, InputStream inputStream);
}
