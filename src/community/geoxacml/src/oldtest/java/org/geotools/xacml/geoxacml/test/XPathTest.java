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

package org.geotools.xacml.geoxacml.test;

//import java.io.ByteArrayOutputStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.geotools.xacml.test.TestSupport;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;

import java.io.FileInputStream;

/**
 * @author Christian Mueller
 * 
 *         Tests for bag functions
 * 
 */
public class XPathTest extends TestCase {

    public XPathTest() {
        super();

    }

    public XPathTest(String arg0) {
        super(arg0);

    }

    @Override
    protected void setUp() throws Exception {
        TestSupport.initOutputDir();
    }

    public void testXPath() {

        PDP pdp = TestSupport.getPDP(TestSupport.getGeoXACMLFNFor("xpath", "XPathPolicy.xml"));

        RequestType request = null;
        try {
            request = RequestType.getInstance(new FileInputStream(TestSupport.getGeoXACMLFNFor(
                    "xpath", "XPathRequest.xml")));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        ResponseType response = pdp.evaluate(request);
        ResultType result = (ResultType) response.getResults().iterator().next();

        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // response.encode(out);
        // System.out.println(new String(out.toByteArray()));
        assertTrue(result.getDecision() == ResultType.DECISION_PERMIT);
        assertTrue(result.getStatus().getCode().iterator().next().equals(Status.STATUS_OK));
    }

}
