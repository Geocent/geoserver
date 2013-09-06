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

/**
 * Abstract baseBag class for geometry construct functions
 * 
 * @author Christian Mueller
 * 
 */
public abstract class GeometryBagConstructFunction extends GeoXACMLBagFunctionBase {

    public GeometryBagConstructFunction(String functionId){
        super(functionId);
    }

    @Override
    protected Class<?>[] getParams() {
        return new Class<?>[] { Geometry.class };
    }
}
