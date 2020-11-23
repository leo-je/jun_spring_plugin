package com.diboot.core.config;

/**
 * 基础常量定义
 * @author Wujun
 * @version 2.0
 * @date 2019/01/01
 */
public class Cons {
    /**
     * 默认字符集UTF-8
     */
    public static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 逗号分隔符 ,
     */
    public static final String SEPARATOR_COMMA = ",";
    /**
     * 下划线分隔符_
     */
    public static final String SEPARATOR_UNDERSCORE = "_";
    /**
     * 排序 - 降序标记
     */
    public static final String ORDER_DESC = "DESC";
    /***
     * 默认字段名定义
     */
    public enum FieldName{
        /**
         * 主键属性名
         */
        id,
        /**
         * 默认的上级ID属性名
         */
        parentId,
        /**
         * 子节点属性名
         */
        children,
        /**
         * 逻辑删除标记字段
         */
        deleted,
        /**
         * 创建时间字段
         */
        createTime
    }

}
