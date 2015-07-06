# Avro based serialization format for simple features
This project aims to create an avro based serialization format and tooling around for persistence of SimpleFeatures in a single file.
The initial use case is  
  * a stable format for backups 
  * a database persistence agnostic format - provides a mechanism for importing and exporting data when geowave has updated the database structure
  
# The schema
The data contained is basically the ogc simple feature spec + classification.  Classification can be handled one of two ways:
  * default classification for each attribute
    * this is stored once at the feature collection level, as a parallel array to the feature attributes
  * per instance/attribute classification
    * this is stored at the feature level, in a parallel array to the attribute value
  
The base collection is in the scope of one feature type, and can hold any number of instances of that feture type.  The following fields are stored at the feature collection level, and so only need to be written once per collection: 
  * feature type name
  * attribute types (array)
  * attribute default classifications (array) (can be null only if classifications are provided per feature instance)
  
There is also an array of the "AttributeValues" type stored with each feature collection.  This array has the following members
  * feature id
  * values (array of bytes)
  * classification (array of strings) (can be null if a default classification is provided at the feature collection level)
  
note that the order of arrays here is meaningful - i.e.   the value at index 0  in AttributeValues.values is typed by the attributetype at index 0 in the feature collection and names by the attributename at index 0 in the feature collection.

# Avro Schema II

Or, instead of all of that, here's the avro schema: 
  * https://github.com/chrisbennight/ImportExport/blob/master/src/main/avro/SingleFeatureCollection.avsc
  


  
