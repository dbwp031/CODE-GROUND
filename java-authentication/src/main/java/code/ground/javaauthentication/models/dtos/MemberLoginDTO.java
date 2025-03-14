package code.ground.javaauthentication.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginDTO {
    private String userId;
    private String password;

    @Override
    public String toString() {
        return "MemberLoginDTO{" +
                "userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
