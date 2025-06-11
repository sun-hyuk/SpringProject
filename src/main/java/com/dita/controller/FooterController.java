package com.dita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

  @GetMapping("/footer")
  public String footerPage() {
    return "footer";  // footer.html 렌더링
  }
}
