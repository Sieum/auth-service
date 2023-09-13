package sieum.auth.oauth2.userinfo;

import java.util.Map;

public class SpotifyOAuth2UserInfo extends OAuth2UserInfo{
    public SpotifyOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getSocialId() {
        return (String)attributes.get("id");
    }

    @Override
    public String getNickname() {
        return (String)attributes.get("display_name");
    }
}
