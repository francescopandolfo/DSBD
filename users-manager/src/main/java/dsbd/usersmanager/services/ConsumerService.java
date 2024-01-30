package dsbd.usersmanager.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.usersmanager.UsersManagerApplication;
import dsbd.usersmanager.models.Consumer;
import dsbd.usersmanager.models.ConsumerRepository;
import dsbd.usersmanager.models.Subscription;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepository repository;
    
    public Consumer add(Consumer cons){
        return repository.save(cons);
    }

    public Iterable<Consumer> getAll(){
        return repository.findAll();
    }

    public Optional<Consumer> get(String username){
        return repository.findByUsername(username);
    }

    public String remove(String username){
        
		SubscriptionService sub_serv = (SubscriptionService)UsersManagerApplication.applicationContext.getBean("subscriptionService");

        Iterable<Subscription> subs = sub_serv.getAll();
        for(Subscription x : subs) if( x.getUsername().equals(username) ){
            return "ATTENZIONE: esistono sottoscrizioni ancora attive per l'utente che si desidera cancellare";
        }

        for(Consumer x : getAll()) if(x.getUsername().equals(username)) {
            repository.delete(x);
            UsersManagerApplication.publishLogOnTopic(String.format("Utente %s cancellato con successo!", x.getUsername())); 
            return String.format("Utente %s cancellato con successo!", x.getUsername());
        }

        return "ERRORE: Utente non trovato";
    }
}
