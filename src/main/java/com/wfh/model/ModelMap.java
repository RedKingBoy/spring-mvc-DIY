package com.wfh.model;

import java.util.LinkedHashMap;
import java.util.Map;

/*
* 此类是用来存储视图上面的数据
* */
public class ModelMap extends LinkedHashMap<String,Object> {

    public void addAttribute(String attrName,Object attrValue){
        this.put(attrName,attrValue);
    }
    public void addAttributes(Map<String,Object> map){
        this.putAll(map);
    }
    public Object removeAttribute(String attrName){
        return this.remove(attrName);
    }
}
