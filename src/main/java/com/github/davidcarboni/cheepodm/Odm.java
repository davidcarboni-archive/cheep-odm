package com.github.davidcarboni.cheepodm;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.github.davidcarboni.cheepodm.json.Serialiser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;

public class Odm {

	private static MongoClient mongo;
	private DB database;
	private Serialiser serialiser;

	/**
	 * Constructs an instance with a default {@link Serialiser}.
	 * 
	 * @param database
	 *            The database to work with.
	 */
	public Odm(DB database) {
		this.database = database;
		serialiser = new Serialiser();
	}

	/**
	 * Constructs an instance with the given {@link Serialiser}.
	 * 
	 * @param database
	 *            The database to work with.
	 * @param serialiser
	 *            A customised selialiser (e.g. with additional type adapters).
	 */
	public Odm(DB database, Serialiser serialiser) {
		this.database = database;
		this.serialiser = serialiser;
	}

	/**
	 * Creates a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param row
	 *            The record to be created. On return, the ID will be set in
	 *            this object.
	 */
	public void create(Document row) {
		checkRow(row);

		String collection = getCollection(row);
		String serialised = serialiser.serialise(row);
		DBObject dbObject = (DBObject) JSON.parse(serialised);
		database.getCollection(collection).insert(dbObject);
		row.setId((ObjectId) dbObject.get("_id"));
	}

	/**
	 * Reads a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param row
	 *            The record to be located. Only {@link Document#getId()} is used to
	 *            locate the record .
	 * @return The read record. If the record does not exist, or if the row
	 *         parameter is null, null is returned.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Document> T read(T row) {
		checkId(row);

		// Read the object:
		String collection = getCollection(row);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", row.getId());
		DBObject dbObject = database.getCollection(collection).findOne(search);

		// Convert to the return type:
		String json = JSON.serialize(dbObject);
		return (T) serialiser.deserialise(json, row.getClass());
	}

	/**
	 * Updates a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param row
	 *            The record to be updated. {@link Document#getId()} is used to
	 *            locate the record and update it using the given row.
	 * @return The previous value of the record. If no matching record could be
	 *         found, null is returned.
	 */
	public <T extends Document> DBObject update(T row) {
		checkId(row);

		// Serialise the update:
		String serialised = serialiser.serialise(row);
		DBObject dbObject = (DBObject) JSON.parse(serialised);

		// Update the object:
		String collection = getCollection(row);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", row.getId());
		return database.getCollection(collection).findAndModify(search,
				dbObject);
	}

	/**
	 * Deletes a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param row
	 *            The record to be deleted. Only {@link Document#getId()} is used to
	 *            locate the record.
	 * @return The previous value of the record. If no matching record could be
	 *         found, null is returned.
	 */
	public <T extends Document> boolean delete(T row) {
		checkId(row);

		// Delete the object:
		String collection = getCollection(row);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", row.getId());
		return database.getCollection(collection).findAndRemove(search) != null;
	}

	/**
	 * Lists records matching the given criteria.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param row
	 *            The search criteria.
	 * @return A {@link List} of matching records.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Document> List<T> search(T criteria) {
		checkRow(criteria);

		// Set up the search object:
		String serialised = serialiser.serialise(criteria);
		DBObject search = (DBObject) JSON.parse(serialised);

		// Convert to the return type:
		List<T> result = new ArrayList<>();
		String collection = getCollection(criteria);
		DBCursor cursor = database.getCollection(collection).find(search);
		while (cursor.hasNext()) {
			String json = JSON.serialize(cursor.next());
			result.add((T) serialiser.deserialise(json, criteria.getClass()));
		}

		return result;
	}

	/**
	 * Gets the collection for the given row type.
	 * 
	 * @param row
	 *            The instance to determine the collection for.
	 * @return The collection name.
	 */
	private String getCollection(Document row) {
		String result = null;
		Table table = row.getClass().getAnnotation(Table.class);
		if (table == null) {
			throw new IllegalArgumentException(row.getClass().getSimpleName()
					+ " is not annotated as a " + Table.class.getSimpleName()
					+ ". Are you sure this is right?");
		}
		result = StringUtils.defaultIfBlank(table.name(), row.getClass()
				.getSimpleName());
		return result;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the parameter is null.
	 * 
	 * @param row
	 *            The instance to check.
	 */
	private void checkRow(Document row) {

		// Check we have a row:
		if (row == null) {
			throw new IllegalArgumentException("No row provided.");
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the field of the parameter
	 * is null.
	 * 
	 * @param row
	 *            The instance to check.
	 */
	private void checkId(Document row) {

		// Check we have a row:
		checkRow(row);

		// Check we have an ID:
		if (row.getId() == null) {
			throw new IllegalArgumentException("The row ID has not been set.");
		}
	}

	public static DB getDatabase(String mongoUri) throws UnknownHostException {

		MongoClientURI uri = new MongoClientURI(mongoUri);
		if (mongo == null) {
			mongo = new MongoClient(uri);
		}

		// Helpful error, rather than just a null pointer:
		if (StringUtils.isBlank(uri.getDatabase())) {
			throw new IllegalArgumentException(
					"No database is specified in the MongoDB URI: " + mongoUri);
		}

		return mongo.getDB(uri.getDatabase());
	}

}
