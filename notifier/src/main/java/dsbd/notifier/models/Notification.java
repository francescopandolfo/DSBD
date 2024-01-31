package dsbd.notifier.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table (name="notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)    
    private Integer id;

    @NotNull
    @Column(name = "id_subscription")
    private Integer idsubscription;

    @NotNull
    private Instant opened;
    private Instant closed;
    private Boolean notified;

    public Integer getId(){ return id; }
    public Integer getIdsubscription(){ return idsubscription; };
    public Instant getOpened(){ return opened; }
    public Instant getClosed(){ return closed; }
    public Boolean getNotified(){ return notified; }
    
    public void setIdsubscription(Integer idsubscription){ this.idsubscription = idsubscription; };
    public void setOpened(Instant opened){ this.opened = opened; }
    public void setClosed(Instant closed){ this.closed = closed; }
    public void setNotified(Boolean notified){ this.notified = notified; }
}
