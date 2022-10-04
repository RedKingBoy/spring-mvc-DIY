package com.wfh.interceptor;

import java.util.List;

public class InterceptorRegistryItem {
    //拦截器
    private HandlerInterceptor interceptor;
    //拦截地址
    private String mappingPath;
    //放行的地址
    private List<String> excludeMappingPath;
    public InterceptorRegistryItem() {
    }

    public InterceptorRegistryItem(HandlerInterceptor interceptor, String mappingPath, List<String> excludeMappingPath) {
        this.interceptor = interceptor;
        this.mappingPath = mappingPath;
        this.excludeMappingPath = excludeMappingPath;
    }

    public HandlerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(HandlerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public List<String> getExcludeMappingPath() {
        return excludeMappingPath;
    }

    public void setExcludeMappingPath(List<String> excludeMappingPath) {
        this.excludeMappingPath = excludeMappingPath;
    }
}
