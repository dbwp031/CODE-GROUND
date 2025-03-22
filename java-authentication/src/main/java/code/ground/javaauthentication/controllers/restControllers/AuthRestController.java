package code.ground.javaauthentication.controllers.restControllers;

import code.ground.javaauthentication.models.dtos.JwtResponseDto;
import code.ground.javaauthentication.models.vos.LoginRequestVo;
import code.ground.javaauthentication.models.vos.SignupRequestVo;
import code.ground.javaauthentication.services.MemberService;
import code.ground.javaauthentication.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestVo vo) {
        return ResponseEntity.ok(memberService.signup(vo.getEmail(), vo.getPassword()));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginRequestVo vo) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(vo.getEmail(), vo.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        memberService.updateTokens(vo.getEmail(), accessToken, refreshToken);

        return ResponseEntity.ok(new JwtResponseDto(accessToken, refreshToken));
    }
}
