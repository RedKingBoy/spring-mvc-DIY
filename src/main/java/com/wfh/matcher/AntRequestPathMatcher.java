package com.wfh.matcher;

//请求匹配器
public class AntRequestPathMatcher {
    /*
     * requestUrl是请求的地址
     * targetUrl方法匹配的地址
     * 考虑/user/admin和/user/{username}
     * */
    public boolean match(String requestUrl, String targetUrl) {
        //防止地址出现//的情况
        requestUrl = requestUrl.replace("//", "/");
        targetUrl = targetUrl.replace("//", "/");
        //完全匹配
        if (requestUrl.equals(targetUrl))
            return true;
        //还有/**,/**/name,/name/**,/name/**/admin的情况发生需要考虑
        if (targetUrl.equals("/**"))
            return true;
        if (targetUrl.startsWith("/**")) {
            targetUrl = targetUrl.replace("/**", "");
            return requestUrl.endsWith(targetUrl);
        }
        if (targetUrl.endsWith("/**")){
            targetUrl = targetUrl.replace("/**","");
            return requestUrl.startsWith(targetUrl);
        }
        if (targetUrl.contains("/**")){
            int index = targetUrl.indexOf("/**");
            return requestUrl.startsWith(targetUrl.substring(0,index))&&requestUrl.endsWith(targetUrl.substring(index+3));
        }
        String[] request = requestUrl.split("/");
        String[] target = targetUrl.split("/");
        //长度必须相等
        if (request.length == target.length) {
            boolean match;
            for (int i = 0; i < target.length; i++) {
                String t = target[i];
                //当{}的存在能够匹配所有内容
                if (t.matches("\\{[a-zA-Z][a-zA-Z0-9]{0,}\\}")) {
                    match = true;
                }else {
                    //没有{}情况下,如果有一个无法精确匹配就返回false
                    match = t.equals(request[i]);
                }
                if (!match){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
