package code.ground.javaauthentication.repositories;

import code.ground.javaauthentication.models.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
