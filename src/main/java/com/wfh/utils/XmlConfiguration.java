package com.wfh.utils;

import com.wfh.xml.SqlStatement;
import com.wfh.xml.SqlType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class XmlConfiguration {
    //用一个sax读取器读取即可
    private static final SAXReader reader = new SAXReader();
    private static final Map<String, SqlStatement> sqlStatements = new HashMap<>();
//    public static void main(String[] args) throws DocumentException, FileNotFoundException {
//        String path = "classPath:mapper/*Mapper.xml";
//        parseXml(path);
//    }
    public static SqlStatement getSqlStatement(String sqlStatementKey){
        return sqlStatements.get(sqlStatementKey);
    }
    public static void parseXml(String locations) throws DocumentException, FileNotFoundException, ClassNotFoundException {
        //类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //将路径通过/分割
        String[] dirs = locations.split("/");
        //获取第一个文件夹
        String firstDir = dirs[0];
        if (firstDir.startsWith("classPath:")) {
            //去除classPath:
            firstDir = firstDir.substring(10);
        }
        //加载资源路径(路径第一个不能带/)
        URL url = classLoader.getResource(firstDir);
        //获取解析文件的根目录的绝对路径的文件
        File root = new File(url.getPath());
        //判断传来的路径中是否有文件
        String fileName = null;
        for (int i = 1; i < dirs.length; i++) {
            //获取每一级文件夹的名称
            String dir = dirs[i];
            if (dir.indexOf(".") > 0) {//说明有后缀是文件
                fileName = dir;
            } else {//不是文件,是文件夹
                File next = new File(root, dir);
                if (next.exists()) {//下一级目录存在
                    root = next;
                } else {//下一级目录不存在
                    throw new RuntimeException("目录" + dir + "不存在");
                }
            }
        }
        File[] files = null;
        if (fileName == null || "".equals(fileName)) {//不存在文件名,那么就是最后一级目录的所有文件
            //获取最终目录下的所有文件
            files = root.listFiles();
        } else {//有xml文件的情况下,考虑*
            if (fileName.startsWith("*")) {//在文件名开头有*
                String finalFileName = fileName.substring(1);
                files = root.listFiles(file -> file.getName().endsWith(finalFileName));
            } else if (fileName.endsWith("*")) {//文件名末尾包含*
                String finalFileName = fileName.substring(0, fileName.length() - 1);
                files = root.listFiles(file -> file.getName().startsWith(finalFileName));
            } else if (fileName.contains("*")) {//文件名包含*
                int index = fileName.indexOf("*");
                String prefix = fileName.substring(0, index);
                String suffix = fileName.substring(index + 1);
                files = root.listFiles(file -> file.getName().startsWith(prefix) && file.getName().endsWith(suffix));
            } else {//文件名不含有*的情况
                String finalFileName = fileName;
                files = root.listFiles(file -> file.getName().equals(finalFileName));
            }
        }
        if (files == null || files.length == 0) {
            throw new RuntimeException("未配置文件");
        } else {
            for (File file : files) {
                System.out.println(file.getName());
                parseXml(file);
            }
        }
    }

    private static void parseXml(File file) throws DocumentException, FileNotFoundException, ClassNotFoundException {
        InputStream is = new FileInputStream(file);
        //xml文档对象
        Document document = reader.read(is);
        //获取xml的唯一根标签
        Element mapperElem = document.getRootElement();
        //获取根标签的namespace
        String namespace = mapperElem.attributeValue("namespace");
        //获取根标签下的所有子元素
        List<Element> elements = mapperElem.elements();
        //遍历每一个标签元素(每一个sql标签)
        for (Element e : elements) {
            //获取sql执行的操作类型
            String sqlType = e.getQualifiedName();
            //获取sql执行后返回的类型
            Class<?> resultType;
            if ("select".equalsIgnoreCase(sqlType)){//查询
                String resultTypeClassName = e.attributeValue("resultType");
                resultType = Class.forName(resultTypeClassName);
            }else {//增删改返回受影响的行数
                resultType = int.class;
            }
            //存储节点
            List<Node> nodes = new ArrayList<>();
            //id标识sql对应的dao层方法名
            String id = e.attributeValue("id");
            //作为sql语句的唯一标识:类名+方法名
            String sqlStatementKey = namespace + "." + id;
            //获取节点的迭代器(节点包括文本和标签)
            Iterator<Node> iterator = e.nodeIterator();
            while (iterator.hasNext()) {//不断循环获取节点
                Node next = iterator.next();
                //存储节点
                nodes.add(next);
            }
            SqlStatement sqlStatement = new SqlStatement(nodes,SqlType.valueOf(sqlType.toUpperCase()),resultType);
            //map存值,后续可以通过类名.方法名获取SqlStatement
            sqlStatements.put(sqlStatementKey, sqlStatement);
        }
    }
}
