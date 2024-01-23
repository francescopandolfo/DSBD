package dsbd.usersmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import dsbd.usersmanager.services.ProducerKafka;
import org.springframework.context.ApplicationContext;

import dsbd.usersmanager.api.SubscriptionController;
import dsbd.usersmanager.models.Consumer;
import dsbd.usersmanager.models.Subscription;
import dsbd.usersmanager.services.ConsumerService;
import dsbd.usersmanager.services.SubscriptionService;

@SpringBootApplication
public class UsersManagerApplication {
	
	private static ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(UsersManagerApplication.class, args);
		//displayAllBeans();
		//ProducerKafka.main(args);
		SubscriptionService subS = (SubscriptionService)applicationContext.getBean("subscriptionService");
		subS.publishLogOnTopic("INVOKE thread processSubscription ... ");

		new Thread(() -> {
			subS.processSubscriptions();
		}).start();
		
	}

	public static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println("************************* " + beanName + "************************* ");
        }
    }

}
