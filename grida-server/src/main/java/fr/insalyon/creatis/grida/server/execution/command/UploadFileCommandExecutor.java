/* Copyright CNRS-CREATIS
 *
 * Rafael Ferreira da Silva
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
package fr.insalyon.creatis.grida.server.execution.command;

import fr.insalyon.creatis.grida.common.Communication;
import fr.insalyon.creatis.grida.common.bean.Operation;
import fr.insalyon.creatis.grida.server.*;
import fr.insalyon.creatis.grida.server.business.BusinessException;
import fr.insalyon.creatis.grida.server.business.OperationBusiness;
import fr.insalyon.creatis.grida.server.business.PoolBusiness;
import fr.insalyon.creatis.grida.server.execution.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Rafael Ferreira da Silva
 */
@Component
public class UploadFileCommandExecutor {

    private OperationBusinessProvider operationBusinessProvider;
    private PoolBusiness poolBusiness;
    private GridaConfiguration configuration;

    public UploadFileCommandExecutor(OperationBusinessProvider operationBusinessProvider,
                                     PoolBusiness poolBusiness,
                                     GridaConfiguration configuration) {
        this.operationBusinessProvider = operationBusinessProvider;
        this.poolBusiness = poolBusiness;
        this.configuration = configuration;
    }

    public void execute(UploadFileCommand command) {
        String proxyFileName = command.getProxyFileName();
        Communication communication = command.getCommunication();
        String localFilePath = command.getLocalFilePath();
        String remoteDir = command.getRemoteDir();
        String[] storageElements = command.getStorageElements();

        OperationBusiness operationBusiness = operationBusinessProvider.get(proxyFileName);

        try {
            String destPath = operationBusiness.uploadFile(null, localFilePath, remoteDir);

            if (storageElements.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (String se : storageElements) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(se);
                }
                configuration.setPreferredSEs(sb.toString());
                operationBusiness.replicateFile(destPath);

            } else {
                try {
                    poolBusiness.addOperation(proxyFileName, destPath, "",
                            Operation.Type.Replicate, proxyFileName);
                } catch (BusinessException ex) {
                    // do nothing
                }
            }
            communication.sendMessage(destPath);

        } catch (BusinessException ex) {
            communication.sendErrorMessage(ex.getMessage());
        }
        communication.sendEndOfMessage();

        if (storageElements.length > 0) {
            configuration.setPreferredSEs();
        }
    }

    public static class UploadFileCommand extends Command {

        private String localFilePath;
        private String remoteDir;
        private String[] storageElements;

        public UploadFileCommand(Communication communication, String proxyFileName,
                                 String localFilePath, String remoteDir) {

            this(communication, proxyFileName, localFilePath, remoteDir, new String[]{});
        }

        public UploadFileCommand(Communication communication, String proxyFileName,
                                 String localFilePath, String remoteDir, String... storageElements) {

            super(communication, proxyFileName);
            this.localFilePath = localFilePath;
            this.remoteDir = remoteDir;
            this.storageElements = storageElements;
        }

        @Override
        protected void executeOn(CommandExecutor commandExecutor) {
            commandExecutor.execute(this);
        }

        public String getLocalFilePath() {
            return localFilePath;
        }

        public String getRemoteDir() {
            return remoteDir;
        }

        public String[] getStorageElements() {
            return storageElements;
        }
    }
}
