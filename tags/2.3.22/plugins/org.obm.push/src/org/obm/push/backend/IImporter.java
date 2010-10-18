package org.obm.push.backend;

import java.util.List;

public interface IImporter {

	void addChanges(List<ItemChange> lic);

	void addDeletions(List<ItemChange> lic);

}
