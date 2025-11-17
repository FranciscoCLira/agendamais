import java.sql.*;

public class QueryCounts {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: QueryCounts <jdbcUrl> <user> <password>");
            System.exit(2);
        }
        String url = args[0];
        String user = args[1];
        String pwd = args[2];

        try (Connection c = DriverManager.getConnection(url, user, pwd)) {
            printCount(c, "INSTITUICAO");
            printCount(c, "USUARIO");
            printCount(c, "LOCAL");
        }
    }

    private static void printCount(Connection c, String table) {
        String q = "SELECT COUNT(*) FROM " + table;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
            if (rs.next()) {
                System.out.printf("%s: %d\n", table, rs.getLong(1));
            }
        } catch (SQLException e) {
            System.out.printf("%s: ERROR (%s)\n", table, e.getMessage());
        }
    }
}
