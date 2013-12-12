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
	 * @param document
	 *            The record to be created. On return, the ID will be set in
	 *            this object.
	 */
	public void create(Document document) {
		checkDocument(document);

		String collection = getCollection(document);
		String serialised = serialiser.serialise(document);
		DBObject dbObject = (DBObject) JSON.parse(serialised);
		database.getCollection(collection).insert(dbObject);
		document.setId((ObjectId) dbObject.get("_id"));
	}

	/**
	 * Reads a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param document
	 *            The record to be located. Only {@link Document#getId()} is
	 *            used to locate the record .
	 * @return The read {@link Document}. If the record does not exist, or if
	 *         the document parameter is null, null is returned.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Document> T read(T document) {
		checkId(document);

		// Read the object:
		String collection = getCollection(document);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", document.getId());
		DBObject dbObject = database.getCollection(collection).findOne(search);

		// Convert to the return type:
		String json = JSON.serialize(dbObject);
		return (T) serialiser.deserialise(json, document.getClass());
	}

	/**
	 * Updates a record.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param document
	 *            The {@link Document} to be updated. {@link Document#getId()}
	 *            is used to locate the record and update it using the given
	 *            document.
	 * @return The previous value of the record. If no matching record could be
	 *         found, null is returned.
	 */
	public <T extends Document> DBObject update(T document) {
		checkId(document);

		// Serialise the update:
		String serialised = serialiser.serialise(document);
		DBObject dbObject = (DBObject) JSON.parse(serialised);

		// Update the object:
		String collection = getCollection(document);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", document.getId());
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
	 * @param document
	 *            The {@link Document} to be deleted. Only
	 *            {@link Document#getId()} is used to locate the record.
	 * @return The previous value of the record. If no matching record could be
	 *         found, null is returned.
	 */
	public <T extends Document> boolean delete(T document) {
		checkId(document);

		// Delete the object:
		String collection = getCollection(document);
		BasicDBObject search = new BasicDBObject();
		search.put("_id", document.getId());
		return database.getCollection(collection).findAndRemove(search) != null;
	}

	/**
	 * Lists records matching the given criteria.
	 * 
	 * @param database
	 *            The Mongo {@link DB}.
	 * @param collection
	 *            The Mongo collection.
	 * @param document
	 *            The search criteria.
	 * @return A {@link List} of matching records.
	 */
	public <T extends Document> List<T> list(Class<T> type) {

		// Convert to the return type:
		List<T> result = new ArrayList<>();
		String collection = getCollection(type);
		DBCursor cursor = database.getCollection(collection).find();
		while (cursor.hasNext()) {
			String json = JSON.serialize(cursor.next());
			result.add(serialiser.deserialise(json, type));
		}

		return result;
	}

	/**
	 * Lists records matching the given criteria.
	 * 
	 * @param criteria
	 *            The search criteria.
	 * @return A {@link List} of matching records.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Document> List<T> search(T criteria) {
		checkDocument(criteria);

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
	 * Gets the collection for the type of the given {@link Document} instance.
	 * 
	 * @param document
	 *            The instance to determine the collection for.
	 * @return The collection name.
	 */
	private String getCollection(Document document) {
		return getCollection(document.getClass());
	}

	/**
	 * Gets the collection for the given type.
	 * 
	 * @param type
	 *            The type to determine the collection for.
	 * @return The collection name.
	 */
	private String getCollection(Class<? extends Document> type) {
		String result = null;
		Table table = type.getAnnotation(Table.class);
		if (table == null) {
			throw new IllegalArgumentException(type.getSimpleName()
					+ " is not annotated as a " + Table.class.getSimpleName()
					+ ". Are you sure this is right?");
		}
		result = StringUtils.defaultIfBlank(table.name(), type.getSimpleName());
		return result;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the parameter is null.
	 * 
	 * @param document
	 *            The instance to check.
	 */
	private void checkDocument(Document document) {

		// Check we have a document:
		if (document == null) {
			throw new IllegalArgumentException("No document provided.");
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the field of the parameter
	 * is null.
	 * 
	 * @param document
	 *            The instance to check.
	 */
	private void checkId(Document document) {

		// Check we have a document:
		checkDocument(document);

		// Check we have an ID:
		if (document.getId() == null) {
			throw new IllegalArgumentException(
					"The document ID has not been set.");
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
