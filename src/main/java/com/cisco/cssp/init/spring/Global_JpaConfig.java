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
import org.springframework.context.annotation.Primary;
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
public class Global_JpaConfig {

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
	public DataSource imsapplDataSource() {
		
		DataSource dataSource=null;
		Connection connection = null;
		try {
				System.out.println("*****************Hello IMS_APPL SOURCE***************");
				Context initContext = new InitialContext();
				Context webContext = (Context)initContext.lookup("java:/comp/env");
				
				System.out.println("*****************webContext***************"+webContext);
	
				dataSource = (DataSource) webContext.lookup("jdbc/obsDBSource");
				System.out.println("*****************dataSource***************"+dataSource);			
	
				StringBuilder builder = new StringBuilder();
				builder.append("\n\n ==========================");
				builder.append("\n Constructed DB Connection Pool with: ");				
			
				System.out.println("*****************connecting to DB***************");
				if(dataSource != null){
					connection = dataSource.getConnection();
				}
				
				builder.append("\n CONNECTION SUCCESSFULL: " + connection);			
				builder.append("\n ");
				builder.append("\n==========================\n\n");
				logger.warn(builder.toString());		
				System.out.println("*****************connected to DB***************");	
				
			} catch (NamingException ne) {
				ne.printStackTrace();
			}
			catch (SQLException e) {
				e.printStackTrace();
				System.out.println("*****************DB connection Exception***************");
			}finally{
				try {
					if(connection != null){
						connection.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
	@Primary
	@Bean(name="imsApplEM")
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		
		emf.setDataSource(imsapplDataSource());
		emf.setPersistenceUnitName("hibernatePU");
		// standard jpa jar scanning works against files containing persistence.xml. But for Junits, lets add 
		// an explicit scan
		emf.setPackagesToScan("com.cisco.convergence");
		emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		emf.setJpaProperties(getJpaProperties());
		return emf;
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
		Properties jpaProperties = new Properties();

		jpaProperties.put("hibernate.dialect", hibernateDialect);
		jpaProperties.put("hibernate.temp.use_jdbc_metadata_defaults",
				hibernateMetaDefaults);
		jpaProperties.put("hibernate.show_sql", hibernateShow_sql);
		jpaProperties.put("hibernate.hbm2ddl.auto", hibernateDdl);
		jpaProperties.put("hibernate.ejb.persistenceUnitName", "hibernatePU");

		StringBuilder builder = new StringBuilder();
		builder.append("\n\n ==========================");
		builder.append("\n Constructed JPA Properties with: ");
		builder.append("\n " + jpaProperties.toString());
		builder.append("\n ");
		builder.append("\n==========================\n\n");

		logger.warn(builder.toString());

		return jpaProperties;
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
	@Bean(name = "imsApplTransactionManager")
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		LocalContainerEntityManagerFactoryBean myemf = entityManagerFactoryBean();
		jpaTransactionManager.setEntityManagerFactory(myemf.getObject());
		return jpaTransactionManager;
	}

}
