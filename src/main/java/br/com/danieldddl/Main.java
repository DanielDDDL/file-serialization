package br.com.danieldddl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.files.benchmarking.MessagePersistBenchMarkTest;
import br.com.danieldddl.files.impl.JsonMessagePersist;
import br.com.danieldddl.files.impl.SerializationMessagePersist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {

    public static void main(String[] args) {

        MessagePersist serializationPersistence = new SerializationMessagePersist();
        MessagePersistBenchMarkTest serializationTest = new MessagePersistBenchMarkTest(serializationPersistence, "serialization");
        serializationTest.test();

        ObjectMapper objectMapper = JsonMapper
                .builder()
                .addModule(new JavaTimeModule())
                .build();

        MessagePersist jsonPersistence = new JsonMessagePersist(objectMapper);
        MessagePersistBenchMarkTest jsonTest = new MessagePersistBenchMarkTest(jsonPersistence, "json");
        jsonTest.test();
    }

}
