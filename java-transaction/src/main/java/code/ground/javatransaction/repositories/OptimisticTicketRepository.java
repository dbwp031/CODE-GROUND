package code.ground.javatransaction.repositories;

import code.ground.javatransaction.entities.OptimisticTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptimisticTicketRepository extends JpaRepository<OptimisticTicket, Long> {
}
