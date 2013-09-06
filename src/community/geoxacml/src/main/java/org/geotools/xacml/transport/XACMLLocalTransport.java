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

import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Transport Object for a local PDP. Since XACML requests are independent of each other, it is
 * possible to start each request of a request list as a single thread. This class is thread safe
 * 
 * @author Christian Muller
 * 
 */
public class XACMLLocalTransport extends XACMLAbstractTransport {
    /**
     * Thread class for evaluating a XACML request
     * 
     * @author Christian Mueller
     * 
     */
    public class LocalThread extends Thread {
        private RequestType requestCtx = null;;

        public RequestType getRequestCtx() {
            return requestCtx;
        }

        private ResponseType responseCtx = null;

        public ResponseType getResponseCtx() {
            return responseCtx;
        }

        LocalThread(RequestType requestCtx) {
            this.requestCtx = requestCtx;
        }

        @Override
        public void run() {
            responseCtx = pdp.evaluate(requestCtx);
        }

    }

    private PDP pdp;

    private boolean multiThreaded = false;

    public XACMLLocalTransport(PDP pdp, boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
        this.pdp = pdp;
    }

    public ResponseType evaluateRequestCtx(RequestType request) {
        log(request);
        ResponseType response = pdp.evaluate(request);
        log(response);
        return response;
    }

    public List<ResponseType> evaluateRequestCtxList(List<RequestType> requests) {
        if (multiThreaded)
            return evaluateRequestCtxListMultiThreaded(requests);
        else
            return evaluateRequestCtxListSerial(requests);
    }

    private List<ResponseType> evaluateRequestCtxListSerial(List<RequestType> requests) {
        List<ResponseType> resultList = new ArrayList<ResponseType>();
        for (RequestType request : requests) {
            log(request);
            ResponseType response = pdp.evaluate(request);
            log(response);
            resultList.add(response);

        }
        return resultList;
    }

    private List<ResponseType> evaluateRequestCtxListMultiThreaded(List<RequestType> requests) {
        List<ResponseType> resultList = new ArrayList<ResponseType>(requests.size());
        List<LocalThread> threadList = new ArrayList<LocalThread>(requests.size());

        if (requests.size() == 1) { // no threading for only one request
            resultList.add(evaluateRequestCtx(requests.get(0)));
            return resultList;
        }

        for (RequestType request : requests) {
            LocalThread t = new LocalThread(request);
            t.start();
            threadList.add(t);
        }
        for (LocalThread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log(t.getRequestCtx());
            log(t.getResponseCtx());
            resultList.add(t.getResponseCtx());
        }
        return resultList;

    }

}
