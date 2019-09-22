package com.jluzh.orm.core;

import com.jluzh.orm.utils.AnnotationUtil;
import com.jluzh.orm.utils.Dom4jUtil;
import org.dom4j.Document;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * @Description:解析核心配置文件的属性
 * @Author lyh-god
 * @Date 2019/9/22
 **/
public class ORMConfig {
    private static String classpath;//classpath类路径
    private static File cfgFile;//核心配置文件
    private static Map<String, String> propConfig = new HashMap<>();//核心配置文件属性名
    private static Set<String> mappingSet;//映射类
    private static Set<String> entitySet;//实体类
    public static List<Mapper> mapperList;//映射信息

    static {
        //得到classpath的luj
        classpath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        //针对中文路径进行转码
        try {
            classpath = URLDecoder.decode(classpath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //加载核心配置文件
        cfgFile = new File(classpath + "miniORM.cfg.xml");
        if (cfgFile.exists()) {
            Document document = Dom4jUtil.getXMLByFilePath(cfgFile.getPath());
            propConfig = Dom4jUtil.Elements2Map(document, "property", "name");
            mappingSet = Dom4jUtil.Elements2Set(document, "mapping", "resource");
            entitySet = Dom4jUtil.Elements2Set(document, "entity", "package");

        } else {
            cfgFile = null;
            System.out.println("找不到核心配置文件");
        }

    }

    //获取连接信息,并且连接数据库
    private Connection getConnection() throws Exception {
        String url = propConfig.get("connection.url");
        String driverClass = propConfig.get("connection.driverClass");
        String username = propConfig.get("connection.username");
        String password = propConfig.get("connection.password");
        Class.forName(driverClass);
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(true);
        return connection;
    }

    private void getMapping() throws ClassNotFoundException {
        mapperList = new ArrayList<>();
        //1解析xml配置文件
        for (String xmlPath : mappingSet) {
            Document document = Dom4jUtil.getXMLByFilePath(classpath + xmlPath);
            String name = Dom4jUtil.getPropValue(document, "class", "name");
            String tabel = Dom4jUtil.getPropValue(document, "class", "tabel");
            Map<String, String> id_id = Dom4jUtil.ElementsID2Map(document);
            Map<String, String> mapping = Dom4jUtil.Elements2Map(document);
            Mapper mapper = new Mapper();
            mapper.setTableName(tabel);
            mapper.setClassName(name);
            mapper.setIdMapper(id_id);
            mapper.setPropMapper(mapping);
            mapperList.add(mapper);
        }


        //2.解析类上的注解获取映射数据
        for (String packagePath : entitySet) {
            Set<String> nameSet = AnnotationUtil.getClassNameByPackage(packagePath);
            for (String name : nameSet) {
                Class clz = Class.forName(name);
                String className = AnnotationUtil.getClassName(clz);
                String tableName = AnnotationUtil.getTableName(clz);
                Map<String, String> id_id = AnnotationUtil.getIdMapper(clz);
                Map<String, String> mapping = AnnotationUtil.getPropMapping(clz);
                Mapper mapper = new Mapper();
                mapper.setTableName(tableName);
                mapper.setClassName(className);
                mapper.setIdMapper(id_id);
                mapper.setPropMapper(mapping);
                mapperList.add(mapper);
            }
        }

    }

    public ORMSession buildORMSession() throws Exception {
        //1连接数据库
        Connection connection = this.getConnection();
        //2.得到映射信息
        this.getMapping();
        //3.生成ORMSession对象
        return new ORMSession(connection);

    }


}
