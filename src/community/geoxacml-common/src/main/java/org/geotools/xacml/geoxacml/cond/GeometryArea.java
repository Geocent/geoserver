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
 * Caculates the area
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryArea extends GeometryScalarFunction {

    public static final String NAME = NAME_PREFIX + "geometry-area";

    public GeometryArea() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {
        try {
            return ((Geometry)objects[0]).getArea();
        } catch (Throwable t) {
            exceptionError(t);
        }
        //never executes due to implementation of exceptionError;
        return null;
    }
}
