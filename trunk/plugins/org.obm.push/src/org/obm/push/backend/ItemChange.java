package org.obm.push.backend;

public class ItemChange {

	public String serverId;
	public String parentId;
	public String displayName;
	public ItemType itemType;
	private IApplicationData data;

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	public IApplicationData getData() {
		return data;
	}

	public void setData(IApplicationData data) {
		this.data = data;
	}

}
