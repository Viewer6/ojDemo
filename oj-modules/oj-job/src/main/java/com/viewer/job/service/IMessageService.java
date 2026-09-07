package com.viewer.job.service;

import com.viewer.job.domain.message.Message;

import java.util.List;

public interface IMessageService {

    boolean batchInsert(List<Message> messageTextList);
}
