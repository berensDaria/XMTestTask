package com.testtask.test.mytest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SliderDatePeriods {

    TODAY("Today", 1),
    TOMORROW("Tomorrow", 2),
    THIS_WEEK("This Week", 3),
    NEXT_WEEK("Next Week", 4),
    THIS_MONTH("This Month", 5),
    NEXT_MONTH("Next Month", 6);

    private final String name;
    private final int code;
}
