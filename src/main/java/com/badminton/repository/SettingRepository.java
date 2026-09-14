package com.badminton.repository;

import com.badminton.entity.Setting;
import com.badminton.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByUserAndSettingKey(User user, String settingKey);
    List<Setting> findByUser(User user);
}
