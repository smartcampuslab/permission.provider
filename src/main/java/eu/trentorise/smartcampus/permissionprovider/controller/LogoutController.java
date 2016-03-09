/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
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
 ******************************************************************************/
package eu.trentorise.smartcampus.permissionprovider.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author raman
 *
 */
@Controller
public class LogoutController {

	@Value("${default.redirect.url}")
	private String defaultRedirect;
		
	@Value("${welive.cas.server}")
	private String casServer;

	/**
	 * Logout from CAS protocol.
	 * @return
	 */
	@RequestMapping("/caslogout")
	public ModelAndView casLogout(HttpServletRequest req, HttpServletResponse res, @RequestParam(required=false) String service) {
		return logoutCommon(req, service);
	}
	/**
	 * Logout from SSO.
	 * @return
	 */
	@RequestMapping("/ssologout")
	public ModelAndView ssoLogout(HttpServletRequest req, HttpServletResponse res, @RequestParam(required=false) String redirect) {
		return logoutCommon(req, redirect);
	}
	/**
	 * Logout from site.
	 * @return
	 */
	@RequestMapping("/logout")
	public ModelAndView logout(HttpServletRequest req, HttpServletResponse res, @RequestParam(required=false) String service) {
		return logoutCommon(req, service);
	}

	private ModelAndView logoutCommon(HttpServletRequest req, String service) {
		String redirect = StringUtils.hasText(service) ? service : defaultRedirect;

		Authentication old = SecurityContextHolder.getContext().getAuthentication();
		if (old != null && old instanceof UsernamePasswordAuthenticationToken) {
			if ("welive".equals(old.getDetails())) {
				redirect = casServer+"/logout?service="+redirect;
			}
		}
		req.getSession().invalidate();
		
		return new ModelAndView("redirect:"+redirect);
	}	
}
