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
@TypeDef(name = "json", typeClass = JsonType.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String spotifyId;

    @Column(nullable = false)
    private String socialId;

    @Column
    private String profileMusicUri;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Column
    private String albumImageUrl;

    @Column
    private String albumArtist;

    @Column
    private String albumTitle;

    @Column
    private String regionCode;

    @Column
    private String refreshToken;

    @Type(type="json")
    @Column
    private Map<String, String> hashTag;

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
