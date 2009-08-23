/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.obm.pool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.minig.obm.pool.impl.ObmConfIni;
import org.osgi.framework.BundleContext;

import fr.aliasource.obm.aliapool.PoolActivator;
import fr.aliasource.obm.aliapool.pool.DataSource;
import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.utils.RunnableExtensionLoader;

/**
 * Creates a connection to the OBM database referenced in the
 * <code>/etc/obm/obm_conf.ini</code> file.
 * 
 * @author tom
 * 
 */
public class OBMPoolActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.minig.obm.pool";

	// The shared instance
	private static OBMPoolActivator plugin;

	private Log logger = LogFactory.getLog(getClass());

	private DataSource ds;
	private String lastInsertIdQuery;

	/**
	 * The constructor
	 */
	public OBMPoolActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		logger.info("Starting OBM connection pool...");

		try {

			ObmConfIni oci = new ObmConfIni();
			String dbType = oci.get("dbtype");
			String login = oci.get("user");
			String password = oci.get("password");
			String dbName = oci.get("db");
			String dbHost = oci.get("host");

			logger.info("dbtype from obm_conf.ini is " + dbType);
			IJDBCDriver cf = null;
			RunnableExtensionLoader<IJDBCDriver> rel = new RunnableExtensionLoader<IJDBCDriver>();
			List<IJDBCDriver> factories = rel.loadExtensions(PLUGIN_ID,
					"jdbcdriver", "jdbc_driver", "implementation");
			for (IJDBCDriver icf : factories) {
				if (icf.getSupportedDbType().equalsIgnoreCase(dbType)) {
					cf = icf;
					break;
				}
			}
			if (cf == null) {
				logger
						.error("No connection factory found for dbtype "
								+ dbType);
				throw new Exception("No connection factory found for dbtype "
						+ dbType);
			} else {
				String drvClass = cf.getDriverClass();
				String query = cf.getKeepAliveQuery();
				String jdbcUrl = cf.getJDBCUrl(dbHost, dbName, login, password);
				lastInsertIdQuery = cf.getLastInsertIdQuery();

				ds = PoolActivator.getDefault().createDataSource(drvClass,
						jdbcUrl, login, password, 4, query);
			}

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			throw new Exception(t);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		ds.stop();

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static OBMPoolActivator getDefault() {
		return plugin;
	}

	public Connection getConnection() {
		Connection con = null;
		try {
			con = ds.getConnection();
		} catch (Throwable e) {
			logger.error("Error getting SQL connection to the OBM database", e);
		}
		return con;
	}

	public int lastInsertId(Connection con) throws SQLException {
		int ret = 0;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(lastInsertIdQuery);
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} finally {
			JDBCUtils.cleanup(null, st, rs);
		}
		return ret;
	}

	public UserTransaction getUserTransaction() {
		return PoolActivator.getDefault().getTransactionManager();
	}

}
