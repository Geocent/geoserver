package org.geotools.xacml.geoxacml.attr;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.GML;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.GeometryCollector;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.dataTypeAttribute.impl.AbstractDataTypeAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeometryDataTypeAttribute  extends AbstractDataTypeAttribute<Geometry> {
    private static final Logger Log =
            Logger.getLogger(GeometryDataTypeAttribute.class.getName());

    private final GML.Version gmlVersion;

    public final static String ID = "urn:ogc:def:dataType:geoxacml:1.0:geometry";

    public GeometryDataTypeAttribute(){
        super();
        this.gmlVersion = GML.Version.GML3;
    }

    @Override
    public String getDatatypeURI() {
        return ID;
    }

    @Override
    public Geometry convertTo(List<?> jaxbRepresentation) throws SyntaxException {
        for(Object o : jaxbRepresentation){
            try{
                GML gml = new GML(gmlVersion);
                Node n = null;
                String xml = null;
                if(o instanceof String){
                    xml = (String)o;
                }else if(o instanceof Node){
                    n = (Node)o;

                    StringWriter sw = new StringWriter();
                    Transformer t = TransformerFactory.newInstance().newTransformer();
                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    t.transform(new DOMSource(n), new StreamResult(sw));
                    xml = sw.toString();
                }


                if(xml != null){
                    ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());

                    if(n == null){
                        n = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in).getDocumentElement();
                    }

                    //TODO: try to interpret from 'n' which version of GML we're working with
                    SimpleFeatureIterator iter = gml.decodeFeatureIterator(in);

                    GeometryCollector collector = new GeometryCollector();

                    while(iter.hasNext()){
                        SimpleFeature feature = iter.next();
                        collector.add((Geometry) feature.getAttribute("GEOMETRY"));
                    }

                    return collector.collect();

                }else{
                    continue;
                }
            }catch (Exception e){
                Log.log(Level.WARNING, "Couldn't convert GeometryDataTypeAttribute.", e);
                continue;
            }
        }
        return null;
    }
}
