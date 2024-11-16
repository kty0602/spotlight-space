package com.spotlightspace.core.user.repository;

import static com.spotlightspace.common.exception.ErrorCode.USER_NOT_FOUND;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserQueryRepository {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(long id);

    default User findByIdOrElseThrow(long id) {
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));
    }

    default User findByEmailOrElseThrow(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));
    }

}

