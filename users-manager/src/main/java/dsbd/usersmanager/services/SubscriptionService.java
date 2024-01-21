package dsbd.usersmanager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.usersmanager.models.Subscription;
import dsbd.usersmanager.models.SubscriptionRepository;

@Service
public class SubscriptionService {
    
    @Autowired
    private SubscriptionRepository repository;

    public Subscription add(Subscription sub){
        return repository.save(sub);
    }
}
