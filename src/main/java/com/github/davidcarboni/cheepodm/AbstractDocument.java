package com.github.davidcarboni.cheepodm;

import org.bson.types.ObjectId;

import com.google.gson.annotations.SerializedName;

public abstract class AbstractDocument {

	@SerializedName("_id")
	private ObjectId id;

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

}
