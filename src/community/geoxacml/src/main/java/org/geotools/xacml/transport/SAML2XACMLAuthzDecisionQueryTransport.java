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

import org.geoserver.platform.GeoServerExtensions;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.ResponseMarshaller;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.IssuerImpl;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.soap.soap11.impl.BodyImpl;
import org.opensaml.xacml.XACMLConstants;
import org.opensaml.xacml.ctx.EnvironmentType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transport Object for a remote PDP reachable by an SAML2P XACMLAuthzDecisionQuery request via
 * the SOAP binding. Since XACML requests are independent of each other, it is possible to start
 * each request of a request list as a single thread. This class itself is threadsafe
 * 
 * @author Christian Mueller
 * @author Benjamin Burns
 *
 */
public class SAML2XACMLAuthzDecisionQueryTransport extends XACMLAbstractTransport {
    /**
     * Thread class for evaluating a XACML request
     *
     * @author Christian Mueller
     * @author Benjamin Burns
     *
     */

    private static final Logger logger =
            (Logger) LoggerFactory.getLogger(SAML2XACMLAuthzDecisionQueryTransport.class);

    private MetadataGenerator metadataGenerator;

    //TODO: break SAML deps into separate project
    private static InheritableThreadLocal<Map<String, ResponseType>> DigestMap = new InheritableThreadLocal<Map<String, ResponseType>>();
    private JKSKeyManager keyManager;
    private String keyName;

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
                responseCtx = sendXACMLAuthzDecisionQuery(requestCtx);
            } catch (RuntimeException ex) {
                this.runtimeException = ex;
            }
        }

    }

    private URL pdpURL;

    private boolean multiThreaded = false;

    public SAML2XACMLAuthzDecisionQueryTransport(URL pdpURL, boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
        this.pdpURL = pdpURL;
    }

    public ResponseType evaluateRequestCtx(RequestType request) {
        initDigestMap();
        ResponseType response = sendXACMLAuthzDecisionQuery(request);
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
            ResponseType response = sendXACMLAuthzDecisionQuery(request);
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
            if (t.getRuntimeException() == null) {
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

    private Element buildEnvelope(Element requestDom) throws MarshallingException, NoSuchAlgorithmException, org.opensaml.xml.security.SecurityException, SignatureException, ParserConfigurationException, IOException, SAXException, UnmarshallingException {

        Unmarshaller requestUnmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(org.opensaml.xacml.ctx.RequestType.DEFAULT_ELEMENT_NAME);
        org.opensaml.xacml.ctx.RequestType request = (org.opensaml.xacml.ctx.RequestType) requestUnmarshaller.unmarshall(requestDom);

        if(request.getEnvironment() == null){
            XMLObjectBuilder<EnvironmentType> environmentBuilder = Configuration.getBuilderFactory().getBuilder(EnvironmentType.DEFAULT_ELEMENT_NAME);
            EnvironmentType environment = environmentBuilder.buildObject(new QName(XACMLConstants.XACML20CTX_NS, EnvironmentType.DEFAULT_ELEMENT_LOCAL_NAME, XACMLConstants.XACMLCONTEXT_PREFIX));
            request.setEnvironment(environment);
        }

        //build issuer
        XMLObjectBuilder<IssuerImpl> issuerBuilder = Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = issuerBuilder.buildObject(new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX));
        issuer.setValue(this.getMetadataGenerator().getEntityId());

        //build XACMLAuthzDecisionQueryType (SAMLP container for XACML request)
        XMLObjectBuilder<XACMLAuthzDecisionQueryType> queryBuilder = Configuration.getBuilderFactory().getBuilder(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
        XACMLAuthzDecisionQueryType authzQuery = queryBuilder.buildObject(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);

        //add issuer
        authzQuery.setIssuer(issuer);

        //set mandatory attributes
        authzQuery.setID(new SecureRandomIdentifierGenerator().generateIdentifier());
        DateTime now = new DateTime(DateTimeZone.forID("UTC"));
        authzQuery.setIssueInstant(now);
        authzQuery.setReturnContext(false);


        //add request
        authzQuery.setRequest(request);

        //build the SOAP body
        XMLObjectBuilder<BodyImpl> bodyBuilder = Configuration.getBuilderFactory().getBuilder(Body.DEFAULT_ELEMENT_NAME);
        BodyImpl body = bodyBuilder.buildObject(Body.DEFAULT_ELEMENT_NAME);

        //Add the SAMLP message to the SOAP body
        body.getUnknownXMLObjects().add(authzQuery);

        //build envelope
        XMLObjectBuilder<Envelope> envelopeBuilder = Configuration.getBuilderFactory().getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        Envelope envelope = envelopeBuilder.buildObject(Envelope.DEFAULT_ELEMENT_NAME);

        //add header
        envelope.setBody(body);

        //do signature
        Credential credential = this.getKeyManager().getCredential(keyName);

        XMLObjectBuilder<Signature> signtureBuilder = (XMLObjectBuilder<Signature>)Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        Signature signature = signtureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);
        SecurityHelper.prepareSignatureParams(signature, credential, Configuration.getGlobalSecurityConfiguration(), null);

        authzQuery.setSignature(signature);

        //marshall first so that signature can be properly generated
        Configuration.getMarshallerFactory().getMarshaller(authzQuery).marshall(authzQuery);

        //now that the DOM is populated, sign
        Signer.signObject(signature);


        //remarshall now that we've got the signature
        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(Envelope.DEFAULT_ELEMENT_NAME);
        Element dom = marshaller.marshall(envelope);

        logger.debug("Sending AuthzRequest from Geoserver" + XMLHelper.prettyPrintXML(dom));
        return dom;
    }

    private ResponseType parseResponse(InputStream is) throws ParserConfigurationException, IOException, SAXException, UnmarshallingException, MarshallingException, SyntaxException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Element dom = factory.newDocumentBuilder().parse(is).getDocumentElement();

        logger.debug("Received AuthzResponse from PDP" + XMLHelper.prettyPrintXML(dom));

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(Envelope.DEFAULT_ELEMENT_NAME);
        Envelope envelope = (Envelope)unmarshaller.unmarshall(dom);


        for(XMLObject xmlobj : envelope.getBody().getOrderedChildren()){
            if(xmlobj instanceof Response){
                Response protocolResponse = (Response)xmlobj;
                for(Assertion assertion : protocolResponse.getAssertions()){
                    for(Statement statement: assertion.getStatements()){
                        if(statement instanceof XACMLAuthzDecisionStatementType){
                            XACMLAuthzDecisionStatementType decision = (XACMLAuthzDecisionStatementType)statement;

                            org.opensaml.xacml.ctx.ResponseType response = decision.getResponse();

                            return ResponseMarshaller.unmarshal(response.getDOM());
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("No XACML Response found in message.");
    }

    private ResponseType sendXACMLAuthzDecisionQuery(RequestType requestCtx) {
        log(requestCtx);

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            RequestMarshaller.marshal(requestCtx, bout);

            byte[] byteArray = bout.toByteArray();
            byte[] msgDigest = getDigestBytes(byteArray);

            try{
                bout.close();
            }catch(IOException ex){
                //do nothing
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            if (msgDigest != null) {
                ResponseType responseCtx = DigestMap.get().get(new String(msgDigest));
                if (responseCtx != null) {
                    return responseCtx;
                }
            }

            Element dom = factory.newDocumentBuilder().parse(new ByteArrayInputStream(byteArray)).getDocumentElement();
            Element envelope = buildEnvelope(dom);

            HttpURLConnection conn = (HttpURLConnection) pdpURL.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("SOAPAction", "");

            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            XMLHelper.writeNode(envelope, out);
            out.close();


            InputStream in = conn.getInputStream();
            ResponseType result = parseResponse(in);
            in.close();
            if (msgDigest != null && result != null)
                DigestMap.get().put(new String(msgDigest), result);

            log(result);
            return result;
        } catch (Exception e) {
            //TODO: break this out
            logger.error("Processing request context failed.", e);
            throw new RuntimeException(e);
        }
    }

    byte[] getDigestBytes(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("No MD5 Algorithm available");
            return null;
        }
        md.update(bytes);
        return md.digest();
    }

    public MetadataGenerator getMetadataGenerator() {
        if(metadataGenerator == null)
            metadataGenerator = GeoServerExtensions.bean(MetadataGenerator.class);
        return metadataGenerator;
    }

    public void setMetadataGenerator(MetadataGenerator metadataGenerator) {
        this.metadataGenerator = metadataGenerator;
    }

    public JKSKeyManager getKeyManager() {
        if(keyManager == null)
            keyManager = GeoServerExtensions.bean(JKSKeyManager.class);
        return keyManager;
    }

    public void setKeyManager(JKSKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

}
