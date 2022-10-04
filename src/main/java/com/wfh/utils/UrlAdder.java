package com.wfh.utils;

public class UrlAdder {
    //此工具是处理匹配地址的,将其转化为标准格式
    public static String addUrlPrefix(String url){
        String temp = url.trim();
        if ("".equals(temp)||temp.startsWith("/"))
            return temp;
        return "/"+temp;
    }
}
