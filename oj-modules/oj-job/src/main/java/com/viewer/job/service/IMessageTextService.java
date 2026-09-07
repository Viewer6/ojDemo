package com.viewer.job.service;

import com.viewer.job.domain.message.MessageText;

import java.util.List;

public interface IMessageTextService {

    boolean batchInsert(List<MessageText> messageTextList);
}
