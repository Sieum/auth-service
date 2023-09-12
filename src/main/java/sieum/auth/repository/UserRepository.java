package sieum.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sieum.auth.entity.User;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndIsDeletedFalse(UUID userId);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialId(String socialId);

}
