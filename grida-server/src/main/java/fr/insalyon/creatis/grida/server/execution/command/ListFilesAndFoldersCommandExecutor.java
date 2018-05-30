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
package fr.insalyon.creatis.grida.server.execution.command;

import fr.insalyon.creatis.grida.common.Communication;
import fr.insalyon.creatis.grida.common.Constants;
import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.server.OperationBusinessProvider;
import fr.insalyon.creatis.grida.server.business.BusinessException;
import fr.insalyon.creatis.grida.server.business.CacheBusiness;
import fr.insalyon.creatis.grida.server.business.OperationBusiness;
import fr.insalyon.creatis.grida.server.execution.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author Rafael Silva
 */
@Component
public class ListFilesAndFoldersCommandExecutor {

    private static final Logger logger = Logger.getLogger(ListFilesAndFoldersCommand.class);

    private OperationBusinessProvider operationBusinessProvider;
    private CacheBusiness cacheBusiness;

    public ListFilesAndFoldersCommandExecutor(OperationBusinessProvider operationBusinessProvider,
                                              CacheBusiness cacheBusiness) {
        this.operationBusinessProvider = operationBusinessProvider;
        this.cacheBusiness = cacheBusiness;
    }

    public void execute(ListFilesAndFoldersCommand command) {

        String path = command.getPath();
        Communication communication = command.getCommunication();

        try {
            if (command.isRefresh()) {
                getDataList(command);

            } else {
                if (cacheBusiness.hasValidCacheData(path)) {
                    logger.info("Listing Files and Folders from cache: " + path);
                    for (String data : cacheBusiness.getCachedPaths(path)) {
                        communication.sendMessage(data);
                    }

                } else {
                    logger.info("Cache list expired for path: " + path);
                    getDataList(command);
                }
            }
        } catch (BusinessException ex) {
            communication.sendErrorMessage(ex.getMessage());
        }
        communication.sendEndOfMessage();
    }

    /**
     * 
     * @throws BusinessException 
     */
    private void getDataList(ListFilesAndFoldersCommand command) throws BusinessException {

        String path = command.getPath();

        List<String> dataList = new ArrayList<String>();
        for (GridData data : operationBusinessProvider.get(command.getProxyFileName())
                .listFilesAndFolders(path,command.isListComments())) {

            String dataPath = data.getType() == GridData.Type.Folder
                    ? data.getName() + Constants.MSG_SEP_2
                    + data.getType().name() + Constants.MSG_SEP_2
                    + data.getPermissions()
                    : data.getName() + Constants.MSG_SEP_2
                    + data.getType().name() + Constants.MSG_SEP_2
                    + data.getLength() + Constants.MSG_SEP_2
                    + data.getModificationDate() + Constants.MSG_SEP_2
                    + data.getReplicas() + Constants.MSG_SEP_2
                    + data.getPermissions() + Constants.MSG_SEP_2
                    + data.getComment();

            command.getCommunication().sendMessage(dataPath);
            dataList.add(dataPath);
        }
        cacheBusiness.addPathToCache(path, dataList);
    }

    public static class ListFilesAndFoldersCommand extends Command {

        private String path;
        private boolean refresh;
        private boolean listComments;

        public ListFilesAndFoldersCommand(Communication communication,
                                          String proxyFileName, String path, String refresh, boolean listComments) {

            super(communication, proxyFileName);
            this.path = path;
            this.refresh = Boolean.valueOf(refresh);
            this.listComments = listComments;
        }

        @Override
        protected void executeOn(CommandExecutor commandExecutor) {
            commandExecutor.execute(this);
        }

        public String getPath() {
            return path;
        }

        public boolean isRefresh() {
            return refresh;
        }

        public boolean isListComments() {
            return listComments;
        }
    }
}
