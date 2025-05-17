package com.bmgf.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MySQLService {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public MySQLService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    /**
     * 创建数据库（如果不存在）
     */
    public void createDatabaseIfNotExists(String dbName) {
        String sql = "CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
        jdbcTemplate.execute(sql);
    }
    /**
     * 初始化漏洞数据（示例，可根据实际漏洞库表结构改写）
     */
    public void initializeVulnData(String dbName) {
        // 切换到目标数据库
        jdbcTemplate.execute("USE `" + dbName + "`");

        // 示例：创建漏洞表（如果不存在）
        String createTableSql = "CREATE TABLE IF NOT EXISTS vuln_samples (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "payload VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        jdbcTemplate.execute(createTableSql);

        // 示例：插入初始化数据（可扩展）
        String insertSql = "INSERT INTO vuln_samples (payload, description) VALUES (?, ?)";

        // 这里示例插入一条数据，实际可批量插入
        jdbcTemplate.update(insertSql,
                "' OR '1'='1", "SQL注入经典示例");
    }
}
