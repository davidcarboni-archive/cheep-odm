package com.github.davidcarboni.cheepodm.json;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SerialiserTest {

	@Test
	public void shouldSerializeObjecId() {

		// Given
		ObjectId id = new ObjectId();
		Serialiser serialiser = new Serialiser();

		// When
		String json = serialiser.serialise(id);
		ObjectId recovered = serialiser.deserialise(json, ObjectId.class);

		// Then
		assertEquals(id, recovered);
	}

	@Test
	public void shouldBeAbleToOverrideObjecIdAdapter() {

		// Given
		// A different type adapter for ObjectId:
		JsonSerializer<ObjectId> customObjectIdSerialiser = new JsonSerializer<ObjectId>() {

			@Override
			public JsonElement serialize(ObjectId id, Type type,
					JsonSerializationContext context) {
				return new JsonPrimitive("wibble");
			}
		};
		Map<Class<?>, Object> typeAdapters = new HashMap<>();
		typeAdapters.put(ObjectId.class, customObjectIdSerialiser);
		Serialiser serialiser = new Serialiser(typeAdapters);

		// When
		String json = serialiser.serialise(new ObjectId());

		// Then
		assertEquals("\"wibble\"", json);
	}

}
