package cn.humorchen.jimu.export.service.impl;

import cn.humorchen.jimu.export.dto.JimuPageDto;
import cn.humorchen.jimu.export.dto.JimuReportDataColumnDTO;
import cn.humorchen.jimu.export.dto.JimuReportDataSourceDTO;
import cn.humorchen.jimu.export.service.IDownloadTaskService;
import cn.humorchen.jimu.export.util.DownloadCenterUtil;
import cn.humorchen.jimu.export.util.DynamicColumnEasyExcelUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author humorchen
 * date: 2024/3/5
 * description: 积木报表动态列easyexcel实现
 **/
@Slf4j
@AllArgsConstructor
public class JimuReportDynamicEasyExcelImpl implements DynamicColumnEasyExcelUtil.DynamicColumnEasyExcelInterface<JSONObject> {
    /**
     * 报表ID
     */
    private String reportId;
    /**
     * 报表名称
     */
    private String reportName;
    /**
     * 任务ID
     */
    private Integer taskId;
    /**
     * 下载任务服务
     */
    private IDownloadTaskService downloadTaskService;
    /**
     * 请求参数
     */
    private String requestParam;
    /**
     * 请求token
     */
    private String requestToken;
    /**
     * 数据源，接口或者SQL
     */
    private JimuReportDataSourceDTO dataSourceDTO;
    /**
     * 数据列
     */
    private List<JimuReportDataColumnDTO> dataColumnDTOList;
    /**
     * 超时时间
     */
    private static final int TIMEOUT_MILLS = 1000 * 60 * 20;

    /**
     * 分页获取数据
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public JimuPageDto<JSONObject> pageGetData(int page, int size) {
        String apiUrl = dataSourceDTO.getApiUrl();
        String sql = dataSourceDTO.getSql();
        JimuPageDto<JSONObject> jimuPageDto = new JimuPageDto<>();
        try {
            // 走API调用分页
            if (StrUtil.isNotBlank(apiUrl)) {
                JSONObject requestParamJson = JSONObject.parseObject(requestParam);
                for (String key : requestParamJson.keySet()) {
                    String value = requestParamJson.getString(key);
                    String encodeValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                    requestParamJson.put(key, encodeValue);
                    log.info("【下载中心】参数转码 key:{} value:{} encodeValue:{}", key, value, encodeValue);

                }
                requestParamJson.put("pageNo", page);
                requestParamJson.put("pageSize", size);
                String finalRequestParam = requestParamJson.toJSONString();
                // 处理api url
                String url = DownloadCenterUtil.getUrlWithParams(apiUrl, requestParamJson);
                // 调用API获取数据
                HttpRequest post = HttpRequest.post(url);
                DownloadCenterUtil.setDownloadCenterHeaderRequest(post);
                post.header("token", requestToken);
                post.body(finalRequestParam);
                post.setConnectionTimeout(TIMEOUT_MILLS);
                post.setReadTimeout(TIMEOUT_MILLS);
                // 发起请求
                HttpResponse httpResponse = post.execute();
                String body = httpResponse.body();
                log.info("【下载中心】 apiUrl:{} 请求url：{} 请求body:{} ", apiUrl, url, finalRequestParam);
                JSONObject jsonObject = JSONObject.parseObject(body);
                jimuPageDto.setData(jsonObject.getJSONArray(JimuPageDto.Fields.data).toJavaList(JSONObject.class));
                jimuPageDto.setCount(jsonObject.getLongValue(JimuPageDto.Fields.count));
                jimuPageDto.setTotal(jsonObject.getLongValue(JimuPageDto.Fields.total));
            } else if (StrUtil.isNotBlank(sql)) {
                // SQL的无法实现，因为走积木SQL的无法被拦截，此处为预留口
            }
        } catch (Exception e) {
            log.error("分页获取数据失败", e);
        }

        if (jimuPageDto.getData() == null) {
            jimuPageDto.setData(Collections.emptyList());
        }
        if (jimuPageDto.getTotal() < 1) {
            jimuPageDto.setTotal(0);
        }
        if (jimuPageDto.getCount() < 0) {
            jimuPageDto.setCount(0);
        }
        return jimuPageDto;
    }


    /**
     * 数据对象转换为字符串
     *
     * @param jsonObject
     * @return
     */
    @Override
    public List<String> mapDataToStringList(JSONObject jsonObject) {
        return dataColumnDTOList.stream().map(column -> {
            String columnName = column.getColumn();
            if (DynamicColumnEasyExcelUtil.ROW.equals(column.getColumn())) {
                return DynamicColumnEasyExcelUtil.ROW;
            }
            return jsonObject.getString(columnName);
        }).collect(Collectors.toList());
    }

    /**
     * 分页获取数据加载第i页时触发函数，用于实现进度变更
     *
     * @param pageNo
     * @param pageSize
     * @param pages
     */
    @Override
    public void onLoadedPage(int pageNo, int pageSize, int pages) {
        if (downloadTaskService != null && taskId != null) {
            // 计算百分比，加一是因为后面还有个上传操作，不能直接就最后一次加载完100%了
            int percent = pageNo * 100 / (pages + 1);
            downloadTaskService.changeTaskPercent(taskId, percent + "%");
        }
    }
}
