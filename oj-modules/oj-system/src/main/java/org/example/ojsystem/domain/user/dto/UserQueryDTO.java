package org.example.ojsystem.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.common.core.domain.PageQueryDTO;

@Getter
@Setter
public class UserQueryDTO extends PageQueryDTO {

    private Long userId;

    private String nickName;
}
