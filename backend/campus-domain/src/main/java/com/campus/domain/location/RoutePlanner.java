package com.campus.domain.location;

public final class RoutePlanner {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private RoutePlanner() {}

    public record Route(long distance, long durationMinutes) {}

    public static Route walking(double fromLat, double fromLng, double toLat, double toLng) {
        requireCoordinate(fromLat, fromLng);
        requireCoordinate(toLat, toLng);
        double p1 = Math.toRadians(fromLat), p2 = Math.toRadians(toLat);
        double dp = Math.toRadians(toLat - fromLat), dl = Math.toRadians(toLng - fromLng);
        double a = Math.sin(dp / 2) * Math.sin(dp / 2)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        long meters = Math.round(EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return new Route(meters, Math.max(1, Math.round(meters / 80.0)));
    }

    private static void requireCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("经纬度超出有效范围");
    }
}
