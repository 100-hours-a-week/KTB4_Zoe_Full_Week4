package kr.adapterz.springboot.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("mysql")
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
class MySqlMigrationTest {

    private static final List<String> EXPECTED_TABLES = List.of(
            "comments",
            "post_drafts",
            "post_images",
            "post_likes",
            "post_reports",
            "post_versions",
            "post_views",
            "posts",
            "refresh_tokens",
            "users"
    );

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void appliesInitialMigrationAndValidatesJpaMappingsOnMySql() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:mysql:");
        }

        Integer successfulMigrationCount = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '1'
                          AND success = TRUE
                        """)
                .query(Integer.class)
                .single();
        assertThat(successfulMigrationCount).isOne();

        List<String> tables = jdbcClient.sql("""
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                          AND table_name <> 'flyway_schema_history'
                        ORDER BY table_name
                        """)
                .query(String.class)
                .list();
        assertThat(tables).containsExactlyElementsOf(EXPECTED_TABLES);
    }
}
