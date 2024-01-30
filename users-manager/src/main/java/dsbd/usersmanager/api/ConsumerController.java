package dsbd.usersmanager.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dsbd.usersmanager.models.Consumer;
import dsbd.usersmanager.services.ConsumerService;

@Controller
@RequestMapping(path= "/consumers")
public class ConsumerController {

    @Autowired
    ConsumerService service;
    
    @PostMapping(path= "/register")
    public @ResponseBody Consumer register(@RequestBody Consumer cons){
        return service.add(cons);
    }

    @DeleteMapping(path = "/{username}")
    public @ResponseBody String unsubscribe(@PathVariable String username){
        return service.remove(username);
    }

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<Consumer> getAll(){
        return service.getAll();
    }

    @GetMapping(path= "/{username}")
    public @ResponseBody Optional<Consumer> get(@PathVariable String username){ //usato dal notifier per ricevere l'email del consumer a cui inviare la notifica
        return service.get(username);
    }

}
