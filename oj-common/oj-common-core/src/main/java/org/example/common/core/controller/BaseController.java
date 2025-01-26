package org.example.common.core.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageInfo;
import org.example.common.core.domain.R;
import org.example.common.core.domain.TableDataInfo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-09
 * Time: 16:58
 */
public class BaseController {

    public R<Void> toR(int rows) {
        return rows>0?R.ok():R.fail();
    }

    public R<Void> toR(boolean result) {
        return result?R.ok():R.fail();
    }

    public TableDataInfo getTableDataInfo(List<?> list) {
        //判断传过来的链表是否为空，为空的话就会调用未查出任何数据的方法
        if (CollectionUtil.isEmpty(list)) {
            return TableDataInfo.empty();
        }
        //返回list，以及总的数据条数
        return TableDataInfo.success(list, new PageInfo<>(list).getTotal());
    }
}
