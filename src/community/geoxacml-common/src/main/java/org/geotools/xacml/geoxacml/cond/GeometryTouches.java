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
 * Check if 2 geometries touch
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryTouches extends GeometryTopologicalFunction {

    public static final String NAME = NAME_PREFIX + "geometry-touches";

    public GeometryTouches() {
        super(NAME);

    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom1 = (Geometry)objects[0];
        Geometry geom2 = (Geometry)objects[1];

        boolean evalResult = false;

        try {
            Geometry[] geoms = transformOnDemand(geom1, geom2);
            evalResult = geoms[0].touches(geoms[1]);
        } catch (Throwable t) {
            exceptionError(t);
        }

        return evalResult;

    }

}
