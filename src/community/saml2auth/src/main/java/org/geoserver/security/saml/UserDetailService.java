package org.geoserver.security.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.xacml.role.XACMLRoleAuthority;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserDetailService implements SAMLUserDetailsService {

    private Marshaller marshaller = null;
    private Map<String, GeoServerRole> roleMap;

    private XACMLRoleAuthority roleAuthority = null;

    private static final Log logger = LogFactory.getLog(UserDetailService.class);

    public UserDetails loadUserBySAML(SAMLCredential credential)
            throws UsernameNotFoundException {
        Assertion assertion = credential.getAuthenticationAssertion();
        List<AttributeStatement> statements = assertion
                .getAttributeStatements();

        String samlXml = "";
        try {
            Element element = getMarshaller(assertion).marshall(assertion);
            samlXml = XMLHelper.prettyPrintXML(element);
        } catch (MarshallingException e) {
            samlXml = "ERROR: Could NOT get the saml assertion xml: " + e;
        }
        logger.info(samlXml);

        String username = "";

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

        logger.info("Assertion has " + statements.size() + " statements");
        int statementCount = 0;
        for (AttributeStatement statement : statements) {
            logger.info("Statement " + statementCount + " has " + statement.getAttributes().size() + " attributes.");
            statementCount ++;

            int attributeCount = 0;

            for (Attribute attribute : statement.getAttributes()) {

                logger.info("Attribute " + attributeCount + " is " + attribute.getName());
                attributeCount++;
                if ("role".equals(attribute.getName())) {
                    logger.info("Found roles in attribute " + (attributeCount - 1) + ".");
                    String[] values = getSimpleAttributeValues(attribute);
                    for (String roleName : values) {
                        if(this.roleMap.containsKey(roleName)){
                            logger.info("Mapped role " + roleName + " to " + roleMap.get(roleName).getAuthority());
                            authorities.add(roleMap.get(roleName));
                        }else{
                            logger.info("No role mapping found for " + roleName);
                        }
                    }
                }else if("uid".equals(attribute.getName())){
                    for(String value : getSimpleAttributeValues(attribute)){
                        if(!value.equals("")){
                            username = value;
                        }
                    }
                    username = getSimpleAttributeValues(attribute)[0];
                }
            }
        }



        if (username.equals("")) {
            throw new IllegalArgumentException("Username not found!");
        }

        if (authorities.isEmpty()) {
            throw new IllegalArgumentException(
                    "No roles found in SAML assertion for user " + username);
        }

        GeoServerUser gsu = new GeoServerUser(username);
        gsu.setAuthorities(authorities);

        //TODO: break this out better
        this.getRoleAuthority().transformUserDetails(gsu);
        return gsu;
    }

    private Marshaller getMarshaller(Assertion assertion) {
        if (marshaller == null) {
            MarshallerFactory marshallerFactory = Configuration
                    .getMarshallerFactory();
            marshaller = marshallerFactory.getMarshaller(assertion);
        }

        return marshaller;
    }

    private static String[] getSimpleAttributeValues(Attribute attribute) {
        List<XMLObject> attributeValues = attribute.getAttributeValues();
        String[] values = new String[attributeValues.size()];
        for (int i = 0; i < attributeValues.size(); i++) {
            XMLObject value = attributeValues.get(i);
            if (value instanceof XSString) {
                values[i] = ((XSString) value).getValue();
            } else {
                values[i] = DomUtils.getTextValue(attributeValues.get(i)
                        .getDOM());
            }
        }
        return values;
    }

    //TODO: break this out better
    public XACMLRoleAuthority getRoleAuthority() {
        if(roleAuthority == null){
            roleAuthority = GeoServerExtensions.bean(XACMLRoleAuthority.class);
        }
        return roleAuthority;
    }

    public void setRoleAuthority(XACMLRoleAuthority roleAuthority) {
        this.roleAuthority = roleAuthority;
    }

    public Map<String,GeoServerRole> getRoleMap(){
        return this.roleMap;
    }

    public void setRoleMap(Map<String,GeoServerRole> adminGroup){
        this.roleMap = adminGroup;
    }
}
