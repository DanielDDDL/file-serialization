package br.com.danieldddl.files.impl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class JsonMessagePersist implements MessagePersist {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMessagePersist.class);

    private static final String MESSAGE_FILENAME = "messages.dat";
    private static final String FILE_DIRECTORY = System.getProperty("user.home");

    private ObjectMapper objectMapper;
    private File messageFile;

    public JsonMessagePersist(ObjectMapper objectMapper) {
        this.messageFile = Utils.touchFrom(FILE_DIRECTORY, MESSAGE_FILENAME);
        this.objectMapper = objectMapper;
    }

    @Override
    public void append (Message message) {

        LOGGER.trace("appending message {} to file using json serialization method", message);

        try (FileOutputStream fileOutputStream = new FileOutputStream(messageFile, true)) {

            fileOutputStream.write(System.lineSeparator().getBytes());
            objectMapper.writeValue(fileOutputStream, message); //uses UTF-8 as charset

        } catch (IOException e) {
            throw new IllegalStateException("Error while appending object bytes serialization to file: ", e);
        }
    }

    @Override
    public List<Message> retrieveLastMessages (Integer numberOfMessages) {

        LOGGER.debug(
                "retrieving the last {} from the messages files " +
                "using json serialization method", numberOfMessages);

        return Utils.readLastLinesFromFile(messageFile, numberOfMessages, StandardCharsets.UTF_8)
                .stream()
                .map(this::jsonToMessage)
                .collect(Collectors.toList());
    }

    private Message jsonToMessage (String jsonMessage) {

        LOGGER.trace("parsing json to Message object: {}", jsonMessage);

        try {
            return objectMapper.readValue(jsonMessage, Message.class);

        } catch (JsonProcessingException e){
            String errorFormat = "Error while processing json record = %s: ";
            throw new IllegalStateException(String.format(errorFormat, jsonMessage), e);
        }
    }


}
