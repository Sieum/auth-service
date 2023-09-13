package sieum.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Verification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sieum.auth.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    // JWT 서명에 사용되는 비밀 키
    @Value("${jwt.secretKey}")
    private String secretKey;

    // 액세스 토큰 만료 기간(ms)
    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    // 리프레시 토큰 만료 기간(ms)
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    // 액세스 토큰을 응답 헤더에 저장하는데 사용되는 헤더 키
    @Value("${jwt.access.header}")
    private String accessHeader;

    // 리프레시 토큰을 응답 헤더에 저장하는데 사용되는 헤더 키
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    // email 내용을 담을 클레임 이름
    private static final String EMAIL_CLAIM = "email";
    // 토큰 추출을 위한 프리픽스
    private static final String BEARER = "Bearer ";

    // 사용자 데이터에 접근하기 위한 리포지토리
    private final UserRepository userRepository;

    //AccessToken 생성 메소드
    public String createAccessToken(UUID uuid) {
        Date now = new Date();
        // JWT 토큰을 생성하는 빌더 반환
        // create메서드는 라이브러리 내부상 static으로 선언되어 있음.
        String accessToken = JWT.create()
                // JWT의 Subject 지정 -> AccessToken이므로 AccessToken
                .withSubject(uuid.toString())
                // 토큰 만료 시간 설정
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                // HMAC512 알고리즘 사용, application-jwt.yml에서 지정한 secret 키로 암호화
                .sign(Algorithm.HMAC512(secretKey));
        System.out.println(accessToken);
        return accessToken;
//        return JWT.create()
        // JWT의 Subject 지정 -> AccessToken이므로 AccessToken
//                .withSubject(uuid.toString())
        // 토큰 만료 시간 설정
//                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
        // HMAC512 알고리즘 사용, application-jwt.yml에서 지정한 secret 키로 암호화
//                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * RefreshToken 생성
     * RefreshToken은 Claim에 email도 넣지 않으므로 withClaim() X
     */
    public String createRefreshToken(UUID uuid) {
        Date now = new Date();
        return JWT.create()
                .withSubject(uuid.toString())
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    // AccessToken 헤더에 실어서 보내기
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        //Authorization : accessToken
        response.setHeader(accessHeader, accessToken);
        log.debug("재발급된 Access Token : {}", accessToken);
    }

    // AccessToken + RefreshToken 헤더에 실어서 보내기
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, "Bearer " + accessToken);
        setRefreshTokenHeader(response, "Bearer " + refreshToken);
        log.info(response.getHeader(accessHeader));
        log.info("accessToken " + accessToken);
        log.info("refreshToken " + refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    // AccessToken 헤더 설정
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    // RefreshToken 헤더 설정
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /**
     * RefreshToken DB에 저장 혹은 업데이트
     * 존재하지 않는 사용자면 Exception 처리
     */
    public void updateRefreshToken(UUID uuid, String refreshToken) {
        userRepository.findById(uuid)
                .ifPresentOrElse(
                        user -> {
                            user.updateRefreshToken(refreshToken);
                            userRepository.save(user);
                        },
                        () -> new Exception("일치하는 회원이 없습니다.")
                );
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * AccessToken에서 Email 추출
     * 추출 전에 JWT.require()로 검증기 생성
     * verify로 AceessToken 검증 후
     * 유효하다면 getClaim()으로 이메일 추출
     * 유효하지 않다면 빈 Optional 객체 반환
     */
    public Optional<String> extractUUID(String accessToken) {
        System.out.println(accessToken);
        try {
            Verification require = JWT.require(Algorithm.HMAC512(secretKey));

            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    // 반환된 빌더로 JWT verifier 생성
                    .build()
                    // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .verify(accessToken)
                    // sub 가져오기
                    .getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("액세스 토큰이 유효하지 않습니다.");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            //HMAC512 알고리즘으로 토큰 복호화
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }
}