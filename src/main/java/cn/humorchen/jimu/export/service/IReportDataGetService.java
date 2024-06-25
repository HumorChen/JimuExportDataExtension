package cn.humorchen.jimu.export.service;

import cn.humorchen.jimu.export.dto.JimuPageDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author humorchen
 * date: 2023/12/19
 * description: 获取数据服务，直接SQL跨库拿数据
 **/
public interface IReportDataGetService<T> {
    /**
     * 执行SQL返回数据
     *
     * @param sql
     * @return
     */
    JSONObject getOne(String sql);

    /**
     * 执行SQL返回数据，数据封装到类cls对象里
     *
     * @param sql
     * @param cls
     * @return
     */
    T getOne(String sql, Class<T> cls);

    /**
     * 执行SQL返回数据
     *
     * @param sql
     * @return
     */
    JSONArray getList(String sql);

    /**
     * 执行SQL返回数据，数据封装到类cls对象里
     *
     * @param sql
     * @param cls
     * @return
     */
    List<T> getList(String sql, Class<T> cls);

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @return
     */
    JSONArray pageGetList(String sql, int page, int pageSize);

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @return
     */
    JimuPageDto<JSONObject> pageGetListForJimu(String sql, int page, int pageSize);

    /**
     * 分页查询
     *
     * @param sql
     * @param page
     * @param pageSize
     * @param cls
     * @return
     */
    JimuPageDto<T> pageGetListForJimu(String sql, int page, int pageSize, Class<T> cls);

    /**
     * 计数
     *
     * @param sql
     * @return
     */
    long count(String sql);


    /**
     * 生成in语句
     *
     * @param columnName
     * @param elements
     * @return string
     */
    default String getColumnInSql(String columnName, List<String> elements) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" ");
        sqlBuilder.append(columnName);
        sqlBuilder.append(" in (");
        for (int i = 0; i < elements.size(); i++) {
            String id = elements.get(i);
            if (i > 0) {
                sqlBuilder.append(",");
            }
            sqlBuilder.append("'");
            sqlBuilder.append(id);
            sqlBuilder.append("'");
        }
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

}
