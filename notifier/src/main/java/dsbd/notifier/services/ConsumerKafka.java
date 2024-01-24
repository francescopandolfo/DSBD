package dsbd.notifier.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsumerKafka {

    @Autowired
    EmailService eService;
    
    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    //private static String BOOTSTRAP_SERVER = "kafka:9092";
    private static String BOOTSTRAP_SERVER = "localhost:29092"; //per il debug

    public static void main(String[] args) {
        String topic = String.format( "%s-%s-%s", args[2],args[3],args[4] ); //nome del topic: DEVE ESSERE uguale a quello prodotto dal microservizio usersmanager
        String groupId      = args[0];
        String username     = args[1];
        String station      = args[2];
        String threshold    = args[3];
        String mintime      = args[4];
        

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // subscribe consumer to our topic(s)
        consumer.subscribe(Arrays.asList(topic));

        // poll for new data
        while(true){
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records){
                System.out.println("Topic: " + topic + ", Value: " + record.value());
                
                publishLogOnTopic(String.format("%s -> Invio notifica a \"%s\"", topic, username));
                EmailService eService = new EmailService();
                //eService.newEmail(username, topic, station, threshold, mintime);
            }
        }
    }

    public static void publishLogOnTopic(String message){
        LocalDateTime now = LocalDateTime.now();
        String[] args = new String[2];
        args[0] = "notifier_LOG";
        args[1] = String.format("%s -> %s", dtf.format(now), message);
        try{            
            ProducerKafka.main(args);
        }
        catch(Exception ex){

        }
    }

}
