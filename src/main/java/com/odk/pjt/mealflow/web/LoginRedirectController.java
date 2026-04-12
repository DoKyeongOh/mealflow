package com.odk.pjt.mealflow.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 백엔드 {@code /login} 으로 직접 들어온 요청을 프론트엔드 로그인 화면으로 넘깁니다.
 * OAuth 콜백 {@code /login/oauth2/**} 와는 경로가 달라 여기서 처리되지 않습니다.
 */
@Controller
public class LoginRedirectController {

    private final String frontendLoginUrl;

    public LoginRedirectController(@Value("${mealflow.frontend.login-url}") String frontendLoginUrl) {
        this.frontendLoginUrl = frontendLoginUrl;
    }

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView(frontendLoginUrl);
    }
}
