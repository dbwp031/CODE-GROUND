package code.ground.javatransaction.services;

import code.ground.javatransaction.entities.Account;
import code.ground.javatransaction.repositories.AccountRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootTest
class PhantomReadCase {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        accountRepository.flush();
    }

    @Test
    void Phantom_Read_시나리오() throws InterruptedException {
        /*REPEATABLE READ 격리 수준은 undo 영역에서 본인의 트랜잭션 번호보다 낮으며 커밋이 완료된 값만 조회한다.*/
        /*그러나, 신규 데이터가 insert 되는 경우, 이는 언두 영역에 들어가는 것이 아닌 테이블에 들어가는 것이기 때문에*/
        /*범위 검색을 하면 해당 조회 시점에 따라 읽히는 레코드의 수가 다를 수 있다.*/

        CountDownLatch beforeCommitLatch = new CountDownLatch(1);
        CountDownLatch afterCommitLatch = new CountDownLatch(1);
        Thread transactionA = new Thread(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
            txTemplate.execute(status -> {
                System.out.println("COMMIT START >>>>>>>>>>>>>>>>>>>>>>>>>>>");
                accountRepository.saveAndFlush(Account.builder().balance(BigDecimal.valueOf(1000)).build());
                accountRepository.saveAndFlush(Account.builder().balance(BigDecimal.valueOf(400)).build());
                accountRepository.saveAndFlush(Account.builder().balance(BigDecimal.valueOf(600)).build());
                accountRepository.saveAndFlush(Account.builder().balance(BigDecimal.valueOf(1200)).build());
                System.out.println("=====================================");
                beforeCommitLatch.countDown();

                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< COMMIT END");
            afterCommitLatch.countDown();
        });

        Thread transactionB = new Thread(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
            txTemplate.execute(status -> {
                try {
                    beforeCommitLatch.await();
                    List<Account> accountList = accountRepository.findAccountByBalanceBetween(BigDecimal.ZERO, BigDecimal.valueOf(900));
                    System.out.println("=====TRANSACTION B READ=====");
                    accountList.forEach(a -> {
                        System.out.println(a.toString());
                    });
                    System.out.println("=====================================");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                /*jpa에서 사용하는 영속성 컨텍스트로 인해, DB에서 entity를 가져오지 않는다.*/
                /*이를 방지하기 위하여 영속성 컨텍스트를 초기화한다.*/
                entityManager.clear();
                try {
                    afterCommitLatch.await();
                    entityManager.clear();
                    List<Account> accountList = accountRepository.findAccountByBalanceBetween(BigDecimal.ZERO, BigDecimal.valueOf(900));
                    System.out.println("=====TRANSACTION B READ=====");
                    accountList.forEach(a -> {
                        System.out.println(a.toString());
                    });
                    System.out.println("=====================================");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        transactionA.start();
        transactionB.start();

        transactionA.join();
        transactionB.join();
    }
}