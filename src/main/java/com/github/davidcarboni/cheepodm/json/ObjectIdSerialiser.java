package com.github.davidcarboni.cheepodm.json;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ObjectIdSerialiser implements JsonSerializer<ObjectId>,
		JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonElement element, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		ObjectId result = null;
		String string = element.getAsJsonObject().get("$oid").getAsString();
		if (StringUtils.isNotBlank(string)) {
			result = new ObjectId(string);
		}
		return result;
	}

	@Override
	public JsonElement serialize(ObjectId id, Type type,
			JsonSerializationContext context) {
		JsonElement result = null;
		if (id != null) {
			result = new JsonPrimitive(id.toString());
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("$oid", id.toStringMongod());
			result = jsonObject;
		}
		return result;
	}
}