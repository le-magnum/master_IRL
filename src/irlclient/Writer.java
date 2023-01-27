package irlclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Writer
{
    private StringBuilder builder;
    private File file;

    private FileWriter fileWriter;

    public Writer(String path)
    throws IOException
    {
        this.builder = new StringBuilder();
        this.file = new File(path);
        this.fileWriter = new FileWriter(path);
        this.file.createNewFile();
    }

    public void addFileHeader(State state)
    {
        builder.append("#colors");
        builder.append(state.toColorString());
        builder.append("#goal\n");
        builder.append(state.toGoalStateString());
        builder.append("#end\n");
    }

    private void addNewEntryHeader(int entryNumber)
    {
        this.builder.append("trajectory:"+entryNumber+"=[\n");
    }

    private void addNewEntryTail()
    {
        this.builder.append("]\n");
    }

    public void addEntry(int entryNumber, String actions, State[] statesInSolution){
        addNewEntryHeader(entryNumber);
        int j = 0;
        for (String str: actions.split("\n")) {
            builder.append(statesInSolution[j]);
            builder.append(str+"\n\n");
            j++;
        }
        addNewEntryTail();
    }

    public void closeAndWrite()
    throws IOException
    {
        fileWriter.write(this.builder.toString());
        fileWriter.close();
    }

    public void addFullState(State state)
    {
        builder.append("#domain\n");
        builder.append("hospital\n");
        builder.append("#levelname\n");
        builder.append("choice\n");
        builder.append("#colors");
        builder.append(state.toColorString());
        builder.append("#initial\n");
        builder.append(state);
        builder.append("#goal\n");
        builder.append(state.toGoalStateString());
        builder.append("#end\n");
    }

}
