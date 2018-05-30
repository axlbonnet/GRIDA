/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.grida.server;

import fr.insalyon.creatis.grida.common.Communication;
import fr.insalyon.creatis.grida.server.dao.*;
import fr.insalyon.creatis.grida.server.execution.*;
import org.apache.log4j.*;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

/**
 * 
 * @author Rafael Silva
 */
@Component
public abstract class Server {

    public static void main(String[] args) {
        PropertyConfigurator.configure(Server.class.getClassLoader().getResource("gridaLog4j.properties"));
        ApplicationContext ac = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        ac.getBean(Server.class).run();
    }

    private static final Logger logger = Logger.getLogger(Server.class);

    private PoolDAO poolDAO;
    private PoolCleanService poolCleanService;
    private PoolDownloadService poolDownloadService;
    private PoolUploadService poolUploadService;
    private PoolDeleteService poolDeleteService;
    private PoolReplicateService poolReplicateService;
    private GridaConfiguration configuration;

    public Server(PoolDAO poolDAO, PoolCleanService poolCleanService,
                  PoolDownloadService poolDownloadService, PoolUploadService poolUploadService,
                  PoolDeleteService poolDeleteService, PoolReplicateService poolReplicateService, GridaConfiguration configuration) {
        this.poolDAO = poolDAO;
        this.poolCleanService = poolCleanService;
        this.poolDownloadService = poolDownloadService;
        this.poolUploadService = poolUploadService;
        this.poolDeleteService = poolDeleteService;
        this.poolReplicateService = poolReplicateService;
        this.configuration = configuration;
    }

    public void run() {
        try {
            logger.info("Starting GRIDA Server on port " + configuration.getPort());

            // Pools
            poolDAO.resetOperations();
            poolCleanService.start();
            poolDownloadService.start();
            poolUploadService.start();
            poolDeleteService.start();
            poolReplicateService.start();

            // Socket
            ServerSocket serverSocket = new ServerSocket(
                    configuration.getPort(), 50, InetAddress.getLocalHost());

            while (true) {
                Socket socket = serverSocket.accept();
                Communication communication = new Communication(socket);
                getExecutor(communication).start();
            }
        } catch (DAOException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Lookup
    protected abstract Executor getExecutor(Communication communication);
}
