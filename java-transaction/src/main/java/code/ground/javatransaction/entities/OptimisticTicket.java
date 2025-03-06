package code.ground.javatransaction.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class OptimisticTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventName;
    private int remainingTickets;

    @Version
    private Long version;

    @Override
    public String toString() {
        return "OptimisticTicket(id=" + this.getId() + ", eventName=" + this.getEventName() + ", remainingTickets=" + this.getRemainingTickets() +
                ", version=" + this.getVersion() + ")";
    }
}
