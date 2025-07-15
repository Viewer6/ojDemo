package com.viewer.common.core.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamListType {
    EXAM_UN_FINISH_LIST(0, "未结束竞赛"),
    EXAM_HISTORY_LIST(1, "历史竞赛");

    private Integer value;
    private String desc;
}
