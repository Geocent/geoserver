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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Executes a buffer operation
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryBuffer extends GeometryBagConstructFunction {

    public static final String NAME = NAME_PREFIX + "geometry-buffer";

    public GeometryBuffer() {
        super(NAME);
    }

    @Override
    protected Class<?>[] getParams(){
        return new Class<?>[] { Geometry.class, Double.class };
    }

    @Override
    public Collection<?> evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom = (Geometry)objects[0];
        double d = ((Double)objects[1]).doubleValue();
        Geometry resultGeom = null;

        try {
            resultGeom = geom.buffer(d);
        } catch (Throwable t) {
            exceptionError(t);
        }


        List<Geometry> list = new ArrayList<Geometry>();
        list.add(resultGeom);
        return list;

    }

}
