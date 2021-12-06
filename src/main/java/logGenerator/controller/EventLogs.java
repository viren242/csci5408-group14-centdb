package logGenerator.controller;

import logGenerator.constant.Constant;
import state.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public class EventLogs
{
    State state;

    public EventLogs()
    {
        state = new State();
    }

    private void storeLog(final String message, final Instant instant)
    {
        try (final FileWriter eventLogFileWriter = new FileWriter(Constant.Event_Log_File, true))
        {
            eventLogFileWriter.append("Timestamp");
            eventLogFileWriter.append(": ");
            eventLogFileWriter.append("").append(String.valueOf(System.currentTimeMillis()));
            eventLogFileWriter.append(" | ");
            eventLogFileWriter.append(state.getUserName());
            eventLogFileWriter.append(" | ");
            eventLogFileWriter.append(message);
            eventLogFileWriter.append(" | ");
            eventLogFileWriter.append(instant.toString());
            eventLogFileWriter.append("\n");
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
    }

    public void storeQueryLog(final String message, final Instant instant)
    {
        final File file = new File(Constant.Event_Log_File);
        if(!file.exists())
        {
            try
            {
                final boolean isFileCreated = file.createNewFile();
                if(isFileCreated)
                {
                    storeLog(message, instant);
                }
            }
            catch(final IOException e)
            {
                e.printStackTrace();
            }
        }
        storeLog(message, instant);
    }
}