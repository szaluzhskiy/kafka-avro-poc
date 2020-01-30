package com.test;

import com.test.dto.insurance.contract.model.InsuranceContractCreateCommand;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerTypes {

  public static void main(final String[] args) {
    final Properties props = Consumer.initializeProperties();
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

    try (KafkaConsumer<Long, InsuranceContractCreateCommand> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singleton("contract.command"));
      while (true) {
        final ConsumerRecords<Long, InsuranceContractCreateCommand> consumerRecords = consumer
            .poll(Duration.ofSeconds(10));
        for (ConsumerRecord<Long, InsuranceContractCreateCommand> consumerRecord : consumerRecords) {
          InsuranceContractCreateCommand createCommand = consumerRecord.value();
          System.out.println(String.format("Object is consumed: key = %s, value = %s, schema = %s",
              consumerRecord.key(), createCommand, createCommand.getSchema().getFullName()));
        }
      }
    }
  }
}
