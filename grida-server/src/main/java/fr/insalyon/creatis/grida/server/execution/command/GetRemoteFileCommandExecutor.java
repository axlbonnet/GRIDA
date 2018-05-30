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
import fr.insalyon.creatis.grida.server.OperationBusinessProvider;
import fr.insalyon.creatis.grida.server.business.BusinessException;
import fr.insalyon.creatis.grida.server.business.CacheBusiness;
import fr.insalyon.creatis.grida.server.execution.*;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author Rafael Ferreira da Silva
 */
@Component
public class GetRemoteFileCommandExecutor {

    private static final Logger logger = Logger.getLogger(GetRemoteFileCommand.class);

    private OperationBusinessProvider operationBusinessProvider;
    private CacheBusiness cacheBusiness;

    public GetRemoteFileCommandExecutor(OperationBusinessProvider operationBusinessProvider,
                                        CacheBusiness cacheBusiness) {
        this.operationBusinessProvider = operationBusinessProvider;
        this.cacheBusiness = cacheBusiness;
    }

    public void execute(GetRemoteFileCommand command) {

        String remoteFilePath = command.getRemoteFilePath();
        String localDirPath = command.getLocalDirPath();
        Communication communication = command.getCommunication();

        try {
            String cachedFileName = cacheBusiness.getCachedFileName(remoteFilePath);
            String fileName = FilenameUtils.getName(remoteFilePath);

            if (cachedFileName == null) {
                String destPath = downloadFile(command, fileName);
                cacheBusiness.addFileToCache(cachedFileName, destPath, remoteFilePath);

            } else {
                long remoteFileTime = -1;
                long cachedFileTime = new File(cachedFileName).lastModified();

                remoteFileTime = operationBusinessProvider.get(command.getProxyFileName())
                        .getModificationDate(remoteFilePath);

                if (remoteFileTime <= cachedFileTime) {
                    String destPath = localDirPath + "/" + fileName;
                    FileUtils.copyFile(new File(cachedFileName), new File(destPath));
                    logger.info("Copying file \"" + remoteFilePath + "\" from the cache.");
                    communication.sendMessage(destPath);

                } else {
                    String destPath = downloadFile(command, fileName);
                    cacheBusiness.addFileToCache(cachedFileName, destPath, remoteFilePath);
                }
                cacheBusiness.updateFile(remoteFilePath);
            }
        } catch (IOException ex) {
            logger.error(ex);
            communication.sendErrorMessage(ex.getMessage());
        } catch (BusinessException ex) {
            communication.sendErrorMessage(ex.getMessage());
        }
        communication.sendEndOfMessage();
    }

    /**
     * Downloads a file from the grid.
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    private String downloadFile(GetRemoteFileCommand command, String fileName) throws BusinessException {

        String destPath = operationBusinessProvider.get(command.getProxyFileName())
                .downloadFile(null, command.getLocalDirPath(),
                        fileName, command.getRemoteFilePath());
        command.getCommunication().sendMessage(destPath);
        return destPath;
    }

    public static class GetRemoteFileCommand extends Command {

        private String remoteFilePath;
        private String localDirPath;

        public GetRemoteFileCommand(Communication communication, String proxyFileName,
                                    String remoteFilePath, String localDirPath) {

            super(communication, proxyFileName);
            this.remoteFilePath = remoteFilePath;
            this.localDirPath = localDirPath;
        }

        public String getRemoteFilePath() {
            return remoteFilePath;
        }

        public String getLocalDirPath() {
            return localDirPath;
        }

        @Override
        protected void executeOn(CommandExecutor commandExecutor) {
            commandExecutor.execute(this);
        }
    }
}
