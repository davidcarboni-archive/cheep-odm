package com.github.davidcarboni.cheepodm;

import org.bson.types.ObjectId;

public interface Row {

	/**
	 * @return the id
	 */
	ObjectId getId();

	/**
	 * @param id
	 *            the id to set
	 */
	void setId(ObjectId id);

}
