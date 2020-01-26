package br.com.danieldddl.files.impl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JsonMessagePersist implements MessagePersist {

    private static final String MESSAGE_FILENAME = "messages.dat";
    private static final String FILE_DIRECTORY = System.getProperty("user.home");

    private ObjectMapper objectMapper;
    private File messageFile;

    public JsonMessagePersist() {

        try {
            this.messageFile = Paths.get(FILE_DIRECTORY, MESSAGE_FILENAME).toFile();
            FileUtils.touch(messageFile);

            objectMapper = JsonMapper
                    .builder()
                    .addModule(new JavaTimeModule())
                    .build();

        } catch (IOException e) {
            throw new IllegalStateException("Could not create file to which messages will be appended to: ", e);
        }

    }

    @Override
    public void append (Message message) {

        try (FileOutputStream fileOutputStream = new FileOutputStream(messageFile, true)) {

            fileOutputStream.write(System.lineSeparator().getBytes());
            objectMapper.writeValue(fileOutputStream, message); //uses UTF-8 as charset

        } catch (IOException e) {
            throw new IllegalStateException("Error while appending object bytes serialization to file: ", e);
        }
    }

    @Override
    public List<Message> retrieveLastMessages (Integer numberOfMessages) {

        return FileReadingUtils.readLastLinesFromFile(messageFile, numberOfMessages)
                .stream()
                .map(this::jsonToMessage)
                .collect(Collectors.toList());

    }

    private Message jsonToMessage (String jsonMessage) {

        try {
            return objectMapper.readValue(jsonMessage, Message.class);

        } catch (JsonProcessingException e){
            String errorFormat = "Error while processing json record = %s: ";
            throw new IllegalStateException(String.format(errorFormat, jsonMessage), e);
        }
    }


}
