package ru.vlmazlov.test.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.vlmazlov.test.exceptions.InvalidFormatException;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vlmazlov on 13.11.15.
 */
public class Timestamp implements Comparable<Timestamp>
{
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

    @JsonIgnore
    private Date date;
    @JsonProperty(value = "lastEntryTimestamp")
    private String timestampString;

    public Timestamp(String timestamp) throws InvalidFormatException
    {
        timestampString = timestamp;

        try
        {
            date = format.parse(timestamp);
        } catch (ParseException exception)
        {
            throw new InvalidFormatException(MessageFormat.format("{0} is not a valid timestamp", timestamp));
        }
    }

    @Override
    public int compareTo(Timestamp other)
    {
        return date.compareTo(other.date);
    }
}
