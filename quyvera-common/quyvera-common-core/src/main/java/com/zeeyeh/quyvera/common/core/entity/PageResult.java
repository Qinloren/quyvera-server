package com.zeeyeh.quyvera.common.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 * @author Qinloren
 * @date 2026/10/20 00:03
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 结果列表
     */
    private List<T> results;

    /**
     * 当前页码
     */
    private long number;

    /**
     * 每页记录数
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 是否是第一页
     */
    private boolean first;

    /**
     * 是否是最后一页
     */
    private boolean last;

    /**
     * 是否拥有上一页
     */
    private boolean previous;

    /**
     * 是否拥有下一页
     */
    private boolean next;
}
