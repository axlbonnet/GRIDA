package fr.insalyon.creatis.grida.server.execution;

import fr.insalyon.creatis.grida.server.execution.cache.*;
import fr.insalyon.creatis.grida.server.execution.cache.AllCachedFilesCommandExecutor.AllCachedFilesCommand;
import fr.insalyon.creatis.grida.server.execution.cache.DeleteCachedFileCommandExecutor.DeleteCachedFileCommand;
import fr.insalyon.creatis.grida.server.execution.command.*;
import fr.insalyon.creatis.grida.server.execution.command.CreateFolderCommandExecutor.CreateFolderCommand;
import fr.insalyon.creatis.grida.server.execution.command.DeleteCommandExecutor.DeleteCommand;
import fr.insalyon.creatis.grida.server.execution.command.ExistDataCommandExecutor.ExistDataCommand;
import fr.insalyon.creatis.grida.server.execution.command.GetModificationDateCommandExecutor.GetModificationDateCommand;
import fr.insalyon.creatis.grida.server.execution.command.GetRemoteFileCommandExecutor.GetRemoteFileCommand;
import fr.insalyon.creatis.grida.server.execution.command.GetRemoteFolderCommandExecutor.GetRemoteFolderCommand;
import fr.insalyon.creatis.grida.server.execution.command.ListFilesAndFoldersCommandExecutor.ListFilesAndFoldersCommand;
import fr.insalyon.creatis.grida.server.execution.command.RenameCommandExecutor.RenameCommand;
import fr.insalyon.creatis.grida.server.execution.command.ReplicatePreferredSEsCommandExecutor.ReplicatePreferredSEsCommand;
import fr.insalyon.creatis.grida.server.execution.command.SetCommentCommandExecutor.SetCommentCommand;
import fr.insalyon.creatis.grida.server.execution.command.UploadFileCommandExecutor.UploadFileCommand;
import fr.insalyon.creatis.grida.server.execution.pool.*;
import fr.insalyon.creatis.grida.server.execution.pool.PoolAddOperationCommandExecutor.PoolAddOperationCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolAllOperationsCommandExecutor.PoolAllOperationsCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolLimitedOperationsByDateCommandExecutor.PoolLimitedOperationsByDateCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolOperationByIdCommandExecutor.PoolOperationByIdCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolOperationsByUserCommandExecutor.PoolOperationsByUserCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolRemoveOperationByIdCommandExecutor.PoolRemoveOperationByIdCommand;
import fr.insalyon.creatis.grida.server.execution.pool.PoolRemoveOperationsByUserCommandExecutor.PoolRemoveOperationsByUserCommand;
import fr.insalyon.creatis.grida.server.execution.zombie.*;
import fr.insalyon.creatis.grida.server.execution.zombie.DeleteZombieFileCommandExecutor.DeleteZombieFileCommand;
import fr.insalyon.creatis.grida.server.execution.zombie.ZombieGetListCommandExecutor.ZombieGetListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by abonnet on 5/17/18.
 *
 * TODO : separate by core/cache/pool
 */
@Component
public class CommandExecutor {

    @Autowired
    private UploadFileCommandExecutor uploadFileCommandExecutor;
    @Autowired
    private GetRemoteFileCommandExecutor getRemoteFileCommandExecutor;
    @Autowired
    private GetRemoteFolderCommandExecutor getRemoteFolderCommandExecutor;
    @Autowired
    private ListFilesAndFoldersCommandExecutor listFilesAndFoldersCommandExecutor;
    @Autowired
    private GetModificationDateCommandExecutor getModificationDateCommandExecutor;
    @Autowired
    private ReplicatePreferredSEsCommandExecutor replicatePreferredSEsCommandExecutor;
    @Autowired
    private DeleteCommandExecutor deleteCommandExecutor;
    @Autowired
    private CreateFolderCommandExecutor createFolderCommandExecutor;
    @Autowired
    private RenameCommandExecutor renameCommandExecutor;
    @Autowired
    private ExistDataCommandExecutor existDataCommandExecutor;
    @Autowired
    private SetCommentCommandExecutor setCommentCommandExecutor;

    // Cache
    @Autowired
    private AllCachedFilesCommandExecutor allCachedFilesCommandExecutor;
    @Autowired
    private DeleteCachedFileCommandExecutor deleteCachedFileCommandExecutor;

    // Core
    @Autowired
    private PoolOperationByIdCommandExecutor poolOperationByIdCommandExecutor;
    @Autowired
    private PoolAddOperationCommandExecutor poolAddOperationCommandExecutor;
    @Autowired
    private PoolOperationsByUserCommandExecutor poolOperationsByUserCommandExecutor;
    @Autowired
    private PoolRemoveOperationByIdCommandExecutor poolRemoveOperationByIdCommandExecutor;
    @Autowired
    private PoolRemoveOperationsByUserCommandExecutor poolRemoveOperationsByUserCommandExecutor;
    @Autowired
    private PoolAllOperationsCommandExecutor poolAllOperationsCommandExecutor;
    @Autowired
    private PoolLimitedOperationsByDateCommandExecutor poolLimitedOperationsByDateCommandExecutor;

    // Zombie
    @Autowired
    private ZombieGetListCommandExecutor zombieGetListCommandExecutor;
    @Autowired
    private DeleteZombieFileCommandExecutor deleteZombieFileCommandExecutor;

    public void execute(UploadFileCommand command) {
        uploadFileCommandExecutor.execute(command);
    }

    public void execute(GetRemoteFileCommand command) {
        getRemoteFileCommandExecutor.execute(command);
    }

    public void execute(GetRemoteFolderCommand command) {
        getRemoteFolderCommandExecutor.execute(command);
    }

    public void execute(ListFilesAndFoldersCommand command) {
        listFilesAndFoldersCommandExecutor.execute(command);
    }

    public void execute(GetModificationDateCommand command) {
        getModificationDateCommandExecutor.execute(command);
    }

    public void execute(ReplicatePreferredSEsCommand command) {
        replicatePreferredSEsCommandExecutor.execute(command);
    }

    public void execute(DeleteCommand command) {
        deleteCommandExecutor.execute(command);
    }

    public void execute(CreateFolderCommand command) {
        createFolderCommandExecutor.execute(command);
    }

    public void execute(RenameCommand command) {
        renameCommandExecutor.execute(command);
    }

    public void execute(ExistDataCommand command) {
        existDataCommandExecutor.execute(command);
    }

    public void execute(SetCommentCommand command) {
        setCommentCommandExecutor.execute(command);
    }

    public void execute(AllCachedFilesCommand command) {
        allCachedFilesCommandExecutor.execute(command);
    }

    public void execute(DeleteCachedFileCommand command) {
        deleteCachedFileCommandExecutor.execute(command);
    }

    public void execute(PoolOperationByIdCommand command) {
        poolOperationByIdCommandExecutor.execute(command);
    }

    public void execute(PoolAddOperationCommand command) {
        poolAddOperationCommandExecutor.execute(command);
    }

    public void execute(PoolOperationsByUserCommand command) {
        poolOperationsByUserCommandExecutor.execute(command);
    }

    public void execute(PoolRemoveOperationByIdCommand command) {
        poolRemoveOperationByIdCommandExecutor.execute(command);
    }

    public void execute(PoolRemoveOperationsByUserCommand command) {
        poolRemoveOperationsByUserCommandExecutor.execute(command);
    }

    public void execute(PoolAllOperationsCommand command) {
        poolAllOperationsCommandExecutor.execute(command);
    }

    public void execute(PoolLimitedOperationsByDateCommand command) {
        poolLimitedOperationsByDateCommandExecutor.execute(command);
    }

    public void execute(ZombieGetListCommand command) {
        zombieGetListCommandExecutor.execute(command);
    }

    public void execute(DeleteZombieFileCommand command) {
        deleteZombieFileCommandExecutor.execute(command);
    }
}
