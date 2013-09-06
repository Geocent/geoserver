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

package org.geotools.xacml.transport;

import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.ResponseMarshaller;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common base class for {@link XACMLTransport} implementations
 * 
 * @author Christian Mueller
 * 
 */
public abstract class XACMLAbstractTransport implements XACMLTransport {

    protected Logger logger = Logger.getLogger(this.getClass().getName());

    public abstract ResponseType evaluateRequestCtx(RequestType request);

    public abstract List<ResponseType> evaluateRequestCtxList(List<RequestType> requests);

    protected void log(RequestType ctx) {

        if (logger.isLoggable(Level.FINE) == false)
            return;


        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            RequestMarshaller.marshal(ctx, out);


            logger.fine(out.toString());
        } catch (Exception e) {
            // do nothing
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

    protected void log(ResponseType ctx) {

        if (logger.isLoggable(Level.FINE) == false)
            return;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ResponseMarshaller.marshal(ctx, out);
            logger.fine(out.toString());
        } catch (Exception e) {
            // do nothing
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

}
