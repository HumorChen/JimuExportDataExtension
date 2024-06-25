package cn.humorchen.jimu.export.service.impl;

import cn.humorchen.jimu.export.dto.JimuPageDto;
import cn.humorchen.jimu.export.service.IReportDataGetService;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author humorchen
 * date: 2023/12/19
 * description:
 **/
@Service
@Slf4j
public class ReportDataGetServiceImpl implements IReportDataGetService {
    @Autowired
    private DataSource dataSource;


    /**
     * 执行SQL返回数据
     *
     * @param sql
     * @return
     */
    @Override
    public JSONObject getOne(String sql) {
        JSONObject ret = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            logSql(sql);
            resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    if (ret != null) {
                        throw new RuntimeException("查询结果不止一条数据");
                    }
                    ret = new JSONObject();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        ret.put(columnName, resultSet.getObject(columnName));
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取数据报错", e);
        } finally {
            // 释放资源
            IoUtil.close(resultSet);
            IoUtil.close(statement);
            IoUtil.close(connection);
        }
        return ret;
    }

    /**
     * 执行SQL返回数据
     *
     * @param sql
     * @return
     */
    @Override
    public JSONArray getList(String sql) {
        JSONArray ret = new JSONArray();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            logSql(sql);
            resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    // 组装数据为json 对象
                    JSONObject data = new JSONObject();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        data.put(columnName, resultSet.getObject(columnName));
                    }
                    ret.add(data);
                }
            }
        } catch (Exception e) {
            log.error("获取数据报错", e);
        } finally {
            // 释放资源
            IoUtil.close(resultSet);
            IoUtil.close(statement);
            IoUtil.close(connection);
        }
        return ret;
    }

    private void logSql(String sql) {
        int len = 5000;
        // 执行SQL
        log.info("执行的SQL：{}", StrUtil.isNotBlank(sql) && sql.length() > len ? sql.substring(0, len) : sql);
    }

    /**
     * 计数
     *
     * @param sql
     * @return
     */
    @Override
    public long count(String sql) {
        String countSQL = getCountSqlFromQuerySql(sql);
        if (StrUtil.isBlank(countSQL)) {
            throw new RuntimeException("计数语句不得为空，SQL为：" + sql);
        }
        long ret = 0;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            logSql(sql);
            resultSet = statement.executeQuery(countSQL);
            if (resultSet != null) {
                while (resultSet.next()) {
                    ret = resultSet.getLong(1);
                }
            }
        } catch (Exception e) {
            log.error("获取数据报错", e);
        } finally {
            // 释放资源
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ignored) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
        return ret;
    }

    /**
     * 从查询语句变计数语句
     *
     * @param sql
     * @return
     */
    public String getCountSqlFromQuerySql(String sql) {
        String selectStr = "select";
        int selectIndex = sql.indexOf(selectStr);
        int fromIndex = sql.indexOf("from");
        return sql.replace(sql.substring(selectIndex + selectStr.length(), fromIndex), " count(*) as c ");
    }

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public JSONArray pageGetList(String sql, int page, int pageSize) {
        String querySql = getPageSqlFromQuerySql(sql, page, pageSize);
        if (StrUtil.isBlank(querySql)) {
            throw new RuntimeException("分页查询解析失败，SQL：" + sql + " 页号: " + page + " 每页数量：" + pageSize);
        }
        return getList(querySql);
    }

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public JimuPageDto<JSONObject> pageGetListForJimu(String sql, int page, int pageSize) {
        JimuPageDto<JSONObject> jimuPageDto = new JimuPageDto<>();
        // 查count
        long count = count(sql);
        long total = count / pageSize + (count % pageSize > 0 ? 1 : 0);
        log.info("数据总条数：{} 条，每页：{} 条，总页数：{} 页", count, pageSize, total);
        jimuPageDto.setTotal(total);
        // 查分页数据
        JSONArray data = pageGetList(sql, page, pageSize);
        List<JSONObject> dataList = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            dataList.add(jsonObject);
        }
        jimuPageDto.setData(dataList);
        jimuPageDto.setCount(count);
        return jimuPageDto;
    }

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @return
     */
    public String getPageSqlFromQuerySql(String sql, int page, int pageSize) {
        Assert.isTrue(page >= 1, () -> new IllegalArgumentException("page不得小于1"));
        Assert.isTrue(pageSize >= 1, () -> new IllegalArgumentException("pageSize不得小于1"));
        int skip = (page - 1) * pageSize;
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" limit ");
        if (skip > 0) {
            builder.append(skip);
            builder.append(",");
        }
        builder.append(pageSize);
        String querySql = builder.toString();
        log.info("分页查询原SQL：{}\n分页SQL处理后：{}", sql, querySql);
        return querySql;
    }

    /**
     * 执行SQL返回数据，数据封装到类cls对象里
     *
     * @param sql
     * @param cls
     * @return
     */
    @Override
    public Object getOne(String sql, Class cls) {
        return getOne(sql).toJavaObject(cls);
    }

    /**
     * 执行SQL返回数据，数据封装到类cls对象里
     *
     * @param sql
     * @param cls
     * @return
     */
    @Override
    public List getList(String sql, Class cls) {
        return getList(sql).toJavaList(cls);
    }

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @param cls
     * @return
     */
    @Override
    public JimuPageDto pageGetListForJimu(String sql, int page, int pageSize, Class cls) {
        JimuPageDto<JSONObject> jimuPageDto = pageGetListForJimu(sql, page, pageSize);
        JimuPageDto ret = new JimuPageDto<>();
        List list = new ArrayList(jimuPageDto.getData().size());
        for (int i = 0; i < jimuPageDto.getData().size(); i++) {
            list.add(jimuPageDto.getData().get(i).toJavaObject(cls));
        }
        ret.setData(list);
        ret.setTotal(jimuPageDto.getTotal());
        ret.setCount(jimuPageDto.getCount());
        return ret;
    }

}
