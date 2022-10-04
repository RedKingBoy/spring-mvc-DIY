package com.wfh.utils;

import com.wfh.annotation.*;
import com.wfh.ioc.BeanDefinition;
import com.wfh.ioc.IocContainer;
import com.wfh.proxy.CGLIBServiceInvocationHandler;
import com.wfh.proxy.JDKServiceInvocationHandler;
import com.wfh.proxy.MapperInvocationHandler;
import net.sf.cglib.proxy.Enhancer;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.List;

//包扫描器
public class PackageScanner {
    public static void scanPackage(String pk) throws Exception {
        String dir = pk.replace(".", "/");
        //获取当前线程的类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //通过类加载器获得资源路径
        URL url = classLoader.getResource(dir);
        //资源路径不能为空
        if (url != null) {
            File file = new File(url.getPath());
            //过滤出以.class结尾的文件和文件夹(endsWith)
            File[] files = file.listFiles(f -> f.getName().endsWith(".class") || f.isDirectory());
            if (files != null && files.length > 0) {
                for (File f : files) {
                    //是文件
                    if (f.isFile()) {
                        String fileName = f.getName();
                        int index = fileName.indexOf(".");
                        String fileClassName = pk + "." + fileName.substring(0, index);
                        //获取控制器类
                        Class<?> clazz = classLoader.loadClass(fileClassName);
                        createBean(clazz);
                    } else if (f.isDirectory()) {
                        //如果是文件夹就递归(新的路径需要重新new,不可使用原来的pk地址赋值因为还有未扫描的其他包)
                        String newPk = pk + "." + f.getName();
                        scanPackage(newPk);
                    }
                }
            }
        }
    }

    //这里是先创建实例,再注入值再创建代理实例,但是controller已被实例化,后创建的service代理实例无法使用
    //应该先创建实例,再注入代理实例类的值,再创建代理实例,再注入非代理实例的值
    public static void injectValue() throws IllegalAccessException {
        //为所有需要代理的类进行代理
        toProxyInstance();
        //获取所有BeanDefinition对象
        List<BeanDefinition> beanInstances = IocContainer.getBeanInstances();
        for (BeanDefinition beanInstance : beanInstances) {
            //为所有非代理类注入值
            if (!beanInstance.isToProxy()){
                injectValue(beanInstance);
            }
        }
    }

    public static void injectValue(BeanDefinition beanInstance) throws IllegalAccessException {
        //获取bean对象
        Object bean = beanInstance.getInstance();
        //获取所有定义的字段
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        if (declaredFields != null && declaredFields.length > 0) {
            for (Field field : declaredFields) {
                //判断字段上是否有AutoWired注解
                if (field.isAnnotationPresent(AutoWired.class)) {
                    //获取字段类型
                    Class<?> fieldType = field.getType();
                    //根据字段类型获取BeanDefinition实例
                    List<BeanDefinition> beanDefinitions = IocContainer.getBeanDefinitions(fieldType);
                    if (beanDefinitions.isEmpty()) {//不存在bean对象
                        throw new RuntimeException(fieldType.getName() + "实例不存在,无法注入值");
                    } else if (beanDefinitions.size() == 1) {//只有一个bean对象
                        BeanDefinition beanDefinition = beanDefinitions.get(0);
                        //获取实例
                        Object instance = beanDefinition.getInstance();
                        //设置字段访问权限为true(因为是私有)
                        field.setAccessible(true);
                        //给字段注入值
                        field.set(bean, instance);
                    } else {//有多个实例的情况下
                        //获取Qualifier注解
                        Qualifier qualifier = field.getAnnotation(Qualifier.class);
                        if (qualifier != null) {
                            //bean对象的id或name属性
                            String name = qualifier.value();
                            if ("".equals(name)) {//当bean名字为空时抛出异常,必须指定名称
                                throw new RuntimeException("Qualifier注解必须指定名称完成值的注入");
                            } else {
                                for (BeanDefinition beanDefinition : beanDefinitions) {
                                    if (name.equals(beanDefinition.getName())) {
                                        field.setAccessible(true);
                                        field.set(bean, beanDefinition.getInstance());
                                        return;
                                    }
                                }
                                throw new RuntimeException("未存在名" + name + "的bean对象");
                            }
                        } else {//Qualifier不存在
                            throw new RuntimeException(fieldType.getName() + "存在多个实例,未配置@Qualifier注解指定具体某一实例");
                        }
                    }
                } else {
                    //javax提供另一个注解Resource:注入值时先通过名称再通过类型查找
                    Resource resource = field.getAnnotation(Resource.class);
                    //判断是否有Resource注解,没有则不需要注入值
                    if (resource != null) {
                        String injectName = resource.name();
                        if (injectName.equals("")) {
                            //bean对象名称为空时,默认为字段名
                            injectName = field.getName();
                        }
                        BeanDefinition beanDefinition = IocContainer.getBeanDefinition(injectName);
                        if (beanDefinition != null) {//根据名称找到了
                            //设置访问权限
                            field.setAccessible(true);
                            //注入值
                            field.set(bean, beanDefinition.getInstance());
                        } else {
                            //当名字无法找到实例时通过类型寻找
                            Class<?> fieldType = field.getType();
                            List<BeanDefinition> beanDefinitions = IocContainer.getBeanDefinitions(fieldType);
                            if (beanDefinitions.isEmpty()) {
                                throw new RuntimeException("未找到名" + injectName + "类型为" + fieldType.getName() + "的实例,无法注入值");
                            } else if (beanDefinitions.size() == 1) {
                                //根据类型找到了
                                field.setAccessible(true);
                                field.set(bean, beanDefinitions.get(0).getInstance());
                            } else {
                                throw new RuntimeException(fieldType.getName() + "存在多个实例,无法注入值");
                            }
                        }
                    }
                }
            }
        }
    }

    //创建bean对象的方法
    private static void createBean(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        //对象id和名称,默认空
        String beanName = null;
        //是否是控制器类,默认不是
        boolean handler = false;
        //是否创建代理实例,默认不创建
        boolean isProxyInstance = false;
        //判断Controller是否有value值
        Controller c = clazz.getAnnotation(Controller.class);
        if (c != null) {
            beanName = c.value();
            handler = true;
        }
        //RestController = Controller + ResponseBody
        RestController rc = clazz.getAnnotation(RestController.class);
        if (rc != null) {
            beanName = rc.value();
            handler = true;
        }
        Service s = clazz.getAnnotation(Service.class);
        if (s != null) {
            beanName = s.value();
            isProxyInstance = true;//业务层需要创建代理实例
        }
        Component cp = clazz.getAnnotation(Component.class);
        if (cp != null) {
            beanName = cp.value();
        }
        Repository r = clazz.getAnnotation(Repository.class);
        if (r != null) {
            beanName = r.value();
        }
        //Mapper的bean对象(Mapper为接口)
        Mapper m = clazz.getAnnotation(Mapper.class);
        if (m != null) {
            beanName = m.value();
            isProxyInstance = true;
        }
        if (beanName != null) {//类上有注解的情况
            if ("".equals(beanName)) {
                beanName = clazz.getSimpleName();
            }
            if (isProxyInstance && clazz.isInterface()) {
                //获取当前线程的上下文类加载器
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                //获取需要生成代理类的接口数组
                Class<?>[] interfaces = {clazz};
                //获取代理实例
                Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, new MapperInvocationHandler());
                BeanDefinition beanDefinition = new BeanDefinition(beanName, beanName, proxyInstance, handler, false);
                IocContainer.addBeanInstance(beanDefinition);
            } else {
                //需要代理实例并且有实现类就先创建实现类的BeanDefinition对象,等注入值后再做代理
                BeanDefinition beanDefinition = new BeanDefinition(beanName, beanName, clazz.newInstance(), handler, isProxyInstance);
                IocContainer.addBeanInstance(beanDefinition);
            }
        }
    }

    private static void toProxyInstance() throws IllegalAccessException {
        //获取当前线程的上下文类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<BeanDefinition> beanInstances = IocContainer.getBeanInstances();
        for (int i = 0; i < beanInstances.size(); i++) {
            BeanDefinition beanDefinition = beanInstances.get(i);
            Object instance = beanDefinition.getInstance();
            if (beanDefinition.isToProxy()) {
                //创建代理类之前先给要代理的目标类的字段注入值
                injectValue(beanDefinition);
                //判断该类是否实现了接口,没有实现使用CGLIB动态代理,实现了使用JDK动态代理
                //此处就是实现类实例,里面的字段会因为代理类而无法注入值,应该处理这个bug
                //获取类实现的接口
                Class<?>[] ImplInterfaces = instance.getClass().getInterfaces();
                if (ImplInterfaces.length > 0) {//有接口(jdk动态代理)
                    Object proxyInstance = Proxy.newProxyInstance(classLoader, ImplInterfaces, new JDKServiceInvocationHandler(instance));
                    beanDefinition.setInstance(proxyInstance);
                } else {//没有接口
                    Enhancer enhancer = new Enhancer();
                    //设置代理类继承的父类
                    enhancer.setSuperclass(instance.getClass());
                    //设置类加载器
                    enhancer.setClassLoader(classLoader);
                    //设置回调函数
                    enhancer.setCallback(new CGLIBServiceInvocationHandler(instance));
                    Object proxy = enhancer.create();
                    beanDefinition.setInstance(proxy);
                }
            }
        }
    }
}
