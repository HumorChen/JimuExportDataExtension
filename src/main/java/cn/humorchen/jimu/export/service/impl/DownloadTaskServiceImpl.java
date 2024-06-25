package cn.humorchen.jimu.export.service.impl;

import cn.humorchen.jimu.export.bean.DownloadTask;
import cn.humorchen.jimu.export.cachekey.DownloadTaskSubmitLimitCacheKey;
import cn.humorchen.jimu.export.dto.JimuReportDataColumnDTO;
import cn.humorchen.jimu.export.dto.JimuReportDataSourceDTO;
import cn.humorchen.jimu.export.dto.PageListDownloadTaskDto;
import cn.humorchen.jimu.export.enums.DownloadTaskStateEnum;
import cn.humorchen.jimu.export.event.DownloadTaskPublishEvent;
import cn.humorchen.jimu.export.mapper.DownloadTaskMapper;
import cn.humorchen.jimu.export.service.IDownloadTaskService;
import cn.humorchen.jimu.export.service.IJimuExportExtensionNeedImplMethodService;
import cn.humorchen.jimu.export.service.IReportDataGetService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 下载任务 服务实现类
 * </p>
 *
 * @author humorchen
 * @since 2024-01-05
 */
@Service
@Slf4j
public class DownloadTaskServiceImpl extends ServiceImpl<DownloadTaskMapper, DownloadTask> implements IDownloadTaskService {
    @Autowired
    private DownloadTaskMapper downloadTaskMapper;
    @Autowired
    private IReportDataGetService reportDataGetService;
    /**
     * 若项目未引入redis则不做限制重复提交
     */
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 注入spring 事件发布器
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private IJimuExportExtensionNeedImplMethodService jimuExportDataUserService;

    /**
     * 注册任务
     *
     * @param downloadTask
     * @return
     */
    @Override
    public DownloadTask registerTask(@NonNull DownloadTask downloadTask) {
        downloadTaskMapper.insert(downloadTask);
        return downloadTask;
    }

    /**
     * 10秒内是否有相同任务未完成，不给再次注册下载任务
     *
     * @param account
     * @param requestBody
     * @return
     */
    @Override
    public boolean setSameTaskLock(String account, String requestBody) {
        if (redisTemplate == null) {
            return true;
        }
        DownloadTaskSubmitLimitCacheKey limitCacheKey = new DownloadTaskSubmitLimitCacheKey(account, MD5.create().digestHex(requestBody));
        int expire = Optional.ofNullable(jimuExportDataUserService.getSubmitLimitSec()).orElse(limitCacheKey.getExpire());
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(limitCacheKey.getKey(), DateUtil.now(), expire, limitCacheKey.getTimeUnit());
        return Boolean.TRUE.equals(setIfAbsent);
    }

    /**
     * 更新任务
     *
     * @param downloadTask
     * @return
     */
    @Override
    public int updateTaskById(@NonNull DownloadTask downloadTask) {
        return downloadTaskMapper.updateById(downloadTask);
    }

    /**
     * 更新任务进度
     *
     * @param id
     * @param percent
     * @return
     */
    @Override
    public int changeTaskPercent(int id, @NonNull String percent) {
        UpdateWrapper<DownloadTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(DownloadTask.ID, id);
        updateWrapper.set(DownloadTask.PERCENT, percent);
        log.info("【下载中心】更新任务进度 id:{} percent:{}", id, percent);
        return downloadTaskMapper.update(null, updateWrapper);
    }

    /**
     * 更新任务状态
     *
     * @param id
     * @param state
     * @return
     */
    @Override
    public int changeTaskState(int id, @NonNull DownloadTaskStateEnum state) {
        UpdateWrapper<DownloadTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(DownloadTask.ID, id);
        updateWrapper.set(DownloadTask.STATE, state.getState());
        return downloadTaskMapper.update(null, updateWrapper);
    }

    /**
     * 更新任务状态
     *
     * @param id
     * @param expectState
     * @param targetState
     * @return
     */
    @Override
    public int compareAndSwapTaskState(int id, @NonNull DownloadTaskStateEnum expectState, @NonNull DownloadTaskStateEnum targetState) {
        UpdateWrapper<DownloadTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(DownloadTask.ID, id);
        updateWrapper.eq(DownloadTask.STATE, expectState.getState());
        updateWrapper.set(DownloadTask.STATE, targetState.getState());
        return downloadTaskMapper.update(null, updateWrapper);
    }

    /**
     * 根据任务ID获取任务
     *
     * @param id
     * @return
     */
    @Override
    public DownloadTask getDownloadTaskById(int id) {
        return downloadTaskMapper.selectById(id);
    }

    /**
     * 查下载任务
     *
     * @param pageListDownloadTaskDto
     * @return
     */
    @Override
    public IPage<DownloadTask> pageListDownloadTask(PageListDownloadTaskDto pageListDownloadTaskDto) {
        Integer id = pageListDownloadTaskDto.getId();
        String startTime = pageListDownloadTaskDto.getStartTime();
        String endTime = pageListDownloadTaskDto.getEndTime();
        String fileName = pageListDownloadTaskDto.getFileName();
        Integer taskState = pageListDownloadTaskDto.getTaskState();
        String account = jimuExportDataUserService.getAccount();
        int pageNo = Optional.ofNullable(pageListDownloadTaskDto.getPageNo()).orElse(1);
        int pageSize = Optional.ofNullable(pageListDownloadTaskDto.getPageSize()).orElse(10);

        QueryWrapper<DownloadTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DownloadTask.ACCOUNT, account);
        queryWrapper.eq(id != null, DownloadTask.ID, id);
        queryWrapper.between(startTime != null && endTime != null, DownloadTask.CREATE_TIME, startTime, endTime);
        queryWrapper.like(StrUtil.isNotBlank(fileName), DownloadTask.TITLE, "%" + fileName + "%");
        queryWrapper.eq(taskState != null, DownloadTask.STATE, taskState);
        // 最新的在前
        queryWrapper.orderByDesc(DownloadTask.CREATE_TIME);
        return page(new Page<>(pageNo, pageSize), queryWrapper);
    }

    /**
     * 重新执行下载任务
     *
     * @param taskId
     */
    @Override
    public void rerunTask(Integer taskId) {
        DownloadTask downloadTask = getDownloadTaskById(taskId);
        if (downloadTask == null || downloadTask.getState() == null) {
            throw new RuntimeException("未找到该任务或任务异常，请刷新后重试");
        }
        if (DownloadTaskStateEnum.RUNNING.getState() == downloadTask.getState() || DownloadTaskStateEnum.SUCCESS.getState() == downloadTask.getState()) {
            throw new RuntimeException("任务" + DownloadTaskStateEnum.of(downloadTask.getState()).getTitle() + ",无法重新执行");
        }
        eventPublisher.publishEvent(new DownloadTaskPublishEvent(taskId));
    }

    /**
     * 根据报表ID获取报表名称
     *
     * @param reportId
     * @return
     */
    @Override
    public String getReportNameByReportId(String reportId) {
        if (StrUtil.isBlank(reportId)) {
            return "";
        }
        String sql = "select name from report.jimu_report where id = '" + reportId + "'";
        JSONObject jsonObject = reportDataGetService.getOne(sql);
        return Optional.ofNullable(jsonObject.getString("name")).orElse("");
    }

    /**
     * 从请求体中获取报表ID
     *
     * @param requestBody
     * @return
     */
    @Override
    public String getReportIdFromRequestBody(String requestBody) {
        if (StrUtil.isNotBlank(requestBody)) {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return jsonObject.getString("excelConfigId");
        }
        return null;
    }

    /**
     * 根据报表ID获取报表API地址或者SQL
     *
     * @param reportId
     * @return
     */
    @Override
    public JimuReportDataSourceDTO getReportApiOrSqlByReportId(String reportId) {
        JimuReportDataSourceDTO jimuReportDataSourceDTO = new JimuReportDataSourceDTO();
        if (StrUtil.isNotBlank(reportId)) {
            String sql = "select db_dyn_sql,api_url from report.jimu_report_db where jimu_report_id = '" + reportId + "'";
            JSONObject jsonObject = reportDataGetService.getOne(sql);
            jimuReportDataSourceDTO.setSql(jsonObject.getString("db_dyn_sql"));
            jimuReportDataSourceDTO.setApiUrl(jsonObject.getString("api_url"));
        }
        List<List<String>> head = new ArrayList<>();
        EasyExcel.write(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }).head(head).sheet("sheet").doWrite(new ArrayList<>());

        return jimuReportDataSourceDTO;
    }

    /**
     * 获取积木报表的头
     *
     * @param reportId
     * @return
     */
    @Override
    public List<JimuReportDataColumnDTO> getReportHead(String reportId) {
        if (StrUtil.isBlank(reportId)) {
            return Collections.emptyList();
        }
        String sql = "select json_str from report.jimu_report where id = '" + reportId + "'";
        JSONObject jsonObject = reportDataGetService.getOne(sql);
        String jsonStr = jsonObject.getString("json_str");
        JSONObject json = JSONObject.parseObject(jsonStr);
        JSONObject rows = json.getJSONObject("rows");
        JSONObject rows0Cells = rows.getJSONObject("0").getJSONObject("cells");
        JSONObject rows1Cells = rows.getJSONObject("1").getJSONObject("cells");

        Set<String> rows0KeySets = rows0Cells.keySet();
        List<JimuReportDataColumnDTO> heads = rows0KeySets.stream().map(key -> {
            JSONObject keyObject = rows0Cells.getJSONObject(key);
            JSONObject columnObject = rows1Cells.getJSONObject(key);
            if (keyObject == null || columnObject == null) {
                return null;
            }
            String name = keyObject.getString("text");
            String column = columnObject.getString("text");
            if (StrUtil.isBlank(name) || StrUtil.isBlank(column)) {
                return null;
            }
            // 处理 #{vpjcgifyua.orderId}
            int indexOf = column.lastIndexOf(".");
            int indexOf2 = column.lastIndexOf("}");
            if (column.startsWith("#") && indexOf >= 0 && indexOf2 >= 0) {
                column = column.substring(indexOf + 1, indexOf2);
                if (StrUtil.isBlank(column)) {
                    return null;
                }
            }
            JimuReportDataColumnDTO jimuReportDataColumnDTO = new JimuReportDataColumnDTO();
            jimuReportDataColumnDTO.setName(name);
            jimuReportDataColumnDTO.setColumn(column);
            jimuReportDataColumnDTO.setIndex(Integer.parseInt(key));
            return jimuReportDataColumnDTO;
        }).filter(Objects::nonNull).sorted(Comparator.comparing(JimuReportDataColumnDTO::getIndex)).collect(Collectors.toList());
        log.info("【下载中心】获取积木报表的头 reportId:{}，heads:{}", reportId, heads);
        return heads;
    }

    /**
     * 从积木请求体中获取请求参数
     *
     * @param json
     * @return
     */
    @Override
    public String getRequestParamFromJson(String json) {
        if (StrUtil.isNotBlank(json)) {
            JSONObject jsonObject = JSONObject.parseObject(json);
            if (jsonObject.containsKey("param")) {
                return jsonObject.getJSONObject("param").toJSONString();
            }
            return "{}";
        }
        return "{}";
    }


}
