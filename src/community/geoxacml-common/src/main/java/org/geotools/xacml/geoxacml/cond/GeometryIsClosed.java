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

import com.vividsolutions.jts.geom.*;
import org.herasaf.xacml.core.function.FunctionProcessingException;

/**
 * Check if a geometry is closed
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryIsClosed extends GeometryCheckFunction {

    public static final String NAME = NAME_PREFIX + "geometry-is-closed";

    public GeometryIsClosed() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom = (Geometry)objects[0];
        boolean evalResult = false;

        try {
            if (geom.isEmpty())
                evalResult = true;
            else if (geom instanceof Point || geom instanceof MultiPoint)
                evalResult = true;
            else if (geom instanceof LineString)
                evalResult = ((LineString) geom).isClosed();
            else if (geom instanceof MultiLineString)
                evalResult = ((MultiLineString) geom).isClosed();
            else
                evalResult = false;
            // evalResult= geom.getBoundary().isEmpty();

        } catch (Throwable t) {
            exceptionError(t);
        }
        return evalResult;
    }

}
