package dsbd.usersmanager.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
//import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
//@IdClass(SubscriptionCompositeKey.class)
@Table (name="subscriptions")
public class Subscription {

    //@Id
    @NotNull(message = "mandatory field!")
    private String username;

    //@Id
    @NotNull(message = "mandatory field!")
    @Column(name = "station")
    private String station;

    //@Id
    @NotNull(message = "mandatory field!")
    @Column(name = "threshold")
    private Integer threshold; //threshold espressa in mm
    //@Id
    @NotNull(message = "mandatory field!")
    @Column(name = "mintime")
    private Integer mintime;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    //public int getId(){ return id; }
    public String getUsername(){ return username; }
    public String getStation(){ return station; }
    public int getThreshold(){ return threshold; }
    public int getMintime(){ return mintime; }
    public int getId(){ return id; }

    public void setUsername(String username){ this.username = username; }
    public void setStation(String station){ this.station = station; }
    public void setThreshold(int threshold){ this.threshold = threshold; }
    public void setMintime(int mintime){ this.mintime = mintime; }
}
