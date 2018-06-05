package fr.insalyon.creatis.grisa.server;

import fr.insalyon.creatis.grida.common.*;
import fr.insalyon.creatis.grida.server.Constants;
import fr.insalyon.creatis.grida.server.*;
import fr.insalyon.creatis.grida.server.operation.*;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.net.*;

import static fr.insalyon.creatis.grida.common.Constants.MSG_SEP_1;
import static fr.insalyon.creatis.grida.common.Constants.MSG_SUCCESS;

/**
 * Created by abonnet on 5/17/18.
 */
public class SpringConfigTest {

    @Configuration
    public static class MostBasicTestConfig {

        @Bean
        public OperationTypeDetector operationTypeDetector(GridaConfiguration gridaConfiguration) {
            return new OperationTypeDetector(gridaConfiguration) {
                @Override
                public OperationType getOperationType() {
                    return OperationType.LCG;
                }
            };
        }

    }

    @Configuration
    public static class MockableTestConfig {

        @Bean
        public OperationTypeDetector operationTypeDetector(GridaConfiguration gridaConfiguration) {
            return new OperationTypeDetector(gridaConfiguration) {
                @Override
                public OperationType getOperationType() {
                    return OperationType.LCG;
                }
            };
        }


        @Bean
        public Operations operations() {
            return Mockito.mock(Operations.class);
        }

    }

    @Test
    public void testSpringConfig() {
        new AnnotationConfigApplicationContext(SpringConfiguration.class, MostBasicTestConfig.class);
    }

    @Test
    public void shouldCreateAFolder() throws IOException, InterruptedException, OperationException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(
                SpringConfiguration.class,
                MockableTestConfig.class);
        Thread serverThread = new Thread( () -> ac.getBean(Server.class).run() );
        serverThread.start();
        Thread.sleep(2 * 1000);
        Operations mockOperations = ac.getBean(Operations.class);
        Communication communication = new Communication(new Socket(InetAddress.getLocalHost(), 9006));
        communication.sendMessage(
                ExecutorConstants.COM_CREATE_FOLDER + MSG_SEP_1
                        + "fakeproxypath" + MSG_SEP_1
                        + "/random/path");
        communication.sendEndOfMessage();

        Assert.assertEquals( MSG_SUCCESS, communication.getMessage());
        communication.close();
        Mockito.verify(mockOperations).createFolder("fakeproxypath","/random/path");

    }

}
