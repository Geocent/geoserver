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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Transport Object for a remote PDP reachable by an http POST request. Since XACML requests are
 * independent of each other, it is possible to start each request of a request list as a single
 * thread. This class itself is threadsafe
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLHttpTransport extends XACMLAbstractTransport {
    /**
     * Thread class for evaluating a XACML request
     * 
     * @author Christian Mueller
     * 
     */

    private static InheritableThreadLocal<Map<String, ResponseType>> DigestMap = new InheritableThreadLocal<Map<String, ResponseType>>();;

    public class HttpThread extends Thread {
        private RequestType requestCtx = null;

        public RequestType getRequestCtx() {
            return requestCtx;
        }

        private ResponseType responseCtx = null;

        private RuntimeException runtimeException = null;

        public RuntimeException getRuntimeException() {
            return runtimeException;
        }

        public ResponseType getResponseCtx() {
            return responseCtx;
        }

        HttpThread(RequestType requestCtx) {
            this.requestCtx = requestCtx;
        }

        @Override
        public void run() {
            try {
                responseCtx = sendHttpPost(requestCtx);
            } catch (RuntimeException ex) {
                this.runtimeException = ex;
            }
        }

    }

    private URL pdpURL;

    private boolean multiThreaded = false;

    public XACMLHttpTransport(URL pdpURL, boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
        this.pdpURL = pdpURL;
    }

    public ResponseType evaluateRequestCtx(RequestType request) {
        initDigestMap();
        log(request);
        ResponseType response = sendHttpPost(request);
        log(response);
        return response;

    }

    public List<ResponseType> evaluateRequestCtxList(List<RequestType> requests) {
        initDigestMap();
        if (multiThreaded)
            return evaluateRequestCtxListMultiThreaded(requests);
        else
            return evaluateRequestCtxListSerial(requests);
    }

    private List<ResponseType> evaluateRequestCtxListSerial(List<RequestType> requests) {
        List<ResponseType> resultList = new ArrayList<ResponseType>();
        for (RequestType request : requests) {
            log(request);
            ResponseType response = sendHttpPost(request);
            log(response);
            resultList.add(response);
        }
        return resultList;
    }

    private List<ResponseType> evaluateRequestCtxListMultiThreaded(List<RequestType> requests) {
        List<ResponseType> resultList = new ArrayList<ResponseType>(requests.size());
        List<HttpThread> threadList = new ArrayList<HttpThread>(requests.size());

        if (requests.size() == 1) { // no threading for only one request
            resultList.add(evaluateRequestCtx(requests.get(0)));
            return resultList;
        }

        for (RequestType request : requests) {
            HttpThread t = new HttpThread(request);
            t.start();
            threadList.add(t);
        }
        for (HttpThread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log(t.getRequestCtx());
            if (t.getRuntimeException() == null) {
                log(t.getResponseCtx());
                resultList.add(t.getResponseCtx());
            } else
                throw t.getRuntimeException();
        }
        return resultList;

    }

    private void initDigestMap() {
        if (DigestMap.get() == null)
            DigestMap.set(new HashMap<String, ResponseType>());
    }

    private ResponseType sendHttpPost(RequestType requestCtx) {
        ByteArrayOutputStream bout = null;
        try {
            bout = new ByteArrayOutputStream();

            RequestMarshaller.marshal(requestCtx, bout);
            byte[] byteArray = bout.toByteArray();
            byte[] msgDigest = getDigestBytes(byteArray);

            if (msgDigest != null) {
                ResponseType responseCtx = DigestMap.get().get(new String(msgDigest));
                if (responseCtx != null) {
                    return responseCtx;
                }
            }

            HttpURLConnection conn = (HttpURLConnection) pdpURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "text/xml, application/xml");
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            out.write(byteArray);
            out.close();
            InputStream in = conn.getInputStream();
            ResponseType result = ResponseMarshaller.unmarshal(in);
            in.close();
            if (msgDigest != null)
                DigestMap.get().put(new String(msgDigest), result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (bout != null) {
                try {
                    bout.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    byte[] getDigestBytes(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "No MD5 Algorithm available");
            return null;
        }
        md.update(bytes);
        return md.digest();
    }

}
