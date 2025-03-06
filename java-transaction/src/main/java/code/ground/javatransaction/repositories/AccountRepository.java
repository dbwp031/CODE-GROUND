package code.ground.javatransaction.repositories;

import code.ground.javatransaction.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // Phantom read 테스트를 위한 범위 조회 메소드
    List<Account> findByBalanceGreaterThanEqual(BigDecimal balance);

    List<Account> findAccountByBalanceBetween(BigDecimal min, BigDecimal max);
}
