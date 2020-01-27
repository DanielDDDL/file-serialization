package br.com.danieldddl.files.impl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class SerializationMessagePersist implements MessagePersist {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationMessagePersist.class);
    
    private static final String MESSAGE_FILENAME = "serialized_messages.dat";
    private static final String MESSAGE_METADATA_FILENAME = "serialized_messages_metadata.dat";
    private static final String FILE_DIRECTORY = System.getProperty("user.home");
    private static final String READING_MODE = "r";

    private File messageFile;
    private File messageMetadataFile;

    private final Object filesLock = new Object();

    public SerializationMessagePersist() {
        this.messageFile = Utils.touchFrom(FILE_DIRECTORY, MESSAGE_FILENAME);
        this.messageMetadataFile = Utils.touchFrom(FILE_DIRECTORY, MESSAGE_METADATA_FILENAME);
    }

    @Override
    public void append (Message message) {

        LOGGER.trace("appending message {} to file using object serialization method", message);

        synchronized (filesLock) {

            try (FileOutputStream fileOutputStream = new FileOutputStream(messageFile, true);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

                objectOutputStream.writeObject(message);

                byte[] messageBytes = byteArrayOutputStream.toByteArray();

                long startOffset = messageFile.length();
                long endOffset = startOffset + messageBytes.length;

                //writing to both files
                writeOffsetToMetadataFile(startOffset, endOffset);
                fileOutputStream.write(messageBytes);

            } catch (IOException e) {
                throw new IllegalStateException("Error while appending object bytes serialization to file: ", e);
            }
        }
    }

    @Override
    public List<Message> retrieveLastMessages (Integer numberOfMessages) {

        LOGGER.trace(
                "retrieving the last {} from the messages files " +
                "using object serialization method", numberOfMessages);

        List<byte[]> messagesInBytes;

        synchronized (filesLock) {

            List<String> offsets = Utils.readLastLinesFromFile(messageMetadataFile, numberOfMessages, StandardCharsets.UTF_8);

            LOGGER.trace(
                    "{} offsets retrieved from metadata file, " +
                    "starting random reading process", offsets.size());

            messagesInBytes = offsets
                    .stream()
                    .map(this::messageBytesFromRandomFileUsingOffset)
                    .collect(Collectors.toUnmodifiableList());
        }

        //different stream to reduce time of the synchronization block
        return messagesInBytes
                .stream()
                .map(this::messageFromBytes)
                .collect(Collectors.toList());

    }

    private void writeOffsetToMetadataFile (long start, long finish) {

        LOGGER.trace("writing offset to metadata file: start {}, finish {}", start, finish);

        String strLineOffsetMetadata = start + " " + finish;

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             messageMetadataFile.toPath(),
                             StandardCharsets.UTF_8,
                             StandardOpenOption.APPEND)) {

            writer.write(strLineOffsetMetadata);
            writer.write(System.lineSeparator());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Message messageFromBytes (byte[] bytes) {

        LOGGER.trace("deserialization of bytes to Message object");

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return (Message) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Error while creating object from stream of bytes: ", e);
        }
    }

    private byte [] messageBytesFromRandomFileUsingOffset(String offset) {

        LOGGER.trace(
                "parsing offset in the following string in order to retrieve " +
                "bytes from random access in file: {}", offset);

        String[] offsetToken = offset.split(" ");

        int start = Integer.parseInt(offsetToken[0]);
        int end = Integer.parseInt(offsetToken[1]);
        int size = end - start;

        LOGGER.trace(
                "parsing successful, retrieving message from the " +
                "following bytes offset: start = {}, end = {}", start, end);

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(messageFile, READING_MODE)) {

            byte[] messageBytes = new byte[size];

            randomAccessFile.seek(start);
            randomAccessFile.read(messageBytes);

            return messageBytes;

        } catch (IOException e) {
            String errorFormat = "Error while reading bytes from RandomAccessFile using offset (%d,%d): ";
            throw new IllegalStateException(String.format(errorFormat, start, end), e);
        }
    }


}
