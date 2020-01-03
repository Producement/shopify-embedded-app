package com.ppublica.shopify.security.configurer.delegates;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ChannelSecurityConfigurer;


/**
 * Ensures that the web application only accepts HTTPS requests. It is required that the application explicitly 
 * add the configurer in WebSecurityConfigurerAdapter.
 * 
 * @author N F
 *
 */
public class ShopifyChannelSecurity implements HttpSecurityBuilderConfigurerDelegate {

	/**
	 * The ChannelSecurityConfigurer is set to require a secure connection for all paths.
	 * 
	 * @param http The HttpSecurityBuilder
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void applyShopifyConfig(HttpSecurityBuilder<?> http) {
		ChannelSecurityConfigurer<HttpSecurity> configurer = new ChannelSecurityConfigurer<HttpSecurity>(null);		
		
		configurer = http.getConfigurer(configurer.getClass());
		
		if (configurer == null) {
			throw new RuntimeException("ChannelSecurityConfigurer is required");
		}
		
		configurer.getRegistry()
						.anyRequest()
							.requiresSecure();
	}

	@Override
	public void applyShopifyInit(HttpSecurityBuilder<?> http) { }
		
		

}
