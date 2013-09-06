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
 * Calulates the boundary of a geometry
 * 
 * @author Christian Mueller
 * 
 */
public class GeometryBoundary extends GeometryBagConstructFunction {

    public static final String NAME = NAME_PREFIX + "geometry-boundary";

    public GeometryBoundary() {
        super(NAME);
    }

    @Override
    public Collection<Geometry> evaluate(Object ... objects) throws FunctionProcessingException {

        Geometry resultGeom = null;

        try {
            resultGeom = ((Geometry)objects[0]).getBoundary();
        } catch (Throwable t) {
            exceptionError(t);
        }

        List<Geometry> retlist = new ArrayList<Geometry>();
        retlist.add(resultGeom);
        return retlist;
    }

}
