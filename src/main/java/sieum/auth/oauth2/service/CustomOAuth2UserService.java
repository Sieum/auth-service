package sieum.auth.oauth2.service;

import sieum.auth.oauth2.CustomOAuth2User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sieum.auth.entity.User;
import sieum.auth.oauth2.OAuthAttributes;
import sieum.auth.repository.UserRepository;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //1)
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService  = new DefaultOAuth2UserService();

        /*
        * 1)authorizationEndpoint에서 authCode 요청
        * 2)발급 받은 authCode로 tokenEndpoint에서 accessToken 요청
        * 3)발급 받은 accessToken으로 loadUser에서 사용자 정보 요청
        * */
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        //3) DefaultOAuth2User를 상속받는 CustomOAuth2User를 생성할 때 필수 매개변수
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        log.info("userNameAttributeName : " + userNameAttributeName);
        //"userNameAttributeName : sub"

        //4) OAuth Provider에게 제공받은 사용자 attribute
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("attributes : " + attributes);
        //attributes : {sub=103151414792709319215, name=김기정, given_name=기정, family_name=김, picture=https://lh3.googleusercontent.com/a/AAcHTtdcIh1LXjllo_2m6wgWvTu4VEog7kpCHfEsJdF6x-VG=s96-c, email=dlsgkrlwjd@gmail.com, email_verified=true, locale=ko}

        //5) 각 social 타입별로 필요한 값만 추출한 attributes
        OAuthAttributes extractAttributes = OAuthAttributes.of(attributes);

        User createdUser = getUser(extractAttributes);

        return new CustomOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName,
                createdUser.getId());
    }

    // 로그인 유저 DB에 저장
    private User getUser(OAuthAttributes attributes){

        User findUser = userRepository.findBySocialId(
                attributes.getOauth2UserInfo().getSocialId()).orElse(null);

        // 로그인 경험이 없는 신규 유저만 DB에 저장
        if (findUser == null) {
            return saveUser(attributes);
        }
        return findUser;
    }


    private User saveUser(OAuthAttributes attributes) {
        attributes.setId(UUID.randomUUID());
        User createdUser = attributes.toEntity(attributes);
        return userRepository.save(createdUser);
    }
}
