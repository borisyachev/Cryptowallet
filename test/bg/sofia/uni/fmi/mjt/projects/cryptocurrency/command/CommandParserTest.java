package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.command;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandParserTest {


    @Test
    public void testNewCommand() {

        String msgToParse = "  buy 10 BTC      ";
        Command actualCommand = CommandParser.newCommand(msgToParse);
        Command expectedCommand = new Command("buy", new String[]{"10", "BTC"});

        assertEquals(expectedCommand.command(), actualCommand.command());
        Assert.assertArrayEquals(expectedCommand.args(), actualCommand.args());
    }
}
