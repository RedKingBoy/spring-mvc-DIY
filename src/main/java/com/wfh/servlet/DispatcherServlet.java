package com.wfh.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfh.adapter.HandlerAdapter;
import com.wfh.ioc.IocContainer;
import com.wfh.mapping.HandlerMapping;
import com.wfh.model.HandlerExecutionChain;
import com.wfh.model.ModelAndView;
import com.wfh.model.ModelMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

//自定义的前置控制器
public class DispatcherServlet extends HttpServlet {
    //因为要分配请求,需要引进处理器映射器的集合,利用servlet的init进行初始化
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //通过监听器初始化的ioc容器取得handlerMapping集合
//        handlerMappings = IocContainer.getMappingList();
        handlerMappings = IocContainer.getBeans(HandlerMapping.class);
        //通过监听器初始化的ioc容器取得handlerAdapter集合
//        handlerAdapters = IocContainer.getAdapterList();
        handlerAdapters = IocContainer.getBeans(HandlerAdapter.class);
    }

    @Override
    //重写service方法,禁用父类service
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req,resp);
    }
    //分配请求
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        try{
            HandlerExecutionChain executionChain = getHandlerExecutionChain(req);
            if (executionChain == null || executionChain.getHandler() == null) {
                //返回404
                notFound(resp);
                return;
            }
            //处理器执行链找到后,需要适配器去适配控制器,因为控制器种类很多,很多控制器又调用不同方法处理请求,需要适配器适配控制器来帮助实现方法
            //异常对象,最终拦截需要
            Exception ex = null;
            ModelAndView modelAndView = null;
            try {
                //先执行前置拦截
                if (!executionChain.applyPreHandle(req, resp)) {//前置拦截不通过
                    return;
                }
                //处理器执行,找到匹配的适配器->利用适配器去执行处理请求
                HandlerAdapter handlerAdapter = getHandlerAdapter(executionChain.getHandler());
                if (handlerAdapter!=null){
                    modelAndView = handlerAdapter.handleRequest(req,resp,executionChain.getHandler());
                }
                //执行后置拦截
                executionChain.applyPostHandle(req, resp);
            } catch (Exception e) {
                ex = e;
            }
            //执行最终拦截
            executionChain.applyAfterComplete(req, resp, ex);
            //存在返回的视图
            if (modelAndView!=null){
                //取视图名称
                String viewName = modelAndView.getViewName();
                //视图名称存在说明返回的是视图
                if (viewName!=null){
                    String option = "/" + viewName +".jsp";
                    ModelMap modelMap = modelAndView.getModelMap();
                    if (modelMap!=null){
                        //将modelMap的数据放入请求中,因为请求数据能够转发共享
                        modelMap.forEach(req::setAttribute);
                        req.getRequestDispatcher(option).forward(req,resp);
                    }
                }else {//返回的是数据
                    Object data = modelAndView.getData();
                    ObjectMapper objectMapper = new ObjectMapper();
                    //将对象转化为json格式的字符串
                    String strData = objectMapper.writeValueAsString(data);
                    //设置返回的数据类型为json格式,必须在获得流之前设置才会生效
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter writer = resp.getWriter();
                    writer.print(strData);
                    writer.flush();
                    writer.close();
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    private HandlerExecutionChain getHandlerExecutionChain(HttpServletRequest req) throws UnsupportedEncodingException {
        req.setCharacterEncoding("utf-8");
        if (handlerMappings == null || handlerMappings.isEmpty()) {
            throw new RuntimeException("处理器映射器未配置,无法处理请求");
        }
        for (HandlerMapping mapping : handlerMappings) {
            return mapping.mapping(req);
        }
        return null;
    }
    private HandlerAdapter getHandlerAdapter(Object handler){
        if (handlerAdapters == null || handlerAdapters.isEmpty()) {
            throw new RuntimeException("处理器适配器未配置,无法处理请求");
        }
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.support(handler.getClass())) {
                return adapter;
            }
        }
        return null;
    }
    private void notFound(HttpServletResponse response) throws IOException {
        String strData = "404 not found resource";
        PrintWriter writer = response.getWriter();
        //设置返回的数据类型为json格式
        response.setContentType("text/html;charset=utf-8");
        writer.print(strData);
        writer.flush();
        writer.close();
    }
}
