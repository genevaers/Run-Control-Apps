package org.genevaers.genevaio.yamlreader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.genevaers.genevaio.dbreader.DatabaseConnection;
import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.ViewNode;

import com.google.common.flogger.FluentLogger;

public abstract class YAMLReaderBase {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    protected boolean hasErrors = false;
    protected static String viewIdsString;
    protected static Set<Integer> viewIds = new TreeSet<>();

    protected static ViewNode currentViewNode;

    protected static Set<Integer> requiredLFs = new TreeSet<>();
    protected static Set<Integer> requiredPFs = new TreeSet<>();
    protected static Set<Integer> requiredLRs = new TreeSet<>();
    protected static Set<Integer> requiredExits = new TreeSet<>();
    protected static Set<Integer> lrlfAssociationIds = new TreeSet<>();
    protected static Set<Integer> lfpfAssociationIds = new TreeSet<>();

  	protected static Map<Integer, LRIndex> effdateStarts = new HashMap<>();
    protected static Map<Integer, LRIndex> effdateEnds = new HashMap<>();

    protected static String environmentName;


    protected void executeAndWriteToRepo(DatabaseConnection dbConnection, String query, DatabaseConnectionParams params, int id) {
        try(PreparedStatement ps = dbConnection.prepareStatement(query);) {
            ps.setInt(1, params.getEnvironmentIdAsInt());
            ps.setInt(2, id);
            executeAndAddResultSetToRepo(ps);
        } catch (SQLException e) {
            logger.atSevere().log("executeAndWriteToRepo %s", e.getMessage());
        }
    }

    protected void executeAndWriteToRepo(DatabaseConnection dbConnection, String query, DatabaseConnectionParams params, String name) {
        try(PreparedStatement ps = dbConnection.prepareStatement(query);) {
            ps.setInt(1, params.getEnvironmentIdAsInt());
            ps.setString(2, name.toUpperCase());
            executeAndAddResultSetToRepo(ps);
        } catch (SQLException e) {
            logger.atSevere().log("executeAndWriteToRepo %s", e.getMessage());
        }
    }

    protected void executeAndWriteToRepo(DatabaseConnection dbConnection, String query, DatabaseConnectionParams params, Set<Integer> idsIn) {
        try(PreparedStatement ps = dbConnection.prepareStatement(query);) {
            int parmNum = 1;
            ps.setInt(parmNum++, params.getEnvironmentIdAsInt());
            Iterator<Integer> ii = idsIn.iterator();
            while(ii.hasNext()) {
                ps.setInt(parmNum++, ii.next());
            }
            executeAndAddResultSetToRepo(ps);
        } catch (SQLException e) {
            logger.atSevere().log("executeAndWriteToRepo %s", e.getMessage());
        }
    }

    private void executeAndAddResultSetToRepo(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            addComponentToRepo(rs);
        }
    }

    protected abstract void addComponentToRepo(ResultSet rs) throws SQLException;

    protected static String getDefaultedString(String rsValue, String defVal) {
        return rsValue == null ? defVal : rsValue;
    }

    abstract public boolean addToRepo(DatabaseConnection dbConnection, DatabaseConnectionParams params);

    public static ViewNode getCurrentViewNode() {
        return currentViewNode;
    }

    protected String getIds(Set<Integer> inputSet) {
        String ids = "";
        inputSet.remove(0);
        if (inputSet.size() > 0) {
            Iterator<Integer> lri = inputSet.iterator();
            ids = lri.next().toString();
            while (lri.hasNext()) {
                ids += "," + lri.next().toString();
            }
        }
        return ids;
    }

    protected String getPlaceholders(int size) {
        StringBuilder builder = new StringBuilder();
        if (size > 1) {
            for (int i = 1; i < size; i++) {
                builder.append("?,");
            }
        }
        builder.append("?");
        return builder.toString();
    }

    protected String getPlaceholders(String ids) {
        String[] pls = ids.split(",");
        return getPlaceholders(pls.length);
    }

    public static void addViewId(int id) {
        viewIds.add(id);
    }

    public static void clearViewIds() {
        viewIds.clear();;
    }

	public static void fixupEffectiveDateIndexes() {
		addEffDateKeyFrom(effdateStarts.entrySet().iterator());
		addEffDateKeyFrom(effdateEnds.entrySet().iterator());
	}

	private static void addEffDateKeyFrom(Iterator<Entry<Integer, LRIndex>> efdi) {
		while (efdi.hasNext()) {
			Entry<Integer, LRIndex> efde = efdi.next();
			LRIndex effStartNdx = efde.getValue();
			LogicalRecord lr = Repository.getLogicalRecords().get(effStartNdx.getLrId());
			effStartNdx.setKeyNumber((short) (lr.getValuesOfIndexBySeq().size() + 1));
			lr.addToIndexBySeq(effStartNdx);
		}
	}

    public void setEnvironmentName(String n) {
        environmentName = n;
    }
}
