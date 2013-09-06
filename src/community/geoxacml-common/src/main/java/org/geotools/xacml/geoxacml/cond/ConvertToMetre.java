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

import org.herasaf.xacml.core.function.FunctionProcessingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts untis to metres
 * 
 * @author Christian Mueller
 * 
 */
public class ConvertToMetre extends ConvertFunction {

    public static final String NAME = NAME_PREFIX + "convert-to-metre";

    private static Map<String, Double> UnitToMetre;

    static {
        UnitToMetre = new HashMap<String, Double>();
        UnitToMetre.put("ångström", 1.0E-10);
        UnitToMetre.put("Å", 1.0E-10);
        UnitToMetre.put("ua", 1.495979E+11);
        UnitToMetre.put("chain", 2.011684E+01);
        UnitToMetre.put("fathom", 1.828804E+00);
        UnitToMetre.put("fermi", 1.0E-15);
        UnitToMetre.put("ft", 3.048E-01);
        UnitToMetre.put("ft 7", 3.048006E-01);
        UnitToMetre.put("in", 2.54E-02);
        UnitToMetre.put("K", 1E+02);
        UnitToMetre.put("light year", 9.46073E+15);
        UnitToMetre.put("microinch", 2.54E-08);
        UnitToMetre.put("micron", 1.0E-06);
        UnitToMetre.put("μ", 1.0E-06);
        UnitToMetre.put("mil", 2.54E-05);
        UnitToMetre.put("mi", 1.609344E+03);
        UnitToMetre.put("mi 7", 1.609347E+03);
        UnitToMetre.put("mile nautical", 1.852E+03);
        UnitToMetre.put("pc", 3.085678E+16);

        UnitToMetre.put("pica computer", 4.233333E-03);
        UnitToMetre.put("pica printer", 4.217518E-03);
        UnitToMetre.put("point computer", 3527778E-04);
        UnitToMetre.put("point printer", 3.514598E-04);
        UnitToMetre.put("rd 7", 5.029210E+00);
        UnitToMetre.put("yd", 9.144E-01);
    }

    public ConvertToMetre() {
        super(NAME);

    }

    @Override
    public Object evaluate(Object ... objects) throws FunctionProcessingException {

        Double multiplyBy = UnitToMetre.get((String)objects[1]);
        if (multiplyBy == null) {
            exceptionError(new Exception("Unit" + objects[1] + " not supported"));
        }
        double input = ((Double)objects[0]).doubleValue();
        return input * multiplyBy;
    }

}
