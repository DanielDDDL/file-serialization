package br.com.danieldddl.files.impl;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileReadingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileReadingUtils.class);

    public static List<String> readLastLinesFromFile (File file, Integer numberOfLines) {

        LOGGER.debug("reading last lines of file");

        List<String> lines = new ArrayList<>();

        try (ReversedLinesFileReader reversedFileReader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {

            for (int i = 0; i < numberOfLines; i++) {

                Optional<String> possibleLine = Optional.ofNullable(reversedFileReader.readLine());
                if (possibleLine.isEmpty()) {
                    break;
                }

                lines.add(possibleLine.get());
            }

            return lines;

        } catch (IOException e) {
            String errorFormat = "Error while reverse reading the last %d records of file %s: ";
            throw new IllegalStateException(String.format(errorFormat, numberOfLines, file.getName()), e);
        }
    }

}
