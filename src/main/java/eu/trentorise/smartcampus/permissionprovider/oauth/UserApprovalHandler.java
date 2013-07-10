/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.permissionprovider.oauth;

import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.TokenServicesUserApprovalHandler;

import eu.trentorise.smartcampus.permissionprovider.Config;
import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;

/**
 * Extension of {@link TokenServicesUserApprovalHandler} to enable automatic authorization
 * for trusted clients.
 * @author raman
 *
 */
public class UserApprovalHandler extends TokenServicesUserApprovalHandler {

	@Autowired
	private ServletContext servletContext;
	@Autowired 
	private ResourceServices resourceService;
	
	@Override
	public AuthorizationRequest updateBeforeApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
		return super.updateBeforeApproval(authorizationRequest, userAuthentication);
	}

	/**
	 * Allows automatic approval for trusted clients.
	 * 
	 * @param authorizationRequest The authorization request.
	 * @param userAuthentication the current user authentication
	 * 
	 * @return Whether the specified request has been approved by the current user.
	 */
	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {

		// If we are allowed to check existing approvals this will short circuit the decision
		if (super.isApproved(authorizationRequest, userAuthentication)) {
			return true;
		}

		if (!userAuthentication.isAuthenticated()) {
			return false;
		}

		String flag = authorizationRequest.getApprovalParameters().get(AuthorizationRequest.USER_OAUTH_APPROVAL);
		boolean approved = flag != null && flag.toLowerCase().equals("true");
		if (approved) return true;
		
		// or trusted client
		// or test token redirect uri
		// or accesses only own resources
		return authorizationRequest.getAuthorities().contains(Config.AUTHORITY.ROLE_CLIENT_TRUSTED.toString())
				|| authorizationRequest.getRedirectUri().equals(ExtRedirectResolver.testTokenPath(servletContext))
				|| useOwnResourcesOnly(authorizationRequest.getClientId(), authorizationRequest.getScope());
	}

	/**
	 * @param clientId
	 * @param resourceUris
	 * @return true if the given client requires access only to the resources managed by the client itself
	 */
	private boolean useOwnResourcesOnly(String clientId, Set<String> resourceUris) {
		if (resourceUris != null) {
			for (String uri : resourceUris) {
				Resource r = resourceService.loadResourceByResourceUri(uri);
				if (r.getAuthority() == AUTHORITY.ROLE_USER && ! clientId.equals(r.getClientId())) return false;
			}
		}
		return true;
	}


}
