package com.gordondickens.bcf.config;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.ApplicationInstanceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudEnvironmentPropertiesFactoryBean;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@PropertySource("classpath:META-INF/spring/database.properties")
@Profile(Env.CLOUD)
public class ApplicationConfigCloud extends ApplicationConfigCommon {
	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationConfigCloud.class);

	@Bean
	public Properties serviceProperties() {
		Properties props = new Properties();
		props.put("url", url);
		props.put("name", databaseName);
		props.put("hostname", host);
		props.put("port", port);
		props.put("username", user);
		props.put("password", password);
		props.put("driver", driverClassName);
		return props;
	}

	@Bean
	public Properties cloudEnvironment() {
		CloudEnvironmentPropertiesFactoryBean cloudProps = new CloudEnvironmentPropertiesFactoryBean();
		CloudEnvironment cloudEnvironment = new CloudEnvironment();
		cloudProps.setCloudEnv(cloudEnvironment);
		Properties props = null;
		try {
			ApplicationInstanceInfo appInfo = cloudEnvironment
					.getInstanceInfo();
			String[] appinfo = { appInfo.getHost(), appInfo.getName(),
					(new Integer(appInfo.getInstanceIndex())).toString(),
					(new Integer(appInfo.getPort())).toString(),
					appInfo.getUris().toString() };
			logger.debug(
					"Cloud Environment Info: \n\tHost '{}', \n\tName '{}', \n\tInstance '{}', \n\tPort '{}', \n\tURIs '{}'",
					appinfo);
			cloudProps.setCloudEnv(cloudEnvironment);
			MysqlServiceCreator mysql = new MysqlServiceCreator(
					cloudEnvironment);

			logger.debug("MySQL driver class '{}', Validation query '{}' ",
					mysql.getDriverClassName(), mysql.getValidationQuery());

			props = cloudProps.getObject();
		} catch (Exception e) {
			logger.error("Error getting Properties from Cloud Environment", e);
		}
		logger.debug("Returning Cloud Environment Properties '{}'", props);
		return props;
	}

	/*
	 * <cloud:service-properties id="serviceProperties" />
	 *
	 * <mongo:db-factory id="mongoDbFactory"
	 * dbname="#{serviceProperties['db.name']}"
	 * host="#{serviceProperties['db.hostname']}"
	 * port="#{serviceProperties['db.port']}"
	 * username="#{serviceProperties['db.username']}"
	 * password="#{serviceProperties['db.password']}"/> -->
	 *
	 */

	@Override
	protected DatabasePopulator databasePopulator(DataSource dataSource) {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(true);
		populator.addScript(new ClassPathResource("batch-mysql-ddl.sql"));
		try {
			populator.populate(dataSource.getConnection());
		} catch (SQLException e) {
			logger.error("Exception Populating Database", e);
		}
		return populator;
	}

}
