package com.example.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/hello")
    public String helloWorld() {
        // 서버 콘솔에 찍히는 로그
        System.out.println("========= Hello World 요청 수신 =========");
        // 브라우저나 포스트맨으로 반환되는 응답
        return "helloworld";
    }
}