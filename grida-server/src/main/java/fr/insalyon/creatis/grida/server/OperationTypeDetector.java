package fr.insalyon.creatis.grida.server;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Created by abonnet on 5/29/18.
 */
@Component
public class OperationTypeDetector {

    private static final Logger logger = Logger.getLogger(OperationTypeDetector.class);

    private GridaConfiguration gridaGridaConfiguration;

    public OperationTypeDetector(GridaConfiguration gridaGridaConfiguration) {
        this.gridaGridaConfiguration = gridaGridaConfiguration;
    }

    public enum OperationType {
        LCG,
        DIRAC,
        UNKNOWN;
    }

    public OperationType getOperationType() {
        OperationType operationType = OperationType.UNKNOWN;
        String commandsType = gridaGridaConfiguration.getCommandsType();
        switch (commandsType) {
            case "lcg":
            {
                boolean isLcgCommandsAvailable =
                        isBinaryAvailable("lcg-cr", null)
                                && isBinaryAvailable("lcg-cp", null);
                if (isLcgCommandsAvailable) {
                    logger.info("LCG commands available.");
                    return OperationType.LCG;
                } else {
                    logger.warn("LCG commands unavailable.");
                    throw new RuntimeException("lcg configured but binaries not found");
                }
            }
            case "dirac":
            {
                String diracBashrc = gridaGridaConfiguration.getDiracBashrc();
                boolean exists = new File(diracBashrc).exists();
                if (!exists) {
                    logger.warn("Dirac bashrc file does not exist: " + diracBashrc);
                }
                boolean isDiracCommandsAvailable =
                        exists
                                && isBinaryAvailable("dls", diracBashrc)
                                && isBinaryAvailable("dget", diracBashrc);
                if (isDiracCommandsAvailable) {
                    logger.info("Dirac commands available.");
                    return OperationType.DIRAC;
                } else {
                    logger.warn("Dirac commands unavailable.");
                    throw new RuntimeException("dirac configured but binaries not found");
                }
            }
            default:
                logger.error("Unkown command type: " + commandsType +
                        ". Possible values are lcg or dirac.");
                throw new RuntimeException("Unkown command type: " + commandsType);
        }
    }

    private boolean isBinaryAvailable(String name, String envFile) {
        boolean isAvailable = false;
        try {
            String command = envFile == null
                    ? "which"
                    : "source " + envFile + "; which";

            ProcessBuilder builder = envFile == null
                    ? new ProcessBuilder("which", name)
                    : new ProcessBuilder(
                    "bash", "-c", "source " + envFile + "; which " + name);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            process.waitFor();

            isAvailable = process.exitValue() == 0;
        } catch (InterruptedException ex) {
            logger.warn(ex);
        } catch (IOException ex) {
            logger.warn(ex);
        }
        return isAvailable;
    }
}
