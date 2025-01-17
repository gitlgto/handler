package com.nzxpc.handler.util.db;

import com.nzxpc.handler.util.BeanContext;
import com.nzxpc.handler.util.db.migrate.DbMigrateUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

public abstract class DataSourceConfigBase {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private DataSourceProperties dataSourceProperties;

    @PostConstruct
    private void init() {
        //用main的话，application没启动，没有初始化
        BeanContext.setApplicationContext(context);
        DbUtil.setJt(jdbcTemplate());
    }

    protected abstract Class[] setEntityPackages();

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public DataSource dataSource() {
        String url = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();
        DbMigrateUtil.migrate(url, username, password, setEntityPackages());
        return DataSourceBuilder.create().type(HikariDataSource.class).url(url).username(username).password(password).build();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager dr = new DataSourceTransactionManager();
        dr.setDataSource(dataSource());
        return dr;
    }
}
