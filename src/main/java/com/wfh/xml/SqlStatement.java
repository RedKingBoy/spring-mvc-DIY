package com.wfh.xml;

import org.dom4j.Element;
import org.dom4j.Node;

import javax.script.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlStatement {
    //SQL操作的类型
    private SqlType sqlType;
    //与sql有关的xml节点
    private List<Node> nodes;
    //sql执行需要的参数
    private List<Object> params = new ArrayList<>();
    //sql执行后返回的类型
    private Class<?> resultType;

    public SqlStatement(List<Node> nodes, SqlType sqlType, Class<?> resultType) {
        this.nodes = nodes;
        this.sqlType = sqlType;
        this.resultType = resultType;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public List<Object> getParams() {
        return params;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    /*
     * 根据xml节点构建sql语句,Bindings是sql判定条件需要的参数
     * */
    public String getSql(Bindings bindings) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //每次请求时会获取sql导致params的数据不断存储,所以每次都要清除
        params.clear();
        StringBuilder stringBuilder = new StringBuilder();
        for (Node node : nodes) {
            //获取节点类型
            short nodeType = node.getNodeType();
            if (nodeType == Node.TEXT_NODE) {//文本类型
                stringBuilder.append(" ").append(node.getText().trim());
            } else if (nodeType == Node.ELEMENT_NODE) {//标签类型
                //强转成标签类型
                Element element = (Element) node;
                //获取标签名
                String tagName = element.getQualifiedName();
                if ("if".equals(tagName)) {//if标签
                    //获取条件表达式
                    String condition = element.attributeValue("test");
                    if (validCondition(condition, bindings)) {//验证条件表达式
                        //表达式通过取出文本拼接即可
                        stringBuilder.append(" ").append(element.getTextTrim());
                    }
                } else if ("forEach".equals(tagName)) {//forEach标签
                    //集合名称
                    String itemsName = element.attributeValue("items");
                    //集合变量名
                    String varName = element.attributeValue("var");
                    //bindings中通过集合名称获取集合
                    Object collection = bindings.get(itemsName);
                    String open = element.attributeValue("open");
                    if (open != null && !"".equals(open)) {
                        stringBuilder.append(open);
                    }
                    //分隔符:用于分割集合每个元素的符号
                    String separate = element.attributeValue("separate");
                    String close = element.attributeValue("close");
                    //获取标签内的文本内容
                    String text = element.getTextTrim();
                    if (collection instanceof Collection) {//集合
                        for (Object obj : (Collection<?>) collection) {
                            //因为集合循环注入值的原因,不能对原文本内容进行更改,不然只更改一次就无法再处理参数了
                            String eachText = text;
                            //考虑到执行sql参数的获取,应该对集合每一个对象进行参数的处理
                            SimpleBindings simpleBindings = new SimpleBindings();
                            simpleBindings.put(varName,obj);
                            eachText = processParams("#\\{[a-zA-Z][a-zA-Z0-9\\.\\(\\)]{0,}\\}",eachText,simpleBindings);
                            eachText = processParams("\\$\\{[a-zA-Z][a-zA-Z0-9\\.\\(\\)]{0,}\\}",eachText,simpleBindings);
                            stringBuilder.append(eachText).append(separate);
                        }
                    }
                    if (separate != null && !"".equals(separate)) {
                        //删除多余的分割符
                        stringBuilder.deleteCharAt(stringBuilder.length()-1);
                    }
                    if (close != null && !"".equals(close)) {
                        stringBuilder.append(close);
                    }
                }
            }
        }
        //因为sql语句中还含有#{}和${}需要处理
        String sql = stringBuilder.toString();
        //基于#{}的正则表达式
        String regex1 = "#\\{[a-zA-Z][a-zA-Z0-9\\.\\(\\)]{0,}\\}";
        sql = processParams(regex1, sql, bindings);
        //基于${}的正则表达式,.要使用转义字符\\
        String regex2 = "\\$\\{[a-zA-Z][a-zA-Z0-9\\.\\(\\)]{0,}\\}";
        sql = processParams(regex2, sql, bindings);
        return sql;
    }

    //检查表达式是否通过,condition表示条件表达式,Bindings表示表达式中变量信息
    private static boolean validCondition(String condition, Bindings bindings) {
        String exception = condition.replace("and", "&&").replace("or", "||");
        //javax提供的脚本引擎用于检测字符串表达式的结果
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        try {
            return (boolean) scriptEngine.eval(exception, bindings);
        } catch (ScriptException e) {

        }
        return false;
    }

    private String processParams(String regex, String originalSql, Bindings bindings) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String sql = originalSql;
        //基于正则表达式的格式
        Pattern pattern = Pattern.compile(regex);
        //通过正则表达式格式去匹配字符串获取匹配器
        Matcher matcher = pattern.matcher(originalSql);
        while (matcher.find()) {//寻找匹配的字符串
            //找到后,获取匹配的字符串
            String group = matcher.group();
            if (regex.startsWith("#\\{")) {//判断为#{}替换为?,还需要记录参数
                String param = group.replace("#{", "").replace("}", "");
                Object value = getParamValue(bindings, param);
                if (value == null) {
                    throw new RuntimeException("未找到名" + param + "的参数");
                } else {
                    //记录sql执行时需要的参数
                    this.params.add(value);
                }
                sql = sql.replace(group, "?");
            } else {//${}的情况
                String param = group.replace("${", "").replace("}", "");
                Object value = getParamValue(bindings, param);
                if (value == null) {
                    throw new RuntimeException("未找到名" + param + "的参数");
                } else {
                    sql = sql.replace(group, value.toString());
                }
            }
        }
        return sql;
    }

    private Object getParamValue(Bindings bindings, String param) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object value;
        if (param.contains(".")) {//当${},#{}中含.时代表调用的是对象里面的属性
            //比如:{userDto.name},{userDto.getName()},{userDto.name.value}
            //"."要使用转义字符\\
            String[] params = param.split("\\.");
            //bindings里面存的是第一个参数
            String param1 = params[0];
            value = bindings.get(param1);
            for (int i = 1; i < params.length; i++) {
                //方法名
                String methodName;
                if (params[i].endsWith("()")) {//方法的情况
                    methodName = params[i].replace("()", "");
                } else {//属性的情况
                    methodName = "get" + params[i].substring(0, 1).toUpperCase() + params[i].substring(1);
                }
                //获取get方法
                Method method = value.getClass().getMethod(methodName);
                //调用方法获取参数值
                value = method.invoke(value);
            }
        } else {
            //从变量中获取sql操作的参数
            value = bindings.get(param);
        }
        return value;
    }
}
