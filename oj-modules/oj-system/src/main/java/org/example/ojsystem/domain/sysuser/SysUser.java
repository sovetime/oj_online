package org.example.ojsystem.domain.sysuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.common.core.domain.BaseEnity;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2024-12-30
 * Time: 14:33
 */
@Data
@TableName("tb_sys_user")
public class SysUser extends BaseEnity {
    //使用雪花算法
    @TableId(type = IdType.ASSIGN_ID)
    private Long id; //主键 不再使用auto_increment

    private String userAccount;
    private String password;
    private String nickName;

}

