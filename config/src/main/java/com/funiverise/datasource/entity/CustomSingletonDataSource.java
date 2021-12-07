package com.funiverise.datasource.entity;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;

import java.sql.SQLException;

/**
 * @author Funny
 * @version 1.0
 * @description: 自定义单例数据源
 * @date 2021/12/7 13:48
 */
public class CustomSingletonDataSource {

    
    private static String dbUrl;
    private static String username;
    private static String password;
    private static String driverClassName;
    private static int initialSize;
    private static int minIdle;
    private static int maxActive;
    private static int maxWait;

    @Value("${spring.datasource.druid.url}")
    public void setDbUrl(String dbUrl) {
        CustomSingletonDataSource.dbUrl = dbUrl;
    }

    @Value("${spring.datasource.druid.username}")
    public void setUsername(String username) {
        CustomSingletonDataSource.username = username;
    }
    @Value("${spring.datasource.druid.password}")
    public void setPassword(String password) {
        CustomSingletonDataSource.password = password;
    }
    @Value("${spring.datasource.druid.driver-class-name}")
    public void setDriverClassName(String driverClassName) {
        CustomSingletonDataSource.driverClassName = driverClassName;
    }
    @Value("${spring.datasource.druid.initial-size}")
    public void setInitialSize(int initialSize) {
        CustomSingletonDataSource.initialSize = initialSize;
    }
    @Value("${spring.datasource.druid.min-idle}")
    public void setMinIdle(int minIdle) {
        CustomSingletonDataSource.minIdle = minIdle;
    }
    @Value("${spring.datasource.druid.max-active}")
    public void setMaxActive(int maxActive) {
        CustomSingletonDataSource.maxActive = maxActive;
    }
    @Value("${spring.datasource.druid.max-wait}")
    public void setMaxWait(int maxWait) {
        CustomSingletonDataSource.maxWait = maxWait;
    }


    public static DruidDataSource getDruidDatasource() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setUrl(dbUrl);
        druidDataSource.setFilters("stat,wall");
        druidDataSource.setInitialSize(initialSize);
        druidDataSource.setMinIdle(minIdle);
        druidDataSource.setMaxActive(maxActive);
        druidDataSource.setMaxWait(maxWait);
        druidDataSource.setUseGlobalDataSourceStat(true);
        druidDataSource.setDriverClassName(driverClassName);
        return druidDataSource;
    }


    public static void main(String[] args) {
        try {
            getDruidDatasource();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
