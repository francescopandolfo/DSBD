package dsbd.usersmanager.models;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ConsumerRepository extends CrudRepository <Consumer, String>{
    
    public Optional<Consumer> findByUsername(String username);

}