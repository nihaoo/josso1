/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.gateway.jaxws.identity.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.ws._1_2.protocol.GlobalSignoffRequestType;
import org.josso.gateway.ws._1_2.protocol.GlobalSignoffResponseType;
import org.josso.gateway.ws._1_2.protocol.ResolveAuthenticationAssertionRequestType;
import org.josso.gateway.ws._1_2.protocol.ResolveAuthenticationAssertionResponseType;
import org.josso.gateway.ws._1_2.wsdl.AssertionNotValidErrorMessage;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityProvider;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityProviderErrorMessage;

/**
 * JAX-WS SSO Identity Provider that is a proxy of an Identity Provider using Webservices.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public class WebserviceSSOIdentityProvider implements SSOIdentityProviderService {

    private static final Log logger = LogFactory.getLog(WebserviceSSOIdentityProvider.class);

    private SSOIdentityProvider _wsSSOIdentityProvider;

    private int _errorCount;

    private int _processedCount;


    /**
     * Build a Webservice SSO Identity Provider.
     *
     * @param wsSSOIdentityProvider the SOAP stub to be invoked.
     */
    public WebserviceSSOIdentityProvider(SSOIdentityProvider wsSSOIdentityProvider) {
        _wsSSOIdentityProvider = wsSSOIdentityProvider;
    }

    /**
     * Initializes this manager instance.
     */
    public void initialize() {

    }

    public String assertIdentityWithSimpleAuthentication(String requester, String securityDomain, String username, String password) throws IdentityProvisioningException {

        throw new UnsupportedOperationException("No JAX-WS implementation yet");
    }

    public String resolveAuthenticationAssertion(String requester, String authenticationAssertionId) throws AssertionNotValidException, IdentityProvisioningException {
        try {
            if (logger.isDebugEnabled())
                logger.debug("[resolveAuthenticationAssertion()] : " + authenticationAssertionId);

            ResolveAuthenticationAssertionRequestType request = new ResolveAuthenticationAssertionRequestType();
            request.setRequester(requester);
            request.setAssertionId(authenticationAssertionId);
            ResolveAuthenticationAssertionResponseType response = _wsSSOIdentityProvider.resolveAuthenticationAssertion(request);

            if (logger.isDebugEnabled())
                logger.debug("[resolveAuthenticationAssertion('"+authenticationAssertionId+"')] : " + response.getSsoSessionId());

            return response.getSsoSessionId();

        } catch (AssertionNotValidErrorMessage e) {
            throw new AssertionNotValidException(e.getFaultInfo().getAssertionId());
        } catch (SSOIdentityProviderErrorMessage e) {
            throw new IdentityProvisioningException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new IdentityProvisioningException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }

    }

    public void globalSignoff(String requester, String sessionId) throws IdentityProvisioningException {
        try {
            if (logger.isDebugEnabled())
                logger.debug("[globalSignoff()] : " + sessionId);
            GlobalSignoffRequestType request = new GlobalSignoffRequestType();
            request.setRequester(requester);
            request.setSsoSessionId(sessionId);
            GlobalSignoffResponseType repsonse = _wsSSOIdentityProvider.globalSignoff(request);

        } catch (SSOIdentityProviderErrorMessage e) {
            throw new IdentityProvisioningException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new IdentityProvisioningException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }

}
