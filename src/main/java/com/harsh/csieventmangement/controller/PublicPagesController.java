package com.harsh.csieventmangement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Short URLs for the public pages Google Play asks for. The pages themselves
 * live in src/main/resources/static.
 */
@Controller
public class PublicPagesController {

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "forward:/privacy-policy.html";
    }

    @GetMapping("/delete-account")
    public String deleteAccount() {
        return "forward:/delete-account.html";
    }
}
