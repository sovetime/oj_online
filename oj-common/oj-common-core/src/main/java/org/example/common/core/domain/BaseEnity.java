package org.example.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 15:15
 */
@Data
public class BaseEnity {
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

}
