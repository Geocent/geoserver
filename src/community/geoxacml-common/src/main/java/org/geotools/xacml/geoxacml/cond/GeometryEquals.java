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
 * Checks if 2 geometries are equal
 * 
 * IMPORTANT: the logic must be in the equals Method of GeomtryAttribute, otherwise the Bag and Set
 * functions would not work as described in the GeoXACML specification
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryEquals extends GeometryTopologicalFunction {

    public static final String NAME = NAME_PREFIX + "geometry-equals";

    public GeometryEquals() {
        super(NAME);

    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom1 = (Geometry)objects[0];
        Geometry geom2 = (Geometry)objects[1];
        boolean evalResult = false;

        try {
            evalResult = geom1.equals(geom2);
        } catch (Throwable t) {
            exceptionError(t);
        }

        return evalResult;

    }

}
