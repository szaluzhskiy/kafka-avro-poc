package com.test;

import com.stza.hello.dto.Payment;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;
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

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaAvroProducerExample");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    props.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, true);

    try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props)) {
//      sendPayment(producer);
//      sendDifferentSchemas(producer);
//      sendSchemaEvolution(producer);
    } catch (final SerializationException e) {
      e.printStackTrace();
    }
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
    sendGenericRecord(producer,
        "Payment.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v1");
          genericRecord.put("amount", 1000.00d * Math.random());
        }
    );

    sendGenericRecord(producer,
        "DifferentPayment.avsc",
        genericRecord -> {
          genericRecord.put("cardNumber", "1111 2222 3333 44444");
          genericRecord.put("total", 1000.00d * Math.random());
          genericRecord.put("created", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
    );
  }

  private static void sendSchemaEvolution(KafkaProducer<String, GenericRecord> producer) {
    sendGenericRecord(producer,
        "Payment.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v1");
          genericRecord.put("amount", 1000.00d * Math.random());
        }
    );

    sendGenericRecord(producer,
        "Payment_v2.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v2");
          genericRecord.put("amount", 1000.00d * Math.random());
          genericRecord.put("newField", 100500);
        }
    );

    sendGenericRecord(producer,
        "Payment_v3.avsc",
        genericRecord -> {
          genericRecord.put("id", "payment_v3");
          genericRecord.put("amount", 1000.00d * Math.random());
          genericRecord.put("differentField", "text value");
        }
    );
  }

  private static void sendGenericRecord(KafkaProducer<String, GenericRecord> producer,
      String schemaFileName,
      Consumer<GenericRecord> initialize) {
    try {
      Schema schema = Schema.parse(Paths.get("avro-api/src/main/resources/avro", schemaFileName).toFile());
      GenericRecord genericRecord = new GenericData.Record(schema);

      initialize.accept(genericRecord);

      ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(TOPIC, schemaFileName, genericRecord);
      producer.send(record);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
