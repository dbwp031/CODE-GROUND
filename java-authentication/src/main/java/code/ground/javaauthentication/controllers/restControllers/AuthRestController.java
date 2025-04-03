package code.ground.javaauthentication.controllers.restControllers;

import code.ground.javaauthentication.models.dtos.JwtResponseDto;
import code.ground.javaauthentication.models.vos.LoginRequestVo;
import code.ground.javaauthentication.models.vos.SignupRequestVo;
import code.ground.javaauthentication.models.vos.TokenRefreshRequestVo;
import code.ground.javaauthentication.services.MemberService;
import code.ground.javaauthentication.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRefreshRequestVo vo) {
        String refreshToken = vo.getRefreshToken();
        if (jwtProvider.validateToken(refreshToken)) {
            Authentication authentication = jwtProvider.getAuthentication(refreshToken);
            String newAccessToken = jwtProvider.createAccessToken(authentication);

            return ResponseEntity.ok(new JwtResponseDto(newAccessToken, refreshToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token 만료");
        }
    }
}
