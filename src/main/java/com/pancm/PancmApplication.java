package com.pancm;

import com.pancm.storm.TopologyApp;
import com.pancm.util.GetSpringBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PancmApplication {

    public static void main(String[] args) {
        //先启动Storm，后启动springboot的工程
        TopologyApp app = new TopologyApp();
        app.runStorm(args);
        // 启动嵌入式的 Tomcat 并初始化 Spring 环境及其各 Spring 组件
        ConfigurableApplicationContext context = SpringApplication.run(PancmApplication.class, args);
        GetSpringBean springBean=new GetSpringBean();
        springBean.setApplicationContext(context);
    }

}
