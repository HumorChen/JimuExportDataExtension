package cn.humorchen.jimu.export.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Collections;
import java.util.List;

/**
 * @author humorchen
 * date: 2023/12/19
 * description:
 **/
@Data
@FieldNameConstants
@Accessors(chain = true)
public class JimuPageDto<T> {
    /**
     * 数据
     */
    private List<T> data;
    /**
     * 积木的count是总数据条数，不是当前页多少条！！！
     */
    private long count;
    /**
     * 积木的total是总页数，不是总数据条数！！！
     */
    private long total;

    public static final JimuPageDto EMPTY = new JimuPageDto().setData(Collections.emptyList()).setTotal(0).setCount(0);

    /**
     * 从分页数据直接构建
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> JimuPageDto<T> fromPageResult(IPage<T> page) {
        if (page == null) {
            return EMPTY;
        }
        JimuPageDto<T> jimuPageDto = new JimuPageDto<>();
        jimuPageDto.setData(page.getRecords());
        jimuPageDto.setTotal(page.getPages());
        jimuPageDto.setCount(page.getTotal());
        return jimuPageDto;
    }
}
