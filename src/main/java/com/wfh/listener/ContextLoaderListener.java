package com.wfh.listener;

import com.wfh.annotation.*;
import com.wfh.ioc.BeanDefinition;
import com.wfh.ioc.IocContainer;
import com.wfh.utils.PackageScanner;
import com.wfh.utils.XmlConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Method;

//上下文监听器
//因为我们自定义的监听器要在服务器启动后创建并且在servlet初始化之前执行,可以在ContextLoaderListener中进行系统初始化配置
//系统初始化配置也就是对handlerMapping进行初始化配置,在servlet初始化时能够取得
public class ContextLoaderListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        //在写框架时,可以自己定义读取配置参数的参数名
        String mvcConfig = context.getInitParameter("mvcConfig");
        //读取配置参数不为空时执行下一步操作
        if (mvcConfig != null && !"".equals(mvcConfig)) {
            //获得当前线程的类加载器
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                //通过类加载器去加载类
                Class<?> configClass = classLoader.loadClass(mvcConfig);
                //通过注解判断是否为配置类:是否能获得handlerMapping
                if (configClass.isAnnotationPresent(Configuration.class)) {
                    //获取配置类上面的ComponentScan注解
                    ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
                    if (componentScan != null) {
                        //获取要扫描的控制器
                        String[] scanController = componentScan.value();
                        //遍历包进行处理器bean对象的创建,存入ioc容器
                        for (String pk : scanController) {
                            PackageScanner.scanPackage(pk);
                        }
                        //扫描包完成后注入值
                        PackageScanner.injectValue();
                    }
                    MapperLocations mapperLocations = configClass.getAnnotation(MapperLocations.class);
                    if (mapperLocations != null) {
                        String path = mapperLocations.value();
                        if (!"".equals(path)) {
                            //解析xml文件获取sql节点
                            XmlConfiguration.parseXml(path);
                        }
                    }
                    //获取配置类对象实例,方法调用时需要
                    Object config = configClass.newInstance();
                    //获取类中定义的方法
                    Method[] methods = configClass.getDeclaredMethods();
                    //遍历方法
                    for (Method method : methods) {
                        //获取方法上的bean注解
                        Bean bean = method.getAnnotation(Bean.class);
                        if (bean != null) {
                            //调用有bean注解的方法获取bean对象
                            Object instance = method.invoke(config);
                            String value = bean.value();
                            if ("".equals(value)) {
                                //如果没有id或name就取方法名
                                value = method.getName();
                            }
                            //创建bean定义对象
                            BeanDefinition beanInstance = new BeanDefinition(value, value, instance, false, false);
                            //将bean对象添加至ioc容器
                            IocContainer.addBeanInstance(beanInstance);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
