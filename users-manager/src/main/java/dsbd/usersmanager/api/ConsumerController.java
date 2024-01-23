package dsbd.usersmanager.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping(path = "/getall")
    public @ResponseBody Iterable<Consumer> getAll(){
        return service.getAll();
    }

}
