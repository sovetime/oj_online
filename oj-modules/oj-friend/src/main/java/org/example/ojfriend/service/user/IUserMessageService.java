package org.example.ojfriend.service.user;


import org.example.common.core.domain.PageQueryDTO;
import org.example.common.core.domain.TableDataInfo;

public interface IUserMessageService {
    TableDataInfo list(PageQueryDTO dto);
}
