package fi.vm.sade.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/redirect")
public class RedirectController {

    @GetMapping
    public String redirect(@RequestParam String to) {
        return "redirect:" + to;
    }

}
