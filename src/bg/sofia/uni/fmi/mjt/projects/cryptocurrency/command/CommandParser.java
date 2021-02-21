package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.command;

import java.util.Arrays;

public class CommandParser {

    public static Command newCommand(String clientMsg) {

        String[] splitCommand = clientMsg.split(" ");
        splitCommand = Arrays.stream(splitCommand)
                .filter(s -> s.equals(" ") || !s.isEmpty())
                .toArray(String[]::new);

        String command = splitCommand[0];
        String[] args = Arrays.copyOfRange(splitCommand, 1, splitCommand.length);
        return new Command(command, args);
    }
}
