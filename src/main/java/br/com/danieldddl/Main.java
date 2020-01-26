package br.com.danieldddl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.files.benchmarking.MessagePersistBenchMarkTest;
import br.com.danieldddl.files.impl.JsonMessagePersist;
import br.com.danieldddl.files.impl.SerializationMessagePersist;

public class Main {

    public static void main(String[] args) {

        MessagePersist serializationPersistence = new SerializationMessagePersist();
        MessagePersistBenchMarkTest serializationTest = new MessagePersistBenchMarkTest(serializationPersistence, "serialization");
        serializationTest.test();

        MessagePersist jsonPersistence = new JsonMessagePersist();
        MessagePersistBenchMarkTest jsonTest = new MessagePersistBenchMarkTest(jsonPersistence, "json");
        jsonTest.test();
    }

}
