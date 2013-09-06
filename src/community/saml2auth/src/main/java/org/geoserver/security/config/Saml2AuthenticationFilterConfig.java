package org.geoserver.security.config;

import org.geoserver.platform.GeoServerExtensions;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import java.util.Collection;
import java.util.Set;

public class Saml2AuthenticationFilterConfig extends SecurityFilterConfig
        implements SecurityAuthFilterConfig {

    private static final long serialVersionUID = 1L;

    private transient WebSSOProfileOptions options = null;
    private String roleServiceName;

    public Saml2AuthenticationFilterConfig() {
        options = GeoServerExtensions.bean(WebSSOProfileOptions.class);
        if(options == null){
            new WebSSOProfileOptions();
        }
    }

    @Override
    public boolean providesAuthenticationEntryPoint() {
        return true;
    }

    public String getRoleServiceName() {
        return roleServiceName;
    }

    public void setRoleServiceName(String roleServiceName) {
        this.roleServiceName = roleServiceName;
    }

    public String getBinding() {
        return options.getBinding();
    }

    /**
     * Sets binding to be used for for sending SAML message to IDP.
     * 
     * @param binding
     *            binding value
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_POST_BINDING_URI
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_REDIRECT_BINDING_URI
     * @see org.opensaml.common.xml.SAMLConstants#SAML2_PAOS_BINDING_URI
     * @see org.springframework.security.saml.SAMLConstants#SAML2_HOK_WEBSSO_PROFILE_URI
     */
    public void setBinding(String binding) {
        options.setBinding(binding);
    }

    /**
     * Sets whether the IdP should refrain from interacting with the user during
     * the authentication process. Boolean values will be marshalled to either
     * "true" or "false".
     * 
     * @return true if passive authentication is allowed, false otherwise, null
     *         will omit the passive parameter from request
     */
    public Boolean getPassive() {
        return options.getPassive();
    }

    /**
     * Sets whether the IdP should refrain from interacting with the user during
     * the authentication process. Boolean values will be marshalled to either
     * "true" or "false", value will be omitted from request when null..
     * 
     * @param passive
     *            true if passive authentication is allowed, false otherwise,
     *            null to omit the field
     */
    public void setPassive(Boolean passive) {
        options.setPassive(passive);
    }

    public Boolean getForceAuthN() {
        return options.getForceAuthN();
    }

    public void setForceAuthN(Boolean forceAuthN) {
        options.setForceAuthN(forceAuthN);
    }

    /**
     * True if scoping element should be included in the requests sent to IDP.
     * 
     * @return true if scoping should be included, scoping won't be included
     *         when null or false
     */
    public Boolean isIncludeScoping() {
        return options.isIncludeScoping();
    }

    public void setIncludeScoping(Boolean includeScoping) {
        options.setIncludeScoping(includeScoping);
    }

    /**
     * @return null to skip proxyCount, 0 to disable proxying, >0 to allow
     *         proxying
     */
    public Integer getProxyCount() {
        return options.getProxyCount();
    }

    /**
     * Determines value to be used in the proxyCount attribute of the scope in
     * the AuthnRequest. In case value is null the proxyCount attribute is
     * omitted. Use zero to disable proxying or value >0 to specify how many
     * hops are allowed.
     * <p>
     * Property includeScoping must be enabled for this value to take any
     * effect.
     * </p>
     * 
     * @param proxyCount
     *            null to skip proxyCount in the AuthnRequest, 0 to disable
     *            proxying, >0 to allow proxying
     */
    public void setProxyCount(Integer proxyCount) {
        options.setProxyCount(proxyCount);
    }

    public Collection<String> getAuthnContexts() {
        return options.getAuthnContexts();
    }

    public void setAuthnContexts(Collection<String> authnContexts) {
        options.setAuthnContexts(authnContexts);
    }

    /**
     * NameID to used or null to omit NameIDPolicy from request.
     * 
     * @return name ID
     */
    public String getNameID() {
        return options.getNameID();
    }

    /**
     * When set determines which NameIDPolicy will be requested as part of the
     * AuthnRequest sent to the IDP.
     * 
     * @see org.opensaml.saml2.core.NameIDType#EMAIL
     * @see org.opensaml.saml2.core.NameIDType#TRANSIENT
     * @see org.opensaml.saml2.core.NameIDType#PERSISTENT
     * @see org.opensaml.saml2.core.NameIDType#X509_SUBJECT
     * @see org.opensaml.saml2.core.NameIDType#KERBEROS
     * @see org.opensaml.saml2.core.NameIDType#UNSPECIFIED
     * 
     * @param nameID
     *            name ID
     */
    public void setNameID(String nameID) {
        options.setNameID(nameID);
    }

    public Boolean isAllowCreate() {
        return options.isAllowCreate();
    }

    /**
     * Flag indicating whether IDP can create new user based on the current
     * authentication request. Null value will omit field from the request.
     * 
     * @param allowCreate
     *            allow create
     */
    public void setAllowCreate(Boolean allowCreate) {
        options.setAllowCreate(allowCreate);
    }

    /**
     * @return comparison mode to use by default mode minimum is used
     */
    public AuthnContextComparisonTypeEnumeration getAuthnContextComparison() {
        return options.getAuthnContextComparison();
    }

    /**
     * Sets comparison to use for WebSSO requests. No change for null values.
     * 
     * @param authnContextComparison
     *            context to set
     */
    public void setAuthnContextComparison(
            AuthnContextComparisonTypeEnumeration authnContextComparison) {
        options.setAuthnContextComparison(authnContextComparison);
    }

    public Set<String> getAllowedIDPs() {
        return options.getAllowedIDPs();
    }

    /**
     * List of IDPs which are allowed to process the created AuthnRequest. IDP
     * the request will be sent to is added automatically. In case value is null
     * the allowedIDPs will not be included in the Scoping element.
     * <p>
     * Property includeScoping must be enabled for this value to take any
     * effect.
     * </p>
     * 
     * @param allowedIDPs
     *            IDPs enabled to process the created authnRequest, null to skip
     *            the attribute from scoptin
     */
    public void setAllowedIDPs(Set<String> allowedIDPs) {
        options.setAllowedIDPs(allowedIDPs);
    }

    /**
     * Human readable name of the local entity.
     * 
     * @return entity name
     */
    public String getProviderName() {
        return options.getProviderName();
    }

    /**
     * Sets human readable name of the local entity used in ECP profile.
     * 
     * @param providerName
     *            provider name
     */
    public void setProviderName(String providerName) {
        options.setProviderName(providerName);
    }

    public Integer getAssertionConsumerIndex() {
        return options.getAssertionConsumerIndex();
    }

    /**
     * When set determines assertionConsumerService and binding to which should
     * IDP send response. By default service is determined automatically.
     * Available indexes can be found in metadata of this service provider.
     * 
     * @param assertionConsumerIndex
     *            index
     */
    public void setAssertionConsumerIndex(Integer assertionConsumerIndex) {
        options.setAssertionConsumerIndex(assertionConsumerIndex);
    }

    public String getRelayState() {
        return options.getRelayState();
    }

    /**
     * Relay state sent to the IDP as part of the authentication request. Value
     * will be returned by IDP and made available in the SAMLCredential after
     * successful authentication.
     * 
     * @param relayState
     *            relay state
     */
    public void setRelayState(String relayState) {
        options.setRelayState(relayState);
    }

    public WebSSOProfileOptions getWebSSOProfileOptions() {
        return options;
    }

}
