package com.wfh.ioc;

import com.wfh.adapter.HandlerAdapter;
import com.wfh.converter.MessageConverter;
import com.wfh.interceptor.InterceptorRegistry;
import com.wfh.interceptor.InterceptorRegistryItem;
import com.wfh.mapping.HandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//ico容器,管理bean对象的容器
public class IocContainer {
    private static List<BeanDefinition> beanInstances = new ArrayList<>();

    //获取所有bean对象
    public static List<BeanDefinition> getBeanInstances() {
        return beanInstances;
    }

    //添加bean对象
    public static void addBeanInstance(BeanDefinition beanInstance) {
        //因为bean对象id和name唯一,所以需要判断集合中是否有id or name 一样的
        if (beanInstances.stream().anyMatch(beanDefinition -> beanDefinition.getId().equals(beanInstance.getId()) ||
                beanDefinition.getName().equals(beanInstance.getName()))) {
            throw new RuntimeException("存在id或name重复的bean对象");
        }
        beanInstances.add(beanInstance);
    }

    public static <T> List<T> getBeans(Class<T> clazz) {
        return beanInstances.stream()
                .filter(bean -> clazz.isAssignableFrom(bean.getInstance().getClass()))
                .map(bean -> (T) bean.getInstance())
                .collect(Collectors.toList());
    }
    public static List<BeanDefinition> getBeanDefinitions(Class<?> clazz) {
        //根据类型获取BeanDefinition
        return beanInstances.stream()
                .filter(bean -> clazz.isAssignableFrom(bean.getInstance().getClass()))
                .collect(Collectors.toList());
    }
    public static BeanDefinition getBeanDefinition(String beanName){
        //根据名称查找BeanDefinition对象,名称具有唯一性
        Optional<BeanDefinition> opt = beanInstances.stream().filter(bean -> bean.getName().equals(beanName)).findFirst();
        //option是容器只能放一个对象,该对象可以为空,不会报空指针
        //opt.orElse()函数是当容器的对象为空时返回()里面的值,否则返回容器里面的对象
        return opt.orElse(null);
    }

    //从bean对象中取handlerMapping
    public static List<HandlerMapping> getMappingList() {
        return beanInstances.stream()
                //过滤出handlerMapping的实例的bean对象,因为bean对象有很多类型
                .filter(bean -> bean.getInstance() instanceof HandlerMapping)
                //映射出handlerMapping的流(强转)
                .map(bean -> (HandlerMapping) bean.getInstance())
                //将流转化为集合
                .collect(Collectors.toList());
    }

    //从bean对象中取MessageConverter
    public static List<MessageConverter> getMessageConverter() {
        return beanInstances.stream()
                //过滤出MessageConverter的实例的bean对象,因为bean对象有很多类型
                .filter(bean -> bean.getInstance() instanceof MessageConverter)
                //映射出MessageConverter的流(强转)
                .map(bean -> (MessageConverter) bean.getInstance())
                //将流转化为集合
                .collect(Collectors.toList());
    }

    public static List<Object> getHandlers() {
        //获得bean对象的所有控制器
        return beanInstances.stream()
                .filter(BeanDefinition::isHandler)
                .map(BeanDefinition::getInstance)
                .collect(Collectors.toList());
    }

    public static List<InterceptorRegistryItem> getInterceptors() {
        Optional<BeanDefinition> opt = beanInstances.stream()
                //过滤出InterceptorRegistry实例
                .filter(bean -> bean.getInstance() instanceof InterceptorRegistry)
                //找到第一个,因为过滤的原因,此方法可能有一个值,也可能没有
                .findFirst();
        //如果有值
        if (opt.isPresent()) {
            return ((InterceptorRegistry) opt.get().getInstance()).getItems();
        }
        return new ArrayList<>();
    }

    public static List<HandlerAdapter> getAdapterList() {
        return beanInstances.stream()
                //过滤出HandlerAdapter的实例的bean对象,因为bean对象有很多类型
                .filter(bean -> bean.getInstance() instanceof HandlerAdapter)
                //映射出HandlerAdapter的流(强转)
                .map(bean -> (HandlerAdapter) bean.getInstance())
                //将流转化为集合
                .collect(Collectors.toList());
    }
}
