package com.github.davidcarboni.cheepodm.json;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serialiser {

	public static final String bsonDateFormat = "MMMM dd, yyyy HH:mm:ss";
	private Map<Class<?>, Object> typeAdapters = new HashMap<>();

	/**
	 * Constructs an instance with a custom type adapter for Bson ID.
	 */
	public Serialiser() {
		// Default constructor
	}

	/**
	 * Constructs an instance with a custom type adapter for Bson ID, plus the
	 * supplied type adapters.
	 * 
	 * @param typeAdapters
	 *            Custom type adapters for Gson serialisation.
	 */
	public Serialiser(Map<Class<?>, Object> typeAdapters) {
		this.typeAdapters.putAll(typeAdapters);
	}

	/**
	 * Constructs an instance with a custom type adapter for Bson ID, plus one
	 * additional supplied type adapter. This is convenient if you have just one
	 * custom adapter and don't want to create a map each time to call
	 * {@link #Serialiser(Map)}.
	 * 
	 * @param typeAdapters
	 *            Custom type adapters for Gson serialisation.
	 */
	public Serialiser(Class<?> type, Object typeAdapter) {
		this.typeAdapters.put(type, typeAdapter);
	}

	/**
	 * Serialises the given object to Json.
	 * 
	 * @param object
	 *            To be serialised.
	 * @return The Json as a String.
	 */
	public String serialise(Object object) {
		Gson gson = newBuilder().create();
		return gson.toJson(object);
	}

	/**
	 * Deserialises the given json String.
	 * 
	 * @param json
	 *            The Json to deserialise.
	 * @param type
	 *            The type to deserialise into.
	 * @return A new instance of the given type.
	 */
	public <T> T deserialise(String json, Class<T> type) {
		Gson gson = newBuilder().create();
		return gson.fromJson(json, type);
	}

	/**
	 * @return A {@link GsonBuilder} with an {@link ObjectIdSerialiser} type
	 *         adapter registered, plus any additional adapters from
	 *         {@link #typeAdapters}.
	 */
	private GsonBuilder newBuilder() {
		GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(ObjectId.class, new ObjectIdSerialiser());
		for (Class<?> type : typeAdapters.keySet()) {
			result.registerTypeAdapter(type, typeAdapters.get(type));
		}
		result.setDateFormat(bsonDateFormat);

		return result;
	}
}
