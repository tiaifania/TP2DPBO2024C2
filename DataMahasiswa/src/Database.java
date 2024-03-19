import javax.swing.plaf.nimbus.State;
import java.sql.*;

//konstruktor
public class Database {
    private Connection connection;

    private Statement statement;

    public Database() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_mahasiswa", "root", "");
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //digunakan untuk select
    public ResultSet selectQuery(String sql) {
        try {
            statement.executeQuery(sql);
            return  statement.getResultSet();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //digunakan untuk insert, update, dan delete
    public int insertUpdateDeleteQuery (String sql) {
        try {
            return  statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //getter
    public Statement getStatement() {
        return statement;
    }
}
