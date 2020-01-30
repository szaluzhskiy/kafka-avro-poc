package com.test;

import java.util.ArrayList;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class AvroSchemaBuilder {

  /**
   * Can generate AVRO schema in JSON format from builder.
   */
  public static void main(String[] args) {
    Schema schema = SchemaBuilder.record("Contract")
        .namespace("com.test")
        .fields()
        .optionalLong("identifier")
        .name("participants")
        .type()
        .nullable()
        .array()
        .items()
        .stringType()
        .arrayDefault(new ArrayList<>())
        .name("id")
        .type(LogicalTypes.uuid().addToSchema(Schema.create(Schema.Type.STRING)))
        .noDefault()
        .endRecord();
    System.out.println(String.format("Generated schema: %s", schema));
  }
}
