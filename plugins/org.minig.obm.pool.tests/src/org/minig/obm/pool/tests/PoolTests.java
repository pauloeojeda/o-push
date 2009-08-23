package org.minig.obm.pool.tests;

import org.minig.obm.pool.OBMPoolActivator;

import junit.framework.TestCase;

public class PoolTests extends TestCase {

	public void testCreatePool() {
		OBMPoolActivator opa = OBMPoolActivator.getDefault();
		assertNotNull(opa);
	}
	
}
