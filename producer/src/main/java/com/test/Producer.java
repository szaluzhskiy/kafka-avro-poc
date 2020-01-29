package com.test;

import com.stza.hello.dto.Payment;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class Producer {

  private static final String TOPIC = "payment-object";

  public static void main(final String[] args) {

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaAvroProducerExample");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    props.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, true);

    try (KafkaProducer<String, Payment> producer = new KafkaProducer<>(props)) {
      for (long i = 0; i < 10; i++) {
        final String orderId = "id" + i;
        final Payment payment = new Payment();
        payment.setId(orderId);
        payment.setAmount(1000.00d * Math.random());
//        payment.setNewField("string 100500");
        final ProducerRecord<String, Payment> record = new ProducerRecord<>(TOPIC, orderId, payment);
        producer.send(record);
      }
      producer.flush();
      System.out.printf("Successfully produced 10 messages to a topic called %s%n", TOPIC);
    } catch (final SerializationException e) {
      e.printStackTrace();
    }
  }

}
