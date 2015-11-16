package ru.vlmazlov.test.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.vlmazlov.test.exceptions.InvalidFormatException;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

/**
 * Created by vlmazlov on 13.11.15.
 */
@Getter
public class Topic
{
    private static final String TIMESTAMPS_DIRECTORY_PATH = "history";

    private String name;
    @JsonIgnore
    private RunEntry lastEntry;

    public Topic(Path topicDirectory) throws InvalidFormatException
    {
        validateTopicPath(topicDirectory);
        name = topicDirectory.getFileName().toString();

        Path lastRunDirectoryPath = null;
        Timestamp lastRunTimestamp = null;

        try
        {
            for (Path entryPath : Files.newDirectoryStream(topicDirectory.resolve(TIMESTAMPS_DIRECTORY_PATH)
                    .toAbsolutePath()))
            {
                Timestamp entryTimestamp = new Timestamp(entryPath.getFileName().toString());

                if (lastRunDirectoryPath == null
                        || lastRunTimestamp.compareTo(entryTimestamp) < 0)
                {
                    lastRunDirectoryPath = entryPath;
                    lastRunTimestamp = entryTimestamp;
                }
            }
        } catch (IOException exception)
        {
            throw new IllegalStateException(MessageFormat.format("Unexpected io problem, exception {0}", exception));
        }

        if (lastRunDirectoryPath != null)
        {
            lastEntry = new RunEntry(lastRunDirectoryPath);
        } else
        {
            lastEntry = null;
        }
    }

    private void validateTopicPath(Path topicDirectory) throws InvalidFormatException
    {
        if (topicDirectory == null)
        {
            throw new InvalidFormatException("No topic directory specified");
        }

        if (Files.notExists(topicDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("Directory {0} does not exist", topicDirectory));
        }

        if (!Files.isDirectory(topicDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("{0} is not a directory", topicDirectory));
        }

        DirectoryStream<Path> topicDirectoryStream;

        try
        {
            topicDirectoryStream = Files.newDirectoryStream(topicDirectory.toAbsolutePath());
        } catch (IOException exception)
        {
            throw new IllegalStateException(MessageFormat
                    .format("Unable to create directory stream, aborting. Exception {0}", exception));
        }

        int entriesNum = 0;

        for (Path entry : topicDirectoryStream)
        {
            if (! entry.getFileName().toString().equals(TIMESTAMPS_DIRECTORY_PATH))
            {
                throw new InvalidFormatException(MessageFormat
                        .format("Unexpected file {0} in the entry directory", entry.getFileName().toString()));
            }

            if (! Files.isDirectory(entry))
            {
                throw new InvalidFormatException(MessageFormat.format("{0} should be a directory but isn't one",
                        TIMESTAMPS_DIRECTORY_PATH));
            }

            entriesNum += 1;
        }

        if (entriesNum != 1)
        {
            throw new InvalidFormatException(MessageFormat
                    .format("{0} contains a wrong number of entries, namely {1}", topicDirectory, entriesNum));
        }
    }
}
