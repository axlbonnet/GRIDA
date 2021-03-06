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
package fr.insalyon.creatis.grida.server.dao.h2;

import fr.insalyon.creatis.grida.common.bean.ZombieFile;
import fr.insalyon.creatis.grida.server.dao.DAOException;
import fr.insalyon.creatis.grida.server.dao.ZombieFilesDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Rafael Silva
 */
public class ZombieFilesData implements ZombieFilesDAO {

    private static final Logger logger = Logger.getLogger(ZombieFilesData.class);
    private static ZombieFilesData instance;
    private Connection connection;

    public static ZombieFilesData getInstance(Connection connection) {
        if (instance == null) {
            instance = new ZombieFilesData(connection);
        }
        return instance;
    }

    private ZombieFilesData(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(String surl) throws DAOException {
        
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "
                    + "ZombieFiles(srm, registration) VALUES(?, ?)");
            ps.setString(1, surl);
            ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            ps.execute();
            
        } catch (SQLException ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
    }
    
    @Override
    public List<ZombieFile> getZombieFiles() throws DAOException {
        
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT "
                    + "srm, registration FROM ZombieFiles "
                    + "ORDER BY registration DESC");
            
            ResultSet rs = ps.executeQuery();
            List<ZombieFile> list = new ArrayList<ZombieFile>();
            
            while (rs.next()) {
                list.add(new ZombieFile(
                        rs.getString("srm"), 
                        new Date(rs.getTimestamp("registration").getTime())));
            }
            
            return list;
            
        } catch (SQLException ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
    }
    
    @Override
    public void delete(String surl) throws DAOException {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE "
                    + "FROM ZombieFiles WHERE srm = ?");

            ps.setString(1, surl);
            ps.execute();

        } catch (SQLException ex) {
            logger.error(ex);
            throw new DAOException(ex);
        }
    }
}
