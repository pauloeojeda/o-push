package org.obm.push.backend.obm22.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obm.pool.OBMPoolActivator;
import org.obm.push.backend.BackendSession;

import fr.aliasource.utils.JDBCUtils;

public class ObmDbHelper {

	private static final Log logger = LogFactory.getLog(ObmDbHelper.class);

	public static Set<String> findHost(BackendSession bs,
			String service, String prop) {
		HashSet<String> ret = new HashSet<String>();

		String q = "SELECT host_ip "
				+ "FROM Domain "
				+ "INNER JOIN DomainEntity ON domainentity_domain_id=domain_id "
				+" INNER JOIN ServiceProperty ON serviceproperty_entity_id=domainentity_entity_id "
				+ "INNER JOIN Host ON CAST(host_id as CHAR) = serviceproperty_value "
				+ "WHERE (domain_name=? OR domain_global) "
				+ "AND serviceproperty_service=? "
				+ "AND serviceproperty_property=?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int idx = bs.getLoginAtDomain().indexOf("@");
		String domain = bs.getLoginAtDomain().substring(idx + 1);
		try {
			con = OBMPoolActivator.getDefault().getConnection();
			ps = con.prepareStatement(q);
			ps.setString(1, domain);
			ps.setString(2, service);
			ps.setString(3, prop);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(rs.getString(1));
			}

		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return ret;
	}
}
