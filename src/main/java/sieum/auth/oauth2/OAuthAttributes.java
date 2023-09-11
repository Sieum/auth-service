package sieum.auth.oauth2;

import lombok.Builder;
import lombok.Getter;
import sieum.auth.oauth2.userinfo.OAuth2UserInfo;
import sieum.auth.entity.User;
import sieum.auth.oauth2.userinfo.SpotifyOAuth2UserInfo;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {

    private OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)
    private UUID id;
    private String refreshToken;
    @Builder
    public OAuthAttributes(OAuth2UserInfo oauth2UserInfo) {
        this.oauth2UserInfo = oauth2UserInfo;
    }

    /**
     * SocialType에 맞는 메소드 호출하여 OAuthAttributesDto 객체 반환
     * 파라미터 : userNameAttributeName -> OAuth2 로그인 시 키(PK)가 되는 값 / attributes : OAuth 서비스의 유저 정보들
     * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 제공하는
     * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
     */
    public static OAuthAttributes of(Map<String, Object> attributes) {

        return ofSpotify(attributes);
    }

    public static OAuthAttributes ofSpotify(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .oauth2UserInfo(new SpotifyOAuth2UserInfo(attributes))
                .build();
    }

    /**
     * of메소드로 OAuthAttributesDto 객체가 생성되어,
     * 유저 정보들이 담긴 OAuth2UserInfo가 소셜 타입별로 주입된 상태
     * OAuth2UserInfo에서 email, nickname, imageUrl을 가져와서 build
     * id에는 UUID로 중복 없는 랜덤 값 생성
     */
    public User toEntity(OAuthAttributes oAuthAttributes) {
        return User.builder()
                .id(oAuthAttributes.getId())
                .socialId(oAuthAttributes.getOauth2UserInfo().getSocialId())
                .refreshToken(oAuthAttributes.getRefreshToken())
                .nickname(oAuthAttributes.getOauth2UserInfo().getNickname())
                .build();
    }

    public void setId(UUID uuid){
        this.id = uuid;
    }

    public void setRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

}