package com.ppublica.shopify.security.configurer.delegates;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ChannelSecurityConfigurer;


/**
 * Ensures that the web application only accepts HTTPS requests. It is required that the application
 * explicitly add the configurer in WebSecurityConfigurerAdapter.
 *
 * @author N F
 */
public class ShopifyChannelSecurity implements HttpSecurityBuilderConfigurerDelegate {

  private final Log logger = LogFactory.getLog(ShopifyChannelSecurity.class);

  /**
   * The ChannelSecurityConfigurer is set to require a secure connection for all paths.
   *
   * @param http The HttpSecurityBuilder
   */
  @SuppressWarnings("unchecked")
  @Override
  public void applyShopifyConfig(HttpSecurityBuilder<?> http) {
    logger.debug("Applying ShopifyChannelSecurity config");

    ChannelSecurityConfigurer<HttpSecurity> configurer = http
        .getConfigurer(ChannelSecurityConfigurer.class);

    if (configurer != null) {
      configurer.getRegistry()
          .anyRequest()
          .requiresSecure();
    }

  }

  @Override
  public void applyShopifyInit(HttpSecurityBuilder<?> http) {
  }


}
