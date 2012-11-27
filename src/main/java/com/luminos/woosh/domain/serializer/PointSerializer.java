package com.luminos.woosh.domain.serializer;

import com.luminos.woosh.synchronization.Serializer;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Ben
 */
public class PointSerializer implements Serializer {

	@Override
	public String serialize(Object value) {
		return ((Point) value).toText();
	}

	@Override
	public Object deserialize(String value) {
		WKTReader geometryReader = new WKTReader();

		try {
			return (Point) geometryReader.read(value);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
