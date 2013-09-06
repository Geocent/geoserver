/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.xacml.geoxacml.cond;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.herasaf.xacml.core.function.FunctionProcessingException;
import org.herasaf.xacml.core.function.impl.bagFunctions.AbstractBagFunction;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Mueller
 * 
 *         Abstract base class for GeoXACML functions.
 * 
 *         Also responsible for coordinate transformations. WGS84 is the common CRS, if
 *         transformation has to be done, the target CRS is always WGS84
 * 
 */
public abstract class GeoXACMLBagFunctionBase extends AbstractBagFunction {

    protected static final String NAME_PREFIX = "urn:ogc:def:function:geoxacml:1.0:";

    protected static final String COMMON_CRS_NAME = "EPSG:4326";

    protected static CoordinateReferenceSystem COMMON_CRS = null;

    private final String functionId;

    public GeoXACMLBagFunctionBase(String functionId) {
        this.functionId = functionId;
    }

    @Override
    public String getFunctionId(){
        return functionId;
    }

    abstract protected Class<?>[] getParams();
    abstract protected Collection<?> evaluate(Object ... objects) throws FunctionProcessingException;

    @Override
    public final Object handle(Object ... objects) throws FunctionProcessingException {
        Class<?>[] params = this.getParams();

        if(objects.length < params.length){
            throw new FunctionProcessingException("Too few arguments (" + objects.length +
                    ") to function '" + this.getFunctionId() + "'. Expected " + params.length + ".");
        }

        if(objects.length > params.length){
            throw new FunctionProcessingException("Too many arguments (" + objects.length +
                    ") to function '" + this.getFunctionId() + "'. Expected " + params.length + ".");
        }

        //check argument types
        for(int i=0;i<objects.length;i++){
            if(!params[i].isInstance(objects[i])){
                throw new FunctionProcessingException("Argument " + i + " to function '" + this.getFunctionId()
                + "' is incorrect type. Expected '" + params[i].getName() + "', got '" + objects[i].getClass().getName());
            }
        }

        return this.evaluate(objects);
    }


    /**
     * @param first
     *            Geometry object
     * @param second
     *            Geometry object
     * @return array with possible replaced Geometry (transformed to WGS84)
     * @throws org.geotools.xacml.geoxacml.cond.GeoXACMLException
     *
     *             This method tries to avoid transformations.
     *
     *             No transformation in the following situations:
     *
     *             1) Both have srsNames and equalsIgnoreCase is true
     *             3) Both CRS are decodeable and equalsIgnoreMetaData is true
     *
     *             Error situations are
     *
     *             1) a CRS is not decodeable
     *
     *             If we need a transformation, both geometries are transformd to WGS84
     */
    protected Geometry[] transformOnDemand(Geometry first, Geometry second) throws GeoXACMLException {
        String srs1 = "EPSG:" + first.getSRID();
        String srs2 = "EPSG:" + second.getSRID();

        if (srs1.equalsIgnoreCase(srs2))
            return new Geometry[] {first, second};

        CoordinateReferenceSystem crs1 = decodeCRS(srs1);
        CoordinateReferenceSystem crs2 = decodeCRS(srs2);

        if (crs1 == null)
            throw new GeoXACMLException("Cannod decode " + srs1);

        if (crs2 == null)
            throw new GeoXACMLException("Cannod decode " + srs2);

        if (CRS.equalsIgnoreMetadata(crs1, crs2)) // CRS are compatible
            return new Geometry[] {first, second};

        try {

                return new Geometry[]{
                    transformToCommonCRS(first, srs1, crs1),
                    transformToCommonCRS(first, srs2, crs2)
                };

        } catch (Exception e) {
            throw new GeoXACMLException(e);
        }
    }

    /**
     * @param g
     * @param srsName
     * @param sourceCRS
     * @return
     * @throws org.geotools.xacml.geoxacml.cond.GeoXACMLException
     *
     *             Transformation of a geomtry to WGS84
     *
     *             No transformation in the following situations
     *
     *             1) the srsName equalsIgnoreCase with EPSG:4326 is true 2) equalsIgnorMetaData
     *             returns true
     */
    protected Geometry transformToCommonCRS(Geometry g, String srsName,
            CoordinateReferenceSystem sourceCRS) throws GeoXACMLException {

        try {
            if (COMMON_CRS == null) {
                synchronized (COMMON_CRS_NAME) {
                    COMMON_CRS = CRS.decode(COMMON_CRS_NAME, true);
                }
            }

            if (COMMON_CRS_NAME.equalsIgnoreCase(srsName))
                return g;

            if (CRS.equalsIgnoreMetadata(sourceCRS, COMMON_CRS))
                return g;

            MathTransform transform = CRS.findMathTransform(sourceCRS, COMMON_CRS);
            return JTS.transform(g, transform);
        } catch (Exception e) {
            throw new GeoXACMLException(e);
        }
    }

    /**
     * @param t
     *            a Throwable
     * @return an EvaluationResult indicating a processing error
     */
    protected void exceptionError(Throwable t) throws FunctionProcessingException{

        Logger log = Logger.getLogger(this.getClass().getName());
        log.log(Level.SEVERE, t.getMessage(), t);

        throw new FunctionProcessingException(t.getLocalizedMessage(), t);

    }

    /**
     * @param srsName
     * @return CoordinateRefernceSystem
     * @throws org.geotools.xacml.geoxacml.cond.GeoXACMLException
     * 
     *             try to decode the value of the GML srsName attribute
     */
    protected CoordinateReferenceSystem decodeCRS(String srsName) throws GeoXACMLException {

        URI srs = null;

        try {
            srs = new URI(srsName);
        } catch (URISyntaxException e) { // failed, continue on
        }

        if (srs != null) {
            // TODO: JD, this is a hack until GEOT-1136 has been resolved
            if ("http".equals(srs.getScheme()) && "www.opengis.net".equals(srs.getAuthority())
                    && "/gml/srs/epsg.xml".equals(srs.getPath()) && (srs.getFragment() != null)) {
                try {
                    return CRS.decode("EPSG:" + srs.getFragment(), true);
                } catch (Exception e) {
                    // failed, try as straight up uri
                    try {
                        return CRS.decode(srs.toString(), true);
                    } catch (Exception e1) {
                        // failed again, do nothing ,should fail below as well
                    }
                }
            }
        }

        try {
            return CRS.decode(srsName, true);
        } catch (NoSuchAuthorityCodeException e) {
            // HACK HACK HACK!: remove when
            // http://jira.codehaus.org/browse/GEOT-1659 is fixed

            if (srsName.toUpperCase().startsWith("URN")) {
                String code = srsName.substring(srsName.lastIndexOf(":") + 1);
                try {
                    return CRS.decode("EPSG:" + code, true);
                } catch (Exception e1) {
                    throw new GeoXACMLException("Could not create crs: " + srs, e);
                }
            }
        } catch (FactoryException e) {
            throw new GeoXACMLException("Could not create crs: " + srs, e);
        }

        return null;
    }

}
