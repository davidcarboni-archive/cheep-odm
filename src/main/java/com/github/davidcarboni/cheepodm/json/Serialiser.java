package com.github.davidcarboni.cheepodm.json;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serialiser {

	public static final String dateFormat = "MMMM dd, yyyy HH:mm:ss";

	public static String serialise(Object object) {
		Gson gson = newBuilder().create();
		return gson.toJson(object);
	}

	public static <T> T deserialise(String json, Class<T> type) {
		Gson gson = newBuilder().create();
		return gson.fromJson(json, type);
	}

	private static GsonBuilder newBuilder() {
		GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(ObjectId.class, new ObjectIdSerialiser());
		result.registerTypeAdapter(DateTime.class, new DateTimeSerialiser());
		result.setDateFormat(dateFormat);

		return result;
	}
}
