package sieum.auth.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseEntity {
    @Column(name = "member_is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(name ="member_created_date")
    private LocalDateTime createdDate;

    @Column(name ="member_modified_date")
    private LocalDateTime modifiedDate;
    public void deleted() {
        this.isDeleted = true;
    }
}