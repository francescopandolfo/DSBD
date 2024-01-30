package dsbd.usersmanager.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dsbd.usersmanager.UsersManagerApplication;
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

    @DeleteMapping(path = "/unsubscribe/{username}&{station}&{threshold}&{mintime}")
    public @ResponseBody String unsubscribe(@PathVariable String username, @PathVariable String station, @PathVariable String threshold, @PathVariable String mintime){
        Subscription sub = new Subscription();
        sub.setUsername(username);
        sub.setStation(station);
        sub.setThreshold(Integer.valueOf(threshold));
        sub.setMintime(Integer.valueOf(mintime));

        service.remove(sub);
        return String.format("Cancellazione da %s-%s-%s avvenuta con successo!",sub.getStation(), sub.getThreshold(), sub.getMintime());
    }
    

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<Subscription> getAll(){
        return service.getAll();
    }

    @PostMapping(path = "/kafka")   //usato per verificare l'invio dei messaggi su kafka
    public @ResponseBody void publish(@RequestBody String message){
        UsersManagerApplication.publishLogOnTopic(message);
    }

    @PostMapping(path = "/error")   //usato per simulare un errore nel programma e incrementare il contatore degli errori monitorato da Prometheus
    public @ResponseBody void throwError(@RequestBody String error){
        UsersManagerApplication.exceptionManager(new Exception(error));
    }
}
