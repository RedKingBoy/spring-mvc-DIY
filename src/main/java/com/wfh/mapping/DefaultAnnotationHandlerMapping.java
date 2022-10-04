package com.wfh.mapping;

import com.wfh.adapter.HandlerMethod;
import com.wfh.annotation.*;
import com.wfh.interceptor.InterceptorRegistryItem;
import com.wfh.ioc.IocContainer;
import com.wfh.matcher.AntRequestPathMatcher;
import com.wfh.model.HandlerExecutionChain;
import com.wfh.utils.UrlAdder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

//基于注解的方式来通过请求来找到处理器执行链
public class DefaultAnnotationHandlerMapping implements HandlerMapping {
    @Override
    public HandlerExecutionChain mapping(HttpServletRequest req) {
        //创建请求匹配器
        AntRequestPathMatcher antRequestPathMatcher = new AntRequestPathMatcher();
        //创建处理器方法,默认null
        HandlerMethod handlerMethod = null;
        //获取请求地址
        String requestURL = req.getRequestURI();
        //lambda需要常量,值不能修改,所以转换一下
        String requestURI = requestURL.replace(req.getContextPath(), "");
        //获取请求方式
        String reqMethod = req.getMethod();
        //获得扫描包的所有控制器,应该根据请求地址和请求方式来匹配相对应的控制器的方法和请求方式
        List<Object> handlers = IocContainer.getHandlers();
        loop :for (Object handler : handlers) {
            Class<?> clazz = handler.getClass();
            //判断类上面是否有RequestMapping注解,判断请求匹配地址是否有前缀
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            String prefix = UrlAdder.addUrlPrefix(requestMapping.value());
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                RequestMapping mapping = m.getAnnotation(RequestMapping.class);
                String suffix = "";
                String targetMethod = null;
                if (mapping != null) {
                    //此处是请求匹配的完整地址
//                    String url = "";
//                    if (!"".equals(prefix)) {
//                        url += prefix;
//                    }
//                    if (!url.startsWith("/")) {
//                        url = "/" + url;
//                    }
//                    String suffix = mapping.value();
//                    if (!"".equals(suffix)) {
//                        if (!suffix.startsWith("/")) {
//                            suffix = "/" + suffix;
//                        }
//                        url += suffix;
//                    }
                    suffix = UrlAdder.addUrlPrefix(mapping.value());
                    targetMethod = mapping.method();
                }
                GetMapping getMapping = m.getAnnotation(GetMapping.class);
                if (getMapping!=null){
                    suffix = UrlAdder.addUrlPrefix(getMapping.value());
                    targetMethod = "GET";
                }
                DELETEMapping deleteMapping = m.getAnnotation(DELETEMapping.class);
                if (deleteMapping!=null){
                    suffix = UrlAdder.addUrlPrefix(deleteMapping.value());
                    targetMethod = "DELETE";
                }
                PutMapping putMapping = m.getAnnotation(PutMapping.class);
                if (putMapping!=null){
                    suffix = UrlAdder.addUrlPrefix(putMapping.value());
                    targetMethod = "PUT";
                }
                PostMapping postMapping = m.getAnnotation(PostMapping.class);
                if (postMapping!=null){
                    suffix = UrlAdder.addUrlPrefix(postMapping.value());
                    targetMethod = "POST";
                }
                String url = prefix + suffix;
                //请求地址和请求方式都匹配上
                //考虑请求地址/user/admin和匹配的地址/user/{username}匹配,类似于这种请求需要使用请求匹配器
                //AntRequestPathMatcher(spring也有)
                if (antRequestPathMatcher.match(requestURI, url) && reqMethod.equals(targetMethod)) {
                    //处理器和方法已经匹配
                    handlerMethod = new HandlerMethod(handler, m);
                    //处理器执行链的处理器已找到
                    //executionChain = new HandlerExecutionChain(handlerMethod);
                    break loop;
                }
            }
        }
        HandlerExecutionChain executionChain = new HandlerExecutionChain(handlerMethod);
        //需要去ioc容器中将拦截器找出来,判断与请求地址的匹配程度
        //如果匹配放入处理器执行链
        List<InterceptorRegistryItem> interceptors = IocContainer.getInterceptors();
        //遍历拦截器
        for (InterceptorRegistryItem item:interceptors){
            //查询放行的地址
            List<String> excludeMappingPath = item.getExcludeMappingPath();
            //如果放行地址与访问地址一致就不加入处理器执行链
            if (excludeMappingPath.stream().anyMatch(path->antRequestPathMatcher.match(requestURI,path))) {
                continue;
            }
            //拦截地址
            String mappingPath = item.getMappingPath();
            if (antRequestPathMatcher.match(requestURI,mappingPath)){
                //满足请求匹配器就在处理器执行链中添加拦截器
                executionChain.addInterceptor(item.getInterceptor());
            }
        }
        return executionChain;
    }

}
