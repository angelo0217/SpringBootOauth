package com.test.config;

import com.test.Const;
import com.test.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    UserServiceImpl userService;
    //
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient("client")
                .authorizedGrantTypes("password","refresh_token")
                .authorities("client")
                .scopes("read")
                .resourceIds(Const.DEMO_RESOURCE_ID)
                .secret("secret").accessTokenValiditySeconds(360);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {


        //走local store
        endpoints.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
                .authenticationManager(authenticationManager).userDetailsService(userService)
                .accessTokenConverter(accessTokenConverter())
                //走local store
                .tokenStore(tokenStore());
                //走Redis
//                .tokenStore(new RedisTokenStore(redisConnectionFactory));
    }
//  一般未使用JWT的
//    @Bean
//    public InMemoryTokenStore tokenStore() {
//        return new InMemoryTokenStore();
//    }

    @Bean
    public TokenStore tokenStore() {
        TokenStore tokenStore = new JwtTokenStore(accessTokenConverter());
        return tokenStore;
    }

    /**
     * JWT token Converter
     * @return
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter() {
            /***
             *  增強token的方法,自訂義一些token返回的信息
             */
            @Override
            public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                String userName = authentication.getUserAuthentication().getName();

                System.out.println(">>>"+userName);

                User user = (User) authentication.getUserAuthentication().getPrincipal();// 登陸後放進去的UserDetail實現類一直查看link{SecurityConfiguration}
                /** 自訂義一些token屬性 ***/
                final Map<String, Object> additionalInformation = new HashMap<>();
                additionalInformation.put("userName", userName);
                additionalInformation.put("roles", user.getAuthorities());
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInformation);
                OAuth2AccessToken enhancedToken = super.enhance(accessToken, authentication);
                return enhancedToken;
            }

        };
        accessTokenConverter.setSigningKey("123");//測試用，資源服務使用相同的字符達到一個對稱加密的效果，生產時候使用RSA非對稱加密方式
        return accessTokenConverter;
    }
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        /**
         * allow表示允許在認證的時候把參數放到Url之中傳過去
         * @see org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter
         */
        oauthServer.allowFormAuthenticationForClients();
    }

    /**
     * POST 測試如下
     * http://localhost:8088/oauth/token?username=admin&password=12345&grant_type=password&scope=read&client_id=client&client_secret=secret
     *
     * body
     * username = admin
     * password = 12345
     * grant_type = password
     * scope = read
     * client_id = client
     * client_secret = secret
     *
     * 會取得到Token
     *
     * 測試結果
     * http://localhost:8088/oauth/2?access_token=c5384c67-5f44-4feb-8a1e-02063dd666d6
     * JWT 下面
     * http://localhost:8088/oauth/2?access_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsidGVzdE9hdXRoIl0sInVzZXJfbmFtZSI6ImFkbWluIiwic2NvcGUiOlsicmVhZCJdLCJyb2xlcyI6W3siYXV0aG9yaXR5IjoiVVNFUiJ9XSwiZXhwIjoxNTI4NzgzMDczLCJ1c2VyTmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiJdLCJqdGkiOiIyMTkyOGZiZi0zYjU2LTQ1NGMtODMyMi0wNmJlMzg4MDY2NjIiLCJjbGllbnRfaWQiOiJjbGllbnQifQ.F_BQR3AvnNJrSjQ4FSExlBFr3ONLu8XHuAzHS123rg8
     */

}

