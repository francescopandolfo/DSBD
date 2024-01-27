package dsbd.usersmanager.models;

import java.io.Serializable;

public class _SubscriptionCompositeKey implements Serializable {
    private Consumer consumer;
    private String station;
    private Integer threshold;
    private Integer mintime;
}
