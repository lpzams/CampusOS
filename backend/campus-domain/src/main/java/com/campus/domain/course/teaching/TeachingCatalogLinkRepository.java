package com.campus.domain.course.teaching;

import java.util.Collection;
import java.util.Map;

/** Stores the explicit relation between a formal class offering and crawler review evidence. */
public interface TeachingCatalogLinkRepository {
    Map<String, Object> findCatalogCourse(String courseSourceId);
    Map<Long, Map<String, Object>> findLinks(Collection<Long> teachingCourseIds);
    void link(Long teachingCourseId, String courseSourceId);
    void unlink(Long teachingCourseId);
}
