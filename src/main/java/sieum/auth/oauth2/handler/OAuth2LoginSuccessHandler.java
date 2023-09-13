package sieum.auth.oauth2.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import sieum.auth.jwt.JwtService;
import sieum.auth.oauth2.CustomOAuth2User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RedirectStrategy redirectStratgy = new DefaultRedirectStrategy();

    @Value("${jwt.redirect}")
    private String REACT_NATIVE_REDIRECT;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("OAuth2 Login 성공!");
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();
            // 로그인에 성공한 경우 access, refresh 토큰 생성
            loginSuccess(request, response, oAuth2User);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 소셜 로그인 성공 시 생성한 access/refreshToken 응답 헤더에 담아 사용자에게 전달
    private void loginSuccess(HttpServletRequest request, HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException, ServletException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getId());
        String refreshToken = jwtService.createRefreshToken(oAuth2User.getId());
        jwtService.updateRefreshToken(oAuth2User.getId(), refreshToken);
        String targetUrl = UriComponentsBuilder.fromUriString(REACT_NATIVE_REDIRECT)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
        redirectStratgy.sendRedirect(request, response, targetUrl);
    }
}
