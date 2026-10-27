package com.viewer.friend.Service.user;

import com.viewer.common.core.domain.PageQueryDTO;
import com.viewer.common.core.domain.TableDataInfo;

public interface IUserMessageService {
    TableDataInfo list(PageQueryDTO dto);
}
