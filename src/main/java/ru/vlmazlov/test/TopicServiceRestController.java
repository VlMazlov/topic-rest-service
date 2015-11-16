package ru.vlmazlov.test;

import lombok.AllArgsConstructor;
import ru.vlmazlov.test.exceptions.BadRequestException;
import ru.vlmazlov.test.model.RunEntry;
import ru.vlmazlov.test.model.Timestamp;
import ru.vlmazlov.test.model.Topic;
import ru.vlmazlov.test.service.TopicService;
import ru.vlmazlov.test.exceptions.NotFoundException;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by vlmazlov on 14.11.15.
 */
@RestController
@RequestMapping(value = "/topics")
public class TopicServiceRestController
{
    @Autowired
    TopicService topicService;

    @RequestMapping(value = "", method = GET)
    public List<Topic> getTopics()
    {
        return topicService.getTopics();
    }

    @RequestMapping(value = "/{topicName}/lastrun/timestamp", method = GET)
    public Timestamp getLastRunTimestamp(@PathVariable(value = "topicName") String topicName)
    {
        validateTopicExistence(topicName);
        validateLastRunExistence(topicName);
        return topicService.getTopic(topicName).getLastEntry().getTimestamp();
    }

    @RequestMapping(value = "/{topicName}/lastrun/stats", method = GET)
    public RunEntry.Statistics getLastRunStatistics(@PathVariable(value = "topicName") String topicName)
    {
        validateTopicExistence(topicName);
        validateLastRunExistence(topicName);
        return topicService.getTopic(topicName).getLastEntry().collectStatistics();
    }

    @RequestMapping(value = "/{topicName}/lastrun/partitions")
    public List<RunEntry.MessageDataEntry> getMessagesByPartition(@PathVariable(value = "topicName") String topicName)
    {
        validateTopicExistence(topicName);
        validateLastRunExistence(topicName);
        return topicService.getTopic(topicName).getLastEntry().getMessageData();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody String handleBadRequest(HttpServletRequest req, BadRequestException ex) {
        return MessageFormat.format("400 BAD REQUEST: {0}", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody String handleNotFound(NotFoundException ex) {
        return MessageFormat.format("404 NOT FOUND: {0}", ex.getMessage());
    }

    private void validateLastRunExistence(String topicName)
    {
        if (topicService.getTopic(topicName).getLastEntry() == null)
        {
            throw new BadRequestException(MessageFormat.format("There are no previous runs for topic {0}", topicName));
        }
    }

    private void validateTopicExistence(String topicName)
    {
        if (topicService.getTopic(topicName) == null)
        {
            throw new NotFoundException(MessageFormat.format("Topic {0} not found", topicName));
        }
    }

    @AllArgsConstructor
    private static class ErrorInfo{
        public final String url;
        public final String ex;

        public ErrorInfo(String url, Exception ex) {
            this.url = url;
            this.ex = ex.getLocalizedMessage();
        }
    }
}
