package dsbd.usersmanager.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@IdClass(SubscriptionCompositeKey.class)
@Table (name="subscriptions")
public class Subscription {

    @Id
    @NotNull(message = "mandatory field!")
    private String username;

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

    //public int getId(){ return id; }
    public String getUsername(){ return username; }
    public String getStation(){ return station; }
    public int getThreshold(){ return threshold; }
    public int getMintime(){ return mintime; }
}
