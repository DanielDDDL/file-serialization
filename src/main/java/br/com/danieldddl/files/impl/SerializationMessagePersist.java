package br.com.danieldddl.files.impl;

import br.com.danieldddl.files.MessagePersist;
import br.com.danieldddl.model.Message;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class SerializationMessagePersist implements MessagePersist {

    private static final String MESSAGE_FILENAME = "serialized_messages.dat";
    private static final String MESSAGE_METADATA_FILENAME = "serialized_messages_metadata.dat";
    private static final String FILE_DIRECTORY = System.getProperty("user.home");
    private static final String READING_MODE = "r";

    private File messageFile;
    private File messageMetadataFile;

    private final Object filesLock = new Object();

    public SerializationMessagePersist() {

        try {
            this.messageFile = touchFrom(MESSAGE_FILENAME);
            this.messageMetadataFile = touchFrom(MESSAGE_METADATA_FILENAME);

        } catch (IOException e) {
            throw new IllegalStateException("Could not create file to which messages will be appended to: ", e);
        }

    }

    private File touchFrom(String filePath) throws IOException {

        File file = Paths.get(FILE_DIRECTORY, filePath).toFile();
        FileUtils.touch(file);

        return file;
    }

    @Override
    public void append(Message message) {

        //write to both files


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

    @Override
    public List<Message> retrieveLastMessages(Integer numberOfMessages) {

        List<Message> messagesRetrieved = new ArrayList<>();

        List<String> offsets = FileReadingUtils.readLastLinesFromFile(messageMetadataFile, numberOfMessages);

        for (String offset : offsets) {

            String[] offsetToken = offset.split(" ");
            int start = Integer.parseInt(offsetToken[0]);
            int end = Integer.parseInt(offsetToken[1]);

            byte[] messageBytes = bytesFromMessageFileInOffset(start, end);
            messagesRetrieved.add(messageFromBytes(messageBytes));
        }


        return messagesRetrieved;
    }

    private void writeOffsetToMetadataFile(long start, long finish) {

        String strLineOffsetMetadata = start + " " + finish;

        try (BufferedWriter writer = Files.newBufferedWriter(messageMetadataFile.toPath(), StandardOpenOption.APPEND)) {

            writer.write(strLineOffsetMetadata);
            writer.write(System.lineSeparator());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Message messageFromBytes(byte[] bytes) {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return (Message) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Error while creating object from stream of bytes: ", e);
        }
    }

    private byte[] bytesFromMessageFileInOffset(int startOffset, int finishOffset) {

        int size = finishOffset - startOffset;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(messageFile, READING_MODE)) {

            byte[] messageBytes = new byte[size];

            randomAccessFile.seek(startOffset);
            randomAccessFile.read(messageBytes);

            return messageBytes;

        } catch (IOException e) {
            String errorFormat = "Error while reading bytes from RandomAccessFile using offset (%d,%d): ";
            throw new IllegalStateException(String.format(errorFormat, startOffset, finishOffset), e);
        }

    }


}
