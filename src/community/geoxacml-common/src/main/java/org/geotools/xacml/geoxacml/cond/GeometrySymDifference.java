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
 * Calculates the symetric differnce
 * 
 * @author Christian Mueller
 * 
 */
public class GeometrySymDifference extends GeometryBagConstructFunction {

    public static final String NAME = NAME_PREFIX + "geometry-sym-difference";

    public GeometrySymDifference() {
        super(NAME);
    }

    @Override
    protected Class<?>[] getParams() {
        return new Class<?>[] { Geometry.class, Geometry.class };
    }

    @Override
    public Collection<?> evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry geom1 = (Geometry)objects[0];
        Geometry geom2 = (Geometry)objects[1];
        Geometry resultGeom = null;

        try {
            Geometry[] geoms = transformOnDemand(geom1, geom2);
            resultGeom = geoms[0].symDifference(geoms[1]);
        } catch (Throwable t) {
            exceptionError(t);
        }

        List<Geometry> list = new ArrayList<Geometry>();
        list.add(resultGeom);
        return list;
    }

}
