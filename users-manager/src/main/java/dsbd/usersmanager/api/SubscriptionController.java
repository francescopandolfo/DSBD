package dsbd.usersmanager.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dsbd.usersmanager.models.Subscription;
import dsbd.usersmanager.services.SubscriptionService;

@Controller
@RequestMapping(path = "/subscriptions")
public class SubscriptionController {
    
    @Autowired
    SubscriptionService service;

    @PostMapping(path = "/register")
    public @ResponseBody Subscription register(@RequestBody Subscription sub){
        return service.add(sub);
    }

    @GetMapping(path = "/getall")
    public @ResponseBody Iterable<Subscription> getAll(){
        return service.getAll();
    }

    @PostMapping(path = "/kafka")   //usato per verificare l'invio dei messaggi su kafka
    public @ResponseBody void publish(@RequestBody String message){
        service.publishLogOnTopic(message);
    }
}
