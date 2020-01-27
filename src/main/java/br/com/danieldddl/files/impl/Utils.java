package br.com.danieldddl.files.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils () {
        //utility class, hiding default constructor
    }

    public static List<String> readLastLinesFromFile (File file, Integer numberOfLines, Charset charset) {

        LOGGER.debug("reading the last {} lines of file {}", numberOfLines, file.getName());

        List<String> lines = new ArrayList<>();

        try (ReversedLinesFileReader reversedFileReader = new ReversedLinesFileReader(file, charset)) {

            for (String line = reversedFileReader.readLine();
                 line != null && numberOfLines > 0;
                 line = reversedFileReader.readLine(), numberOfLines--) {

                lines.add(line);
            }

            LOGGER.debug("reading finished. {} last lines retrieved from file", lines.size());
            return lines;

        } catch (IOException e) {
            String errorFormat = "Error while reverse reading the last %d records of file %s: ";
            throw new IllegalStateException(String.format(errorFormat, numberOfLines, file.getName()), e);
        }
    }

    public static File touchFrom (String fileDirectory, String filename) {

        LOGGER.debug("creating file (if it doesnt't already exist) on location {}", filename);

        try {

            File file = Paths.get(fileDirectory, filename).toFile();
            FileUtils.touch(file);

            return file;

        } catch (IOException e) {
            String errorFormat = "Error while creating file %s on specified directory, %s:";
            throw new IllegalStateException(String.format(errorFormat, filename, fileDirectory), e);
        }
    }

}
