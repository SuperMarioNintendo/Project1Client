import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/waterpurification";
    private static final String USER = "root";
    private static final String PASS = "SuperMarioStar1981";

    // Predefined SQL queries
    private static final Map<String, String> QUERIES = new HashMap<>();

    static {
        QUERIES.put("a", "SELECT Name, TreatmentPerformed FROM WaterTreatmentFacility;");
        QUERIES.put("b", "SELECT Name, TreatmentPerformed FROM WaterTreatmentFacility WHERE TreatmentPerformed = 'Filtration';");
        QUERIES.put("c", "SELECT SUM(ConsumptPerMonthAvg) AS TotalConsumption FROM Business;");
        QUERIES.put("d", "SELECT SUM(WasteWaterPerMonthAvg) AS TotalWasteWater FROM Business;");
        QUERIES.put("e", "SELECT AVG(ConsumptPerMonthAvg) AS AvgIndustrialConsumption FROM Business WHERE Type IN ('Industrial', 'Hybrid');");
        QUERIES.put("f", "SELECT AVG(WasteWaterPerMonthAvg) AS AvgAgriculturalWasteWater FROM Business WHERE Type IN ('Agricultural', 'Hybrid');");
        QUERIES.put("g", "SELECT Location, PresQual FROM WaterSource WHERE PresQual < 5;");
        QUERIES.put("h", "SELECT T.TreatmentName, T.Description FROM Treatment T JOIN TreatmentTime TT ON T.TreatmentName = TT.TreatmentType WHERE TT.TreatedSourceLocation = 'Dropstar River';");
        QUERIES.put("i", "SELECT RegulatedTreatment FROM Regulation WHERE IDNumber = 1;");
        QUERIES.put("j", "INSERT INTO Treatment (TreatmentNumber, TreatmentName, Description) VALUES (6, 'UV Disinfection', 'Using ultraviolet light to disinfect water.'), (7, 'Ozonation', 'Using ozone gas to disinfect water.');");
        QUERIES.put("k", "INSERT INTO Regulation (IDNumber, RegulatedTreatment, RegName, RegApprovalDate, RegDescription) VALUES (6, 'UV Disinfection', 'New Disinfection Standard 2025', '2025-01-01', 'Regulation requiring UV disinfection for all drinking water.');");
        QUERIES.put("l", "INSERT INTO TreatmentsPerformed (Treatment, TreatmentFacility) VALUES ('UV Disinfection', 'Facility Ultraviolet');");
        QUERIES.put("m", "SELECT SUM(QuantityTreated) AS TotalTreatedWater FROM TreatmentTime WHERE TreatmentDate BETWEEN '1950-01-01' AND '2020-01-31';");
    }
    public static void main(String[] args) throws Exception {
        // Ensure the JDBC driver is available (optional for modern Java)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found. Ensure the Connector/J JAR is in the classpath.");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Database connection has been established successfully.");

            while (true) {
                System.out.println("\nEnter a query ID (a-m) to run. If you want to quit, type 'exit' to quit:");
                String userInput = scanner.nextLine().trim().toLowerCase();

                if ("exit".equals(userInput)) {
                    System.out.println("Thanks for your time. Exiting application.");
                    break;
                }

                String sqlQuery = QUERIES.get(userInput);
                if (sqlQuery != null) {
                    System.out.println("Executing query " + userInput + ": " + sqlQuery);
                    executeUserQuery(conn, sqlQuery, userInput);
                } else {
                    System.out.println("The input is invalid. Please enter a valid query ID (a-m) or 'exit'.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method the given SQL query and prints the results or update status.
     * @param conn represents the database connection.
     * @param sqlQuery represents the SQL query to execute.
     * @param queryIdentifier represents the ID of the query, used to determine if it's an update or select.
     */
    private static void executeUserQuery(Connection conn, String sqlQuery, String queryIdentifier) {
        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            
            // Check if the query is an INSERT (j, k, l)
            if (queryIdentifier.matches("[jkl]")) {
                int rowsAffected = pstmt.executeUpdate();
                conn.commit(); // Commit the transaction for DML operations
                System.out.println("Query " + queryIdentifier + " has been executed successfully. The following rows were affected: " + rowsAffected);
            } else {
                // Assume it's a SELECT query
                try (ResultSet result = pstmt.executeQuery()) {
                    printResultSet(result);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            try {
                // Rollback changes if a DML operation fails
                if (queryIdentifier.matches("[jkl]") && conn != null) {
                    conn.rollback();
                    System.out.println("Transaction has been rolled back due to error.");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        }
    }

    /**
     * This is a helper method to print the contents of a ResultSet.
     * @param results represents the ResultSet to print.
     * @throws SQLException dictates what happens when a database access error occurs.
     */
    private static void printResultSet(ResultSet results) throws SQLException {
        ResultSetMetaData resultsMetaData = results.getMetaData();
        int columns = resultsMetaData.getColumnCount();

        if (!results.isBeforeFirst()) { // Check if there are any results
            System.out.println("The query was executed, but no results were found.");
            return;
        }

        // Print column headers
        for (int i = 1; i <= columns; i++) {
            System.out.printf("%-25s", resultsMetaData.getColumnLabel(i));
        }
        System.out.println();
        System.out.println("-".repeat(25 * columns));

        // Print table data
        while (results.next()) {
            for (int i = 1; i <= columns; i++) {
                System.out.printf("%-25s", results.getString(i));
            }
            System.out.println();
        }
    }
}
