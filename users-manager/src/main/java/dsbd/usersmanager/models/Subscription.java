package dsbd.usersmanager.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull(message = "mandatory field!")
    private String username, station;

    @NotNull(message = "mandatory field!")
    private Integer threshold, mintime; //threshold espressa in mm

    public int getId(){ return id; }
    public String getUsername(){ return username; }
    public String getStation(){ return station; }
    public int getThreshold(){ return threshold; }
    public int getMintime(){ return mintime; }
}
