# SQL data loading

Docker runs the files in this directory only when the MySQL volume is first created.

## Local demo import

For a non-Docker local MySQL demonstration, import the single bundled file `CampusOS_local_demo.sql` into a new or empty MySQL server:

```powershell
mysql --default-character-set=utf8mb4 -uroot -p < docs/sql/CampusOS_local_demo.sql
```

It includes the application seed data, crawler-derived course reviews, role-permission model, pending-claim teacher accounts, and course-teacher mappings. It does not require Docker or external files.

For an existing `campus_os` database, run these scripts explicitly in order:

```powershell
docker cp docs/sql/004_course_review_schema.sql campus-mysql:/tmp/004_course_review_schema.sql
docker cp docs/sql/004_course_reviews.sql campus-mysql:/tmp/004_course_reviews.sql
docker cp docs/sql/006_swjtuh_xipu_map.sql campus-mysql:/tmp/006_swjtuh_xipu_map.sql
docker cp docs/sql/009_crawler_teacher_identity.sql campus-mysql:/tmp/009_crawler_teacher_identity.sql
docker cp docs/sql/010_teaching_schedule_indexes.sql campus-mysql:/tmp/010_teaching_schedule_indexes.sql
docker cp docs/sql/011_teaching_course_catalog_link.sql campus-mysql:/tmp/011_teaching_course_catalog_link.sql
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/004_course_review_schema.sql"
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/004_course_reviews.sql"
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/006_swjtuh_xipu_map.sql"
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/009_crawler_teacher_identity.sql"
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/010_teaching_schedule_indexes.sql"
docker exec campus-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -proot campus_os < /tmp/011_teaching_course_catalog_link.sql"
```

`004_course_reviews.sql` is generated from `web-crawler/output/reviews.csv`. It imports only approved, anonymous course-review data and uses source IDs plus `INSERT IGNORE`, so it can be rerun safely. The crawler does not contain a student course-selection roster; it cannot legitimately be converted into student enrollments.

`009_crawler_teacher_identity.sql` generates one disabled, pending-claim teacher account for each distinct professor name in the imported catalog and maps every catalog course to that account. These are references derived only from public names, not verified real-world identities. An administrator must call `PUT /api/admin/user/{userId}/claim-crawler-teacher` with `{"newPassword":"..."}` before a placeholder account is enabled. It also adds role-permission mappings for student, teacher, academic administrator, and system administrator.
