package logGenerator.controller;

import logGenerator.constant.Constant;
import state.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public class GeneralLogs
{
    State state;

    public GeneralLogs()
    {
        state = new State();
    }

    private void storeLog(final String message, final Instant instant)
    {
        try (final FileWriter generalLogFileWriter = new FileWriter(Constant.General_Log_File, true))
        {
            generalLogFileWriter.append("Timestamp");
            generalLogFileWriter.append(": ");
            generalLogFileWriter.append("").append(String.valueOf(System.currentTimeMillis()));
            generalLogFileWriter.append(" | ");
            generalLogFileWriter.append(state.getUserName());
            generalLogFileWriter.append(" | ");
            generalLogFileWriter.append(message);
            generalLogFileWriter.append(" | ");
            generalLogFileWriter.append(instant.toString());
            generalLogFileWriter.append("\n");
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
    }

    public void storeQueryLog(final String message, final Instant instant) {
        final File file = new File(Constant.General_Log_File);
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