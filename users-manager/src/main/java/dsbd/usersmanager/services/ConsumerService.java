package dsbd.usersmanager.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.usersmanager.models.Consumer;
import dsbd.usersmanager.models.ConsumerRepository;

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
}
