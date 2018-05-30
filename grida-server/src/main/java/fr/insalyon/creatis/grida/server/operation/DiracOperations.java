/* Copyright CNRS-CREATIS
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
package fr.insalyon.creatis.grida.server.operation;

import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.server.GridaConfiguration;
import fr.insalyon.creatis.grida.server.execution.PoolProcessManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

public class DiracOperations implements Operations {

    private final static Logger logger = Logger.getLogger(DiracOperations.class);

    private final String bashrcPath;

    private static int INDEX_PERMISSION = 0;
    private static int INDEX_NB_LINKS = 1;
    private static int INDEX_OWNER = 2;
    private static int INDEX_GROUP = 3;
    private static int INDEX_SIZE = 4;
    private static int INDEX_DATE = 5;
    private static int INDEX_TIME = 6;
    private static int INDEX_NAME = 7;

    private GridaConfiguration configuration;

    public DiracOperations(GridaConfiguration configuration) {
        this.configuration = configuration;
        this.bashrcPath = configuration.getDiracBashrc();
    }

    @Override
    public long getModificationDate(String proxy, String path)
        throws OperationException {

        logger.info("[dirac] Getting modification date for: " + path);
        String[] output = executeCommand(
            proxy,
            "Unable to get modification date for '" + path,
            "dls", "-l", path);
        try {
            SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date modifTime = formatter.parse(
                output[INDEX_DATE] + " " + output[INDEX_TIME]);
            return modifTime.getTime();
        } catch (ParseException ex) {
            logger.error(ex);
            throw new OperationException(ex);
        }
    }

    @Override
    public List<GridData> listFilesAndFolders(
        String proxy, String path, boolean listComment)
        throws OperationException {

        if (listComment) {
            String error = "[dirac] Listing with comments is not implemented.";
            logger.error(error);
            throw new OperationException(error);
        }

        logger.info("[dirac] Listing contents of: " + path);
        try {
            Process process = processFor(proxy, "dls", "-l", path);

            List<GridData> data = new ArrayList<GridData>();

            try (BufferedReader r = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {
                String s = null;
                String cout = "";
                boolean isFirst = true;

                while ((s = r.readLine()) != null) {
                    if (isFirst) {
                        // Skip first line, which is the name of the concerned
                        // path for the dls command.  Other commands ignore
                        // output, so this does not hurt.
                        isFirst = false;
                    } else {
                        cout += s + "\n";
                        data.add(parseLine(s));
                    }
                }
                process.waitFor();
                OperationsUtil.close(process);

                if (process.exitValue() != 0) {
                    logger.error("[dirac] Unable to list folder '" + path +
                                 "': " + cout);
                    throw new OperationException(cout);
                }
            }
            process = null;

            return data;
        } catch (InterruptedException | IOException ex) {
            logger.error(ex);
            throw new OperationException(ex);
        }
    }

    private GridData parseLine(String line)
        throws OperationException {

        String[] tokens = line.split("\\s+");

        StringBuilder dataName = new StringBuilder();
        for (int i = INDEX_NAME; i < tokens.length; i++) {
            if (tokens[i].equals("->")) {
                break;
            }
            if (dataName.length() > 0) {
                dataName.append(" ");
            }
            dataName.append(tokens[i]);
        }

        GridData data;
        if (tokens.length == 0 ||
            tokens[INDEX_PERMISSION].length() == 0) {
            logger.error("Cannot extract permissions from line: " + line);
            throw new OperationException(
                "Cannot extract permissions from line: " + line);
        } else if (tokens[INDEX_PERMISSION].charAt(0) == 'd') {
            data = new GridData(
                dataName.toString(),
                GridData.Type.Folder,
                tokens[INDEX_PERMISSION]);
        } else {
            String modifTime = "unknown";
            if (tokens.length < INDEX_NAME) {
                logger.warn(
                    "Cannot get modification time; setting it to \"unknown\"." +
                    "Line: " + line);
            } else {
                modifTime = tokens[INDEX_DATE] + " " + tokens[INDEX_TIME];
            }

            Long length = null;
            try {
                length = new Long(tokens[INDEX_SIZE]);
            } catch (java.lang.NumberFormatException e) {
                logger.warn(
                    "Cannot parse long: \"" + tokens[INDEX_SIZE] +
                    "\". Setting file length to 0");
                length = new Long(0);
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                logger.warn(
                    "Cannot get long. Setting file length to 0");
                length = new Long(0);
            }

            String comment = "";
            String replicas = "-";
            data = new GridData(
                dataName.toString(),
                GridData.Type.File,
                length,
                modifTime,
                replicas,
                tokens[INDEX_PERMISSION],
                comment);
        }

        return data;
    }

    @Override
    public String downloadFile(
        String operationID,
        String proxy,
        String localDirPath,
        String fileName,
        String remoteFilePath)
        throws OperationException {

        try {
            logger.info("[dirac] Downloading: " + remoteFilePath +
                        " - To: " + localDirPath);

            Process process = processFor(
                proxy, "dget", remoteFilePath, localDirPath);

            PoolProcessManager.getInstance().addProcess(operationID, process);

            try (BufferedReader r = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {
                String s = null;
                String cout = "";

                while ((s = r.readLine()) != null) {
                    cout += s + "\n";
                }
                process.waitFor();
                OperationsUtil.close(process);

                if (process.exitValue() != 0) {
                    logger.error(cout);
                    File file = new File(localDirPath + "/" + fileName);
                    FileUtils.deleteQuietly(file);
                    throw new OperationException(cout);
                }
            }
            process = null;

            return localDirPath + "/" + fileName;
        } catch (InterruptedException | IOException ex) {
            logger.error(ex);
            throw new OperationException(ex);
        } finally {
            PoolProcessManager.getInstance().removeProcess(operationID);
        }
    }

    @Override
    public String uploadFile(
        String operationID,
        String proxy,
        String localFilePath,
        String remoteDir) throws OperationException {

        try {
            String fileName = new File(localFilePath).getName();
            boolean completed = false;

            logger.info("[dirac] Uploading file: " + localFilePath +
                        " - To: " + remoteDir);

            List<String> ses = configuration.getPreferredSEs();
            logger.info("[dirac] Uploading preferred SE: " + ses);

            if (ses.size() == 0) {
                // Use the default SE
                Process process = processFor(
                    proxy, "dput", localFilePath, remoteDir);
                completed = uploadToSe(operationID, process);
            } else {
                for (String se : ses) {
                    Process process = processFor(
                        proxy, "dput", "-D", se, localFilePath, remoteDir);

                    logger.info("[dirac] Uploading file to se: " + se);
                    completed = uploadToSe(operationID, process);
                    if (completed) break;
                }
            }

            logger.info("[dirac] Uploading completed: " + completed);

            if (!completed) {
                throw new OperationException(
                    "Failed to perform upload from Dirac command.");
            }

            FileUtils.deleteQuietly(new File(localFilePath));
            return remoteDir + "/" + fileName;
        } catch (InterruptedException | IOException ex) {
            logger.error(ex);
            throw new OperationException(ex);
        } finally {
            PoolProcessManager.getInstance().removeProcess(operationID);
        }
    }

    @Override
    public void replicateFile(String proxy, String sourcePath)
        throws OperationException {

        for (String se : configuration.getPreferredSEs()) {
            logger.info(
                "[dirac] Replicating: " + sourcePath + " - To: " + se);

            executeCommand(
                proxy,
                "Unable to replicate file to '" + se,
                "drepl", "-D", se, sourcePath);
        }
    }

    @Override
    public boolean isDir(String proxy, String path) throws OperationException {
        // Listing a directory returns the its content, and gives no information
        // about the directory itself.  So we list the parent directory, extract
        // the info of the file/folder we are interested in.
        File f = new File(path);
        String name = f.getName();

        List<GridData> gdl = listFilesAndFolders(proxy, f.getParent(), false);
        for (GridData gd : gdl) {
            if (gd.getName().equals(name)) {
                return gd.getPermissions().startsWith("d");
            }
        }
        throw new OperationException("[dirac] isDir : Path not found : " + path);
    }

    @Override
    public void deleteFolder(String proxy, String path)
        throws OperationException {

        logger.info("[dirac] Deleting folder '" + path + "'.");
        executeCommand(
            proxy,
            "Unable to delete folder '" + path,
            "drm", "-r", path);
    }

    @Override
    public void deleteFile(String proxy, String path)
        throws OperationException {

        logger.info("[dirac] Deleting file '" + path + "'");
        executeCommand(
            proxy,
            "Unable to delete file '" + path,
            "drm", path);
    }

    @Override
    public void createFolder(String proxy, String path)
        throws OperationException {

        logger.info("[dirac] Creating folder '" + path + "'");
        executeCommand(
            proxy,
            "Unable to create folder '" + path,
            "dmkdir", path);
    }

    @Override
    public void rename(String proxy, String oldPath, String newPath)
        throws OperationException {

        String error =
            "[dirac] Rename operation not implemented. '" +
            oldPath + "' to '" + newPath + "'.";
        logger.error(error);
        throw new OperationException(error);
    }

    @Override
    public boolean exists(String proxy, String path) throws OperationException {
        logger.info("[dirac] Checking existence of '" + path + "'");
        String[] output = executeCommand(
            proxy,
            "Unable to verify existence for '" + path,
            "dls", "-l", path);
        return output.length > 0;
    }

    @Override
    public long getDataSize(String proxy, String path)
        throws OperationException {

        long size = 0;
        for (GridData data : listFilesAndFolders(proxy, path, false)) {
            if (data.getType() == GridData.Type.Folder) {
                size += getDataSize(proxy, path + "/" + data.getName());
            } else {
                size += data.getLength();
            }
        }
        return size;
    }

    @Override
    public void setComment(String proxy, String lfn, String comment)
        throws OperationException {

        String error =
            "[dirac] Set comment operation not implemented. Path: '" +
            lfn + "' Comment: '" + comment + "'.";
        logger.error(error);
        throw new OperationException(error);
    }

    private Process processFor(String proxy, String... command)
        throws IOException {

        StringBuilder sb = new StringBuilder("source ")
            .append(bashrcPath).append(";");
        for (String s : command) {
            sb.append(" ").append(s);
        }

        return OperationsUtil.getProcess(proxy, "bash", "-c", sb.toString());
    }

    private String[] executeCommand(
        String proxy, String errorMessage, String... arguments)
        throws OperationException {

        try {
            Process process = processFor(proxy, arguments);
            String cout = "";
            try (BufferedReader r = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {
                String s = null;
                boolean isFirst = true;
                while ((s = r.readLine()) != null) {
                    if (isFirst) {
                        // Skip first line, which is the name of the concerned
                        // path for the dls command.  Other commands ignore
                        // output, so this does not hurt.
                        isFirst = false;
                    } else {
                        cout += s + "\n";
                    }
                }
                process.waitFor();
                OperationsUtil.close(process);
            }

            if (process.exitValue() != 0) {
                logger.error(
                    "[dirac] " + errorMessage + ": " + cout);
                throw new OperationException(cout);
            }
            process = null;
            return cout.split("\\s+");
        } catch (IOException | InterruptedException ex) {
            logger.error(ex);
            throw new OperationException(ex);
        }
    }

    private boolean uploadToSe(String operationID, Process process)
        throws InterruptedException, IOException {
        PoolProcessManager.getInstance().addProcess(operationID, process);

        boolean completed = false;
        try (BufferedReader r = new BufferedReader(
                 new InputStreamReader(process.getInputStream()))) {
            String s = null;
            String cout = "";

            while ((s = r.readLine()) != null) {
                cout += s + "\n";
            }
            process.waitFor();
            OperationsUtil.close(process);

            if (process.exitValue() != 0) {
                logger.error(cout);
                PoolProcessManager.getInstance().removeProcess(operationID);
            } else {
                completed = true;
            }
        }
        return completed;
    }
}
