package code.ground.javatransaction.services;

import code.ground.javatransaction.entities.Account;
import code.ground.javatransaction.repositories.AccountRepository;
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
class DirtyReadCase {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void Dirty_Read_시나리오() throws InterruptedException {
        Account account = accountRepository.save(Account.builder().balance(BigDecimal.valueOf(1000)).build());

        CountDownLatch latch = new CountDownLatch(1);

        Thread transactionB = new Thread(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
            txTemplate.execute(status -> {
                account.setBalance(new BigDecimal("500.0"));
                accountRepository.saveAndFlush(account);
                // 변경 후 롤백
                status.setRollbackOnly();

                latch.countDown();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            Account accountAfter = accountRepository.save(Account.builder().balance(BigDecimal.valueOf(1000)).build());
            System.out.println("롤백 후 금액: " + accountAfter.getBalance());
        });

        Thread transactionA = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
            txTemplate.execute(status -> {
                Account a = accountRepository.findById(account.getId()).orElse(null);
                System.out.println("Dirty Read - 읽은 잔액: " + a.getBalance());
                return null;
            });
        });

        transactionB.start();
        transactionA.start();

        transactionB.join();
        transactionA.join();
    }


    @Test
    void Dirty_Read_시나리오_정상케이스() throws InterruptedException {
        Account account = accountRepository.save(Account.builder().balance(BigDecimal.valueOf(1000)).build());

        CountDownLatch latch = new CountDownLatch(1);

        Thread transactionB = new Thread(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            txTemplate.execute(status -> {
                account.setBalance(new BigDecimal("500.0"));
                accountRepository.save(account);
                // 변경 후 롤백
                status.setRollbackOnly();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
                return null;
            });
        });

        Thread transactionA = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            txTemplate.execute(status -> {
                Account a = accountRepository.findById(account.getId()).orElse(null);
                System.out.println("Dirty Read - 읽은 잔액: " + a.getBalance());
                return null;
            });
        });

        transactionB.start();
        transactionA.start();

        transactionB.join();
        transactionA.join();
    }
}