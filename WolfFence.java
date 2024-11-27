import java.io.*; // For reading files
import java.util.*; // For List, ArrayList, Comparator, etc.

public class WolfFence {

    public static void main(String[] args) {
        // Initialize an empty list of points
        List<Point> points = new ArrayList<>();

        // Read points from the input file
        try {
            points = readPointsFromFile("lamar_valley_gps_points.txt"); // File path to the dataset
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        // Start measuring the total execution time
        long startTime = System.nanoTime();

        // Step 1: Compute the Convex Hull
        long hullStart = System.nanoTime();
        List<Point> convexHull = computeConvexHull(points);
        long hullEnd = System.nanoTime();

        // Step 2: Compute the Fence Length (Perimeter of the Convex Hull)
        long lengthStart = System.nanoTime();
        double fenceLength = computeFenceLength(convexHull);
        long lengthEnd = System.nanoTime();

        long endTime = System.nanoTime();

        // Print results
        System.out.println("Convex Hull Points: " + convexHull);
        System.out.println("Fence Length: " + fenceLength + " miles");
        System.out.println("Convex Hull Computation Time: " + (hullEnd - hullStart) / 1e6 + " ms");
        System.out.println("Fence Length Calculation Time: " + (lengthEnd - lengthStart) / 1e6 + " ms");
        System.out.println("Total Execution Time: " + (endTime - startTime) / 1e6 + " ms");
    }

    /**
     * Reads points from a file and stores them in a list.
     * Each line in the file should contain two numbers (latitude and longitude) separated by a comma.
     * @param filename Path to the input file.
     * @return List of points read from the file.
     * @throws IOException If there is an issue reading the file.
     */
    public static List<Point> readPointsFromFile(String filename) throws IOException {
        List<Point> points = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        // Read each line of the file
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(","); // Split by comma
            if (parts.length == 2) {
                try {
                    double x = Double.parseDouble(parts[0].trim()); // Latitude
                    double y = Double.parseDouble(parts[1].trim()); // Longitude
                    points.add(new Point(x, y)); // Add the point to the list
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        }
        reader.close(); // Close the file reader
        return points;
    }

    /**
     * Computes the Convex Hull using the Monotone Chain Algorithm.
     * @param points List of points (latitude, longitude) representing wolf habitat.
     * @return List of points forming the convex hull in counter-clockwise order.
     */
    public static List<Point> computeConvexHull(List<Point> points) {
        // Sort points by x-coordinate, then by y-coordinate
        points.sort(Comparator.comparingDouble((Point p) -> p.x).thenComparingDouble(p -> p.y));

        // Build the lower hull
        List<Point> lower = new ArrayList<>();
        for (Point p : points) {
            while (lower.size() >= 2 && cross(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0) {
                lower.remove(lower.size() - 1); // Remove the last point
            }
            lower.add(p); // Add the current point to the hull
        }

        // Build the upper hull
        List<Point> upper = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            Point p = points.get(i); // Start from the rightmost point
            while (upper.size() >= 2 && cross(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0) {
                upper.remove(upper.size() - 1); // Remove the last point
            }
            upper.add(p); // Add the current point to the hull
        }

        // Remove the last points of lower and upper hulls to avoid duplication
        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);

        // Combine the lower and upper hulls
        lower.addAll(upper);
        return lower; // Return the points forming the convex hull
    }

    /**
     * Computes the total length of the fence (perimeter of the convex hull).
     * @param hull List of points forming the convex hull.
     * @return Total length of the fence in miles.
     */
    public static double computeFenceLength(List<Point> hull) {
        double length = 0.0; // Initialize total length to 0
        for (int i = 0; i < hull.size(); i++) {
            Point p1 = hull.get(i);
            Point p2 = hull.get((i + 1) % hull.size()); // Wrap around to the first point
            length += distance(p1, p2); // Add the distance to the total length
        }
        return length; // Return the total length of the fence
    }

    /**
     * Calculates the Haversine distance between two points (latitude and longitude).
     * @param p1 First point (latitude, longitude).
     * @param p2 Second point (latitude, longitude).
     * @return Distance between p1 and p2 in miles.
     */
    public static double distance(Point p1, Point p2) {
        final double R = 3963.0; // Earth's radius in miles
        double lat1 = Math.toRadians(p1.x);
        double lon1 = Math.toRadians(p1.y);
        double lat2 = Math.toRadians(p2.x);
        double lon2 = Math.toRadians(p2.y);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in miles
    }

    /**
     * Calculates the cross product of vectors OA and OB to determine the turn direction.
     * Positive value: Counter-clockwise turn.
     * Negative value: Clockwise turn.
     * Zero: Collinear points.
     * @param o Origin point (shared by both vectors).
     * @param a Point forming vector OA.
     * @param b Point forming vector OB.
     * @return Cross product value.
     */
    public static double cross(Point o, Point a, Point b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }

    /**
     * Represents a 2D point with x and y coordinates.
     */
    static class Point {
        double x, y; // Use double to support latitude and longitude values

        // Constructor to initialize the point
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")"; // String representation of the point
        }
    }
}
