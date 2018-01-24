package org.jboss.as.test.integration.management.cli;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.test.integration.management.util.CLITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.WildflyTestRunner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author kanovotn@redhat.com
 */
@RunWith(WildflyTestRunner.class)
public class GrepTestCase {

    private static ByteArrayOutputStream cliOut;

    @BeforeClass
    public static void setup() throws Exception {
        cliOut = new ByteArrayOutputStream();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cliOut = null;
    }

    @Test
    public void testGrepHelp() throws Exception {
        testCommand("grep --help", "SYNOPSIS", false);
    }

    // in linux grep this works
    // the correct should be grep -G '^no\(fork\|group\)
    @Test
    public void testGrepBasicRegexpWithExtendedFormat() throws Exception {
        testCommand("echo nofork | grep -G '^no(fork|group)'", "", true);
    }

    // in linux grep this works
    // this too echo "kni(fe)" | grep -E '(fork|kni\(fe\))'
    @Test
    public void testGrepBasicRegexp() throws Exception {
        testCommand("echo nofork | grep -G '^no\\(fork\\|group\\)'", "nofork", true);
    }
    @Test
    public void testGrepExtendedRegexp() throws Exception {
        testCommand("echo nofork | grep -E '^no(fork|group)'", "nofork", true);
    }

    // in linux grep this works
    @Test
    public void testGrepFixedStringsPatternFromFile() throws Exception {
        String fileName = "fixedStringsPatterns";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("pa?[a-z]p");
            bw.newLine();
            bw.write("xa?[0-9]");
            bw.newLine();
            testCommand("echo nofork | grep -F -f " + fileName, "pa?[a-z]p", true);
        } finally {
            Path filePath = Paths.get(fileName);
            Files.delete(filePath);
        }
    }

    // in linux grep this works
    @Test
    public void testGrepFixedStringsPatternAsArgument() throws Exception {
        testCommand("echo pa?[a-z]p | grep -F \"pa?[a-z]p\"", "pa?[a-z]p", true);
    }

    // in linux grep this works
    @Test
    public void testGrepFixedStringsPatternAsEArgument() throws Exception {
        testCommand("echo pa?[a-z]p | grep -F -e \"pa?[a-z]p\"", "pa?[a-z]p", true);
    }

    @Test
    public void testGrepIgnoreCase() throws Exception {
        testCommand("echo ABcd | grep -i \"ABCD\"", "ABcd", true);
    }

    @Test
    public void testGrepPerlRegex() throws Exception {
        testCommand("echo FOO | grep -P '(?i)foo'", "FOO", true);
    }

    // in linux grep this works gives ""
    @Test
    public void testGrepPerlRegexWithoutPArgument() throws Exception {
        testCommand("echo FOO | grep '(?i)foo'", "", true);
    }

    // in linux grep this works
    @Test
    public void testGrepWithPatternFromFile() throws Exception {
        String fileName = "pattern";
        try (BufferedWriter br = new BufferedWriter(new FileWriter(fileName))) {
            br.write("pa?[a-z]p");
            testCommand("echo papp | grep -E -f " + fileName, "papp", true);
        } finally {
            Path filePath = Paths.get(fileName);
            Files.delete(filePath);
        }
    }

    // seems -e  is not implemented?
    @Test
    public void testGrepWithRegexpWithEArgument() throws Exception {
        testCommand("echo papp | grep -E -e pa?[a-z]p", "papp", true);
    }

    // ============================================================
    @Test
    public void testGrepWithoutArguments() throws Exception {
        testCommand("grep", "SYNOPSIS", false);
    }

    @Test
    public void testGrepWithoutInput() throws Exception {
        testCommand("grep java", "no file or input given", false);
    }

    @Test
    public void testGrepWithInputFromFile() throws Exception {
        cliOut.reset();
        final CommandContext ctx = CLITestUtil.getCommandContext(cliOut);
        String fileName = "testGrepWithInputFile";
        try {
            ctx.handle("version >" + fileName);

            ctx.handle("grep java.version " + fileName);
            String output = cliOut.toString(StandardCharsets.UTF_8.name());
            assertTrue("Wrong results of the grep command", output.contains("java.version"));

            Path filePath = Paths.get(fileName);
            Files.delete(filePath);
        } finally {
            ctx.terminateSession();
            cliOut.reset();
        }
    }

    @Test
    public void testGrepWithInputNonExistingFile() throws Exception {
        String fileName = "testNoneExistingFile";
        testCommand("grep java.version " + fileName, "No such file or directory", false);
    }

    @Test
    public void testGrepWithInvalidPattern() throws Exception {
        testCommand("grep \\.?\\(\\/1\\)", "invalid pattern", false);
    }

    @Test(expected = OperationFormatException.class)
    public void testGrepWithInvalidParameter() throws Exception {
        testCommand("grep --", "'' is not a valid parameter name", false);
    }

    @Test(expected = OperationFormatException.class)
    public void testGrepWithNonExistingParameter() throws Exception {
        testCommand("grep --mo", "'mo' is not a valid parameter name", false);
    }

    @Test
    public void testGrepStringWithDoubleQuotes() throws Exception {
        testCommand("echo \"String with double quotes\" | grep \"String with double quotes\"",
                "\"String with double quotes\"", true);
    }

    @Test
    public void testGrepStringWithDoubleQuotesBackslashes() throws Exception {
        testCommand("echo \"String with double quotes\" | grep \"\\\"String with double quotes\"\\\"",
                "\"String with double quotes\"", true);
    }

    @Test
    public void testGrepInvalidExpressionWithBracketsSlash() throws Exception {
        testCommand("echo a | grep \\((a))", "\"invalid pattern\"", false);
    }

    @Test
    public void testGrepExpressionWithBrackets() throws Exception {
        testCommand("echo (a) | grep (\\(a\\))", "(a)", true);
    }

    @Test
    public void testGrepExpressionWithBracketsDoubleQuotes() throws Exception {
        testCommand("echo (a) | grep \"(\\(a\\))\"", "(a)", true);
    }

    @Test
    public void testGrepExpressionWithQuestionMark() throws Exception {
        testCommand("echo test?test | grep .*\\?", "test?test", true);
    }

    @Test
    public void testGrepExpressionWithPipe() throws Exception {
        testCommand("echo test\\|test | grep .*\\|", "test|test", true);
    }

    @Test
    public void testGrepExpressionWithDollar() throws Exception {
        testCommand("echo test\\$test | grep .*\\$", "test$test", true);
    }

    private void testCommand(String cmd, String expectedOutput, boolean exactMatch) throws Exception {
        cliOut.reset();
        final CommandContext ctx = CLITestUtil.getCommandContext(cliOut);
        try {
            ctx.handle(cmd);
            String output = cliOut.toString(StandardCharsets.UTF_8.name());
            if (exactMatch) {
                assertEquals("Wrong results of the grep command", expectedOutput, output.trim());
            } else {
                assertTrue("Wrong results of the grep command", output.contains(expectedOutput));
            }
        } finally {
            ctx.terminateSession();
            cliOut.reset();
        }
    }
}
