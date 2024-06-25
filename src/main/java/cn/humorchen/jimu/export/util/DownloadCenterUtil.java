package cn.humorchen.jimu.export.util;

import cn.humorchen.jimu.export.pojo.DownloadTaskJson;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author humorchen
 * date: 2024/1/18
 * description: 下载中心工具类
 **/
@Slf4j
public class DownloadCenterUtil {
    private static final String DOWNLOAD_CENTER_HEADER_REQUEST = "download-center-request";

    /**
     * 是否为下载中心发起的请求
     *
     * @return
     */
    public static boolean isDownloadCenterRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.debug("DownloadCenterUtil#isDownloadCenterRequest requestAttributes is null");
            return false;
        }
        return isDownloadCenterRequest(requestAttributes.getRequest());
    }

    /**
     * 是否为下载中心发起的请求
     *
     * @param servletRequest
     * @return
     */
    public static boolean isDownloadCenterRequest(HttpServletRequest servletRequest) {
        return servletRequest != null && StrUtil.isNotBlank(servletRequest.getHeader(DOWNLOAD_CENTER_HEADER_REQUEST));
    }

    /**
     * 设置下载中心请求头
     *
     * @param headers
     */
    public static void setDownloadCenterHeaderRequest(HttpHeaders headers) {
        if (headers != null) {
            headers.set(DOWNLOAD_CENTER_HEADER_REQUEST, "true");
        }
    }

    /**
     * 复制下载中心请求头
     *
     * @param request
     * @param headers
     */
    public static void copyDownloadCenterRequestBodyToHeader(HttpServletRequest request, HttpHeaders headers) {
        if (request == null || headers == null) {
            return;
        }
        // 复制request请求里的请求体
        headers.set(DownloadTaskJson.Fields.requestBody, RequestUtil.getRequestBody(request));
    }


    /**
     * 获取下载请求头
     *
     * @param request
     * @return
     */
    public static String getRequestBodyFromHeader(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader(DownloadTaskJson.Fields.requestBody);
    }

    /**
     * 设置下载中心请求头
     *
     * @param request
     */
    public static void setDownloadCenterHeaderRequest(HttpRequest request) {
        if (request != null) {
            request.header(DOWNLOAD_CENTER_HEADER_REQUEST, "true");
        }
    }


    /**
     * 获取带参数的url
     *
     * @param url
     * @param params
     * @return
     */
    public static String getUrlWithParams(String url, JSONObject params) {
        if (StrUtil.isBlank(url) || params == null) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.getString(key)).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

}
