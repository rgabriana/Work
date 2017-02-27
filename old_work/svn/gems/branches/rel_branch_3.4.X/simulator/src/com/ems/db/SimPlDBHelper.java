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

import com.ems.commands.plprofile.Profile;
import com.ems.plugload.PlugloadUnit;
import com.ems.utils.Utils;

/**
 * @author yogesh
 *
 */
public class SimPlDBHelper {
    public Logger oLogger = Logger.getLogger(SimPlDBHelper.class.getName());

    private static final SimPlDBHelper m_instance = new SimPlDBHelper();
    private static final String DBFILE = "simpl.db";
    private static final String SIM_PLUGLOAD_TABLE = "sim_pl";
    private SqlJetDb m_DB = null;


    /**
     * PL table
     * 
     * @author yogesh
     * 
     */
    private enum SIM_PL {
        DEVICE_MAC, SPROFILE_WEEKDAY, SPROFILE_WEEKEND, SPROFILE_HOLIDAY, SPROFILE_OVERRIDE2, GPRPFILE;

        public String getName() {
            return this.toString();
        }
    }

    /**
     * PL table index
     * 
     * @author yogesh
     * 
     */
    private enum SIM_PL_IDX {
        DEVICE_MAC_IDX;

        public String getName() {
            return this.toString();
        }
    }

    private SimPlDBHelper() {
        openDB();
    }

    public synchronized static SimPlDBHelper getInstance() {
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
                StringBuffer createPLtableQuery = new StringBuffer();
                createPLtableQuery.append("CREATE TABLE ").append(SIM_PLUGLOAD_TABLE).append("(")
                        .append(SIM_PL.DEVICE_MAC.getName()).append(" TEXT NOT NULL PRIMARY KEY ").append(", ")
                        .append(SIM_PL.SPROFILE_WEEKDAY.getName()).append(" blob ").append(", ")
                        .append(SIM_PL.SPROFILE_WEEKEND.getName()).append(" blob ").append(", ")
                        .append(SIM_PL.SPROFILE_HOLIDAY.getName()).append(" blob ").append(", ")
                        .append(SIM_PL.SPROFILE_OVERRIDE2.getName()).append(" blob ").append(", ")
                        .append(SIM_PL.GPRPFILE.getName()).append(" blob").append(")");
                String createSuNameIndexQuery = "CREATE INDEX " + SIM_PL_IDX.DEVICE_MAC_IDX.getName() + " ON "
                        + SIM_PLUGLOAD_TABLE + "(" + SIM_PL.DEVICE_MAC.getName() + ")";
                oLogger.info("Creating PL table " + createPLtableQuery.toString());
                oLogger.info("Creating PL table idx " + createSuNameIndexQuery);
                m_DB.beginTransaction(SqlJetTransactionMode.WRITE);
                m_DB.createTable(createPLtableQuery.toString());
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

    public void addPL(PlugloadUnit pl) {
        try {
            m_DB.beginTransaction(SqlJetTransactionMode.WRITE);
            ISqlJetTable table = m_DB.getTable(SIM_PLUGLOAD_TABLE);
            try {
                ISqlJetCursor cursor = table.lookup(SIM_PL_IDX.DEVICE_MAC_IDX.getName(), pl.getsName());
                if (cursor.eof()) {
                    oLogger.info(pl.getsName() + " adding to db");
                    Profile oProfile = pl.getProfile();
                    if (oProfile != null) {
                        byte[] sProfileWeekday = oProfile.getScheduleProfile(Profile.WEEKDAY);
                        byte[] sProfileWeekend = oProfile.getScheduleProfile(Profile.WEEKEND);
                        byte[] sProfileHoliday = oProfile.getScheduleProfile(Profile.HOLIDAY);
                        byte[] sProfileOverride2 = oProfile.getScheduleProfile(Profile.OVERRIDE2);
                        byte[] gProfile = oProfile.getAdvanceProfile();
                        table.insert(pl.getsName(), sProfileWeekday, sProfileWeekend, sProfileHoliday, sProfileOverride2, gProfile);
                    }
                } else {
                    Profile oProfile = pl.getProfile();
                    oLogger.info(pl.getsName() + " already in db, updating...");
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_PL.SPROFILE_WEEKDAY.getName()), 0, 0, false);
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_PL.SPROFILE_WEEKEND.getName()), 1, 0, false);
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_PL.SPROFILE_HOLIDAY.getName()), 2, 0, false);
                    oProfile.setScheduleProfile(cursor.getBlobAsArray(SIM_PL.SPROFILE_OVERRIDE2.getName()), 3, 0, false);
                    oProfile.setAdvanceProfile(cursor.getBlobAsArray(SIM_PL.GPRPFILE.getName()), 0, false);
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
            ISqlJetTable table = m_DB.getTable(SIM_PLUGLOAD_TABLE);
            cursor = table.lookup(SIM_PL_IDX.DEVICE_MAC_IDX.getName(), suname);
            if (!cursor.eof()) {
                switch (type) {
                case Profile.WEEKDAY:
                    values.put(SIM_PL.SPROFILE_WEEKDAY.getName(), prf);
                    oLogger.fine(suname + " Weekday: " + Utils.getPacket(prf));
                    break;
                case Profile.WEEKEND:
                    values.put(SIM_PL.SPROFILE_WEEKEND.getName(), prf);
                    oLogger.fine(suname + " Weekend: " + Utils.getPacket(prf));
                    break;
                case Profile.HOLIDAY:
                    values.put(SIM_PL.SPROFILE_HOLIDAY.getName(), prf);
                    oLogger.fine(suname + " Holiday: " + Utils.getPacket(prf));
                    break;
                case Profile.OVERRIDE2:
                    values.put(SIM_PL.SPROFILE_OVERRIDE2.getName(), prf);
                    oLogger.fine(suname + " Override2: " + Utils.getPacket(prf));
                    break;
                case Profile.ADVANCE:
                    values.put(SIM_PL.GPRPFILE.getName(), prf);
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
            ISqlJetTable table = m_DB.getTable(SIM_PLUGLOAD_TABLE);

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
                    oLogger.info(cursor.getRowId() + " : " + cursor.getString(SIM_PL.DEVICE_MAC.getName()) + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_PL.SPROFILE_WEEKDAY.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_PL.SPROFILE_WEEKEND.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_PL.SPROFILE_HOLIDAY.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_PL.SPROFILE_OVERRIDE2.getName())) + "}, " + " {"
                            + Utils.getPacket(cursor.getBlobAsArray(SIM_PL.GPRPFILE.getName())) + "}");
                } while (cursor.next());
            }
        } finally {
            cursor.close();
        }
    }
}
