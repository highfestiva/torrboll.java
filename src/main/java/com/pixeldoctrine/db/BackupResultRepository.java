package com.pixeldoctrine.db;

import com.pixeldoctrine.entity.BackupResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BackupResultRepository {

    @Autowired
    private DataSource dataSource;

    public void save(BackupResult result) throws SQLException {
        Connection connection = dataSource.getConnection();
        // using raw SQL to take advantage of SQLite's "INSERT OR REPLACE" (would otherwise have gone with JOOQ)
        PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO " +
                "BACKUP_LOG (TIMESTAMP, SERVICE, CLIENT, SYSTEM, JOB, PERC) " +
                "VALUES (DATETIME(?), ?, ?, ?, ?, ?)");
        String datestr = Instant.ofEpochMilli(result.getDate().getTime()).toString();
        statement.setString(1, datestr);
        statement.setString(2, result.getService());
        statement.setString(3, result.getClient());
        statement.setString(4, result.getSystem());
        statement.setString(5, result.getJob());
        statement.setInt(6, result.getPercent());
        statement.executeUpdate();
        connection.close();
    }

    public List<BackupResult> load(int days) throws SQLException, ParseException {
        Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        String sql = MessageFormat.format(
                "SELECT * FROM BACKUP_LOG WHERE TIMESTAMP >= DATE(''NOW'', ''-{0} DAYS'') ORDER BY TIMESTAMP",
                days-1);
        ResultSet rs = stmt.executeQuery(sql);
        List<BackupResult> result = new ArrayList<>();
        SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next()) {
            BackupResult br = new BackupResult(
                    timestampFormatter.parse(rs.getString("TIMESTAMP")),
                    rs.getString("SERVICE"),
                    rs.getString("CLIENT"),
                    rs.getString("SYSTEM"),
                    rs.getString("JOB"),
                    rs.getInt("PERC"),
                    null);
            result.add(br);
        }
        return result;
    }
}
