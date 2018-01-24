package org.jboss.as.test.integration.management.cli.modules;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.test.integration.management.util.CLITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.WildflyTestRunner;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author kanovotn@redhat.com
 */
@RunWith(WildflyTestRunner.class)
public class PipeTestCase {

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
    public void testPipeWithGrep() throws Exception {
        testCommand("version | grep \"^java\\.vm\"", "java.vm.version", false);
    }

    private void testCommand(String cmd, String expectedOutput, boolean exactMatch) throws Exception {
        cliOut.reset();
        final CommandContext ctx = CLITestUtil.getCommandContext(cliOut);
        try {
            ctx.handle(cmd);
            String output = cliOut.toString(StandardCharsets.UTF_8.name());
            if (exactMatch) {
                assertEquals("Wrong results of the command - " + cmd, expectedOutput, output.trim());
            } else {
                assertTrue("Wrong results of the command - " + cmd, output.contains(expectedOutput));
            }
        } finally {
            ctx.terminateSession();
            cliOut.reset();
        }
    }
}
