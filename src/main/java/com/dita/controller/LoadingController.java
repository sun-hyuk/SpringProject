package com.dita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // 이 클래스가 웹 요청을 처리하는 컨트롤러임을 나타냄
public class LoadingController {

    @GetMapping("/loading")  
    public String loading() {
        return "loading";  // templates 폴더 안에 있는 loading.html 뷰를 리턴 (렌더링)
    }
}
