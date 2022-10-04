package com.wfh.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterceptorRegistry {
    private List<InterceptorRegistryItem> items = new ArrayList<>();

    public void addInterceptor(HandlerInterceptor interceptor,String mappingPath,String... excludeMappingPath){
        items.add(new InterceptorRegistryItem(interceptor,mappingPath, Arrays.asList(excludeMappingPath)));
    }

    public List<InterceptorRegistryItem> getItems() {
        return items;
    }
}
