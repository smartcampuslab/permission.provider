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

package eu.trentorise.smartcampus.permissionprovider.auth.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.common.AlreadyRegisteredException;
import eu.trentorise.smartcampus.permissionprovider.common.RegistrationException;
import eu.trentorise.smartcampus.permissionprovider.manager.RegistrationManager;
import eu.trentorise.smartcampus.permissionprovider.model.Registration;

/**
 * @author raman
 *
 */
@Controller
@RequestMapping(value = "/internal")
public class RegistrationController {

	@Autowired
	private RegistrationManager manager;
	
	/**
	 * Redirect to login page
	 * @param req
	 * @return
	 */
	@RequestMapping("/login")
	public String loginPage(HttpServletRequest req) {
		return "registration/login";
	}
	
	/**
	 * Login the user 
	 * 
	 * @param model
	 * @param username
	 * @param password
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(
			Model model,
			@RequestParam String username, 
			@RequestParam String password,
			HttpServletRequest req) 
	{
		try {
			Registration user = manager.getUser(username, password);
			String targetEnc = null;
			try {
				targetEnc = URLEncoder.encode((String) req.getSession()
						.getAttribute("redirect"), "UTF8");
			} catch (UnsupportedEncodingException e) {
				throw new RegistrationException(e);
			}
			req.getSession().setAttribute(
					InternalRegFilter.SESSION_INTERNAL_CHECK, "true");
			String redirect = String
					.format("redirect:/eauth/internal?target=%s&email=%s&name=%s&surname=%s",
							targetEnc,
							user.getEmail(), 
							user.getName(),
							user.getSurname());;
			return redirect;
		} catch (RegistrationException e) {
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/login";
		}
	}
	/**
	 * Redirect to registration page
	 * @param model
	 * @param req
	 * @return
	 */
	@RequestMapping("/register")
	public String regPage(Model model,
			HttpServletRequest req) {
		model.addAttribute("reg", new RegistrationBean());
		return "registration/register";
	}
	
	/**
	 * Register the user and redirect to the 'registersuccess' page
	 * @param model
	 * @param reg
	 * @param result
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String register(Model model, 
			@ModelAttribute("reg") @Valid RegistrationBean reg,
			BindingResult result,
			HttpServletRequest req) 
	{
		if (result.hasErrors()) {
			return "registration/register";
        }
		try {
			Locale locale = LocaleContextHolder.getLocale();
			manager.register(reg.getName(), reg.getSurname(), reg.getEmail(), reg.getPassword(), locale.getLanguage());
			return "registration/regsuccess";
		} catch (RegistrationException e) {
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/register";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", RegistrationException.class.getSimpleName());
			return "registration/register";
		}
	}
	
	/**
	 * Register with the REST call
	 * @param model
	 * @param reg
	 * @param result
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/register/rest", method = RequestMethod.POST)
	public @ResponseBody void registerREST(@RequestBody RegistrationBean reg,
			HttpServletResponse res) 
	{
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<RegistrationBean>> errors = validator.validate(reg);
	      
		if (errors.size() > 0) {
			res.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
        }
		try {
			manager.register(reg.getName(), reg.getSurname(), reg.getEmail(), reg.getPassword(), reg.getLang());
		} catch(AlreadyRegisteredException e) {
			res.setStatus(HttpStatus.CONFLICT.value());
		} catch (RegistrationException e) {
			e.printStackTrace();
			res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpStatus.CONFLICT.value());
			res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	

	/**
	 * Redirect to the resend page to ask for the email
	 * @param model
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/resend")
	public String resendPage() {
		return "registration/resend";
	}

	/**
	 * Resend the confirmation link to the registered user.
	 * @param model
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/resend", method = RequestMethod.POST)
	public String resendConfirm(Model model, @RequestParam String username) {
		try {
			manager.resendConfirm(username);
			return "registration/regsuccess";
		} catch (RegistrationException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/resend";
		}
	}

	/**
	 * Confirm the user given the confirmation code sent via mail. Redirect to confirmsuccess page
	 * @param model
	 * @param confirmationCode
	 * @return
	 */
	@RequestMapping("/confirm")
	public String confirm(Model model, @RequestParam String confirmationCode) {
		try {
			Registration user = manager.confirm(confirmationCode);
			if (user.getPassword() != null) {
				return "registration/confirmsuccess";
			} else {
				model.addAttribute("reg", new RegistrationBean());
				return "registration/changepwd";
			}
		} catch (RegistrationException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/confirmerror";
		}
	}
	
	@RequestMapping(value = "/reset")
	public String resetPage() {
		return "registration/resetpwd";
	}
	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public String reset(Model model, @RequestParam String username) {
		try {
			manager.resetPassword(username);
		} catch (RegistrationException e) {
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/resetpwd";
		}
		return "registration/resetsuccess";
	}
	@RequestMapping(value = "/changepwd", method = RequestMethod.POST)
	public String changePwd(Model model, 
			@ModelAttribute("reg") @Valid RegistrationBean reg,
			BindingResult result,
			HttpServletRequest req) 
	{
		if (result.hasFieldErrors("password")) {
			return "registration/changepwd";
		}
		try {
			manager.updatePassword(reg.getEmail(), reg.getPassword());
		} catch (RegistrationException e) {
			e.printStackTrace();
			model.addAttribute("error", e.getClass().getSimpleName());
			return "registration/changepwd";
		}
		return "registration/changesuccess";
	}
}
