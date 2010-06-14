package org.obm.push.searchsource.ldap;

import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.IniFile;

class Configuration {

	private static final String LDAP_CONF_FILE = "/etc/opush/ldap_conf.ini";

	private static final String SEARCH_LDAP_URL = "search.ldap.url";
	private static final String SEARCH_LDAP_BASE = "search.ldap.basedn";
	private static final String SEARCH_LDAP_FILTER = "search.ldap.filter";

	private String baseDn;
	private String filter;
	private Properties env;
	private Log logger;
	private boolean validConf;

	public Configuration() {
		logger = LogFactory.getLog(getClass());
		IniFile ini = new IniFile(LDAP_CONF_FILE) {
			@Override
			public String getCategory() {
				return null;
			}
		};
		init(ini);
	}

	DirContext getConnection() throws NamingException {
		try {
			return new InitialDirContext(env);
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public void init(IniFile ini) {
		String url = ini.getData().get(SEARCH_LDAP_URL);
		baseDn = ini.getData().get(SEARCH_LDAP_BASE);
		filter = ini.getData().get(SEARCH_LDAP_FILTER);

		env = new Properties();
		if (url != null && baseDn != null && filter != null) {
			validConf = true;
		} else {
			logger.error("Can not find data in file " + LDAP_CONF_FILE
					+ ", research in ldap will not be activated");
			return;
		}

		if (!url.startsWith("ldap://")) {
			url = "ldap://" + url;
		}

		env.put("java.naming.factory.initial",
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", url);

		logger.info(Activator.PLUGIN_ID + " initialised, url: " + url
				+ " basedn: " + baseDn + " filter: " + filter
				+ " (valid conf: " + validConf + ")");
	}

	public String getBaseDn() {
		return baseDn;
	}

	public String getFilter() {
		return filter;
	}

	public void cleanup(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
			}
		}
	}

	public boolean isValid() {
		return validConf;
	}

}
