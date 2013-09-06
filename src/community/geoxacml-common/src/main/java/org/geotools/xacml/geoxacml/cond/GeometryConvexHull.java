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
 * Calculates the convex hull
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryConvexHull extends GeometryConstructFunction {

    public static final String NAME = NAME_PREFIX + "geometry-convex-hull";

    public GeometryConvexHull() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom = (Geometry)objects[0];
        Geometry resultGeom = null;

        try {
            resultGeom = geom.convexHull();
        } catch (Throwable t) {
            exceptionError(t);
        }

        return resultGeom;

    }

}
