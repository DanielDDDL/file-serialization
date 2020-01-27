package br.com.danieldddl.files.benchmarking;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.model.Message;
import br.com.danieldddl.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class MessagePersistBenchMarkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePersistBenchMarkTest.class);

    private MessagePersist messagePersist;
    private String label;

    private int [] tests = {10,100,1000,5000,10000,50000,100000};

    public MessagePersistBenchMarkTest (MessagePersist messagePersist, String label) {

        this.messagePersist = messagePersist;
        this.label = label;
    }

    public void test () {

        LOGGER.info("starting tests for {}", label);

        Arrays.stream(tests).forEach(this::testWriteMessage);
        Arrays.stream(tests).forEach(this::readMessages);
    }

    private void testWriteMessage (Integer numberOfMessages) {

        LOGGER.debug("writing test for {} messages on {}", numberOfMessages, label);

        Message message = dummyMessage();

        Long startWriting = System.nanoTime();

        for (int i = 0; i < numberOfMessages; i++) {
            messagePersist.append(message);
        }

        Long endWriting = System.nanoTime();

        LOGGER.info("{} - write::: {} messages ::: {} nanoseconds",
                label, numberOfMessages, (endWriting - startWriting));
    }

    private void readMessages (Integer numberOfMessages) {

        LOGGER.debug("reading test for {} messages on {}", numberOfMessages, label);

        Long startWriting = System.nanoTime();
        List<Message> messages = messagePersist.retrieveLastMessages(numberOfMessages);
        Long endWriting = System.nanoTime();

        LOGGER.info("{} - read::: {} messages ::: {} nanoseconds",
                label, numberOfMessages, (endWriting - startWriting));

        messages.forEach(message -> LOGGER.trace("{}", message));
    }

    private Message dummyMessage () {
        Person person = new Person("Dummy", "dummy@email.com");
        return new Message(person, "ola", LocalDateTime.now());
    }
}
