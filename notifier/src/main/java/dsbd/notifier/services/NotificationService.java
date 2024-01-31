package dsbd.notifier.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.notifier.models.Notification;
import dsbd.notifier.models.NotificationRepository;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository repository;

    //apre una nuova notifica solo se non ne esiste una già aperta per la stessa sottoscrizione
    public boolean add(Integer idsubscription){
        boolean trovataNotificaAperta = false;
        //for(Notification x : repository.findByIdIdsubscription(idsubscription).get()){
        for( Notification x : repository.findAll() ){
            if(x.getIdsubscription().equals(idsubscription)){
                if(x.getClosed() == null ){ //trovata nessuna notifica ancora aperta
                    trovataNotificaAperta = true;
                    break;   
                }
            }
        }
        if(trovataNotificaAperta) return false;
        
        Notification nuovaNotifica = new Notification();
        nuovaNotifica.setIdsubscription(idsubscription);
        nuovaNotifica.setOpened(Instant.now());
        repository.save(nuovaNotifica);
        return true;
    }

    //chiude una notifica aperta precedentemente
    public Notification close(Integer idsubscription){
        //for(Notification x : repository.findByIdIdsubscription(idsubscription).get()){
        for( Notification x : repository.findAll() ){
            if(x.getIdsubscription().equals(idsubscription)){
                if(x.getClosed() == null){ //questa è la notifica non ancora chiusa
                    x.setClosed(Instant.now());
                    repository.save(x);
                    return x;
                }
            }
        }
        return null;
    }
}
