package com.campus.application.location;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.domain.location.RoutePlanner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LocationAppService {
    private final CampusAppService records;
    public LocationAppService(CampusAppService records) { this.records = records; }

    public List<Map<String, Object>> list(String category) { return records.search("location", "categoryCode", category); }
    public Map<String, Object> detail(Long id) { return records.get("location", id); }
    public List<Map<String, Object>> search(String keyword) { return records.search("location", "name", keyword); }

    public Map<String, Object> route(Map<String, Object> command) {
        try {
            RoutePlanner.Route route = RoutePlanner.walking(number(command, "fromLat"), number(command, "fromLng"),
                    number(command, "toLat"), number(command, "toLng"));
            return Map.of("distance", route.distance(), "duration", route.durationMinutes(), "steps",
                    List.of(Map.of("instruction", "步行至目的地", "distance", route.distance(), "direction", "目的地")));
        } catch (RuntimeException e) { throw Values.invalid(e); }
    }

    private double number(Map<String, Object> body, String key) { return Values.decimal(body.get(key)).doubleValue(); }
}
