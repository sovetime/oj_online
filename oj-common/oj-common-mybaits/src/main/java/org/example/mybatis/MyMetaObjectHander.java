package org.example.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.example.common.core.constants.Constants;
import org.example.common.core.utils.ThreadLocalUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-09
 * Time: 21:21
 */
@Component
@Slf4j
public class MyMetaObjectHander implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        //创建人  获取当前用户用户id  如何获取当前调用接口的用户的id呢？
        this.strictInsertFill(metaObject, "createBy", Long.class, ThreadLocalUtil.get(Constants.USER_ID, Long.class));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, ThreadLocalUtil.get(Constants.USER_ID, Long.class));
    }
}
