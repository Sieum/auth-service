package sieum.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sieum.auth.oauth2.handler.OAuth2LoginFailureHandler;
import sieum.auth.oauth2.handler.OAuth2LoginSuccessHandler;
import sieum.auth.oauth2.service.CustomOAuth2UserService;
import sieum.auth.repository.CookieAuthorizationRequestRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig { // WebSecurityConfigurerAdapter : Spring Security 5.7.0v 부터 deprecated
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    private static final String[] swaggerPatterns = {
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v2/api-docs",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //Spring Security 전에 선행되는 CorsFilter를 Security에 통합
                .cors().configurationSource(corsConfigurationSource())
                .and()
                // HTML <form>을 통해 아이디 비밀번호를 제공하여 로그인하는 방식이 아니므로 disable
                .formLogin().disable() // FormLogin 사용 X
                // JWT 토큰 로그인(Bearer)방식을 사용할 것이므로 browser가 제공하는 기본 사용자 인증 disable
                .httpBasic().disable() // httpBasic 사용 X
                // REST API를 사용하여 서버에 인증 정보를 저장하지 않으므로 csrf protection disable
                .csrf().disable() // csrf 보안 사용 X
                // Click jacking, DDos 막기 위한 protection을 disable
                .headers().frameOptions().disable()
                .and()

                // 세션 사용하지 않으므로 세션 정책을 STATELESS로 설정
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                //인증/인가 설정 시 HttpServletRequest를 이용
                .authorizeRequests()

                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                //== URL별 권한 관리 옵션 ==//

                // 기본 페이지, css, image, js 하위 폴더에 있는 자료들은 모두 접근 가능, h2-console에 접근 가능
                .antMatchers("/","/css/**","/images/**","/js/**","/favicon.ico","/h2-console/**").permitAll()
                //Swagger URL 허용
                .antMatchers(swaggerPatterns).permitAll()
                // 회원가입 접근 가능 자체 회원 가입이 없으니 빼도 될 거 같음
                .antMatchers("/oauth2/**").permitAll()

                // 위의 경로 이외에는 모두 인증된 사용자만 접근 가능
                .anyRequest().authenticated();
    http
            .oauth2Login()
            // 사용자 로그인 후 auth state 저장
            .authorizationEndpoint().authorizationRequestRepository(cookieAuthorizationRequestRepository)
            .and()
            // 사용자 authCode가 반환되는 경로로 Provider developer에 설정한 redirectUri와 일치해야함
            .redirectionEndpoint().baseUri("/oauth2/callback/*")
            .and()
            // 생략 된 tokenEndpoint에서 반환 받은 authCode로 accessToken 요청
//            .tokenEndpoint()
            /*
             * Spring Security 내장 필터 OAuth2LoginAuthenticationFilter가
             * OAuth 2.0 인증 공급자에게 사용자 정보를 가져올 때 사용할 서비스 지정
             * tokenEndpoint에서 발급받은 accessToken으로 customOAuth2UserService의 loadUser에서 사용자 정보를 요청하는 REST API 호출
             * */
            .userInfoEndpoint().userService(customOAuth2UserService)
            .and()
            // 동의하고 계속하기를 눌렀을 때 Handler 설정
            .successHandler(oAuth2LoginSuccessHandler)
            // 소셜 로그인 실패 시 핸들러 설정
            .failureHandler(oAuth2LoginFailureHandler);


        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        configuration.setAllowCredentials(true); // axios에서 withCredentials:true로 설정한 경우 필수
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
