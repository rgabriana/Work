/**
 * 
 */
package com.ems.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.ems.commands.profile.Profile;
import com.ems.su.SensorUnit;
import com.ems.utils.Utils;

/**
 * @author yogesh
 * 
 */
public class SimDBHelper {
    public Logger oLogger = Logger.getLogger(SimDBHelper.class.getName());

    private static final SimDBHelper m_instance = new SimDBHelper();
    private static final String DBFILE = "sim.db";
    private static final String SIM_SU_TABLE = "sim_su";
    private SqlJetDb m_DB = null;


    /**
     * SU table
     * 
     * @author yogesh
     * 
     */
    private enum SIM_SU {
        DEVICE_MAC, SPROFILE_WEEKDAY, SPROFILE_WEEKEND, SPROFILE_HOLIDAY, GPRPFILE;

        public String getName() {
            return this.toString();
        }
    }

    /**
     * SU table index
     * 
     * @author yogesh
     * 
     */
    private enum SIM_SU_IDX {
        DEVICE_MAC_IDX;

        public String getName() {
            return this.toString();
        }
    }

    private SimDBHelper() {
        openDB();
    }

    public synchronized static SimDBHelper getInstance() {
        return m_instance;
    }

    private void openDB() {
        File oFile = new File(DBFILE);
        boolean bCreateDB = false;
        try {
            if (!oFile.exists()) {
                bCreateDB = true;
            }
            m_DB = SqlJetDb.open(oFile, true);
            if (bCreateDB) {
                StringBuffer createSUtableQuery = new StringBuffer();
                createSUtableQuery.append("CREATE TABLE ").append(SIM_SU_TABLE).append("(")
                        .append(SIM_SU.DEVICE_MAC.getName()).append(" TEXT NOT NULL PRIMARY KEY ").append(", ")
                        .append(SIM_SU.SPROFILE_WEEKDAY.getName()).append(" blob ").append(", ")
                        .append(SIM_SU.SPROFILE_WEEKEND.getName()).append(" blob ").append(", ")
                        .append(SIM_SU.SPROFILE_HOLIDAY.getName()).append(" blob ").append(", ")
                        .append(SIM_SU.GPRPFILE.getName()).append(" blob").append(")");
                String createSuNameIndexQuery = "CREATE INDEX " + SIM_SU_IDX.DEVICE_MAC_IDX.getName() + " ON "
                        + SIM_SU_TABLE + "(" + SIM_SU.DEVICE_MAC.getName() + ")";
                oLogger.info("Creating SU table " + createSUtableQuery.toString());
                oLogger.info("Creating SU table idx " + createSuNameIndexQuery);
                m_DB.beginTransaction(SqlJetTransactionMode.WRITE);
                m_DB.createTable(createSUtableQuery.toString());
                m_DB.createIndex(createSuNameIndexQuery);
                m_DB.commit();
            }
        } catch (SqlJetException e) {
            oLogger.warning(e.getMessage());
        } finally {
        }
    }

    private void close() {
        try {
            if (m_DB != null)
                m_DB.close();
        } catch (SqlJetException e) {
            oLogger.warning(e.getMessage());
        }
    }

    public void shutdown() {
        close();
    }

    public void addSU(SensorUnit su) {
        try {
            m_DB.beginTransaction(SqlJetTransactionMode.WRITE);
            ISqlJetTable table = m_DB.getTable(SIM_SU_TABLE);
            try {
                ISqlJetCursor cursor = table.lookup(SIM_SU_IDX.DEVICE_MAC_IDX.getName(), su.getsName());
                if (cursor.eof()) {
                    oLogger.info(su.getsName() + " adding to db");
                    Profile oProfile = su.getProfile();
                    if (oProfile != null) {
                        byte[] sProfileWeekday = oProfile.getScheduleProfile(Profile.WEEKDAY);
                        byte[] sProfileWeekend = oProfile.getScheduleProfile(Profile.WEEKEND);
                        byte[] sProfileHoliday = oProfile.getScheduleProfile(Profile.HOLIDAY);
                        byte[] gProfile = oProfile.getAdvanceProfile();
                        table.insert(su.getsName(), sProfileWeekday, sProfileWeekend, sProfileHoliday, gProfile);
                    }
                } else {
                    Profile oProfile = su.getProfile();
                    oLogger.info(su.getsName() + " already in db, updating...");
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_SU.SPROFILE_WEEKDAY.getName()), 0, 0, false);
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_SU.SPROFILE_WEEKEND.getName()), 1, 0, false);
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_SU.SPROFILE_HOLIDAY.getName()), 2, 0, false);
                    oProfile.setAdvanceProfile(cursor.getBlobAsArray(SIM_SU.GPRPFILE.getName()), 0, false);
                }
                printRecords(cursor);
            } catch (SqlJetException e) {
                oLogger.warning(e.getMessage());
            }
            m_DB.commit();
        } catch (SqlJetException e1) {
            oLogger.warning(e1.getMessage());
        } finally {
            // dumpDatabase();
        }
    }

    public void updateProfile(String suname, byte[] prf, int type) {
        ISqlJetCursor cursor = null;
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            m_DB.beginTransaction(SqlJetTransactionMode.WRITE);
            ISqlJetTable table = m_DB.getTable(SIM_SU_TABLE);
            cursor = table.lookup(SIM_SU_IDX.DEVICE_MAC_IDX.getName(), suname);
            if (!cursor.eof()) {
                switch (type) {
                case Profile.WEEKDAY:
                    values.put(SIM_SU.SPROFILE_WEEKDAY.getName(), prf);
                    oLogger.fine(suname + " Weekday: " + Utils.getPacket(prf));
                    break;
                case Profile.WEEKEND:
                    values.put(SIM_SU.SPROFILE_WEEKEND.getName(), prf);
                    oLogger.fine(suname + " Weekend: " + Utils.getPacket(prf));
                    break;
                case Profile.HOLIDAY:
                    values.put(SIM_SU.SPROFILE_HOLIDAY.getName(), prf);
                    oLogger.fine(suname + " Holiday: " + Utils.getPacket(prf));
                    break;
                case Profile.ADVANCE:
                    values.put(SIM_SU.GPRPFILE.getName(), prf);
                    oLogger.fine(suname + " Advance: " + Utils.getPacket(prf));
                    break;
                }
                cursor.updateByFieldNames(values);
            }
        } catch (SqlJetException e) {
            oLogger.warning(e.getMessage());
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                m_DB.commit();
            } catch (SqlJetException e) {
                oLogger.warning(e.getMessage());
            }
        }
    }

    private void dumpDatabase() {
        try {
            ISqlJetTable table = m_DB.getTable(SIM_SU_TABLE);

            m_DB.beginTransaction(SqlJetTransactionMode.READ_ONLY);
            printRecords(table.order(table.getPrimaryKeyIndexName()));
        } catch (SqlJetException e) {
            oLogger.warning(e.getMessage());
        } finally {
            try {
                m_DB.commit();
            } catch (SqlJetException e) {
                oLogger.warning(e.getMessage());
            }
        }
    }

    private void printRecords(ISqlJetCursor cursor) throws SqlJetException {
        try {
            if (!cursor.eof()) {
                do {
                    oLogger.info(cursor.getRowId() + " : " + cursor.getString(SIM_SU.DEVICE_MAC.getName()) + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_SU.SPROFILE_WEEKDAY.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_SU.SPROFILE_WEEKEND.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_SU.SPROFILE_HOLIDAY.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_SU.GPRPFILE.getName())) + "}");
                } while (cursor.next());
            }
        } finally {
            cursor.close();
        }
    }

}
