package cn.chen.service.serviceImpl;

import cn.chen.annotation.EnjoyService;
import cn.chen.service.ChenService;

@EnjoyService("ChenServiceImpl")
public class ChenServiceImpl implements ChenService {

    @Override
    public String query(String name, Integer age) {
        return "name=="+name+"\nage=="+age;
    }
}
