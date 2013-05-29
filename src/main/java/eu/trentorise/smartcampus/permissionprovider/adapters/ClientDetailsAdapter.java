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

package eu.trentorise.smartcampus.permissionprovider.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.PermissionBasic;

/**
 * @author raman
 *
 */
@Component
@Transactional
public class ClientDetailsAdapter {

	/**
	 * 
	 */
	private static final String GT_CLIENT_CREDENTIALS = "client_credentials";
	private static final String GT_IMPLICIT = "implicit";
	private static final String GT_AUTHORIZATION_CODE = "authorization_code";
	private static final String GT_REFRESH_TOKEN = "refresh_token";

	public synchronized String generateClientId() {
		return UUID.randomUUID().toString();
	}
	public synchronized String generateClientSecret() {
		return UUID.randomUUID().toString();
	}
	
	public List<ClientAppBasic> convertToClientApps(List<ClientDetailsEntity> entities){
		if (entities == null) {
			return Collections.emptyList();
		}
		List<ClientAppBasic> res = new ArrayList<ClientAppBasic>();
		for (ClientDetailsEntity e : entities) {
			res.add(convertToClientApp(e));
		}
		return res;
	}

	/**
	 * @param e
	 * @return
	 */
	public ClientAppBasic convertToClientApp(ClientDetails e) {
		ClientAppBasic res = new ClientAppBasic();
		res.setClientId(e.getClientId());
		res.setClientSecret(e.getClientSecret());
		res.setGrantedTypes(e.getAuthorizedGrantTypes());
		
		ClientAppInfo info = ClientAppInfo.convert(e.getAdditionalInformation());
		if (info != null) {
			res.setName(info.getName());
			res.setNativeAppsAccess(info.isNativeAppsAccess());
		}
		res.setServerSideAccess(e.getAuthorizedGrantTypes().contains(GT_AUTHORIZATION_CODE));
		res.setBrowserAccess(e.getAuthorizedGrantTypes().contains(GT_IMPLICIT));
		res.setRedirectUris(StringUtils.collectionToCommaDelimitedString(e.getRegisteredRedirectUri()));
		// TODO convert permissions
		res.setPermissions(Collections.<PermissionBasic>emptyList());
		return res;
	}
	public String defaultGrantTypes() {
		return GT_CLIENT_CREDENTIALS;
	}
	public String defaultAuthorities() {
		return "ROLE_CLIENT";
	}
	public String defaultScope() {
		return "read,write";
	}
	/**
	 * @param client
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public ClientDetailsEntity convertFromClientApp(ClientDetailsEntity client, ClientAppBasic data) {
		try {
			ClientAppInfo info = null;
			if (client.getAdditionalInformation() == null) {
				info = new ClientAppInfo();
			} else {
				info = ClientAppInfo.convert(client.getAdditionalInformation());
			}
			info.setName(data.getName());
			info.setNativeAppsAccess(data.isNativeAppsAccess());
			client.setAdditionalInformation(info.toJson());
			Set<String> types = new HashSet<String>(client.getAuthorizedGrantTypes());
			if (data.isBrowserAccess()) {
				types.add(GT_IMPLICIT);
			} else {
				types.remove(GT_IMPLICIT);
			} 
			// TODO decide the grant type for native app access
			if (data.isServerSideAccess() || data.isNativeAppsAccess()) {
				types.add(GT_AUTHORIZATION_CODE);
				types.add(GT_REFRESH_TOKEN);
			} else {
				types.remove(GT_AUTHORIZATION_CODE);
				types.remove(GT_REFRESH_TOKEN);
			}
			client.setAuthorizedGrantTypes(StringUtils.collectionToCommaDelimitedString(types));
			
			client.setRedirectUri(data.getRedirectUris());
			return client;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * @param client
	 * @param data
	 */
	public String validate(ClientDetailsEntity client, ClientAppBasic data) {
		if (client == null) return "app not found";
		if (data.getName() == null || data.getName().trim().isEmpty()) {
			return "name cannot be empty";
		}
		if ((data.isServerSideAccess() || data.isNativeAppsAccess()) && (data.getRedirectUris() == null || data.getRedirectUris().trim().isEmpty())) {
			return "redirect URL is required for Server-side or native access";
		}
		return null;
	}
}