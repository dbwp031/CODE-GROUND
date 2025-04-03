package code.ground.javaauthentication.services;

import code.ground.javaauthentication.models.dtos.JwtResponseDto;
import code.ground.javaauthentication.models.entities.Member;
import code.ground.javaauthentication.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member signup(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        return memberRepository.save(member);
    }

    @Transactional
    public void updateTokens(String email, String accessToken, String refreshToken) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setAccessToken(accessToken);
        member.setRefreshToken(refreshToken);

        memberRepository.save(member);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }
}
