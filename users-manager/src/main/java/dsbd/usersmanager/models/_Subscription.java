package dsbd.usersmanager.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

//classe creata con il solo scopo di far creare la tabella da JPA con la chiave esterna consumers.username 
//ma non usata per il salvataggio dei records: cercava di inserire anche nella tabella consumers quindi dava errore di Duplicato sulla reference table.

@Entity
@IdClass(_SubscriptionCompositeKey.class)
@Table (name="subscriptions")
public class _Subscription {

    @Id
    @NotNull(message = "mandatory field!")
    @ManyToOne()
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Consumer consumer;

    @Id
    @NotNull(message = "mandatory field!")
    @Column(name = "station")
    private String station;

    @Id
    @NotNull(message = "mandatory field!")
    @Column(name = "threshold")
    private Integer threshold; //threshold espressa in mm
    @Id
    @NotNull(message = "mandatory field!")
    @Column(name = "mintime")
    private Integer mintime;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    //public int getId(){ return id; }
    public String getUsername(){ return consumer.getUsername(); }
    public String getStation(){ return station; }
    public int getThreshold(){ return threshold; }
    public int getMintime(){ return mintime; }
    public int getId(){ return id; }
}
