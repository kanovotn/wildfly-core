package org.jboss.as.test.integration.management.cli;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.impl.CommandContextConfiguration;
import org.jboss.as.cli.impl.ReadlineConsole;
import org.jboss.as.test.integration.management.util.CLITestUtil;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.WildflyTestRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * @author kanovotn@redhat.com
 */
@RunWith(WildflyTestRunner.class)
public class MoreTestCase {
    @Test
    public void test() throws Exception {
        CommandContextConfiguration commandContextConfiguration = new CommandContextConfiguration.Builder()
                .setController(TestSuiteEnvironment.getServerAddress() + ":" + TestSuiteEnvironment.getServerPort() )
                //.setUsername("jboss")
                //.setPassword("jboss".toCharArray())
                .setDisableLocalAuth(true)
                .setInitConsole(true)
                .build();
        Class<?> c = Class.forName("org.jboss.as.cli.impl.CommandContextImpl");
        Constructor<?> constructor = c.getDeclaredConstructor(CommandContextConfiguration.class);
        constructor.setAccessible(true);
        Object commandContextImpl = constructor.newInstance(commandContextConfiguration);

        Method method = c.getDeclaredMethod("getConsole");
        method.setAccessible(true);
        Object readLineConsole = method.invoke(commandContextImpl);
        Assert.assertTrue(readLineConsole!=null);

        Method method2 = ReadlineConsole.class.getDeclaredMethod("forcePagingOutput", boolean.class);
        method2.invoke(readLineConsole,true);

        method2 = ReadlineConsole.class.getDeclaredMethod("isPagingOutputEnabled");
        System.out.println("Paging output enabled: " + method2.invoke(readLineConsole));

        CommandContext ctx = CLITestUtil.getCommandContext(TestSuiteEnvironment.getServerAddress(),
                TestSuiteEnvironment.getServerPort(), System.in, System.out);
        ctx.connectController();
        try {
            ctx.handle(":read-resource");
        } finally {
            ctx.terminateSession();
        }
    }
}
