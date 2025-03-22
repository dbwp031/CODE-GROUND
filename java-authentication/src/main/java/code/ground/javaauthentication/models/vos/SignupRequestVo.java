package code.ground.javaauthentication.models.vos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestVo {
    private String username;
    private String password;
    private String email;
    private String role;
}
