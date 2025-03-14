package code.ground.javaauthentication.controllers.restControllers;

import code.ground.javaauthentication.models.dtos.MemberLoginDTO;
import code.ground.javaauthentication.models.entities.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class HomeRestController {

    @PostMapping("/signIn")
    public Member loginPost(@RequestBody MemberLoginDTO memberLoginDTO) {
        System.out.println(memberLoginDTO.toString());
        Member member = Member.builder().build();
        return member;
    }
}
