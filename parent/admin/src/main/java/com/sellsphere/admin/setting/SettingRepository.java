package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {

    List<Setting> findAllByCategory(SettingCategory category);

    List<Setting> findAllByCategoryIn(List<SettingCategory> category);

}
