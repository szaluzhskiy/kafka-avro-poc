package com.test;

import static java.lang.String.format;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

@Slf4j
public class Consumer {

  private static final String TOPIC = "payment";

  public static void main(final String[] args) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleAvroConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
//    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true"); // необходимо, если хотим использовать
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

    try (KafkaConsumer<Long, GenericRecord> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singleton(TOPIC));
      while (true) {
        final ConsumerRecords<Long, GenericRecord> consumerRecords = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<Long, GenericRecord> consumerRecord : consumerRecords) {
          GenericRecord genericRecord = consumerRecord.value();
          System.out.println(format("Object is consumed: %s, key = %s, headers = %s",
              genericRecord, consumerRecord.key(), consumerRecord.headers()));
        }
      }
    }
  }
}
