package cn.tellsea.freestyle.common.dynamicdb;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: He Zhigang
 * @Date: 2019/12/10 12:41
 * @Description:
 */
@EnableTransactionManagement
@Configuration
@MapperScan("cn.tellsea.freestyle.*.mapper")
public class MybatisPlusConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    @Bean(name = DbGlobal.DB1)
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource source() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = DbGlobal.DB2)
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource target() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 动态数据源配置
     *
     * @return
     */
    @Bean
    @Primary
    public DataSource multipleDataSource(@Qualifier(DbGlobal.DB1) DataSource source,
                                         @Qualifier(DbGlobal.DB2) DataSource target) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DbGlobal.DB1, source);
        targetDataSources.put(DbGlobal.DB2, target);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 程序默认数据源，这个要根据程序调用数据源频次，经常把常调用的数据源作为默认
        dynamicDataSource.setDefaultTargetDataSource(source);
        return dynamicDataSource;
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(multipleDataSource(source(), target()));

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        sqlSessionFactory.setConfiguration(configuration);
        // 乐观锁插件
        //PerformanceInterceptor(),OptimisticLockerInterceptor()
        // 分页插件
        sqlSessionFactory.setPlugins(paginationInterceptor());
        return sqlSessionFactory.getObject();
    }
}
