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
import org.herasaf.xacml.core.function.FunctionProcessingException;


/**
 * Calculates the difference
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryDistance extends GeometryScalarFunction {

    public static final String NAME = NAME_PREFIX + "geometry-distance";

    public GeometryDistance() {
        super(NAME);
    }

    @Override
    protected Class<?>[] getParams() {
        return new Class<?>[] { Geometry.class, Geometry.class };
    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {


        Geometry geom1 = (Geometry)objects[0];
        Geometry geom2 = (Geometry)objects[1];
        double distance = 0;

        try {
            Geometry[] geoms = transformOnDemand(geom1, geom2);
            distance = geoms[0].distance(geoms[1]);
        } catch (Throwable t) {
            exceptionError(t);
        }

        return distance;
    }

}
