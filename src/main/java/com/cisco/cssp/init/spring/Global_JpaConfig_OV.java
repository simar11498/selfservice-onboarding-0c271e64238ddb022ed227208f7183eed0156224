package com.cisco.cssp.init.spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * 
 * Spring Config for JPA. 
 * 
 * 
 * @author pnightin
 * 
 * @see <a
 *      href="http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/orm.html#orm-jpa">
 *      Spring Jpa docs </a>
 *      
 * @see LocalContainerEntityManagerFactoryBean
 *
 */
@Aspect
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Global_JpaConfig_OV {

	public static final String JPA_PC = "within(com.cisco.cssp.sample.spring.Demo_DataAccessObject)";
	final private Logger logger = LogManager.getLogger(getClass());


	/**
	 * 
	 * java simon monitoring 
	 * 
	 */
	@Pointcut("within(org.apache.commons.dbcp.BasicDataSource*)")
    private void dbcpPC() {} ;
    
	/**
	 * Connection Pool
	 * 
	 * @return
	 */


	/**
	 * 
	 * Considerations:
	 * <ol>
	 * <li>DBCP docs are not very clear</li>
	 * <li>pool configuration is autowired via Spring Java Config</li>
	 * <li>this pool grows/shrinks dynamically based on evictable settings. Most apps should have a fixed pool, similar to having fixed heap sizes</li>
	 * <li>JavaSimion registers this bean into JMX, so that CSAP can graph usage.</li>
	 * </ol>
	 * @return
	 * 
	 * @see Jmx_SpringRegistration
	 * @see Jmx_SimonRegistration
	 * @see http://commons.apache.org/proper/commons-dbcp/configuration.html
	 * 
	 */
	@Bean(destroyMethod = "close")
	public DataSource ovDataSource() {
		
		DataSource dataSource=null;
		try {
			logger.info("*****************OV DATA SOURCE***************");
			Context initContext = new InitialContext();
			Context webContext = (Context)initContext.lookup("java:/comp/env");
			
			logger.info("*****************webContext OV DATASOURCE***************"+webContext);

			dataSource = (DataSource) webContext.lookup("jdbc/ovDBSource");
			logger.info("*****************dataSource***************"+dataSource);
		} catch (NamingException e) {
			e.printStackTrace();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("\n\n ==========================");
		builder.append("\n Constructed OV DB Connection Pool with: ");
		Connection connection = null;
		try {
			logger.info("*****************connecting to OV DB***************");
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("*****************DB SQL exception***************");
		} finally{ //SA - Fix
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		builder.append("\n CONNECTION SUCCESSFULL: " + connection);
	
		builder.append("\n ");
		builder.append("\n==========================\n\n");
		logger.warn(builder.toString());
		return dataSource;

	}
	
//	@Bean(destroyMethod = "close")
//	public BasicDataSource helloDataSource() {
//		BasicDataSource helloDataSource = new BasicDataSource();
//		helloDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
//		helloDataSource.setUrl("jdbc:oracle:thin:@dbs-nprd2-vm-020.cisco.com:1540:N2S0020A");
//		helloDataSource.setUsername("ANF_ADMIN");
//		helloDataSource.setPassword("b7XZ6Na9");
//		helloDataSource.setMaxWait(500);
//		helloDataSource.setMinEvictableIdleTimeMillis(30000);
//		helloDataSource.setTimeBetweenEvictionRunsMillis(30000);
//		helloDataSource.setMaxIdle( 30 );
//		helloDataSource.setMaxActive( 40 );
//
//		StringBuilder builder = new StringBuilder();
//		builder.append("\n\n ==========================");
//		builder.append("\n Constructed DB Connection Pool with: ");
//		builder.append("\n Url: " + helloDataSource.getUrl());
//		builder.append("\n getUsername: " + helloDataSource.getUsername());
//		builder.append("\n getMaxWait: " + helloDataSource.getMaxWait());
//		builder.append("\n getMaxIdle: " + helloDataSource.getMaxIdle());
//		builder.append("\n getMinEvictableIdleTimeMillis: " + helloDataSource.getMinEvictableIdleTimeMillis());
//		builder.append("\n getTimeBetweenEvictionRunsMillis: " + helloDataSource.getTimeBetweenEvictionRunsMillis());
//		builder.append("\n getMaxActive: " + helloDataSource.getMaxActive());
//		builder.append("\n getInitialSize: " + helloDataSource.getInitialSize());
//		builder.append("\n ");
//		builder.append("\n==========================\n\n");
//
//		logger.warn(builder.toString());
//		return helloDataSource;
//
//	}

	/**
	 * This creates the entity manager factory. 
	 * 
	 * @see <a
	 *      href="http://static.springsource.org/spring/docs/3.2.0.RELEASE/spring-framework-reference/htmlsingle/#orm-jpa">
	 *      Spring Jpa docs </a>
	 * 
	 * 
	 * 
	 * @return
	 */
	@Bean(name="oneViewEM")
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean emf1 = new LocalContainerEntityManagerFactoryBean();
		
		// entityManagerFactory.setPersistenceUnitName("hibernatePU");
		emf1.setDataSource(ovDataSource());
		// entityManagerFactory.setPersistenceXmlLocation("classpath:persistence.xml");
		emf1.setPersistenceUnitName("hibernateOVPU");
		
		// standard jpa jar scanning works against files containing persistence.xml. But for Junits, lets add 
		// an explicit scan
		emf1.setPackagesToScan("com.cisco.convergence");
		
//		 persistence.xml will override the above - so leave commented out.
//		 emf.setPersistenceXmlLocation("classpath:persistence.xml");
		
		// entityManagerFactory.setPersistenceProvider(new
		// HibernatePersistence());
		emf1.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		emf1.setJpaProperties(getJpaProperties());
		return emf1;
	}
	
	

	@Value("${hibernate.dialect}")
	private String hibernateDialect;
	@Value("${hibernate.metaDefaults}")
	private String hibernateMetaDefaults;
	@Value("${hibernate.show_sql}")
	private String hibernateShow_sql;
	@Value("${hibernate.hbm2ddl.auto}")
	private String hibernateDdl;

	private Properties getJpaProperties() {
		Properties jpaProperties1 = new Properties();

		jpaProperties1.put("hibernate.dialect", hibernateDialect);
		jpaProperties1.put("hibernate.temp.use_jdbc_metadata_defaults",
				hibernateMetaDefaults);
		jpaProperties1.put("hibernate.show_sql", hibernateShow_sql);
		jpaProperties1.put("hibernate.hbm2ddl.auto", hibernateDdl);
		jpaProperties1.put("hibernate.ejb.persistenceUnitName", "hibernateOVPU");

		StringBuilder builder = new StringBuilder();
		builder.append("\n\n ==========================");
		builder.append("\n Constructed JPA Properties with: ");
		builder.append("\n " + jpaProperties1.toString());
		builder.append("\n ");
		builder.append("\n==========================\n\n");

		logger.warn(builder.toString());

		return jpaProperties1;
	}

	/**
	 * EnableTransactionManagement automatically looks for any
	 * PlatformTransactionManager in the @Configuration class
	 * 
	 * @see <a href=
	 *      "http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/transaction/annotation/EnableTransactionManagement.html"
	 *      />
	 * @return
	 */
	@Bean(name = "OVTransactionManager")
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager jpaTransactionManager1 = new JpaTransactionManager();
		LocalContainerEntityManagerFactoryBean myemf1 = entityManagerFactoryBean();
		jpaTransactionManager1.setEntityManagerFactory(myemf1.getObject());
		return jpaTransactionManager1;
	}

}
