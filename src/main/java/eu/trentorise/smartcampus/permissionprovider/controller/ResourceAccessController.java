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

package eu.trentorise.smartcampus.permissionprovider.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.AuthorityMapping;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ResourceManager;
import eu.trentorise.smartcampus.permissionprovider.model.BasicClientInfo;
import eu.trentorise.smartcampus.permissionprovider.model.Client;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.ClientModel;
import eu.trentorise.smartcampus.permissionprovider.model.Permission;
import eu.trentorise.smartcampus.permissionprovider.model.PermissionData;
import eu.trentorise.smartcampus.permissionprovider.model.Permissions;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.model.Scope;
import eu.trentorise.smartcampus.permissionprovider.model.Scope.ACCESS_TYPE;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceParameterModel;
import eu.trentorise.smartcampus.permissionprovider.oauth.AutoJdbcTokenStore;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceServices;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;

/**
 * Controller for remote check the access to the resource
 * 
 * @author raman
 *
 */
@Controller
public class ResourceAccessController extends AbstractController {

	private static Log logger = LogFactory.getLog(ResourceAccessController.class);
	@Autowired
	private ResourceServices resourceServices;
	@Autowired
	private ResourceServerTokenServices resourceServerTokenServices;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceManager resourceManager;
	@Autowired
	private ClientDetailsManager clientDetailsManager;
	@Autowired
	private AutoJdbcTokenStore autoJdbcTokenStore;
	@Value("${api.token}")
	private String token;
	private ObjectMapper mapper = new ObjectMapper();

	private static ResourceFilterHelper resourceFilterHelper = new ResourceFilterHelper();

	/** create API **/
	@Autowired
	private ClientDetailsManager clientDetailsAdapter;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private AttributesAdapter attributesAdapter;
	/** GRANT TYPE: CLIENT CRIDENTIALS FLOW */
	private static final String GT_CLIENT_CREDENTIALS = "client_credentials";
	/** GRANT TYPE: IMPLICIT FLOW */
	private static final String GT_IMPLICIT = "implicit";
	/** GRANT TYPE: AUTHORIZATION GRANT FLOW */
	private static final String GT_AUTHORIZATION_CODE = "authorization_code";
	/** GRANT TYPE: REFRESH TOKEN */
	private static final String GT_REFRESH_TOKEN = "refresh_token";

	@RequestMapping(method = RequestMethod.POST, value = "/create/client/permissions/services")
	public @ResponseBody Response createClientServicesPermissions(@RequestBody Client client) {

		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);

		try {
			Long userId = getUserId();

			// step1 (saveEmpty).
			ClientAppBasic appData = client.getClientAppBasic();
			Permissions permissions = client.getPermissions();
			List<String> serviceIds = client.getServiceIds();

			ClientDetailsEntity clientDetails = new ClientDetailsEntity();

			if (!StringUtils.hasText(appData.getName())) {
				throw new IllegalArgumentException("An app name cannot be empty");
			}

			for (ClientDetailsEntity cde : clientDetailsRepository.findAll()) {
				if (ClientAppInfo.convert(cde.getAdditionalInformation()).getName().equals(appData.getName())) {
					throw new IllegalArgumentException("An app with the same name already exists");
				}
			}

			clientDetails.setClientId(appData.getClientId());
			clientDetails.setAuthorities("ROLE_CLIENT");
			clientDetails.setAuthorizedGrantTypes(GT_CLIENT_CREDENTIALS);
			clientDetails.setDeveloperId(userId);
			clientDetails.setClientSecret(appData.getClientSecret());
			clientDetails.setClientSecretMobile(appData.getClientSecretMobile());

			ClientAppInfo info = new ClientAppInfo();
			info.setName(appData.getName());
			info.setNativeAppsAccess(appData.isNativeAppsAccess());
			info.setNativeAppSignatures(Utils.normalizeValues(appData.getNativeAppSignatures()));
			Set<String> types = new HashSet<String>();
			if (appData.isBrowserAccess()) {
				types.add(GT_IMPLICIT);
			} else {
				types.remove(GT_IMPLICIT);
			}
			if (appData.isServerSideAccess() || appData.isNativeAppsAccess()) {
				types.add(GT_AUTHORIZATION_CODE);
				types.add(GT_REFRESH_TOKEN);
			} else {
				types.remove(GT_AUTHORIZATION_CODE);
				types.remove(GT_REFRESH_TOKEN);
			}
			clientDetails.setAuthorizedGrantTypes(StringUtils.collectionToCommaDelimitedString(types));
			if (info.getIdentityProviders() == null) {
				info.setIdentityProviders(new HashMap<String, Integer>());
			}

			for (String key : attributesAdapter.getAuthorityUrls().keySet()) {
				if (appData.getIdentityProviders().get(key)) {
					Integer value = info.getIdentityProviders().get(key);
					AuthorityMapping a = attributesAdapter.getAuthority(key);
					if (value == null || value == ClientAppInfo.UNKNOWN) {
						info.getIdentityProviders().put(key,
								a.isPublic() ? ClientAppInfo.APPROVED : ClientAppInfo.REQUESTED);
					}
				} else {
					info.getIdentityProviders().remove(key);
				}
			}

			if (info.getResourceApprovals() == null) {
				info.setResourceApprovals(new HashMap<String, Boolean>());
			}

			Collection<String> resourceIds = new HashSet<String>(clientDetails.getResourceIds());
			Collection<String> scopes = new HashSet<String>(clientDetails.getScope());

			for (String r : permissions.getSelectedResources().keySet()) {
				Resource resource = resourceRepository.findOne(Long.parseLong(r));
				// if not checked, remove from permissions and from pending
				// requests
				if (!permissions.getSelectedResources().get(r)) {
					info.getResourceApprovals().remove(r);
					resourceIds.remove(r);
					scopes.remove(resource.getResourceUri());
					// if checked but requires approval, check whether
					// - is the resource of the same client, so add
					// automatically
					// - already approved (i.e., included in client resourceIds)
					// - already requested (i.e., included in additional info
					// approval requests map)
				} else if (resource.getClientId() != null && !appData.getClientId().equals(resource.getClientId())
						&& resource.isApprovalRequired()) {
					if (!resourceIds.contains(r) && !info.getResourceApprovals().containsKey(r)) {
						info.getResourceApprovals().put(r, true);
					}
					// if approval is not required, include directly in client
					// resource ids
				} else {
					resourceIds.add(r);
					scopes.add(resource.getResourceUri());
				}
			}
			clientDetails.setResourceIds(StringUtils.collectionToCommaDelimitedString(resourceIds));
			clientDetails.setScope(StringUtils.collectionToCommaDelimitedString(scopes));
			clientDetails.setAdditionalInformation(info.toJson());

			clientDetailsRepository.save(clientDetails);

			// build permissions for each service.
			for (String serviceId : serviceIds) {
				resourceManager.buildPermissions(clientDetails, serviceId, userId);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}

		return response;
	}

	/**
	 * Check the access to the specified resource using the client app token
	 * header
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/access")
	public @ResponseBody Boolean canAccessResource(@RequestHeader("Authorization") String token,
			@RequestParam String scope, HttpServletRequest request) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			Collection<String> actualScope = auth.getAuthorizationRequest().getScope();
			String asString = StringUtils.collectionToCommaDelimitedString(actualScope);
			actualScope = StringUtils.commaDelimitedListToSet(asString.toLowerCase());
			Collection<String> scopeSet = StringUtils.commaDelimitedListToSet(scope.toLowerCase());
			if (actualScope != null && !actualScope.isEmpty() && actualScope.containsAll(scopeSet)) {
				return true;
			}
		} catch (AuthenticationException e) {
			logger.error("Error validating token: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Get information about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientinfo")
	public @ResponseBody BasicClientInfo getClientInfo(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
				if (client != null) {
					BasicClientInfo info = new BasicClientInfo();
					info.setClientId(clientId);
					info.setClientName((String) client.getAdditionalInformation().get("name"));
					return info;
				}
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
	}

	/**
	 * Get all authorization about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping("/resources/clientinfo/oauth")
	public @ResponseBody List<BasicClientInfo> getClientOAuthInfo(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);

			if (auth.getName() != null && !auth.getName().isEmpty()) {
				List<BasicClientInfo> infos = new ArrayList<BasicClientInfo>();
				String userId = auth.getName();
				// get different client_id for user in oauth_access_token
				// collection.
				List<Map<String, Object>> oAuthTokens = autoJdbcTokenStore.findClientIdsByUserName(userId);
				// loop through client_id and create info obj for each client_id
				// and add to list.
				for (Map oAuth2AccessTokenMap : oAuthTokens) {
					if (oAuth2AccessTokenMap.containsKey("client_id")) {
						String json = (String) oAuth2AccessTokenMap.get("additional_information");
						Map<String, Object> clientDetails = mapper.readValue(json, Map.class);
						String clientId = String.valueOf(oAuth2AccessTokenMap.get("client_id"));
						BasicClientInfo info = new BasicClientInfo();
						info.setClientId(clientId);
						info.setClientName(String.valueOf(clientDetails.get("name")));
						infos.add(info);
					}
				}

				return infos;

			} else {
				logger.error("Error getting information about client");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return null;
	}

	/**
	 * Get all authorization about the client handling the user name.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientinfo/oauth/{userId}")
	public @ResponseBody List<BasicClientInfo> getClientOAuthInfoByUserName(
			@RequestHeader("Authorization") String token, @PathVariable String userId, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			if (token == null || !token.matches(getAPICredentials())) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}

			if (userId != null && !userId.isEmpty()) {
				List<BasicClientInfo> infos = new ArrayList<BasicClientInfo>();
				// get different client_id for user in oauth_access_token
				// collection.
				List<Map<String, Object>> oAuthTokens = autoJdbcTokenStore.findClientIdsByUserName(userId);
				// loop through client_id and create info obj per client_id.
				for (Map oAuth2AccessTokenMap : oAuthTokens) {
					if (oAuth2AccessTokenMap.containsKey("client_id")) {
						String json = (String) oAuth2AccessTokenMap.get("additional_information");
						Map<String, Object> clientDetails = mapper.readValue(json, Map.class);
						String clientId = String.valueOf(oAuth2AccessTokenMap.get("client_id"));
						BasicClientInfo info = new BasicClientInfo();
						info.setClientId(clientId);
						info.setClientName(String.valueOf(clientDetails.get("name")));
						infos.add(info);
					}
				}

				return infos;

			} else {
				logger.error("Error getting information about client");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return null;
	}

	/**
	 * Get information about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientspec")
	public @ResponseBody ClientModel getClientSpec(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
				if (client != null) {
					ClientModel model = ClientModel.fromClient(client);
					// Set<String> scopes = model.getScopes();
					// for (String scope: scopes) {
					// Resource r = resourceManager.getResource(scope);
					// if (r != null) {
					//
					// }
					// }
					List<ResourceParameter> params = resourceManager.getOwnResourceParameters(clientId);
					if (params != null) {
						model.setOwnParameters(new HashSet<ServiceParameterModel>());
						for (ResourceParameter rp : params) {
							ServiceParameterModel spm = new ServiceParameterModel();
							spm.setName(rp.getParameter());
							spm.setService(rp.getService().getServiceName());
							spm.setValue(rp.getValue());
							spm.setVisibility(rp.getVisibility());
							model.getOwnParameters().add(spm);
						}
					}
					return model;
				}
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
	}

	@RequestMapping(value = "/resources/clientspec", method = RequestMethod.POST)
	public @ResponseBody ClientModel createClientSpec(@RequestHeader("Authorization") String token,
			@RequestBody ClientModel model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				try {
					clientDetailsManager.createNew(model,
							clientDetailsRepository.findByClientId(clientId).getDeveloperId());
				} catch (Exception e) {
					logger.error("Error creating client: " + e.getMessage());
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				return model;
			}
		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	@RequestMapping("/resources/permissions")
	public @ResponseBody PermissionData getServicePermissions(HttpServletRequest request,
			HttpServletResponse response) {
		PermissionData result = new PermissionData();
		result.setPermissions(new LinkedList<Permission>());

		Map<String, List<Scope>> map = new HashMap<String, List<Scope>>();
		List<Resource> resources = resourceManager.getAllAvailableResources();
		for (Resource r : resources) {
			String id = r.getService().getServiceId();
			List<Scope> list = map.get(id);
			if (list == null) {
				list = new LinkedList<Scope>();
				map.put(id, list);
			}
			Scope s = new Scope();
			s.setId(r.getResourceUri());
			s.setDescription(r.getDescription());
			s.setAccess_type(ACCESS_TYPE.fromAuthority(r.getAuthority()));
			list.add(s);
		}

		List<Service> serviceObjects = resourceManager.getServiceObjects();
		for (Service s : serviceObjects) {
			Permission permission = new Permission();
			permission.setName(s.getName());
			permission.setDescription(s.getDescription());
			permission.setScopes(map.get(s.getId()));
			result.getPermissions().add(permission);
		}

		return result;
	}

	private static class ResourceFilterHelper extends OAuth2AuthenticationProcessingFilter {
		public String parseTokenFromRequest(HttpServletRequest request) {
			return parseToken(request);
		}
	}

	/**
	 * @return
	 */
	private String getAPICredentials() {
		return "Basic " + token;
	}
}
