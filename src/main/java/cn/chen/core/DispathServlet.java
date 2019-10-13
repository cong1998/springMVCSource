package cn.chen.core;

import cn.chen.annotation.EnjoyAutowired;
import cn.chen.annotation.EnjoyController;
import cn.chen.annotation.EnjoyRequestMapping;
import cn.chen.annotation.EnjoyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.peer.CanvasPeer;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispathServlet extends HttpServlet {

    List<String> classNames = new ArrayList<>();

    Map<String,Object> beans = new HashMap<>();

    Map<String,Method> handlerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //扫描所有的bean     扫描所有的class文件
        scanPackage("cn.chen");

        doInstance();

        doIOC();

        buildUrlMapping();  //chen/query   -->method
    }

    private void buildUrlMapping() {
        if(beans.isEmpty()){
            System.out.println("beans为空，buildUrlMapping失败");
            return ;
        }

        for(Map.Entry<String,Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if(clazz.isAnnotationPresent(EnjoyController.class)){
                EnjoyController requestMapping = clazz.getAnnotation(EnjoyController.class);
                String clazzPath = requestMapping.value();
                Method[] methods = clazz.getMethods();
                for(Method method : methods){
                    if(method.isAnnotationPresent(EnjoyRequestMapping.class)){
                        EnjoyRequestMapping methodMapping = method.getAnnotation(EnjoyRequestMapping.class);
                        String methodPath = methodMapping.value();

                        handlerMap.put(clazzPath+methodPath,method);

                    }else{
                        continue;
                    }
                }
            }else{
                continue;
            }
        }

    }

    private void doIOC() {
        if(beans.isEmpty()){
            System.out.println("bean创建失败");
            return;
        }

        for(Map.Entry<String,Object> entry:beans.entrySet()){
            Object instance = entry.getValue();

            Class<?> clazz = instance.getClass();

            Field[] fields = clazz.getDeclaredFields();

            if(clazz.isAnnotationPresent(EnjoyController.class)){
                for(Field field : fields){
                   if(field.isAnnotationPresent(EnjoyAutowired.class)){
                       EnjoyAutowired autowired = field.getAnnotation(EnjoyAutowired.class);
                       String key = autowired.value();
                       field.setAccessible(true);
                       try {
                           field.set(instance,beans.get(key));
                       } catch (IllegalAccessException e) {
                           e.printStackTrace();
                       }
                   }else{
                       continue;
                   }
                }
            }else {
                continue;
            }




        }
    }

    private void doInstance() {
        if(classNames.size()<=0){
            System.out.println("包扫描失败");
            return ;
        }

        for(String className : classNames){
            String actualClassName = className.replace(".class","");
            try {
                Class<?> clazz = Class.forName(actualClassName);
                if(clazz.isAnnotationPresent(EnjoyController.class)){
                    Object instance = clazz.newInstance();

                    EnjoyRequestMapping requestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
                    beans.put(requestMapping.value(),instance);

                }else if(clazz.isAnnotationPresent(EnjoyService.class)){
                    Object instance = clazz.newInstance();
                    EnjoyService enjoyService = clazz.getAnnotation(EnjoyService.class);

                    beans.put(enjoyService.value(),instance);
                }


            }catch (ClassNotFoundException e){

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanPackage(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] fileList = file.list();
        for(String path : fileList){
            if(path.equals("annotation") || path.equals("core")){
                continue;
            }
            File filePath = new File(fileStr+path);

            if(filePath.isDirectory()){
                scanPackage(fileStr+"."+path);
            }else{
                classNames.add(basePackage+"."+filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(handlerMap.isEmpty()){
            return ;
        }

         //获取请求路径  /chen-mvc/chen/query
        String uri = req.getRequestURI();
        System.out.println("uri"+uri);
        String contextPath = req.getContextPath();  //chen-mvc
        System.out.println("contextPath"+contextPath);
        String path = uri.replaceFirst(contextPath,"");  //   /chen/query
        System.out.println("path"+path);
        if(!handlerMap.containsKey(path)){
            resp.getWriter().write("404");
        }
        Method method = handlerMap.get(path);
        try {
            //获取方法的参数列表
            Class<?>[] parameterTypes = method.getParameterTypes();

            //获取请求的参数
            Map<String,String[]> parameterMap = req.getParameterMap();

            //保存参数值
            Object[] paramValues = new Object[parameterTypes.length];
            for(int i=0;i<paramValues.length;i++){
                String requestParam = parameterTypes[i].getSimpleName();
                if(requestParam.equals("httpServletRequest") ){
                    paramValues[i]=req;
                    continue;
                }
                if(requestParam.equals("httpServletResponse") ){
                    paramValues[i]=resp;
                    continue;
                }
                if(requestParam.equals("String")){
                    for(Map.Entry<String,String[]> param : parameterMap.entrySet()){
                        String value = Arrays.toString(param.getValue()).replaceAll("\\[\\]","");//.replaceAll()
                        paramValues[i]=value;
                    }
                }
            }

            method.invoke(this.handlerMap.get(path),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
