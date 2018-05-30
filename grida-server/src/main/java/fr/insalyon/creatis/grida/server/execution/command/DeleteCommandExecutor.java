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
import fr.insalyon.creatis.grida.server.OperationBusinessProvider;
import fr.insalyon.creatis.grida.server.business.BusinessException;
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
public class DeleteCommandExecutor {

    private static final Logger logger = Logger.getLogger(DeleteCommand.class);

    private OperationBusinessProvider operationBusinessProvider;

    public DeleteCommandExecutor(OperationBusinessProvider operationBusinessProvider) {
        this.operationBusinessProvider = operationBusinessProvider;
    }

    public void execute(DeleteCommand command) {

        Communication communication = command.getCommunication();
        List<String> errorFiles = new ArrayList<String>();

        for (String file : command.getPaths()) {
            try {
                operationBusinessProvider.get(command.getProxyFileName()).delete(file);
            } catch (BusinessException ex) {
                errorFiles.add(file);
            }
        }
        if (errorFiles.isEmpty()) {
            communication.sendSucessMessage();
        } else {
            String errorMsg = "Unable to delete the following data: "
                    + errorFiles.toString();
            
            communication.sendErrorMessage(errorMsg);
            logger.error(errorMsg);
        }
        communication.sendEndOfMessage();
    }

    public static class DeleteCommand extends Command {

        private String[] paths;

        public DeleteCommand(Communication communication, String proxyFileName, String... paths) {

            super(communication, proxyFileName);
            this.paths = paths;
        }

        @Override
        protected void executeOn(CommandExecutor commandExecutor) {
            commandExecutor.execute(this);
        }

        public String[] getPaths() {
            return paths;
        }
    }

}
