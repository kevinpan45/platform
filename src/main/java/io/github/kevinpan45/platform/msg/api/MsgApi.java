package io.github.kevinpan45.platform.msg.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("msg")
public class MsgApi {
    @GetMapping("hello")
    public String getMsg() {
        return "Hello World!";
    }
}
