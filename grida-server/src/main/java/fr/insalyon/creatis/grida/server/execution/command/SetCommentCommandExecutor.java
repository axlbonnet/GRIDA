/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.creatis.grida.server.execution.command;

import fr.insalyon.creatis.grida.server.OperationBusinessProvider;
import fr.insalyon.creatis.grida.server.execution.*;
import fr.insalyon.creatis.grida.common.Communication;
import fr.insalyon.creatis.grida.server.business.BusinessException;
import fr.insalyon.creatis.grida.server.business.OperationBusiness;
import org.springframework.stereotype.Component;

/**
 *
 * @author glatard
 */
@Component
public class SetCommentCommandExecutor {

    private OperationBusinessProvider operationBusinessProvider;

    public SetCommentCommandExecutor(OperationBusinessProvider operationBusinessProvider) {
        this.operationBusinessProvider = operationBusinessProvider;
    }

    public void execute(SetCommentCommand command) {
           try {
            operationBusinessProvider.get(command.getProxyFileName())
                    .setComment(command.getLfn(), command.getRevision());

        } catch (BusinessException ex) {
            command.getCommunication().sendErrorMessage(ex.getMessage());
        }
        command.getCommunication().sendEndOfMessage();
    }

    public static class SetCommentCommand extends Command {

        private String lfn;
        private String revision;

        public SetCommentCommand(Communication communication, String proxyFileName,
                                 String lfn, String revision) {

            super(communication, proxyFileName);
            this.lfn = lfn;
            this.revision = revision;
        }

        @Override
        protected void executeOn(CommandExecutor commandExecutor) {
            commandExecutor.execute(this);
        }

        public String getLfn() {
            return lfn;
        }

        public String getRevision() {
            return revision;
        }
    }
}
