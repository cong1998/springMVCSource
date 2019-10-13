package cn.chen.controller;


import cn.chen.annotation.EnjoyAutowired;
import cn.chen.annotation.EnjoyController;
import cn.chen.annotation.EnjoyRequestMapping;
import cn.chen.annotation.EnjoyRequestParam;
import cn.chen.service.ChenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@EnjoyController
@EnjoyRequestMapping("/chen")
public class ChenController {

    @EnjoyAutowired("ChenServiceImpl")
    private ChenService service;


    @EnjoyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @EnjoyRequestParam("name") String name,@EnjoyRequestParam("age") String age){
        try {
            PrintWriter printWriter = response.getWriter();
            String result = service.query(name,Integer.parseInt(age));
            printWriter.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
