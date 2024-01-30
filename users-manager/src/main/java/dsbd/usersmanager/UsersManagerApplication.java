package dsbd.usersmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import dsbd.usersmanager.services.ProducerKafka;
import dsbd.usersmanager.services.SubscriptionService;

@SpringBootApplication
public class UsersManagerApplication {

	
	public static boolean debug = false;
	
	@Autowired
	public static MeterRegistry registry;

	public static ApplicationContext applicationContext;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
	
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(UsersManagerApplication.class, args);

		SubscriptionService subS = (SubscriptionService)applicationContext.getBean("subscriptionService");
		//subS.publishLogOnTopic("INVOKE thread processSubscription ... ");
		
		new Thread(() -> {
			subS.startProcessingSubscriptions();
		}).start();
	
	}
/*
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}
*/	

	@Bean
    InitializingBean forcePrometheusPostProcessor(BeanPostProcessor meterRegistryPostProcessor, MeterRegistry registry) {
		UsersManagerApplication.registry = registry;
        return () -> meterRegistryPostProcessor.postProcessAfterInitialization(registry, "");
    }
	

	public static void exceptionManager(Exception ex){
		Timer timer = Timer.builder("UsersManagerApplication").tag("method", "exceptionManager").register(registry);
                    timer.record( () -> publishLogOnTopic(String.format("ERRORE su UsersManagerApplication: %s", ex.getMessage()))
					);
    }

	public static void publishLogOnTopic(String message){
        LocalDateTime now = LocalDateTime.now();
        String[] args = new String[2];
        args[0] = "usersmanager_LOG";
        args[1] = String.format("%s -> %s", dtf.format(now), message);
        try{            
            ProducerKafka.main(args);
        }
        catch(Exception ex){ exceptionManager(ex); }
    }
}
