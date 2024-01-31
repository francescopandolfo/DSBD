package dsbd.notifier.models;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Integer> {
    //public Optional<Iterable<Notification>> findByIdIdsubscription(Integer idsubscription);
}
