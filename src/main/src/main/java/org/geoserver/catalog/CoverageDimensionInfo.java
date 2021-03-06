/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.io.Serializable;
import java.util.List;

import org.geotools.util.NumberRange;

/**
 * A coverage dimension.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public interface CoverageDimensionInfo extends Serializable {

    /**
     * Id of the dimension.
     */
    String getId();

    /**
     * The name of the dimension.
     * 
     * @uml.property name="name"
     */
    String getName();

    /**
     * Sets the name of the dimension.
     * 
     * @uml.property name="name"
     */
    void setName(String name);

    /**
     * The description of the dimension.
     * 
     * @uml.property name="description"
     */
    String getDescription();

    /**
     * Sets the description of the dimension.
     * 
     * @uml.property name="description"
     */
    void setDescription(String description);

    /**
     * The range of the dimension.
     * 
     * @uml.property name="range"
     */
    NumberRange getRange();

    /**
     * Sets the range of the dimension.
     * 
     * @uml.property name="range"
     */
    void setRange(NumberRange range);

    /**
     * The null values of the dimension.
     * 
     * @uml.property name="nullValues"
     */
    List<Double> getNullValues();
}
