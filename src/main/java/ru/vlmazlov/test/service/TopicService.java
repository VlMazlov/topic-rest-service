package ru.vlmazlov.test.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vlmazlov.test.exceptions.InvalidFormatException;
import ru.vlmazlov.test.model.Topic;

import java.lang.IllegalStateException;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vlmazlov on 14.11.15.
 */
@Service
public class TopicService
{
    private static final String TOPICS_DIRECTORY_NAME = "topics";

    @Value("${base_dir}")
    String baseDirectoryPath;

    private Map<String, Topic> topics;

    public List<Topic> getTopics()
    {
        return topics.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public Topic getTopic(String topicName)
    {
        return topics.get(topicName);
    }

    @PostConstruct
    public void load() throws InvalidFormatException
    {
        validateBaseDirectory();
        topics = new HashMap<>();

        Path topicsDirectoryPath = Paths.get(baseDirectoryPath).resolve(TOPICS_DIRECTORY_NAME);

        DirectoryStream<Path> topicsDirectoryStream = null;
        try
        {
            topicsDirectoryStream = Files.newDirectoryStream(topicsDirectoryPath.toAbsolutePath());
        } catch (IOException exception)
        {
            throw new IllegalStateException(MessageFormat
                    .format("Unable to create directory stream, aborting. Exception {0}", exception));
        }

        for (Path topicPath : topicsDirectoryStream)
        {
            topics.put(topicPath.getFileName().toString(), new Topic(topicPath));
        }
    }

    private void validateBaseDirectory() throws InvalidFormatException
    {
        if (baseDirectoryPath == null)
        {
            throw new InvalidFormatException("No base directory path specified");
        }

        Path baseDirectory;

        try {
            baseDirectory = Paths.get(baseDirectoryPath);
        } catch (InvalidPathException exception)
        {
            throw new InvalidFormatException("Path could not be parsed, exception: " + exception);
        }

        if (Files.notExists(baseDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("Directory {0} does not exist", baseDirectoryPath));
        }

        if (!Files.isDirectory(baseDirectory))
        {
            throw new InvalidFormatException(MessageFormat.format("{0} is not a directory", baseDirectoryPath));
        }

        DirectoryStream<Path> topicsDirectoryStream;

        try
        {
            topicsDirectoryStream = Files.newDirectoryStream(baseDirectory.toAbsolutePath());
        } catch (IOException exception)
        {
            throw new IllegalStateException(MessageFormat
                    .format("Unable to create directory stream, aborting. Exception {0}", exception));
        }

        int entriesNum = 0;

        for (Path topicPath : topicsDirectoryStream)
        {
            if (! topicPath.getFileName().toString().equals(TOPICS_DIRECTORY_NAME))
            {
                throw new InvalidFormatException(MessageFormat.format("Unexpected entry {0} in the base directory",
                        topicPath.getFileName()));
            }

            if (! Files.isDirectory(topicPath))
            {
                throw new IllegalStateException(MessageFormat
                        .format("{0} should be a directory but isn't one", TOPICS_DIRECTORY_NAME));
            }

            entriesNum += 1;
        }

        if (entriesNum != 1)
        {
            throw new InvalidFormatException(MessageFormat
                    .format("Base directory contains a wrong number of entries, namely {0}", entriesNum));
        }
    }
}
