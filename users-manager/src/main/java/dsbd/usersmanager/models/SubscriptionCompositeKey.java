package dsbd.usersmanager.models;

import java.io.Serializable;
import java.util.Set;

public class SubscriptionCompositeKey implements Serializable {
    private String username;
    private String station;
    private Integer threshold;
    private Integer mintime;
}
