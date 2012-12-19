package org.ng12306.tpms.runtime;

import java.util.UUID;

public class Entity {
    private UUID _id;

	public UUID getId() {
		return this._id;
	}

	public void setId(UUID value) {
		this._id = value;
	}
}
