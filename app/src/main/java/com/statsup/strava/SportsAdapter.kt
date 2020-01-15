package com.statsup.strava

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.statsup.Sports
import com.statsup.Sports.Companion.byCode
import com.statsup.Sports.WORKOUT

class SportsAdapter : TypeAdapter<Sports>() {

    override fun write(out: JsonWriter, value: Sports) {
        out.value(value.toString())
    }


    override fun read(reader: JsonReader): Sports {
        if (reader.peek() != JsonToken.NULL) {
            return byCode(reader.nextString())
        }
        reader.nextNull()
        return WORKOUT
    }
}
