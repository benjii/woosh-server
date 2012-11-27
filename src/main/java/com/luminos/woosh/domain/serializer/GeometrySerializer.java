package com.luminos.woosh.domain.serializer;

import com.luminos.woosh.synchronization.Serializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Ben
 */
public class GeometrySerializer implements Serializer {

	@Override
	public String serialize(Object value) {
		return ((Geometry) value).toText();
	}

	@Override
	public Object deserialize(String value) {		
		WKTReader geometryReader = new WKTReader();

		try {
			return geometryReader.read(value);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}


}
