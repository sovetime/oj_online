package org.example.ojfriend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Scanner;

@SpringBootApplication
@MapperScan("org.example.ojfriend.**.mapper")
@EnableFeignClients(basePackages = {"org.example.api"})
public class OjFriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjFriendApplication.class, args);
        System.out.println();
    }

}
