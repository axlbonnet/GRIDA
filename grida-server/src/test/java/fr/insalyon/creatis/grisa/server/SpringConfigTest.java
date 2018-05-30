package fr.insalyon.creatis.grisa.server;

import fr.insalyon.creatis.grida.server.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

/**
 * Created by abonnet on 5/17/18.
 */
public class SpringConfigTest {

    @Configuration
    public static class SpringTestConfig {

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

    @Test
    public void testSpringConfig() {
        new AnnotationConfigApplicationContext(SpringConfiguration.class, SpringTestConfig.class);
    }

}
