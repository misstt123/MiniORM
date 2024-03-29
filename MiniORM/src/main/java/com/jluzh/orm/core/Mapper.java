package com.jluzh.orm.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:封装和存储映射信息
 * @Author lyh-god
 * @Date 2019/9/22
 **/
public class Mapper {
    private String className;//类名
    private String tableName;//表名
    private Map<String, String> idMapper = new HashMap<>();//id属性
    public Map<String, String> propMapper = new HashMap<>();//非id属性

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getIdMapper() {
        return idMapper;
    }

    public void setIdMapper(Map<String, String> idMapper) {
        this.idMapper = idMapper;
    }

    public Map<String, String> getPropMapper() {
        return propMapper;
    }

    public void setPropMapper(Map<String, String> propMapper) {
        this.propMapper = propMapper;
    }

    @Override
    public String toString() {
        return "Mapper{" +
                "className='" + className + '\'' +
                ", tableName='" + tableName + '\'' +
                ", idMapper=" + idMapper +
                ", propMapper=" + propMapper +
                '}';
    }
}
