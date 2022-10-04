package com.wfh.ioc;

//bean对象的定义
public class BeanDefinition {
    //bean对象的id
    private String id;
    //bean对象的名字
    private String name;
    //bean对象(handlerMapping)
    private Object instance;
    //判断是否为handler
    private boolean handler;
    //判断是否生成代理实例
    private boolean toProxy;

    public BeanDefinition() {
    }

    public BeanDefinition(String id, String name, Object instance,boolean handler,boolean toProxy) {
        this.id = id;
        this.name = name;
        this.instance = instance;
        this.handler = handler;
        this.toProxy = toProxy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public boolean isHandler() {
        return handler;
    }

    public void setHandler(boolean handler) {
        this.handler = handler;
    }

    public boolean isToProxy() {
        return toProxy;
    }

    public void setToProxy(boolean toProxy) {
        this.toProxy = toProxy;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instance=" + instance +
                ", handler=" + handler +
                ", toProxy=" + toProxy +
                '}';
    }
}
