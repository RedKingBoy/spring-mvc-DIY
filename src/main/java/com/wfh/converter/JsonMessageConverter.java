package com.wfh.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBase;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class JsonMessageConverter implements MessageConverter {
    @Override
    public boolean support(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    public <T> T convert(HttpServletRequest req, Class<T> clazz,Class<?> genericClass) {
        T t = null;
        try {
            //获取流
            //要通过字符流来读取json格式字符串才不会乱码
            BufferedReader reader = req.getReader();
            String json = "";
            String value = "";
            while ((value=reader.readLine())!=null){
                json += value;
            }
            //对象映射器
            ObjectMapper objectMapper = new ObjectMapper();
            TypeBase typeBase = null;
            if (List.class.isAssignableFrom(clazz)){
                //对象映射器获取类型构建工厂构建对应类型和泛型的集合
               typeBase = objectMapper.getTypeFactory().constructCollectionType(List.class,genericClass);
            }else if (Set.class.isAssignableFrom(clazz)){
                typeBase = objectMapper.getTypeFactory().constructCollectionType(Set.class,genericClass);
            }
            if (typeBase!=null){
                t = objectMapper.readValue(json,typeBase);
            }else {
                //通过对象映射器将字符串转化对应的类型,自动为对象赋值
                t = objectMapper.readValue(json, clazz);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }
}
