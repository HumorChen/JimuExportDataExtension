package cn.humorchen.jimu.export.config;

import cn.humorchen.jimu.export.service.IJimuExportExtensionNeedImplMethodService;
import cn.humorchen.jimu.export.util.DownloadCenterUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义积木报表鉴权(如果不进行自定义，则所有请求不做权限控制)
 * 1.自定义获取登录token
 * 2.自定义获取登录用户
 */
@Slf4j
@Component
public class JimuReportTokenService implements JmReportTokenServiceI {

    @Autowired
    private IJimuExportExtensionNeedImplMethodService jimuExportExtensionNeedImplMethodService;

    /**
     * 通过请求获取Token
     *
     * @param request
     * @return
     */
    @Override
    public String getToken(HttpServletRequest request) {
        return jimuExportExtensionNeedImplMethodService.getToken(request);
    }

    /**
     * 通过Token获取登录人用户名
     *
     * @param token
     * @return
     */
    @Override
    public String getUsername(String token) {
        return jimuExportExtensionNeedImplMethodService.getAccount(token);
    }

    /**
     * 自定义用户拥有的角色
     *
     * @param token
     * @return
     */
    @Override
    public String[] getRoles(String token) {
        return jimuExportExtensionNeedImplMethodService.getRoles(jimuExportExtensionNeedImplMethodService.getAccount(token));
    }

    /**
     * Token校验
     *
     * @param token
     * @return
     */
    @Override
    public Boolean verifyToken(String token) {
        return jimuExportExtensionNeedImplMethodService.verifyToken(token);
    }

    /**
     * 自定义请求头
     *
     * @return
     */
    @Override
    public HttpHeaders customApiHeader() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpHeaders header = new HttpHeaders();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            // 拷贝token
            String token = jimuExportExtensionNeedImplMethodService.getToken(request);
            String tokenHeaderName = jimuExportExtensionNeedImplMethodService.getTokenHeaderName();
            header.set(tokenHeaderName, token);
            // 拷贝请求body过去
            DownloadCenterUtil.copyDownloadCenterRequestBodyToHeader(request, header);
            // 如果是下载中心发起的请求，设置请求头
            if (DownloadCenterUtil.isDownloadCenterRequest(request)) {
                DownloadCenterUtil.setDownloadCenterHeaderRequest(header);
            }
            jimuExportExtensionNeedImplMethodService.customApiHeader(header);
        }

        return header;
    }
}