package com.campus.infrastructure.persistence.activity;

import com.campus.domain.activity.entity.Activity;
import com.campus.domain.activity.entity.ActivityRegistration;
import com.campus.domain.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ActivityRepositoryImpl implements ActivityRepository {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;

    // ===== 转换方法 =====
    private Activity toActivity(ActivityPO po) { Activity a = new Activity(); BeanUtils.copyProperties(po, a); return a; }
    private ActivityPO toActivityPO(Activity a) { ActivityPO po = new ActivityPO(); BeanUtils.copyProperties(a, po); return po; }
    private ActivityRegistration toReg(ActivityRegistrationPO po) { ActivityRegistration r = new ActivityRegistration(); BeanUtils.copyProperties(po, r); return r; }
    private ActivityRegistrationPO toRegPO(ActivityRegistration r) { ActivityRegistrationPO po = new ActivityRegistrationPO(); BeanUtils.copyProperties(r, po); return po; }

    // ===== Activity CRUD =====

    @Override public void save(Activity activity) { ActivityPO po = toActivityPO(activity); activityMapper.insert(po); activity.setId(po.getId()); }
    @Override public void update(Activity activity) { activityMapper.updateById(toActivityPO(activity)); }
    @Override public Activity findById(Long id) { return toActivity(activityMapper.selectById(id)); }
    @Override public void deleteById(Long id) { activityMapper.deleteById(id); }

    @Override
    public List<Activity> findList(String category, String status, int offset, int size) {
        return activityMapper.selectPage(category, status, offset, size)
                .stream().map(this::toActivity).collect(Collectors.toList());
    }

    @Override
    public long countList(String category, String status) {
        return activityMapper.count(category, status);
    }

    @Override
    public List<Activity> findAdminList(String category, String status, int offset, int size) {
        return activityMapper.selectAdminPage(category, status, offset, size)
                .stream().map(this::toActivity).collect(Collectors.toList());
    }

    @Override
    public long countAdminList(String category, String status) {
        return activityMapper.countAdmin(category, status);
    }

    // ===== Registration =====

    @Override
    public void saveRegistration(ActivityRegistration registration) {
        ActivityRegistrationPO po = toRegPO(registration);
        registrationMapper.insert(po);
        registration.setId(po.getId());
    }

    @Override public void updateRegistration(ActivityRegistration registration) {
        registrationMapper.updateById(toRegPO(registration));
    }

    @Override public ActivityRegistration findRegistrationById(Long id) {
        return toReg(registrationMapper.selectById(id));
    }

    @Override
    public ActivityRegistration findRegistration(Long activityId, Long userId) {
        return toReg(registrationMapper.selectByActivityAndUser(activityId, userId));
    }

    @Override
    public List<ActivityRegistration> findRegistrationsByActivityId(Long activityId, int offset, int size) {
        return registrationMapper.selectByActivityId(activityId, offset, size)
                .stream().map(this::toReg).collect(Collectors.toList());
    }

    @Override
    public long countRegistrationsByActivityId(Long activityId) {
        return registrationMapper.countByActivityId(activityId);
    }

    @Override
    public List<Long> findRegisteredActivityIds(Long userId) {
        return registrationMapper.selectRegisteredActivityIds(userId);
    }

    @Override
    public List<ActivityRegistration> findRegistrationsByUserId(Long userId) {
        return registrationMapper.selectByUserId(userId)
                .stream().map(this::toReg).collect(Collectors.toList());
    }

    @Override
    public boolean existsRegistration(Long activityId, Long userId) {
        return registrationMapper.countByActivityAndUser(activityId, userId) > 0;
    }
}
