package java.com.cisco.convergence.obs.test;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.cisco.ca.csp.ef.update.exception.EFUpdateException;
import com.cisco.convergence.obs.exception.OnBoardingException;
import com.cisco.convergence.obs.exception.OnboardingSelfServiceException;
import com.cisco.convergence.obs.jpa.model.ContractInfo;
import com.cisco.convergence.obs.model.Company;
import com.cisco.convergence.obs.rest.EntitlementService;
import com.cisco.convergence.obs.service.DAOnboardingService;
import com.cisco.cssp.init.spring.Global_Context;
import com.cisco.cssp.init.spring.Global_JpaConfig;
import com.cisco.cssp.init.spring.Global_JpaConfig_OV;
import com.google.gson.Gson;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath*:c.xml" })
@ContextConfiguration(classes={Global_Context.class, Global_JpaConfig.class, Global_JpaConfig_OV.class}, loader=AnnotationConfigContextLoader.class)
public class DAOnboardingServiceTest {
	
	DAOnboardingService test = Mockito.mock(DAOnboardingService.class);
	
	@Autowired
	DAOnboardingService onboardService;
	
	@Autowired
	EntitlementService entitlementService;
	
	@Mock
	HttpServletRequest request;
	
	
	@BeforeClass
    public static void setUpClass() throws Exception {
       
        try {
            // Create initial context
        	System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");
            ic.createSubcontext("java:/comp/env/jdbc/obsDBSource");
            ic.createSubcontext("java:/comp/env/jdbc/ovDBSource");
            // Construct DataSource
            
           OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
           ds.setURL("jdbc:oracle:thin:@dbs-nprd2-vm-020.cisco.com:1540:N2S0020A");
           ds.setUser("ANF_ADMIN");
           ds.setPassword("b7XZ6Na9");
            
           ic.rebind("java:/comp/env/jdbc/obsDBSource", ds);
           
           OracleConnectionPoolDataSource ds1 = new OracleConnectionPoolDataSource();
           ds1.setURL("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=ON)(FAILOVER=ON)(ADDRESS=(PROTOCOL=TCP)(HOST=64.100.61.205)(PORT=1541))(ADDRESS=(PROTOCOL=TCP)(HOST=64.100.61.206)(PORT=1541)))(CONNECT_DATA=(SERVICE_NAME=CAFI2STG.cisco.com)(SERVER=DEDICATED)))");
           ds1.setUser("NRT_ADMIN");
           ds1.setPassword("NRTADTXQ7277");
            
           ic.rebind("java:/comp/env/jdbc/ovDBSource", ds1);
           
           
        } catch (NamingException ex) {
           ex.printStackTrace();
        }
        
    }
	
	@Test
	public final void testOnboardUser() {
		try {
			
			onboardService.onboardUser(request,"");
		} catch (EFUpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OnboardingSelfServiceException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println(e.getErrorCode());
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testCompareGUID(){
		
		
		boolean  isDirectCustomer = onboardService.compareUserGUIDtoPartyGUID(request,"lhalladay", "", "142856648");
		System.out.println("isDirectCustomer::"+isDirectCustomer);
	}
	
	
	@Test
	public void testGetEligibleCompanies(){
		List<Company> companies = onboardService.getEligibleCompanies(request,"4911391", "satcomdirectinc");
		
		System.out.println("companies::"+companies);
		
		if(companies != null){
			System.out.println("No of companies in hierarchy:: "+companies.size());
		}

		String json = new Gson().toJson(companies);
		System.out.println(json);
		
		
	}
	
	@Test
	public void testUpdateAAACache(){
		onboardService.updateAAACache("42590351");
	}
	
	
	@Test
	public void testGetSiteUseIdForContract() throws OnBoardingException{
		//HttpServletRequest httpServletrequest = null;
		ContractInfo cs = entitlementService.getCrPartyBySiteUseId(request, "94133248", "FOC1816R0T8");
		
		String json = new Gson().toJson(cs);
		System.out.println(json);
	}
	
	@Test
	public void testGetUserProfile(){
		
		try {
			request.setAttribute("REMOTE_USER", "test");
			entitlementService.getUserProfileData(request);
		} catch (OnBoardingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
