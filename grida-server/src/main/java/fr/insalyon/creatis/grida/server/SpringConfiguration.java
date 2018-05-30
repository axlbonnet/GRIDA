package fr.insalyon.creatis.grida.server;

import fr.insalyon.creatis.grida.server.dao.*;
import fr.insalyon.creatis.grida.server.operation.*;
import org.springframework.context.annotation.*;

/**
 * Created by abonnet on 5/15/18.
 */
@Configuration
@ComponentScan
public class SpringConfiguration {

    private GridaConfiguration configuration;
    private LCGFailoverOperations lcgFailoverOperations;
    private OperationTypeDetector operationTypeDetector;

    public SpringConfiguration(GridaConfiguration configuration,
                               LCGFailoverOperations lcgFailoverOperations,
                               OperationTypeDetector operationTypeDetector) {
        this.configuration = configuration;
        this.lcgFailoverOperations = lcgFailoverOperations;
        this.operationTypeDetector = operationTypeDetector;
    }

    @Bean
    public PoolDAO poolDAO() {
        return DAOFactory.getDAOFactory().getPoolDAO();
    }

    @Bean
    public CacheFileDAO cacheFileDAO() {
        return DAOFactory.getDAOFactory().getCacheFileDAO();
    }

    @Bean
    public CacheListDAO cacheListDAO() {
        return DAOFactory.getDAOFactory().getCacheListDAO();
    }

    @Bean
    public ZombieFilesDAO zombieFilesDAO() {
        return DAOFactory.getDAOFactory().getZombieFilesDAO();
    }

    @Bean
    public Operations operations() {
        switch (operationTypeDetector.getOperationType()) {
            case DIRAC:
                return new DiracOperations(configuration);
            case LCG:
                return new LCGOperations(configuration, lcgFailoverOperations);
            default:
                throw new RuntimeException("Unkown Grida operation type");
        }
    }
}
