package code.ground.javatransaction.services;

import code.ground.javatransaction.entities.OptimisticTicket;
import code.ground.javatransaction.repositories.OptimisticTicketRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
public class OptimisticLockTest {
    @Autowired
    private OptimisticTicketRepository optimisticTicketRepository;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    public void testOptimisticLockException() {
        OptimisticTicket ticket = OptimisticTicket.builder()
                .eventName("COLDPLAY CONCWERT")
                .remainingTickets(2)
                .build();
        optimisticTicketRepository.save(ticket);

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        OptimisticTicket ticket1 = em1.find(OptimisticTicket.class, ticket.getId());
        OptimisticTicket ticket2 = em2.find(OptimisticTicket.class, ticket.getId());
        System.out.println("ticket1: " + ticket1);
        System.out.println("ticket2: " + ticket2);
        assertThat(ticket1.getVersion()).isEqualTo(0L);
        assertThat(ticket2.getVersion()).isEqualTo(0L);
        /*각각 트랜잭션 시작*/
        em1.getTransaction().begin();
        em2.getTransaction().begin();

        /*트랜잭션 1*/
        ticket1.setRemainingTickets(ticket1.getRemainingTickets() - 1);
        em1.getTransaction().commit();
        em1.close();

        /*트랜잭션 2*/
        ticket2.setRemainingTickets(ticket2.getRemainingTickets() - 1);
        try{
            em2.getTransaction().commit();
            /*jakarta.persistence.RollbackException*/
        } catch (RollbackException e) {
            if (e.getCause() instanceof OptimisticLockException) {
                assertThat(e.getCause()).isInstanceOf(OptimisticLockException.class);
                System.out.println("OptimisticLockException 발생");
            } else {
                throw e;
            }
        }
        em2.close();

        EntityManager em3 = emf.createEntityManager();
        em3.getTransaction().begin();
        OptimisticTicket ticket3 = em3.find(OptimisticTicket.class, ticket.getId());
        System.out.println("ticket3: " + ticket3);
        assertThat(ticket3.getVersion()).isEqualTo(1L);
        em3.getTransaction().commit();
        em3.close();
    }
}
