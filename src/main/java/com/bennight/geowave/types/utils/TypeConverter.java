package com.bennight.geowave.types.utils;

import com.bennight.geowave.types.generated.AttributeValues;
import com.bennight.geowave.types.generated.FeatureDefinition;
import com.bennight.geowave.types.generated.SingleFeatureCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import mil.nga.giat.geowave.core.store.data.field.FieldUtils;
import mil.nga.giat.geowave.core.store.data.field.FieldWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Note - class is not threadsafe
 */
public class TypeConverter {
    private final EncoderFactory ef = EncoderFactory.get();
    private final DecoderFactory df = DecoderFactory.get();
    private final SpecificDatumWriter<SingleFeatureCollection> datumWriter = new SpecificDatumWriter<SingleFeatureCollection>();
    private final SpecificDatumReader<SingleFeatureCollection> datumReader = new SpecificDatumReader<SingleFeatureCollection>();
    private final WKBWriter wkbWriter = new WKBWriter(3);

    /***
     * @param avroObject Avro object to serialized
     * @return byte array of serialized avro data
     * @throws IOException
     */
    public byte[]  serializeSingleFeatureCollection(final SingleFeatureCollection avroObject) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BinaryEncoder encoder = ef.binaryEncoder(os, null);
        datumWriter.setSchema(avroObject.getSchema());
        datumWriter.write(avroObject, encoder);
        encoder.flush();
        return os.toByteArray();
    }

    /***
     *
     * @param features Features to be serialized (must have at least one value)
     * @param avroObjectToReuse null or SingleFeatureCollection instance to be re-used.  If null a new instance will be allocated
     * @param defaultClassifications null map of attribute names vs. classification.  if null all values will be set to the default classification
     * @param defaultClassification null or default classification.  if null and defaultClassifications are not provided an exception will be thrown
     * @return
     * @throws IOException
     */
    public byte[] serializeSingleFeatureCollection(final List<SimpleFeature> features, SingleFeatureCollection avroObjectToReuse, Map<String, String> defaultClassifications, String defaultClassification) throws IOException {
        if (features == null || features.size() == 0) {
            throw new IOException("Collection can not be null and must have at least one value");
        }

        if (defaultClassification == null && defaultClassifications == null){
            throw new IOException("if per attribute classifications aren't provided then a default classification must be provided");
        }

        SimpleFeature sf = features.get(1);
        SimpleFeatureType sft = sf.getType();
        if (avroObjectToReuse == null){
            avroObjectToReuse = new SingleFeatureCollection();
        }

        FeatureDefinition fd = avroObjectToReuse.getFeatureType();
        if (fd == null) {
        	fd = new FeatureDefinition();
        }
        fd.setFeatureTypeName(sft.getTypeName());
        
        List<String> attributes = new ArrayList<>(sft.getAttributeCount());
        List<String> types = new ArrayList<>(sft.getAttributeCount());
        List<String> classifications = new ArrayList<>(sft.getAttributeCount());
        String classification = null;
        for (AttributeDescriptor attr : sft.getAttributeDescriptors()){
            attributes.add(attr.getLocalName());
            types.add(attr.getType().getBinding().toString());
            if (defaultClassifications != null) {
                classification = defaultClassifications.get(attr.getLocalName());
            }
            if (classification == null) {
                classification = defaultClassification;
            }
            if (classification == null) {
                throw new IOException("No default classification was provided, and no classification for: '" + attr.getLocalName() + "' was provided");
            }
            classifications.add(classification);
            classification = null;
        }

        
        fd.setAttributeNames(attributes);
        fd.setAttributeTypes(types);
        fd.setAttributeDefaultClassifications(classifications);
        avroObjectToReuse.setFeatureType(fd);

        List<AttributeValues> attributeValues = new ArrayList<>(features.size());

        for (SimpleFeature feat : features){
            AttributeValues av = new AttributeValues();
            av.setFid(feat.getID());
            List<ByteBuffer> values = new ArrayList<>(sft.getAttributeCount());
            for (AttributeDescriptor attr : sft.getAttributeDescriptors()){
                Object o = feat.getAttribute(attr.getLocalName());
                byte[] bytes = null;
                if (o instanceof Geometry) {
                    bytes = wkbWriter.write((Geometry)o);
                } else {
                    FieldWriter fw = FieldUtils.getDefaultWriterForClass(attr.getType().getBinding());
                    bytes = fw.writeField(o);
                }
                values.add(ByteBuffer.wrap(bytes));
            }
            av.setValues(values);
            attributeValues.add(av);
        }

        avroObjectToReuse.setValues(attributeValues);
        return serializeSingleFeatureCollection(avroObjectToReuse);
    }




    /***
     * @param avroData serialized bytes of SingleFeatureCollection
     * @param avroObjectToReuse null or SingleFeatureCollection instance to be re-used.  If null a new object will be allocated.
     * @return instance of SingleFeatureCollection with values parsed from avroData
     * @throws IOException
     */
    public SingleFeatureCollection deserializeSingleFeatureCollection(final byte[] avroData, SingleFeatureCollection avroObjectToReuse) throws IOException {
        BinaryDecoder decoder = df.binaryDecoder(avroData, null);
        if (avroObjectToReuse == null){
            avroObjectToReuse = new SingleFeatureCollection();
        }
        datumReader.setSchema(avroObjectToReuse.getSchema());
        return datumReader.read(avroObjectToReuse, decoder);
    }




}
