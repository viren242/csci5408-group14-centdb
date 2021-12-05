package logGenerator.controller;

import logGenerator.constant.Constant;
import state.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public class QueryLogs
{
    State state;

    public QueryLogs()
    {
        state = new State();
    }

    private void storeLog(final String message, final Instant instant)
    {
        try (final FileWriter queryLogFileWriter = new FileWriter(Constant.Query_Log_File, true))
        {
            queryLogFileWriter.append("Timestamp");
            queryLogFileWriter.append(": ");
            queryLogFileWriter.append("").append(String.valueOf(System.currentTimeMillis()));
            queryLogFileWriter.append(" | ");
            queryLogFileWriter.append(state.getUserName());
            queryLogFileWriter.append(" | ");
            queryLogFileWriter.append(message);
            queryLogFileWriter.append(" | ");
            queryLogFileWriter.append(instant.toString());
            queryLogFileWriter.append("\n");
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
    }

    public void storeQueryLog(final String message, final Instant instant)
    {
        final File file = new File(Constant.Query_Log_File);
        if (!file.exists())
        {
            try
            {
                final boolean isFileCreated = file.createNewFile();
                if (isFileCreated)
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