package org.example.common.core.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.core.enums.ResultCode;

import java.util.ArrayList;
import java.util.List;

//表格分页数据对象
@Getter
@Setter
@NoArgsConstructor
public class TableDataInfo {

    //总记录数
    private long total;
    //列表数据
    private List<?> rows;
    //消息状态码
    private int code;
    //消息内容
    private String msg;

    //未查出任何数据时调用
    public static TableDataInfo empty() {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(ResultCode.SUCCESS.getCode());
        rspData.setRows(new ArrayList<>());
        rspData.setMsg(ResultCode.SUCCESS.getMsg());
        rspData.setTotal(0);
        return rspData;
    }

    //查出数据时调用
    public static TableDataInfo success(List<?> list,long total) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(ResultCode.SUCCESS.getCode());
        rspData.setRows(list);
        rspData.setMsg(ResultCode.SUCCESS.getMsg());
        rspData.setTotal(total);
        return rspData;
    }
}
