package com.bundee.msfw.servicefw.fw;

import java.io.IOException;

import com.bundee.msfw.defs.UTF8String;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class UTF8StringSerializer extends TypeAdapter<UTF8String> {

	@Override
	public UTF8String read(JsonReader jReader) throws IOException {
		return new UTF8String(jReader.nextString());
	}

	@Override
	public void write(JsonWriter jWriter, UTF8String val) throws IOException {
		if(val != null) {
			jWriter.value(val.getUTF8String());
		} else {
			jWriter.value((String) null);
		}
	}

}
