package sieum.auth.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(name = "members")
//@TypeDef(name = "json", typeClass = JsonType.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "member_spotify_user_id", nullable = false)
    private String socialId;

    @Column(name = "member_profile_music_uri")
    private String profileMusicUri;

    @Column(name = "member_nickname", nullable = false)
    private String nickname;

    @Column(name = "member_profile_image_url")
    private String profileImageUrl;

    @Column(name = "member_album_image_url")
    private String albumImageUrl;

    @Column(name = "member_album_artist")
    private String albumArtist;

    @Column(name = "member_album_title")
    private String albumTitle;

    @Column(name = "member_region_code")
    private String regionCode;

    @Column(name = "member_refresh_token")
    private String refreshToken;

//    @Type(type="json")
//    @Column
//    private Map<String, String> hashTag;

    public void updateName(String nickname){
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

//    public User updateUser(UserProfileUpdateDto userProfileUpdateDto) {
//        if (userProfileUpdateDto.getName() != null)
//            this.name = userProfileUpdateDto.getName();
//        if (userProfileUpdateDto.getProfileImage() != null)
//            this.profileImage = userProfileUpdateDto.getProfileImage();
//        if (userProfileUpdateDto.getMeetingUrl() != null)
//            this.meetingUrl = userProfileUpdateDto.getMeetingUrl();
//        return this;
//    }
}
