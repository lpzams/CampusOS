package com.campus.application.location.service;

import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.location.entity.Location;
import com.campus.domain.location.entity.LocationFloor;
import com.campus.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationAppService {

    private final LocationRepository locationRepository;

    private static final double EARTH_RADIUS = 6371000.0; // 地球半径（米）
    private static final double STEP_DISTANCE = 50.0;     // 每步距离（米）

    // ==================== 14.1 获取校园地点列表 ====================

    public List<Map<String, Object>> getLocationList(String category) {
        List<Location> locations = locationRepository.findAll(category);

        return locations.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("name", l.getName());
            item.put("category", l.getCategoryDesc());
            item.put("categoryCode", l.getCategory());
            item.put("longitude", l.getLongitude());
            item.put("latitude", l.getLatitude());
            item.put("address", l.getAddress());
            item.put("building", l.getBuilding());
            item.put("image", l.getImage());
            item.put("description", l.getDescription());
            return item;
        }).collect(Collectors.toList());
    }

    // ==================== 14.2 获取地点详情 ====================

    public Map<String, Object> getLocationDetail(Long id) {
        Location location = locationRepository.findById(id);
        if (location == null) throw new BusinessException(ResultCode.LOCATION_NOT_FOUND);

        List<LocationFloor> floors = locationRepository.findFloorsByLocationId(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", location.getId());
        result.put("name", location.getName());
        result.put("category", location.getCategoryDesc());
        result.put("categoryCode", location.getCategory());
        result.put("longitude", location.getLongitude());
        result.put("latitude", location.getLatitude());
        result.put("address", location.getAddress());
        result.put("building", location.getBuilding());
        result.put("image", location.getImage());
        result.put("description", location.getDescription());
        result.put("facilities", parseJsonArray(location.getFacilities()));
        result.put("openTime", location.getOpenTime());

        // 楼层信息
        List<Map<String, Object>> floorList = new ArrayList<>();
        for (LocationFloor f : floors) {
            Map<String, Object> floorItem = new LinkedHashMap<>();
            floorItem.put("floor", f.getFloor());
            floorItem.put("rooms", parseJsonArray(f.getRooms()));
            floorList.add(floorItem);
        }
        result.put("floors", floorList);

        return result;
    }

    // ==================== 14.3 搜索地点 ====================

    public List<Map<String, Object>> searchLocations(String keyword, Double userLng, Double userLat) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "搜索关键词不能为空");
        }

        List<Location> locations = locationRepository.searchByKeyword(keyword.trim());

        return locations.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("name", l.getName());
            item.put("category", l.getCategoryDesc());
            item.put("categoryCode", l.getCategory());
            item.put("longitude", l.getLongitude());
            item.put("latitude", l.getLatitude());
            item.put("address", l.getAddress());
            // 如果提供了用户坐标，计算距离
            if (userLng != null && userLat != null) {
                item.put("distance", (int) haversineDistance(userLat, userLng, l.getLatitude(), l.getLongitude()));
            } else {
                item.put("distance", 0);
            }
            return item;
        }).collect(Collectors.toList());
    }

    // ==================== 14.4 获取路径规划 ====================

    public Map<String, Object> getRoute(Double fromLng, Double fromLat,
                                         Double toLng, Double toLat, String mode) {
        if (fromLng == null || fromLat == null || toLng == null || toLat == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "起止坐标不能为空");
        }

        // 计算总距离（米）
        double totalDistance = haversineDistance(fromLat, fromLng, toLat, toLng);

        // 步行速度 ~5km/h = ~83m/min
        int duration = Math.max(1, (int) Math.round(totalDistance / 83.0));

        // 查找最近的已知地点作为起点/终点名称
        String startName = findNearestLocationName(fromLng, fromLat);
        String endName = findNearestLocationName(toLng, toLat);

        // 生成步行步骤
        List<Map<String, Object>> steps = generateWalkingSteps(fromLat, fromLng, toLat, toLng, totalDistance);

        String modeDesc = "walk".equals(mode) ? "步行" : mode;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("distance", (int) totalDistance);
        result.put("duration", duration);
        result.put("mode", modeDesc);

        Map<String, Object> start = new LinkedHashMap<>();
        start.put("name", startName);
        start.put("longitude", fromLng);
        start.put("latitude", fromLat);
        result.put("start", start);

        Map<String, Object> end = new LinkedHashMap<>();
        end.put("name", endName);
        end.put("longitude", toLng);
        end.put("latitude", toLat);
        result.put("end", end);

        result.put("steps", steps);
        return result;
    }

    // ===== 私有方法 =====

    /** Haversine 公式计算两点间距离（米） */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /** 查找最近的已知地点名称 */
    private String findNearestLocationName(Double lng, Double lat) {
        List<Location> all = locationRepository.findAll(null);
        if (all.isEmpty()) return "未知地点";

        Location nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Location l : all) {
            double d = haversineDistance(lat, lng, l.getLatitude(), l.getLongitude());
            if (d < minDist) {
                minDist = d;
                nearest = l;
            }
        }
        // 仅在距离较近（<300米）时返回地点名
        return (nearest != null && minDist < 300) ? nearest.getName() : "未知地点";
    }

    /** 生成步行步骤 */
    private List<Map<String, Object>> generateWalkingSteps(double fromLat, double fromLng,
                                                            double toLat, double toLng,
                                                            double totalDistance) {
        List<Map<String, Object>> steps = new ArrayList<>();

        if (totalDistance < 1) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("instruction", "您已到达目的地");
            step.put("distance", 0);
            step.put("direction", "");
            steps.add(step);
            return steps;
        }

        // 计算方位角
        double bearing = calculateBearing(fromLat, fromLng, toLat, toLng);
        String direction = bearingToDirection(bearing);

        int remainingDistance = (int) totalDistance;

        // 如果距离小于一步，合并为一步
        if (remainingDistance <= STEP_DISTANCE) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("instruction", "向" + direction + "步行" + remainingDistance + "米");
            step.put("distance", remainingDistance);
            step.put("direction", direction);
            steps.add(step);
        } else {
            // 拆分为多步
            int stepCount = (int) Math.ceil(remainingDistance / STEP_DISTANCE);
            for (int i = 0; i < stepCount; i++) {
                int stepDist = (i == stepCount - 1)
                        ? remainingDistance - (i * (int) STEP_DISTANCE)
                        : (int) STEP_DISTANCE;

                if (stepDist <= 0) break;

                // 每两步稍微调整方向（模拟实际行走）
                String stepDirection = direction;
                if (i > 0 && stepCount > 1) {
                    String[] altDirs = getAlternativeDirections(direction);
                    stepDirection = altDirs[i % altDirs.length];
                }

                Map<String, Object> step = new LinkedHashMap<>();
                step.put("instruction", "向" + stepDirection + "步行" + stepDist + "米");
                step.put("distance", stepDist);
                step.put("direction", stepDirection);
                steps.add(step);
            }
        }

        return steps;
    }

    /** 计算方位角（度） */
    private double calculateBearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double y = Math.sin(dLng) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                - Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /** 方位角 → 中文方向 */
    private String bearingToDirection(double bearing) {
        if (bearing >= 337.5 || bearing < 22.5) return "北";
        if (bearing >= 22.5 && bearing < 67.5) return "东北";
        if (bearing >= 67.5 && bearing < 112.5) return "东";
        if (bearing >= 112.5 && bearing < 157.5) return "东南";
        if (bearing >= 157.5 && bearing < 202.5) return "南";
        if (bearing >= 202.5 && bearing < 247.5) return "西南";
        if (bearing >= 247.5 && bearing < 292.5) return "西";
        return "西北";
    }

    /** 获取备选方向（用于多步路径的多样性） */
    private String[] getAlternativeDirections(String main) {
        switch (main) {
            case "北": return new String[]{"北", "东北", "西北"};
            case "南": return new String[]{"南", "东南", "西南"};
            case "东": return new String[]{"东", "东北", "东南"};
            case "西": return new String[]{"西", "西北", "西南"};
            case "东北": return new String[]{"东北", "北", "东"};
            case "东南": return new String[]{"东南", "南", "东"};
            case "西北": return new String[]{"西北", "北", "西"};
            case "西南": return new String[]{"西南", "南", "西"};
            default: return new String[]{main};
        }
    }

    /** 解析 JSON 数组字符串为 List */
    @SuppressWarnings("unchecked")
    private List<String> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            // 简单的 JSON 数组解析：[ "a", "b" ] → List
            String trimmed = json.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String inner = trimmed.substring(1, trimmed.length() - 1);
                if (inner.trim().isEmpty()) return Collections.emptyList();
                return Arrays.stream(inner.split(","))
                        .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("JSON数组解析失败: {}", json);
        }
        return Collections.emptyList();
    }
}
