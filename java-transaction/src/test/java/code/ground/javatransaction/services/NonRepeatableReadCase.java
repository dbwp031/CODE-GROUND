package code.ground.javatransaction.services;

import code.ground.javatransaction.entities.Account;
import code.ground.javatransaction.repositories.AccountRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootTest
class NonRepeatableReadCase {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Test
    void Non_Repeatable_Read_시나리오() throws InterruptedException {
        /*ISOLATION READ COMMITTED는 다른 트랜잭션에서 커밋하지 않은 데이터는 읽지 않는다.*/
        /*TransactionA에서 1000 -> 500 한 후 커밋 전에는 TransactionB에서 1000으로 읽는다.*/
        /*이후 TransactionA가 커밋을 한 후, TransactionB에서 다시 읽으면 500으로 보인다.*/
        /*이렇게 한 트랜잭션 내에서 동일한 레코드를 여러 번 조회했을 때, 조회 시점에 따라 레코드의 값이 변경되는 문제를 non-repeatable read라고 한다.*/
        Account account = accountRepository.save(Account.builder().id(1L).balance(BigDecimal.valueOf(1000)).build());
        System.out.println("=====================================");
        System.out.println("NonRepeatable Read - 초기 잔액: " + account.getBalance());
        System.out.println("=====================================");

        CountDownLatch beforeCommitLatch = new CountDownLatch(1);
        CountDownLatch afterCommitLatch = new CountDownLatch(1);
        Thread transactionA = new Thread(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            txTemplate.execute(status -> {
                System.out.println("COMMIT START >>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("[TRANSACTION A] Account 잔액 변경: " + account.getBalance() + " -> 500.0");
                account.setBalance(new BigDecimal("500.0"));
                accountRepository.saveAndFlush(account);
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

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            txTemplate.execute(status -> {
                try {
                    beforeCommitLatch.await();
                    Account beforeCommitAccount = accountRepository.findById(1L).orElse(null);
                    System.out.println("[TRANSACTION B] 읽은 잔액: " + beforeCommitAccount.getBalance());
                    System.out.println("=====================================");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                /*jpa에서 사용하는 영속성 컨텍스트로 인해, DB에서 entity를 가져오지 않는다.*/
                /*이를 방지하기 위하여 영속성 컨텍스트를 초기화한다.*/
                entityManager.clear();
                try {
                    afterCommitLatch.await();
                    Account afterCommitAccount = accountRepository.findById(1L).orElse(null);
                    System.out.println("[TRANSACTION B] 읽은 잔액: " + afterCommitAccount.getBalance());
                    System.out.println( "=====================================");
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