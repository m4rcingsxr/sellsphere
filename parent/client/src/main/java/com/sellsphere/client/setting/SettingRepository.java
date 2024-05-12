package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
    List<Setting> findAllByCategory(SettingCategory settingCategory);

    @Query("SELECT s FROM Setting s WHERE s.category = ?1 OR s.category = ?2")
    List<Setting> findAllByCategories(SettingCategory catOne, SettingCategory catTwo);

    Setting findByKey(String key);
}
