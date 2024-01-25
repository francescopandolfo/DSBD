package dsbd.notifier.services;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerKafka {
    
    private static String BOOTSTRAP_SERVER = "kafka:9092";
    //private static String BOOTSTRAP_SERVER = "localhost:29092"; //solo per il debug

    /* args[0] = topic
     * args[1] = message */
    public static void main(String[] args) {

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(args[0], args[1]);

        // send data - asynchronous
        producer.send(producerRecord);

        // flush data - synchronous
        producer.flush();
        // flush and close producer
        producer.close();

    }
    
}
