package com.test;

import com.test.dto.Payment;
import com.test.dto.insurance.contract.model.InsuranceContract;
import com.test.dto.insurance.contract.model.InsuranceContractComposite;
import com.test.dto.insurance.contract.model.InsuranceContractCreateCommand;
import com.test.dto.insurance.contract.model.InsuranceContractOperation;
import com.test.dto.insurance.contract.model.InsuranceContractType;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class Producer {

  private static final String TOPIC = "payment";

  public static void main(final String[] args) {
    final Properties props = initializeProducerProperties();

    try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props)) {
//      sendPayment(producer);
//      sendDifferentSchemas(producer);
      sendSchemaEvolution(producer);
    } catch (final SerializationException e) {
      e.printStackTrace();
    }
  }

  public static Properties initializeProducerProperties() {
    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9093");
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaAvroProducerExample");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    props.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
    return props;
  }

  private static void sendPayment(KafkaProducer<String, GenericRecord> producer) {
    for (long i = 0; i < 10; i++) {
      final String orderId = "id" + i;
      final Payment payment = new Payment();
      payment.setId(orderId);
      payment.setAmount(1000.00d * Math.random());
      final ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(TOPIC, orderId, payment);
      producer.send(record);
    }
    producer.flush();
    System.out.printf("Successfully produced 10 messages to a topic called %s%n", TOPIC);
  }

  private static void sendDifferentSchemas(KafkaProducer<String, GenericRecord> producer) {
    String topic = "card";
    sendGenericRecord(producer,
        topic,
        "Card.avsc",
        genericRecord -> {
          genericRecord.put("number", "payment_v1");
          genericRecord.put("balance", 1000L);
          genericRecord.put("expirationDate", 2981);
        }
    );

    sendGenericRecord(producer,
        topic,
        "DifferentCard.avsc",
        genericRecord -> {
          genericRecord.put("cardNumber", "1111 2222 3333 44444");
          genericRecord.put("total", 1000.00d * Math.random());
          genericRecord.put("created", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
    );
  }

  private static void sendSchemaEvolution(KafkaProducer<String, GenericRecord> producer) {
    sendGenericRecord(producer,
        TOPIC,
        "Payment.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v1");
          genericRecord.put("amount", 1000.00d * Math.random());
        }
    );

    sendGenericRecord(producer,
        TOPIC,
        "Payment_v2.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v2");
          genericRecord.put("amount", 1000.00d * Math.random());
          genericRecord.put("newField", 100500);
        }
    );

    sendGenericRecord(producer,
        TOPIC,
        "Payment_v3.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v3");
          genericRecord.put("amount", 1000.00d * Math.random());
          genericRecord.put("differentField", "text value");
        }
    );

    sendGenericRecord(producer,
        TOPIC,
        "Payment_v4.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v3");
          genericRecord.put("amount", "1000.00d * Math.random()");
          genericRecord.put("differentField", "text value");
        }
    );
  }

  private static void sendGenericRecord(KafkaProducer<String, GenericRecord> producer,
      String topic,
      String schemaFileName,
      Consumer<GenericRecord> initialize) {
    try {
      Schema schema = Schema.parse(Paths.get("avro-api/src/main/resources/avro", schemaFileName).toFile());
      GenericRecord genericRecord = new GenericData.Record(schema);

      initialize.accept(genericRecord);

      ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, schemaFileName, genericRecord);
      System.out.println(String.format("Sending record: %s", record));
      producer.send(record);
      System.out.println(String.format("Record key=%s was sent", record.key()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
