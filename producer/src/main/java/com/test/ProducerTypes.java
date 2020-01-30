package com.test;

import static com.test.Producer.initializeProducerProperties;

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
public class ProducerTypes {

  private static final String TOPIC = "contract.command";

  public static void main(final String[] args) {
    final Properties props = Producer.initializeProducerProperties();

    try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props)) {
      sendWithTypes(producer);
    } catch (final SerializationException e) {
      e.printStackTrace();
    }
  }

  private static void sendWithTypes(KafkaProducer<String, GenericRecord> producer) {
    final InsuranceContract insuranceContract = new InsuranceContract();
    insuranceContract.setKind("kind value");
    Map<String, String> additionalProps = new HashMap<>();
    additionalProps.put("additional info", "additional value");
    insuranceContract.setAdditionalProperties(additionalProps);
    insuranceContract.setClassifiedIndicator(true);
    insuranceContract.setClosingDate(LocalDate.now());
    insuranceContract.setStartDate(Instant.now());
    insuranceContract.setDocumentId(UUID.randomUUID().toString());

    final InsuranceContractComposite insuranceContractComposite = new InsuranceContractComposite();
    insuranceContractComposite.setActualContracts(Collections.singletonList(insuranceContract));

    InsuranceContractCreateCommand insuranceContractCreateCommand = new InsuranceContractCreateCommand();
    insuranceContractCreateCommand.setContractType(InsuranceContractType.DRAFT_CONTRACT);
    insuranceContractCreateCommand.setInsuranceContract(insuranceContract);
    insuranceContractCreateCommand.setOperationType(InsuranceContractOperation.DRAFT);
    insuranceContractCreateCommand.setInsuranceContractComposite(insuranceContractComposite);

    final ProducerRecord<String, GenericRecord> record =
        new ProducerRecord<>(TOPIC, "test types", insuranceContractCreateCommand);
    System.out.println(String.format("Sending record: %s", record));
    producer.send(record);
    System.out.println(String.format("Record key=%s was sent", record.key()));
  }
}
