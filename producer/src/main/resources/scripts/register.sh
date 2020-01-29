curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{ "schema": "{ \"type\": \"record\", \"name\": \"Payment\", \"namespace\": \"com.epam.edp.sr.insuance_contract\", \"fields\": [ { \"name\": \"id\", \"type\": \"string\" }, { \"name\": \"amount\", \"type\": \"double\" } ]}" }' \
http://localhost:8081/subjects/payment-value/versions