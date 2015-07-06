package com.bennight.geowave.types.utils;

import com.bennight.geowave.types.generated.SingleFeatureCollection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.avro.generic.GenericData;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class TypeConverterTest {

    private final List<SimpleFeature> features = new ArrayList<SimpleFeature>();
    private final List<Map<String, String>> classifications = new ArrayList<Map<String,String>>();
    private SimpleFeatureType testPoint = null;
    private SimpleFeatureBuilder testPointBuilder = null;
    private SimpleFeatureTypeBuilder testPointTypeBuilder = new SimpleFeatureTypeBuilder();
    private final GeometryFactory gf = new GeometryFactory();
    private final TypeConverter tc = new TypeConverter();


    @Before
    public void init() {
        testPointTypeBuilder.add("geom", Geometry.class);
        testPointTypeBuilder.add("name", String.class);
        testPointTypeBuilder.add("source", String.class);
        testPointTypeBuilder.setName("testPoint");
        testPoint = testPointTypeBuilder.buildFeatureType();

        testPointBuilder = new SimpleFeatureBuilder(testPoint);

        testPointBuilder.set("geom", gf.createPoint(new Coordinate(1, 2)));
        testPointBuilder.set("name", "first geometry");
        testPointBuilder.set("source", "first source");

        features.add(testPointBuilder.buildFeature("first"));

        testPointBuilder.set("geom", gf.createPoint(new Coordinate(3,4)));
        testPointBuilder.set("name", "second geometry");
        testPointBuilder.set("source", "second source");

        features.add(testPointBuilder.buildFeature("second"));

        testPointBuilder.set("geom", gf.createPoint(new Coordinate(5,6)));
        testPointBuilder.set("name", "third geometry");
        testPointBuilder.set("source", "third source");

        features.add(testPointBuilder.buildFeature("third"));
    }


    @Test
    public void testSerializeDeserializeSingleFeatureCollection() throws Exception {

        byte[] serializedForm = tc.serializeSingleFeatureCollection(features, null, null, "");
        System.out.println(serializedForm.length);

    }

}