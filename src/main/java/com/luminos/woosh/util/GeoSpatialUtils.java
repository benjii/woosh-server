package com.luminos.woosh.util;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * 
 * @author Ben
 */
public class GeoSpatialUtils {

	private static final DecimalFormat DEFAULT_DECIMAL_FORMATTER = new DecimalFormat("###.000000");
	
	public static final Integer LAT_LONG = 4326;
	
	
	/**
	 * Creates a point from an given latitude and longitude.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static final Point createPoint(String latitude, String longitude) {
		Coordinate location = null;
		
		if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude)) {
			return createPoint(0.0D, 0.0D);
		}
		
		try {
			location = new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("When constructing a point from string-encoded coordinates, those coordinates must be parsable to type Double.");
		}
		
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
		
		return geometryFactory.createPoint(location);
	}

	/**
	 * Creates a point from an given latitude and longitude.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static final Point createPoint(Double latitude, Double longitude) {
		Coordinate location = new Coordinate(latitude, longitude);
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));

		return geometryFactory.createPoint(location);
	}
	
	/**
	 * Serialises a point to a latitude / longitude string.
	 * 
	 * @param point
	 * @return
	 */
	public static final String pointAsString(Point point) {
		return DEFAULT_DECIMAL_FORMATTER.format(point.getX()) + "," + DEFAULT_DECIMAL_FORMATTER.format(point.getY());
	}

}
