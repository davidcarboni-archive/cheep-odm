package com.github.davidcarboni.cheepodm.json;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeSerialiser implements JsonSerializer<DateTime>,
		JsonDeserializer<DateTime> {

	public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	@Override
	public JsonElement serialize(DateTime dateTime, Type type,
			JsonSerializationContext context) {
		DateFormat df = new SimpleDateFormat(dateFormat);
		return new JsonPrimitive(df.format(dateTime.toDate()));
	}

	@Override
	public DateTime deserialize(JsonElement element, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		DateFormat df = new SimpleDateFormat(dateFormat);
		try {
			return new DateTime(df.parse(element.getAsString()));
		} catch (ParseException e) {
			return null;
		}
	}
}
