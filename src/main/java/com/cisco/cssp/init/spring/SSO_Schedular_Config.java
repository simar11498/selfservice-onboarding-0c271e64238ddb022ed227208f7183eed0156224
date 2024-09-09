package com.cisco.cssp.init.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SSO_Schedular_Config {

	
	@Bean
	public SSO_Schedulars bean() {
		return new SSO_Schedulars();
	}

    
	

}
