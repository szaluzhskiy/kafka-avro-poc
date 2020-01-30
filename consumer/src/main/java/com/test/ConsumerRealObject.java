package com.test;

import static java.lang.String.format;

import com.stza.hello.dto.Payment;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
public class ConsumerRealObject {

  public static void main(final String[] args) {
    final Properties props = Consumer.initializeProperties();
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

    try (KafkaConsumer<Long, Payment> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singleton(Consumer.TOPIC));
      while (true) {
        final ConsumerRecords<Long, Payment> consumerRecords = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<Long, Payment> consumerRecord : consumerRecords) {
          Payment payment = consumerRecord.value();
          System.out.println(format("Object is consumed: key = %s, value = %s, schema = %s",
              consumerRecord.key(), payment, payment.getSchema().getFullName()));
        }
      }
    }
  }

}
