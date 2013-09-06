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
 * Checks isWithinDistance
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryIsWithinDistance extends GeometryScalarFunction {

    public static final String NAME = NAME_PREFIX + "geometry-is-within-distance";

    public GeometryIsWithinDistance() {
        super(NAME);
    }

    @Override
    protected Class<?>[] getParams() {
        return new Class<?>[] { Geometry.class, Geometry.class, Double.class };
    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom1 = (Geometry)objects[0];
        Geometry geom2 = (Geometry)objects[1];
        double dist = ((Double)objects[2]).doubleValue();

        boolean evalResult = false;

        try {
            evalResult = geom1.isWithinDistance(geom2, dist);
        } catch (Throwable t) {
            exceptionError(t);
        }
        return evalResult;
    }

}
