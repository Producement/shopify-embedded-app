package com.ppublica.shopify.security.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.ppublica.shopify.security.service.DefaultShopifyUserService;
import com.ppublica.shopify.security.service.ShopifyOAuth2AuthorizedClientService;
import com.ppublica.shopify.security.service.TokenService;
import com.ppublica.shopify.security.web.AuthorizationSuccessPageStrategy;
import com.ppublica.shopify.security.web.ForwardAuthorizationSuccessPageStrategy;
import com.ppublica.shopify.security.web.GenerateDefaultAuthorizationPageStrategy;
import com.ppublica.shopify.security.web.NoRedirectSuccessHandler;
import com.ppublica.shopify.security.web.ShopifyAuthorizationCodeTokenResponseClient;
import com.ppublica.shopify.security.web.ShopifyHttpSessionOAuth2AuthorizationRequestRepository;
import com.ppublica.shopify.security.web.ShopifyOAuth2AuthorizationRequestResolver;
import com.ppublica.shopify.security.authentication.CipherPassword;
import com.ppublica.shopify.security.authentication.ShopifyVerificationStrategy;
import com.ppublica.shopify.security.configurer.delegates.ShopifyChannelSecurity;
import com.ppublica.shopify.security.configurer.delegates.ShopifyCsrf;
import com.ppublica.shopify.security.configurer.delegates.ShopifyHeaders;
import com.ppublica.shopify.security.configurer.delegates.ShopifyLogout;
import com.ppublica.shopify.security.configurer.delegates.ShopifyOAuth2;
import com.ppublica.shopify.security.repository.ShopifyTokenRepositoryImpl;
import com.ppublica.shopify.security.repository.TokenRepository;

/*
 * ppublica.shopify.security.endpoints.install=
 * ppublica.shopify.security.endpoints.authorization-redirect=
 * ppublica.shopify.security.endpoints.login=
 * ppublica.shopify.security.endpoints.logout=
 * ppublica.shopify.security.endpoints.authentication-failure=
 * ppublica.shopify.security.endpoints.uninstall=
 * ppublica.shopify.security.endpoints.enable-default-info-page=
 * ppublica.shopify.security.endpoints.menu-link=
 * 
 * ppublica.shopify.security.cipher.password= **required**
 * 
 * ppublica.shopify.security.client.client_id= **required**
 * ppublica.shopify.security.client.client_secret= **required**
 * ppublica.shopify.security.client.scope= **required**
 * 
 * 
 * Requires the following beans to be in the ApplicationContext:
 * JdbcTemplate
 * 
 * Beans created:
 * 
 * TokenRepository
 * ShopifyPaths
 * CipherPassword
 * OAuth2UserService<OAuth2UserRequest, OAuth2User>
 * OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
 * AuthorizationSuccessPageStrategy
 * AuthenticationSuccessHandler
 * ClientRegistration
 * ClientRegistrationRepository
 * TokenService 
 * OAuth2AuthorizedClientService
 * ShopifyHttpSessionOAuth2AuthorizationRequestRepository
 * OAuth2AuthorizationRequestResolver
 * ShopifyVerificationStrategy
 * CsrfTokenRepository
 * ShopifyHeaders
 * ShopifyChannelSecurity
 * ShopifyCsrf
 * ShopifyLogout
 * ShopifyOAuth2
 * 
 */
@Configuration
public class SecurityBeansConfig {
	
	public static final String SHOPIFY_REGISTRATION_ID = "shopify";
	
	
	@Bean
	public TokenRepository getTokenRepository(JdbcTemplate jdbc) {
		ShopifyTokenRepositoryImpl repo = new ShopifyTokenRepositoryImpl();
		repo.setJdbc(jdbc);
		
		return repo;
	}
	
	@Bean
	public ShopifyPaths shopifyPaths(@Value("${ppublica.shopify.security.endpoints.install:}") String installPath,
							  @Value("${ppublica.shopify.security.endpoints.authorization-redirect:}") String authorizationRedirectPath,
							  @Value("${ppublica.shopify.security.endpoints.login:}") String loginEndpoint,
							  @Value("${ppublica.shopify.security.endpoints.logout:}") String logoutEndpoint,
							  @Value("${ppublica.shopify.security.endpoints.authentication-failure:}") String authenticationFailureUri,
							  @Value("${ppublica.shopify.security.endpoints.uninstall:}") String uninstallUri,
							  @Value("${ppublica.shopify.security.endpoints.enable-default-info-page:}") String enableDefaultInfoPage,
							  @Value("${ppublica.shopify.security.endpoints.menu-link:}") String menuLink) {
		
		boolean enableDefaultInfo = false;
		if(enableDefaultInfoPage != null) {
			enableDefaultInfo = Boolean.parseBoolean(enableDefaultInfoPage);
		}
		return new ShopifyPaths(installPath, authorizationRedirectPath, loginEndpoint,
								logoutEndpoint, authenticationFailureUri, uninstallUri, enableDefaultInfo, menuLink);
		
	}

	
	@Bean
	public CipherPassword cipherPassword(@Value("${ppublica.shopify.security.cipher.password:#{null}}") String password) {
		if(password == null) {
			throw new RuntimeException("Cipher password is required! Set the property ppublica.shopify.security.cipher.password");
		}
		return new CipherPassword(password);
	}
	
	
	@Bean
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> userService() {
		return new DefaultShopifyUserService();
	}
	
	
	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
		return new ShopifyAuthorizationCodeTokenResponseClient();
	}
	
	@Bean
	public AuthorizationSuccessPageStrategy authorizationPageStrategy(ShopifyPaths path) {
		boolean isCustomAuthorizationRedirectPath = path.isCustomAuthorizationRedirectPath();

		if(isCustomAuthorizationRedirectPath) {
			return new ForwardAuthorizationSuccessPageStrategy(path.getAuthorizationRedirectPath());
		} else {
			return new GenerateDefaultAuthorizationPageStrategy(path.getMenuLinks());
		}
	}
	
	
	@Bean
	public AuthenticationSuccessHandler successHandler(AuthorizationSuccessPageStrategy authorizationPageStrategy) {
		return new NoRedirectSuccessHandler(authorizationPageStrategy);
	}
	
	
	@Bean
	protected ClientRegistration shopifyClientRegistration(@Value("${ppublica.shopify.security.client.client_id:#{null}}")String clientId,
			 @Value("${ppublica.shopify.security.client.client_secret:#{null}}")String clientSecret, 
			 @Value("${ppublica.shopify.security.client.scope:#{null}}")String scope,
			 ShopifyPaths shopifyPaths) {
		
		if(clientId == null) {
			throw new RuntimeException("Client id is required! Set the property ppublica.shopify.security.client.client_id");
		}
		
		if(clientSecret == null) {
			throw new RuntimeException("Client secret is required! Set the property ppublica.shopify.security.client.client_secret");
		}
		
		if(scope == null) {
			throw new RuntimeException("Scope is required! Set the property publica.shopify.security.client.scope");
		}
		
        return ClientRegistration.withRegistrationId(SHOPIFY_REGISTRATION_ID)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUriTemplate("{baseUrl}" + shopifyPaths.getAuthorizationRedirectPath() + "/{registrationId}")
            .scope(scope.split(","))
            .authorizationUri("https://{shop}/admin/oauth/authorize")
            .tokenUri("https://{shop}/admin/oauth/access_token")
            .clientName("Shopify")
            .build();
    }
	
	
	@Bean
    public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration shopifyClientRegistration) {
        return new InMemoryClientRegistrationRepository(shopifyClientRegistration);
    }
	
	@Bean
	public TokenService tokenService(TokenRepository repo, CipherPassword cipherPassword, ClientRegistrationRepository clientRegistrationRepository) {
		return new TokenService(repo, cipherPassword, clientRegistrationRepository);
	}
	
	
	// used by AuthenticatedPrincipalOAuth2AuthorizedClientRepository
	@Bean
	public OAuth2AuthorizedClientService clientService(TokenService tokenService) {
		return new ShopifyOAuth2AuthorizedClientService(tokenService);
	}
	
	
	@Bean
	public ShopifyHttpSessionOAuth2AuthorizationRequestRepository customAuthorizationRequestRepository(ShopifyPaths shopifyPaths) {
		return new ShopifyHttpSessionOAuth2AuthorizationRequestRepository(shopifyPaths.getInstallPath());
	}
	
	
	@Bean
	public OAuth2AuthorizationRequestResolver shopifyOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
					ShopifyHttpSessionOAuth2AuthorizationRequestRepository customAuthorizationRequestRepository,
					ShopifyPaths shopifyPaths) {
		return new ShopifyOAuth2AuthorizationRequestResolver(clientRegistrationRepository, customAuthorizationRequestRepository, shopifyPaths.getInstallPath(), shopifyPaths.getLoginEndpoint());
	}
	
	
	@Bean
	public ShopifyVerificationStrategy shopifyVerficationStrategy(ClientRegistrationRepository clientRegistrationRepository,
					ShopifyHttpSessionOAuth2AuthorizationRequestRepository customAuthorizationRequestRepository) {
		return new ShopifyVerificationStrategy(clientRegistrationRepository, customAuthorizationRequestRepository);
	}
	
	
	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
		CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
		repo.setCookieHttpOnly(false);
		
		return repo;
	}
	
	@Bean
	public ShopifyHeaders shopifyHeaders() {
		return new ShopifyHeaders();
	}
	
	@Bean
	public ShopifyChannelSecurity shopifyChannelSecurity() {
		return new ShopifyChannelSecurity();
	}
	
	@Bean
	public ShopifyCsrf shopifyCsrf(ShopifyPaths shopifyPaths, CsrfTokenRepository csrfTokenRepo) {
		return new ShopifyCsrf(shopifyPaths.getUninstallUri(), csrfTokenRepo);
	}
	
	@Bean
	public ShopifyLogout shopifyLogout(ShopifyPaths shopifyPaths) {
		return new ShopifyLogout(shopifyPaths.getLoginEndpoint(), shopifyPaths.getLogoutEndpoint());
	}
	
	@Bean
	public ShopifyOAuth2 shopifyOAuth2(ShopifyPaths shopifyPaths) {
		return new ShopifyOAuth2(shopifyPaths.getAnyAuthorizationRedirectPath(), shopifyPaths.getLoginEndpoint(), shopifyPaths.getAuthenticationFailureUri());
	}
		
	
}