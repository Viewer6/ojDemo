package com.viewer.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamListType {
    EXAM_UN_FINISH_LIST(0, "未结束竞赛"),
    EXAM_HISTORY_LIST(1, "历史竞赛"),
    USER_EXAM_LIST(2, "竞赛列表");

    private Integer value;
    private String desc;

    ExamListType(Integer value) {
        this.value = value;
    }
}
