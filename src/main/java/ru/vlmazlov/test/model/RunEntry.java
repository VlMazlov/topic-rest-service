package ru.vlmazlov.test.model;

import ru.vlmazlov.test.exceptions.InvalidFormatException;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vlmazlov on 13.11.15.
 */
public class RunEntry
{
    private static final String MESSAGE_DATA_FILENAME = "offsets.csv";

    @Getter
    private Timestamp timestamp;
    @Getter
    private List<MessageDataEntry> messageData;

    public RunEntry(Path runEntryDirectory) throws InvalidFormatException
    {
        validateRunEntryDirectory(runEntryDirectory);

        CSVParser parser;
        List<CSVRecord> csvRecords;

        Path messageDataFile = runEntryDirectory.resolve(MESSAGE_DATA_FILENAME);

        try
        {
            parser = new CSVParser(new FileReader(messageDataFile.toFile()), CSVFormat.DEFAULT);
            csvRecords = parser.getRecords();
        } catch (FileNotFoundException exception)
        {
            throw new IllegalStateException(MessageFormat
                    .format("The existence of file {0} is confirmed but reading it is impossible", messageDataFile));
        } catch (IOException exception)
        {
            throw new InvalidFormatException(MessageFormat.format("{0} is not a valid csv file", messageDataFile));
        }

        try
        {
            messageData = csvRecords.stream()
                    .map(record -> new MessageDataEntry(record.get(0), record.get(1)))
                    .collect(Collectors.toList());
        } catch (RuntimeException exception)
        {
            throw new InvalidFormatException(MessageFormat.format("Parsing {0} failed with exception {1}",
                    messageDataFile, exception));
        }

        timestamp = new Timestamp(runEntryDirectory.getFileName().toString());
    }

    public Statistics collectStatistics()
    {
        Statistics statistics = new Statistics();

        statistics.setTotalMessages(messageData.stream().mapToLong(MessageDataEntry::getMessageQuantity).sum());
        statistics.setAverageMessages(statistics.getTotalMessages() / messageData.size());
        statistics.setMinimumMessages(messageData.stream().mapToLong(MessageDataEntry::getMessageQuantity).min()
                .getAsLong());
        statistics.setMaximumMessages(messageData.stream().mapToLong(MessageDataEntry::getMessageQuantity).max()
                .getAsLong());

        return statistics;
    }

    private void validateRunEntryDirectory(Path runEntryDirectory) throws InvalidFormatException
    {
        if (runEntryDirectory == null)
        {
            throw new InvalidFormatException("No run entry directory specified");
        }

        if (Files.notExists(runEntryDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("Directory {0} does not exist", runEntryDirectory));
        }

        if (!Files.isDirectory(runEntryDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("{0} is not a directory", runEntryDirectory));
        }

        DirectoryStream<Path> runEntryDirectoryStream;

        try
        {
            runEntryDirectoryStream = Files.newDirectoryStream(runEntryDirectory.toAbsolutePath());
        } catch (IOException exception)
        {
            throw new IllegalStateException(MessageFormat
                    .format("Unable to create directory stream, aborting. Exception {0}", exception));
        }

        int entriesNum = 0;

        for (Path entry : runEntryDirectoryStream)
        {
            if (!entry.getFileName().toString().equals(MESSAGE_DATA_FILENAME))
            {
                throw new IllegalStateException(MessageFormat
                        .format("Unexpected file {0} in the entry directory", entry.getFileName().toString()));
            }

            if (!Files.isRegularFile(entry))
            {
                throw new IllegalStateException(MessageFormat.format("{0} should be a regular file but isn't one",
                        MESSAGE_DATA_FILENAME));
            }

            entriesNum += 1;
        }

        if (entriesNum != 1)
        {
            throw new InvalidFormatException(MessageFormat
                    .format("{0} contains a wrong number of entries, namely {1}", runEntryDirectory, entriesNum));
        }
    }

    public static class MessageDataEntry
    {
        @Getter
        private int partitionNumber;
        @Getter
        private long messageQuantity;

        MessageDataEntry(String partitionNumber, String messageQuantity)
        {
            this.partitionNumber = Integer.parseInt(partitionNumber);
            this.messageQuantity = Long.parseLong(messageQuantity);
        }
    }

    @Data
    public static class Statistics
    {
        private long totalMessages;
        private long minimumMessages;
        private long maximumMessages;
        private long averageMessages;
    }
}
