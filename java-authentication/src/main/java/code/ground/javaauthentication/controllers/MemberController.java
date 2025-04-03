package code.ground.javaauthentication.controllers;

import code.ground.javaauthentication.models.entities.Member;
import code.ground.javaauthentication.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    @GetMapping("/me")
    public String getMyInfo(Authentication authentication, Model model) {
        String email = authentication.getName();  // JWT로부터 이메일 추출
        Member member = memberService.findByEmail(email);

        model.addAttribute("member", member);
        return "mypage";
    }
}
