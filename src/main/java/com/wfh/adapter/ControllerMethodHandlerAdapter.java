package com.wfh.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfh.annotation.*;
import com.wfh.converter.MessageConverter;
import com.wfh.ioc.IocContainer;
import com.wfh.model.ModelAndView;
import com.wfh.model.ModelMap;
import com.wfh.utils.UrlAdder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ControllerMethodHandlerAdapter implements HandlerAdapter {
    @Override
    public boolean support(Class<?> clazz) {
        //此适配器只支持HandlerMethod类型
        return clazz == HandlerMethod.class;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        ModelAndView modelAndView = null;
        //强转HandlerMethod类型
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取控制器实例
        Object handlerMethodHandler = handlerMethod.getHandler();
        //获取处理方法(此方法是反射获取的可以获得反射相关的值)
        Method method = handlerMethod.getMethod();
        //获取方法的参数
        Parameter[] parameters = method.getParameters();
        //调用方法传参的值(长度与类型一样)
        Object[] paramsValue = new Object[parameters.length];
        //用i循环,因为要给参数赋值要用到下标
        for (int i = 0; i < parameters.length; i++) {
            //考虑类型的特殊情况
            Parameter parameter = parameters[i];
            //获取方法的参数类型
            Class<?> parameterType = parameter.getType();
            if (parameterType == HttpServletRequest.class) {
                paramsValue[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramsValue[i] = resp;
            } else if (parameterType == HttpSession.class) {
                paramsValue[i] = req.getSession();
            } else if (parameterType == ModelMap.class) {
                //如果参数是与视图相关的数据,那么视图就得创建出来
                modelAndView = new ModelAndView();
                modelAndView.setModelMap(new ModelMap());
                paramsValue[i] = modelAndView.getModelMap();
            } else {
                //一般情况的参数分三种:1.请求url地址后的参数2.请求地址中的参数3.请求体中
                //需要注解
                //1.请求url地址后参数
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String paramName = requestParam.value();
                    String[] parameterValues = req.getParameterValues(paramName);
                    //如果参数是集合
                    if (Collection.class.isAssignableFrom(parameterType)) {
                        //集合不做处理
                    } else {
                        try {
                            if (parameterValues!=null&&parameterValues.length==1){
                                Object parseValue = parseValue(parameterValues[0], parameterType);
                                paramsValue[i] = parseValue;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    continue;
                }
                //如果参数上有RequestBody的注解,说明参数在请求体中
                //2.请求体参数
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    Type type = null;
                    //参数类型为集合需要处理,也就是处理集合元素类型即泛型,明确泛型才能转对应集合
                    if (Collection.class.isAssignableFrom(parameterType)){//参数为集合类型
                        //获取带泛型参数的类型:如Collection<String>的类型
                        ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                        //获取参数类型中的泛型,泛型可能多个所以是数组
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        type = actualTypeArguments[0];
                    }
                    List<MessageConverter> messageConverters = IocContainer.getBeans(MessageConverter.class);
                    Object convert = null;
                    for (MessageConverter messageConverter : messageConverters) {
                        //消息转换器支持的类型,说明找到了
                        if (messageConverter.support(parameterType)) {
                            convert = messageConverter.convert(req, parameterType,(Class<?>) type);
                            break;
                        }
                    }
                    paramsValue[i] = convert;
                    continue;
                }
                //3.路径参数
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                if (pathVariable != null) {
                    //检查类上面是否有RequestMapping
                    RequestMapping requestMapping = handlerMethodHandler.getClass().getAnnotation(RequestMapping.class);
                    String prefix = "";
                    String suffix = "";
                    if (requestMapping != null) {
                        prefix = UrlAdder.addUrlPrefix(requestMapping.value());
                    }
                    //检查方法上面的匹配路径
                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    if (getMapping != null) {
                        suffix = UrlAdder.addUrlPrefix(getMapping.value());
                    }
                    PostMapping postMapping = method.getAnnotation(PostMapping.class);
                    if (postMapping != null) {
                        suffix = UrlAdder.addUrlPrefix(postMapping.value());
                    }
                    DELETEMapping deleteMapping = method.getAnnotation(DELETEMapping.class);
                    if (deleteMapping != null) {
                        suffix = UrlAdder.addUrlPrefix(deleteMapping.value());
                    }
                    PutMapping putMapping = method.getAnnotation(PutMapping.class);
                    if (putMapping != null) {
                        suffix = UrlAdder.addUrlPrefix(putMapping.value());
                    }
                    RequestMapping rm = method.getAnnotation(RequestMapping.class);
                    if (rm != null) {
                        suffix = UrlAdder.addUrlPrefix(rm.value());
                    }
                    //获取匹配路径
                    String url = prefix + suffix;
                    //获取请求路径
                    String requestUrl = req.getRequestURI().replace(req.getContextPath(), "");
                    //注解的值
                    String pathValue = pathVariable.value();
                    //获取匹配路径上变成参数的路径
                    String urlValue = "{" + pathValue + "}";
                    String[] values1 = url.split("/");
                    String[] values2 = requestUrl.split("/");
                    String param = "";
                    for (int n = 0; n < values1.length; n++) {
                        if (values1[n].equals(urlValue)) {
                            param = values2[n];
                            break;
                        }
                    }
                    try {
                        Object parseValue = parseValue(param, parameterType);
                        paramsValue[i] = parseValue;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                //此处表明参数没有任何注解
                //这种情况是转化为对象的情况
                if ("GET".equalsIgnoreCase(req.getMethod())
                        || "DELETE".equalsIgnoreCase(req.getMethod())) {
                    //获取url地址后参数map集合
                    Map<String, String[]> parameterMap = req.getParameterMap();
                    //将map转化为json格式的字符串
                    StringBuffer stringBuffer = new StringBuffer("{");
                    //取map的键值对
                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        //获取键
                        String key = entry.getKey();
                        //获取值
                        String[] value = entry.getValue();
                        //值不为空
                        if (value != null) {
                            //一个参数的值只有一个时
                            if (value.length == 1) {
                                stringBuffer.append("\"").append(key).append("\":\"").append(value[0]).append("\",");
                            }//一个参数的值有多个时
                            else if (value.length > 1) {
                                stringBuffer.append("\"").append(key).append("\":[");
                                for (String v : value) {
                                    stringBuffer.append("\"").append(v).append("\",");
                                }
                                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                                stringBuffer.append("],");
                            }
                        }
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    stringBuffer.append("}");
                    //对象映射器
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Object obj = objectMapper.readValue(stringBuffer.toString(), parameterType);
                        paramsValue[i] = obj;
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            //因为该方法返回的是ModelAndView,考虑数据如何处理
            //检查ResponseBody来确定返回数据还是视图
            Object result = method.invoke(handlerMethodHandler, paramsValue);
            if (modelAndView == null) {
                modelAndView = new ModelAndView();
            }
            //检查控制器和方法上是否有ResponseBody,RestController
            if (method.isAnnotationPresent(ResponseBody.class)
                    || handlerMethodHandler.getClass().isAnnotationPresent(ResponseBody.class)
                    || handlerMethodHandler.getClass().isAnnotationPresent(RestController.class)) {
                modelAndView.setData(result);
            }else {
                modelAndView.setViewName(result.toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    //用于解析方法参数的类型需要与匹配路径参数做转换(PathVariable)
    private static Object parseValue(String strValue, Class<?> clazz) throws ParseException {
        if (clazz == String.class) return strValue;
        if (clazz == Integer.class || clazz == int.class) return Integer.parseInt(strValue);
        if (clazz == Byte.class || clazz == byte.class) return Byte.parseByte(strValue);
        if (clazz == Short.class || clazz == short.class) return Short.parseShort(strValue);
        if (clazz == Long.class || clazz == long.class) return Long.parseLong(strValue);
        if (clazz == Double.class || clazz == double.class) return Double.parseDouble(strValue);
        if (clazz == Float.class || clazz == float.class) return Float.parseFloat(strValue);
        if (clazz == Boolean.class || clazz == boolean.class) return Boolean.parseBoolean(strValue);
        if (clazz == Character.class || clazz == char.class) {
            if (strValue.length() == 1) {
                return strValue.charAt(0);
            }
            throw new RuntimeException("cannot convert this String to char because it's length is too long");
        }
        if (Date.class.isAssignableFrom(clazz)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(strValue);
            long time = date.getTime();
            if (clazz == Date.class) {
                return date;
            }
            if (clazz == java.sql.Date.class) {
                return new java.sql.Date(time);
            }
            if (clazz == Timestamp.class) {
                return new Timestamp(time);
            }
            if (clazz == Time.class) {
                return new Time(time);
            }
        }
        if (clazz == BigDecimal.class) {
            return new BigDecimal(strValue);
        }
        return null;
    }
}
