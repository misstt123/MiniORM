package com.jluzh.orm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description:
 * @Author lyh-god
 * @Date 2019/9/22
 **/
public class ORMSession {
    private Connection connection;

    public ORMSession(Connection connection) {
        this.connection = connection;
    }

    //1.保存数据
    public void save(Object entity) throws Exception {
        String sql = "";
        //获取映射信息
        List<Mapper> mapperList = ORMConfig.mapperList;
        //找到和entity一致的类
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(entity.getClass().getName())) {
                //拼接sql语句
                String sql1 = "insert into " + mapper.getTableName() + "( ";
                String sql2 = " ) values( ";
                //得到当前对象的所有属性
                Field[] fields = entity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);

                    //遍历field根据mapper得到字段名
                    String columnName = mapper.getPropMapper().get(field);
                    String columnValue = field.get(entity).toString();
                    sql1 += columnName + ",";
                    sql2 += "'" + columnValue + "',";
                }
                sql = sql1.substring(0, sql1.length() - 1) + sql2.substring(0, sql2.length() - 1) + ")";
                break;
            }
        }
        System.out.println("miniORM-save:" + sql);

        //调用jdbc发送sql语句
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

    }


    //2.根据主键就行删除
    public void delete(Object entity) throws Exception {
        String delSQL = "delete from ";
        //1、从ORMconfig从获取映射信息
        List<Mapper> mapperList = ORMConfig.mapperList;
        //2.遍历mapperlist中的集合
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(entity.getClass().getName())) {
                String tableName = mapper.getTableName();
                delSQL += tableName + " where ";
                Map<String, String> idMapper = mapper.getIdMapper();
                //获取idmapper的所有key数组
                Object[] idProp = idMapper.keySet().toArray();
                //获取idmapper 所有的values的数组
                Object[] idColumn = idMapper.values().toArray();

                //获取主键的值
                Field field = entity.getClass().getDeclaredField(idProp[0].toString());
                //通过反射获取值
                String columnValue = field.get(entity).toString();
                //拼接字符串
                delSQL += idColumn[0].toString() + " = " + columnValue;
                PreparedStatement preparedStatement = connection.prepareStatement(delSQL);
                preparedStatement.executeUpdate();

                break;
            }
        }
        System.out.println("miniORM-delete:" + delSQL);
    }

    //3.根据主键进行查询,并且封装到相应的类中
    public Object findOne(Class clz, Object id) throws Exception {
        String querySql = "select from ";
        //从ormconfig获取相应的映射信息
        List<Mapper> mapperList = ORMConfig.mapperList;
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(clz.getName())) {
                String tableName = mapper.getTableName();
                querySql += tableName;
                Object[] columnValue = mapper.getIdMapper().values().toArray();
                querySql += " where " + columnValue[0].toString() + " = " + id.toString();
                break;
            }
        }
        System.out.println("MiniORM-findOne:" + querySql);
        PreparedStatement preparedStatement = connection.prepareStatement(querySql);
        //获取结果集
        ResultSet rs = preparedStatement.executeQuery();
        //封装届国际级
        if (rs.next()) {
            //通过反射获取实例对象
            Object obj = clz.newInstance();
            for (Mapper mapper : mapperList) {
                if (mapper.getClassName().equals(obj.getClass().getName())) {
                    Map<String, String> propMap = mapper.getPropMapper();
                    //对propMap进行遍历
                    Set<String> keySet = propMap.keySet();
                    for (String prop : keySet) {
                        String columnName = propMap.get(prop);
                        Field field = clz.getDeclaredField(prop);
                        field.setAccessible(true);
                        field.set(obj, rs.getObject(columnName));

                    }

                    break;
                }
            }
            preparedStatement.close();
            rs.close();
            return obj;

        } else {

            return null;
        }

    }

    public  void close() throws SQLException {
        if(connection!=null){
            connection.close();
            connection=null;
        }
    }


}
