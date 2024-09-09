#!/bin/bash
logFile="${WORKSPACE}/package/repo/.openshift/config/logging.properties"
serverFile="${WORKSPACE}/package/repo/.openshift/config/server.xml"
contextFile="${WORKSPACE}/package/repo/.openshift/config/context.xml"
standaloneFile="${WORKSPACE}/package/repo/.openshift/config/standalone.xml"
pomFile="${WORKSPACE}/pom.xml"
assemblyFile="${WORKSPACE}/assembly.xml"
webFile="${WORKSPACE}/package/repo/.openshift/config/web.xml"
log4jJarFile="${WORKSPACE}/src/main/webapp/WEB-INF/lib/slf4j-log4j12-1.7.16.jar"
ojdbcJarFile="${WORKSPACE}/src/main/webapp/WEB-INF/lib/ojdbc6.jar"
jbossWebXMLFile="${WORKSPACE}/src/main/webapp/jboss-web.xml"
applicationWebXMLFile="${WORKSPACE}/src/main/webapp/web.xml"
#jbosseapDir="${WORKSPACE}/package/dependencies/jbosseap"
#jbossewsDir="${WORKSPACE}/package/dependencies/jbossews"
packageDir="${WORKSPACE}/package"

preStartFile=""
preRestartFile=""

printTitleFlag=1
printTitleFooterFlag=0
printHeaderFlag=1
printFooterFlag=0

simpleEcho () {
	#using blue color for the output
	#echo -e "\e[1;34m$1\e[0m"
	echo "$1"
}

printOutput () {
	if [[ $printTitleFlag == 1 ]]
	then
		simpleEcho "------------------------------------------------------------------------"
		simpleEcho "RECOMMENDED CONFIGURATIONS"
		simpleEcho "------------------------------------------------------------------------"
		
		printTitleFlag=0
		printTitleFooterFlag=1
	fi
	
	if [[ $printHeaderFlag == 1 ]]
	then
		simpleEcho "in $1 file:"
		printHeaderFlag=0
		printFooterFlag=1
	fi
	
	simpleEcho "	$2"
}

validateTagAndValue()
{
       if [[ $1 != $2 ]]
       then
			printOutput $6 "$4 tag is missing"
       else
              if [[ $3 != $1 ]]
              then
					printOutput $6 "Correct value for $4 should be $5"
              fi                   
       fi
}

CheckLoggingPropertiesFile () {
	printHeaderFlag=1
	printFooterFlag=0
	temp=0
	
	#Verify SimpleFormatter.format in logging.properties
	if [[ -e $logFile ]]
	then
    	simpleFormatter=$(grep "java.util.logging.SimpleFormatter.format\=" $logFile | wc -l)
    	simpleFormatterValue=$(grep "%1\$tY-%1\$tm-%1\$td\ %1\$tH:%1\$tM:%1\$tS\ %4\$-6s\ %2\$s\ %5\$s%6\$s%n" $logFile | wc -l)
    	if [[ $simpleFormatter == $temp ]]
    	then
    		printOutput "LOGGING.PROPERTIES" "Add java.util.logging.SimpleFormatter.format=%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS %4\$-6s %2\$s %5\$s%6\$s%n"
    	else
            if [[ $simpleFormatterValue == $temp ]]
            then
            	printOutput "LOGGING.PROPERTIES" "Default value for java.util.logging.SimpleFormatter.format=%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS %4\$-6s %2\$s %5\$s%6\$s%n"
            fi
    	fi
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
		
}

CheckServerFile () {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="SERVER.XML"
	temp=0
	
	address=$(grep "<Connector address=\"\${OPENSHIFT_JBOSSEWS_IP}\"" $serverFile  | wc -l)
    port=$(grep "port=\"\${OPENSHIFT_JBOSSEWS_HTTP_PORT}" $serverFile  | wc -l)
    protocol=$(grep "protocol=\"HTTP/1.1\"" $serverFile  | wc -l)
    connectionTimeout=$(grep "connectionTimeout=\"20000\"" $serverFile  | wc -l)
    maxThreads=$(grep "maxThreads=" $serverFile | wc -l)
    maxThreadsVal=$(grep "maxThreads=\"60\"" $serverFile | wc -l)
    redirectPort=$(grep "redirectPort=\"8443\"/>" $serverFile | wc -l)
       
    if [[ $address == $temp ]] 
    then
    	printOutput $currentFileName "Add following tag:"
    	printOutput $currentFileName "<Connector address=\"\${OPENSHIFT_JBOSSEWS_IP}\""
        printOutput $currentFileName "	port=\"\${OPENSHIFT_JBOSSEWS_HTTP_PORT}\""
        printOutput $currentFileName "	protocol=\"HTTP\/1.1\""
        printOutput $currentFileName "	connectionTimeout=\"20000\""
        printOutput $currentFileName "	maxThreads=\"60\""
        printOutput $currentFileName "	redirectPort=\"8443\"\/>"
        
	else
    	if [[ $port != $temp ]] && [[ $protocol != $temp ]] && [[ $connectionTimeout != $temp ]] && [[ $redirectPort != $temp ]]
    	then
        	if [[ $maxThreads == $temp ]]
        	then
                printOutput $currentFileName "Add maxThreads=\"60\" after connectionTimeout=\"20000\""                      
            else
            	if [[ $maxThreadsVal == $temp ]]
            	then
                	printOutput $currentFileName "Default value for maxThread = \"60\""
                fi
             fi
    	fi
    fi
	
	# Verify the value for unpackWARs
    unpackWar=$(grep -i unpackWARs=\"false\" $serverFile | wc -l)    
    if [[ $unpackWar == $temp ]]
   	then
    	printOutput $currentFileName "Default value for unpackWARs should be false"
    fi
    
	# Verify the value for autoDeploy    
    autoDeploy=$(grep -i autoDeploy=\"true\" $serverFile | wc -l)    
    if [[ $autoDeploy == $temp ]]
    then
    	printOutput $currentFileName "Default value for autoDeploy should be true"
    fi
    
	# Verify warName in pom.xml is same as server.xml
    if [[ -e $pomFile ]]
    then
    	warNameElement=$(grep -o '<warName>.*</warName>' $pomFile | wc -l)
        warName=$(grep -o '<warName>.*</warName>' $pomFile | sed 's/\(<warName>\|<\/warName>\)//g')
        war=$(grep -o 'webapps/.*\.war' $serverFile | sed 's/\(webapps\/\|\.war\)//g')    
        contextRoot=$(grep 'Context path="/"' $serverFile | wc -l)
        
        if [[ $contextRoot != $temp ]] && [[ "$war" == "ROOT" ]]
        then
        	printOutput $currentFileName "remove 'Context path' tag"
		else 
			if [[ $warNameElement != $temp ]] && [[ $warName != $war ]]
        	then
				printOutput $currentFileName "warName in pom.xml should match with the war in server.xml"
        	fi
    	fi
     fi

	# Check for context path
	contextPath=$(grep -i "<Context path=\"/" $serverFile| wc -l)
	if [[ $contextPath == $temp ]]
	then
		printOutput $currentFileName "Context path does not exist"
	else
		docBase=$(grep -i "docBase=${OPENSHIFT_DEPENDENCIES_DIR}" $serverFile)
	if [[ ${docBase} == $temp ]]
		then
			printOutput $currentFileName "docbase should start with ${OPENSHIFT_DEPENDENCIES_DIR}"
		fi			
	fi
	
	#check for <Valve className="com.redhat.valves.tomcat7.RemoteUserValve" />
	remoteUserValve=$(grep "<\!--.*<Valve className=\"com.redhat.valves.tomcat7.RemoteUserValve\".*/>" $serverFile | wc -l)
	if [[ $remoteUserValve != $temp ]]
	then
		printOutput $currentFileName "uncomment the '<Valve className=\"com.redhat.valves.tomcat7.RemoteUserValve\" />' tag"
	fi
	
	#dsx connection url
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

CheckContextFile () {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="CONTEXT.XML"
	temp=0
	
	resource=$(grep -i "Resource name=" $contextFile | wc -l )
	#Check for resource tag
	if [[ $resource == $temp ]]
	then
		printOutput $currentFileName "Resource tag does not exist"
	else
		#Collect all parameters
		maxActiveTag=$(grep -i "maxActive=" $contextFile | wc -l)
		maxActiveCorrectValue=$(grep -i "maxActive=\"60\"" $contextFile | wc -l)
		maxIdleTag=$(grep -i "maxIdle=" $contextFile | wc -l)
		maxIdleCorrectValue=$(grep -i "maxIdle=\"1\"" $contextFile | wc -l)
		initialSizeTag=$(grep -i "initialSize=" $contextFile | wc -l)
		initialSizeCorrectValue=$(grep -i "initialSize=\"2\"" $contextFile | wc -l)
		minIdleTag=$(grep -i "minIdle=" $contextFile | wc -l)
		minIdleCorrectValue=$(grep -i "minIdle=\"2\"" $contextFile | wc -l)
		jmxEnabledTag=$(grep -i "jmxEnabled=" $contextFile | wc -l)
		jmxEnabledCorrectValue=$(grep -i "jmxEnabled=\"true\"" $contextFile | wc -l)
		timeBetweenEvictionRunsMillisTag=$(grep -i "timeBetweenEvictionRunsMillis=" $contextFile | wc -l)
		timeBetweenEvictionRunsMillisCorrectValue=$(grep -i "timeBetweenEvictionRunsMillis=\"180000\"" $contextFile | wc -l)
		minEvictableIdleTimeMillisTag=$(grep -i "minEvictableIdleTimeMillis=" $contextFile | wc -l)
		minEvictableIdleTimeMillisCorrectValue=$(grep -i "minEvictableIdleTimeMillis=\"180000\"" $contextFile | wc -l)
		maxWaitTag=$(grep -i "maxWait=" $contextFile | wc -l)
		maxWaitCorrectValue=$(grep -i "maxWait=\"20000\"" $contextFile | wc -l)
		logAbandonedTag=$(grep -i "logAbandoned=" $contextFile | wc -l)
		logAbandonedCorrectValue=$(grep -i "logAbandoned=\"false\"" $contextFile | wc -l)
		testOnBorrowTag=$(grep -i "testOnBorrow=" $contextFile | wc -l)
		testOnBorrowCorrectValue=$(grep -i "testOnBorrow=\"true\"" $contextFile | wc -l)
		testOnConnectTag=$(grep -i "testOnConnect=" $contextFile | wc -l)
		testOnConnectCorrectValue=$(grep -i "testOnConnect=\"true\"" $contextFile | wc -l)
		suspectTimeoutTag=$(grep -i "suspectTimeout=" $contextFile | wc -l)
		suspectTimeoutCorrectValue=$(grep -i "suspectTimeout=\"120\"" $contextFile | wc -l)
		validationQueryTag=$(grep -i "validationQuery=" $contextFile | wc -l)
		validationQueryCorrectValue=$(grep -i "validationQuery=\"select 1 from dual\"" $contextFile | wc -l)
		validationIntervalTag=$(grep -i "validationInterval=" $contextFile | wc -l)
		validationIntervalCorrectValue=$(grep -i "validationInterval=\"200\"" $contextFile | wc -l)
		initSQLTag=$(grep -i "initSQL=" $contextFile | wc -l)
		initSQLCorrectValue=$(grep -i "initSQL=\"select 1 from dual\"" $contextFile | wc -l)
		removeAbandonedTag=$(grep -i "removeAbandoned=" $contextFile | wc -l)
		removeAbandonedCorrectValue=$(grep -i "removeAbandoned=\"true\"" $contextFile | wc -l)
		removeAbandonedTimeoutTag=$(grep -i "removeAbandonedTimeout=" $contextFile | wc -l)
		removeAbandonedTimeoutCorrectValue=$(grep -i "removeAbandonedTimeout=\"1800\"" $contextFile | wc -l)
		maxAgeTag=$(grep -i "maxAge=" $contextFile | wc -l)
		maxAgeCorrectValue=$(grep -i "maxAge=\"180000\"" $contextFile | wc -l)
		
		#Check all parameters
		validateTagAndValue $resource $maxActiveTag $maxActiveCorrectValue "maxActive" 60 $currentFileName
		validateTagAndValue $resource $maxIdleTag $maxIdleCorrectValue "maxIdle" 1 $currentFileName
		validateTagAndValue $resource $initialSizeTag $initialSizeCorrectValue "initialSize" 2 $currentFileName
		validateTagAndValue $resource $minIdleTag $minIdleCorrectValue "minIdle" 2 $currentFileName
		validateTagAndValue $resource $jmxEnabledTag $jmxEnabledCorrectValue "jmxEnabled" true $currentFileName
		validateTagAndValue $resource $timeBetweenEvictionRunsMillisTag $timeBetweenEvictionRunsMillisCorrectValue "timeBetweenEvictionRunsMillis" 180000 $currentFileName
		validateTagAndValue $resource $minEvictableIdleTimeMillisTag $minEvictableIdleTimeMillisCorrectValue "minEvictableIdleTimeMillis" 180000 $currentFileName
		validateTagAndValue $resource $maxWaitTag $maxWaitCorrectValue "maxWait" 20000 $currentFileName
		validateTagAndValue $resource $logAbandonedTag $logAbandonedCorrectValue "logAbandoned" false $currentFileName
		validateTagAndValue $resource $testOnBorrowTag $testOnBorrowCorrectValue "testOnBorrow" true $currentFileName
		validateTagAndValue $resource $testOnConnectTag $testOnConnectCorrectValue "testOnConnect" true $currentFileName
		validateTagAndValue $resource $suspectTimeoutTag $suspectTimeoutCorrectValue "suspectTimeout" 120 $currentFileName
		validateTagAndValue $resource $validationQueryTag $validationQueryCorrectValue "validationQuery" "select 1 from dual" $currentFileName
		validateTagAndValue $resource $validationIntervalTag $validationIntervalCorrectValue "validationInterval" 200 $currentFileName
		validateTagAndValue $resource $initSQLTag $initSQLCorrectValue "initSQL" "select 1 from dual" $currentFileName
		validateTagAndValue $resource $removeAbandonedTag $removeAbandonedCorrectValue "removeAbandoned" true $currentFileName
		validateTagAndValue $resource $removeAbandonedTimeoutTag $removeAbandonedTimeoutCorrectValue "removeAbandonedTimeout" 1800 $currentFileName
		validateTagAndValue $resource $maxAgeTag $maxAgeCorrectValue "maxAge" 180000 $currentFileName
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

CheckStandaloneFile () {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="STANDALONE.XML"
	temp=0
	
	#Check deployment-scanner
	scanIntervalTag=$(grep -i "scan-interval=" $standaloneFile | wc -l)
	scanIntervalCorrectValue=$(grep -i "scan-interval=\"0\"" $standaloneFile | wc -l)
	scanEnabledTag=$(grep -i "scan-enabled=" $standaloneFile | wc -l)
	scanEnabledCorrectValue=$(grep -i "scan-enabled=\"false\"" $standaloneFile | wc -l)
	
	validateTagAndValue 1 $scanIntervalTag $scanIntervalCorrectValue "scan-interval" 0 $currentFileName
	validateTagAndValue 1 $scanEnabledTag $scanEnabledCorrectValue "scan-enabled" false $currentFileName
	
	subsystemThreads=$(grep -i "urn:jboss:domain:threads:1.0" $standaloneFile | wc -l)
	if [[ $subsystemThreads == $temp ]]
	then
		printOutput $currentFileName "Tag for \"urn:jboss:domain:threads:1.0\" is missing. Add Following tag:" #Add before tag in echo
		printOutput $currentFileName "<subsystem xmlns=\"urn:jboss:domain:threads:1.0\">"
		printOutput $currentFileName "	<bounded-queue-thread-pool name=\"http-executor\" blocking=\"true\">"
		printOutput $currentFileName "	<core-threads count=\"60\"  />"
		printOutput $currentFileName "	<queue-length count=\"60\"  />"
		printOutput $currentFileName "	<max-threads count=\"60\"  />"
		printOutput $currentFileName "	<keepalive-time time=\"10\" unit=\"seconds\" />"
		printOutput $currentFileName "	</bounded-queue-thread-pool>"
		printOutput $currentFileName "</subsystem>"
	else
		coreThreadsCountTag=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "core-threads count" | wc -l)
		coreThreadsCountCorrectValue=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "core-threads count=\"60\"" | wc -l)
		queueLengthCountTag=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "queue-length count" | wc -l)
		queueLengthCountCorrectValue=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "queue-length count=\"60\"" | wc -l)
		maxThreadsCountTag=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "max-threads count" | wc -l)
		maxThreadsCountCorrectValue=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "max-threads count=\"60\"" | wc -l)
		keepaliveTimeTag=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "<keepalive-time time" | wc -l)
		keepaliveTimeCorrectValue=$(awk '/<subsystem xmlns="urn:jboss:domain:threads:1.0">/,/<\/subsystem>/' $standaloneFile | grep "<keepalive-time time=\"10\" unit=\"seconds\"" | wc -l)
		
		validateTagAndValue 1 $coreThreadsCountTag $coreThreadsCountCorrectValue "core-threads count" 60 $currentFileName
		validateTagAndValue 1 $queueLengthCountTag $queueLengthCountCorrectValue "queue-length count" 60 $currentFileName
		validateTagAndValue 1 $maxThreadsCountTag $maxThreadsCountCorrectValue "max-threads count" 60 $currentFileName
		validateTagAndValue 1 $keepaliveTimeTag $keepaliveTimeCorrectValue "keepalive-time time" 10 $currentFileName
	fi
	
	subsystemWeb=$(grep -i "urn:jboss:domain:web:1.1" $standaloneFile | wc -l)
	if [[ $subsystemWeb == $temp ]]
	then
		printOutput $currentFileName "Tag for \"urn:jboss:domain:web:1.1\" is missing. Add Following tag:"  #Add before tag in echo
		printOutput $currentFileName "<subsystem xmlns=\"urn:jboss:domain:web:1.1\" default-virtual-server=\"default-host\" native=\"false\">"
		printOutput $currentFileName "	<connector name=\"http\""
		printOutput $currentFileName "		protocol=\"HTTP/1.1\""
		printOutput $currentFileName "		scheme=\"http\""
		printOutput $currentFileName "		socket-binding=\"http\""
		printOutput $currentFileName "		executor=\"http-executor\"/>"
		printOutput $currentFileName "</subsystem>"
	fi
	
	#check <datasource> tags
	dataSourceTagCount=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile |  grep "</datasource>" | wc -l)
	if [[ $dataSourceTagCount != $temp ]]
	then	
		#collect values under <pool> tag 	
		minPoolSizeTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<min-pool-size>" | wc -l)
		minPoolSizeCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<min-pool-size>2</min-pool-size>" | wc -l)
		maxPoolSizeTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<max-pool-size>" | wc -l)
		maxPoolSizeCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<max-pool-size>20</max-pool-size>" | wc -l)
		prefillTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<prefill>" | wc -l)
		prefillCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<prefill>true</prefill>" | wc -l)
		useStrictMinTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<use-strict-min>" | wc -l)
		useStrictMinCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<use-strict-min>false</use-strict-min>" | wc -l)

		#validating values under <pool> tag
		validateTagAndValue $dataSourceTagCount $minPoolSizeTag $minPoolSizeCorrectValue "min-pool-size" 2 $currentFileName
		validateTagAndValue $dataSourceTagCount $maxPoolSizeTag $maxPoolSizeCorrectValue "max-pool-size" 20 $currentFileName
		validateTagAndValue $dataSourceTagCount $prefillTag $prefillCorrectValue "prefill" true $currentFileName
		validateTagAndValue $dataSourceTagCount $useStrictMinTag $useStrictMinCorrectValue "use-strict-min" false $currentFileName
		
		#collect values under <validation> tag
		enabledTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "enabled" | wc -l)
		enabledCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "enabled=\"true\"" | wc -l)
		validConnectionCheckerTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile  | grep "<valid-connection-checker" | wc -l)
		validConnectionCheckerCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | awk '/<valid-connection-checker/,/\/valid-connection-checker>/' | grep "class-name=\"org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker\"" | wc -l)
		staleConnectionCheckerTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile  | grep "<stale-connection-checker" | wc -l)
		staleConnectionCheckerCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | awk '/<stale-connection-checker/,/\/stale-connection-checker>/' | grep "class-name=\"org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker\"" | wc -l)
		checkValidConnectionSqlTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<validate-on-match>" | wc -l)
		checkValidConnectionSqlCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<check-valid-connection-sql>select 1 from dual</check-valid-connection-sql>" | wc -l)
		validateOnMatchTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<validate-on-match>" | wc -l)
		validateOnMatchCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<background-validation>true</background-validation>" | wc -l)
		backgroundValidationTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<background-validation>" | wc -l)
		backgroundValidationCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<background-validation>true</background-validation>" | wc -l)
		backgroundValidationMillisTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<background-validation-millis>" | wc -l)
		backgroundValidationMillisCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<background-validation-millis>180000</background-validation-millis>" | wc -l)
		
		#validating values under <validation> tag
		validateTagAndValue $dataSourceTagCount $enabledTag $enabledCorrectValue "enabled" "true" $currentFileName
		validateTagAndValue $dataSourceTagCount $validConnectionCheckerTag $validConnectionCheckerCorrectValue "valid-connection-checker" "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker" $currentFileName
		validateTagAndValue $dataSourceTagCount $staleConnectionCheckerTag $staleConnectionCheckerCorrectValue "stale-connection-checker" "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker" $currentFileName
		validateTagAndValue $dataSourceTagCount $checkValidConnectionSqlTag $checkValidConnectionSqlCorrectValue "check-valid-connection-sql" "select 1 from dual" $currentFileName
		validateTagAndValue $dataSourceTagCount $validateOnMatchTag $validateOnMatchCorrectValue "validate-on-match" false $currentFileName
		validateTagAndValue $dataSourceTagCount $backgroundValidationTag $backgroundValidationCorrectValue "background-validation" true $currentFileName
		validateTagAndValue $dataSourceTagCount $backgroundValidationMillisTag $backgroundValidationMillisCorrectValue "background-validation-millis" 180000 $currentFileName
		
		#collect values under <timeout> tag	
		blockingTimeoutMillisTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<blocking-timeout-millis>" | wc -l)
		blockingTimeoutMillisCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<blocking-timeout-millis>30000</blocking-timeout-millis>" | wc -l)
		idleTimeoutMinutesTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<blocking-timeout-millis>" | wc -l)
		idleTimeoutMinutesCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<idle-timeout-minutes>5</idle-timeout-minutes>" | wc -l)
		queryTimeoutTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<query-timeout>" | wc -l)
		#<!- This value is in seconds. Please make sure to mention more than max running query time from application. For ex Query runs for 180 seconds, mention as 200 ->
		queryTimeoutCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<query-timeout>0</query-timeout>" | wc -l)
		
		#validating values under <timeout> tag
		validateTagAndValue $dataSourceTagCount $blockingTimeoutMillisTag $blockingTimeoutMillisCorrectValue "blocking-timeout-millis" 30000 $currentFileName
		validateTagAndValue $dataSourceTagCount $idleTimeoutMinutesTag $idleTimeoutMinutesCorrectValue "idle-timeout-minutes" 5 $currentFileName
		validateTagAndValue $dataSourceTagCount $queryTimeoutTag $queryTimeoutCorrectValue "query-timeout" 0 $currentFileName
		 
		#collect values under <statement> tag
		trackStatementsTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<track-statements>" | wc -l)
		trackStatementsCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<track-statements>false</track-statements>" | wc -l)
		preparedStatementCacheSizeTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<prepared-statement-cache-size>" | wc -l)
		preparedStatementCacheSizeCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<prepared-statement-cache-size>0</prepared-statement-cache-size>" | wc -l)
		sharePreparedStatementsTag=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<share-prepared-statements>" | wc -l)
		sharePreparedStatementsCorrectValue=$(awk '/<datasources>/,/<\/datasources>/' $standaloneFile | grep "<share-prepared-statements>false</share-prepared-statements>" | wc -l)
		
		#validating values under <statement> tag
		validateTagAndValue $dataSourceTagCount $trackStatementsTag $trackStatementsCorrectValue "track-statements" false $currentFileName
		validateTagAndValue $dataSourceTagCount $preparedStatementCacheSizeTag $preparedStatementCacheSizeCorrectValue "prepared-statement-cache-size" 0 $currentFileName
		validateTagAndValue $dataSourceTagCount $sharePreparedStatementsTag $sharePreparedStatementsCorrectValue "share-prepared-statements" false $currentFileName
	fi
	
	connectionDefinitionTagCount=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile| grep -o "</connection-definition>" | wc -l)
	if [[ $connectionDefinitionTagCount != $temp ]]
	then
		#collect values under <pool> tag
		minPoolSizeTagJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<min-pool-size>" | wc -l)
		minPoolSizeCorrectValueJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<min-pool-size>2</min-pool-size>" | wc -l)
		maxPoolSizeTagJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<max-pool-size>" | wc -l)
		maxPoolSizeCorrectValueJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<max-pool-size>20</max-pool-size>" | wc -l)
		prefillTagJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<prefill>" | wc -l)
		prefillCorrectValueJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<prefill>true</prefill>" | wc -l)
		useStrictMinTagJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<use-strict-min>" | wc -l)
		useStrictMinCorrectValueJMS=$(awk '/<connection-definition/,/\/connection-definition>/' $standaloneFile | grep "<use-strict-min>false</use-strict-min>" | wc -l)

		#validating values under <pool> tag
		validateTagAndValue $connectionDefinitionTagCount $minPoolSizeTagJMS $minPoolSizeCorrectValueJMS "min-pool-size" 2 $currentFileName
		validateTagAndValue $connectionDefinitionTagCount $maxPoolSizeTagJMS $maxPoolSizeCorrectValueJMS "max-pool-size" 20 $currentFileName
		validateTagAndValue $connectionDefinitionTagCount $prefillTagJMS $prefillCorrectValueJMS "prefill" true $currentFileName
		validateTagAndValue $connectionDefinitionTagCount $useStrictMinTagJMS $useStrictMinCorrectValueJMS "use-strict-min" false $currentFileName
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

checkApplicationWebFile() {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="src/main/webapp/WEB.XML"
	temp=0
	
	userDataTag=$(awk '/<user-data-constraint/,/\/user-data-constraint>/' $applicationWebXMLFile | grep "<transport-guarantee>" | wc -l)
	
	if [[ $userDataTag != $temp ]]
	then
		printOutput $currentFileName "Remove user-data-constraint and transport-guarantee tag"
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

checkWebFile() {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="WEB.XML"
	temp=0
	
	#JMS
	#resource-ref
	resRefName=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "res-ref-name" | wc -l)
	resRefNameCorrect=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "<res-ref-name>jms/<CONNECTION_FACTORY_NAME>/res-ref-name>" | wc -l)
	resType=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "res-type" | wc -l)
	resTypeCorrect=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "<res-type>javax.jms.ConnectionFactory</res-type>" | wc -l)
	resAuth=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "res-auth" | wc -l)
	resAuthCorrect=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "<res-auth>Application</res-auth>" | wc -l)
	resSharing=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "res-sharing-scope" | wc -l)
	resSharingCorrect=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "<res-sharing-scope>Shareable</res-sharing-scope>" | wc -l)
	lookupName=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "lookup-name" | wc -l)
	lookupNameCorrect=$(awk '/<resource-ref id=/,/<\/resource-ref>/' $webFile | grep "<lookup-name>java:/jms/<CONNECTION_FACTORY_NAME></lookup-name>" | wc -l)
	
	validateTagAndValue 1 $resRefName $resRefNameCorrect "res-ref-name" "jms/<CONNECTION_FACTORY_NAME>" $currentFileName
	validateTagAndValue 1 $resType $resTypeCorrect "res-type" "javax.jms.ConnectionFactory" $currentFileName
	validateTagAndValue 1 $resAuth $resAuthCorrect "res-auth" "Application" $currentFileName
	validateTagAndValue 1 $resSharing $resSharingCorrect "res-sharing-scope" "Shareable" $currentFileName
	validateTagAndValue 1 $lookupName $lookupNameCorrect "lookup-name" "java:/jms/<CONNECTION_FACTORY_NAME>" $currentFileName
	
	#message destination
	msgDestName=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "message-destination-ref-name" | wc -l)
	msgDestNameCorrect=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "<message-destination-ref-name>jms/<Queue></message-destination-ref-name>" | wc -l)
	msgDestType=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "message-destination-type" | wc -l)
	msgDestTypeCorrect=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "<message-destination-type>javax.jms.Queue</message-destination-type>" | wc -l)
	msgDestUsage=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "message-destination-usage" | wc -l)
	msgDestUsageCorrect=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "<message-destination-usage>ConsumesProduces</message-destination-usage>" | wc -l)
	msgDestLink=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "message-destination-link" | wc -l)
	msgDestLinkCorrect=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "<message-destination-link><Queue></message-destination-link>" | wc -l)
	msgDestLookupName=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "lookup-name" | wc -l)
	msgDestLookupNameCorrect=$(awk '/<message-destination-ref id=/,/<\/message-destination-ref>/' $webFile | grep "<lookup-name>java:/jms/<Queue></lookup-name>" | wc -l)
	
	validateTagAndValue 1 $msgDestName $msgDestNameCorrect "message-destination-ref-name" "jms/<Queue>" $currentFileName
	validateTagAndValue 1 $msgDestType $msgDestTypeCorrect "message-destination-type" "javax.jms.Queue" $currentFileName
	validateTagAndValue 1 $msgDestUsage $msgDestUsageCorrect "message-destination-usage" "ConsumesProduces" $currentFileName
	validateTagAndValue 1 $msgDestLink $msgDestLinkCorrect "message-destination-link>" "<Queue>" $currentFileName
	validateTagAndValue 1 $msgDestLookupName $msgDestLookupNameCorrect "lookup-name" "java:/jms/<Queue>" $currentFileName
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

checkPomFileForOutputDirectory() {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="POM.XML"
	temp=0
	
	outputDirTag=$(awk '/<outputDirectory>/,/<\/outputDirectory>/' $pomFile |wc -l)
	projectBuildDir=$(awk '/<outputDirectory>/,/<\/outputDirectory>/' $pomFile | grep "\${project.build.directory}" | wc -l)
	projectBaseDir=$(awk '/<outputDirectory>/,/<\/outputDirectory>/' $pomFile | grep "\${project.basedir}" | wc -l)
	
	if [[ $outputDirTag == $temp ]]
	then
		printOutput $currentFileName "outputDirectory tag is missing"
	else
		if [[  $projectBuildDir != $temp ]]
		then
			projectBuildDirCorrectValue=$(awk '/<outputDirectory>/,/<\/outputDirectory>/' $pomFile | grep $1 | wc -l)
			if [[ $projectBuildDirCorrectValue == $temp ]]
			then
				printOutput $currentFileName "Incorrect path for <outputdirectory>. Correct path is \"$1\""
			fi
		fi
		if [[  $projectBaseDir != $temp ]]
		then
			projectBaseDirCorrectValue=$(awk '/<outputDirectory>/,/<\/outputDirectory>/' $pomFile | grep $2 | wc -l)
			if [[ $projectBaseDirCorrectValue == $temp ]]
			then
				printOutput $currentFileName "Incorrect path for <outputdirectory>. Correct path is \"$2\""
			fi
		fi
	fi
	
	#log4j dependency check
	log4jArtifactId=$(awk '/org.slf4j/,/\/version>/' $pomFile | grep "<artifactId>slf4j-log4j12</artifactId>" | wc -l)
	log4jVersion=$(awk '/org.slf4j/,/\/version>/' $pomFile | grep "<version>1.7.16</version>" | wc -l)
	log4jScopeProvided=$(awk '/org.slf4j/,/\/dependency>/' $pomFile | grep "<scope>provided</scope>" | wc -l)
	
	if [[ $log4jArtifactId == $temp ]]
	then
		printOutput $currentFileName "Correct artifactID is slf4j-log4j12"
	fi
	
	if [[ $log4jVersion == $temp ]]
	then
		printOutput $currentFileName "Correct version for slf4j-log4j12 is 1.7.16"
	fi
	
	if [[ $log4jArtifactId != $temp ]] && [[ $log4jVersion != $temp ]]
	then
		if [[ $log4jScopeProvided != $temp ]]
		then
			if [[ ! -e $log4jJarFile ]]
			then
				printOutput $currentFileName "slf4j-log4j12-1.7.16.jar is missing from /src/main/webapp/WEB-INF/lib"
			fi
		else
			if [[ -e $log4jJarFile ]]
			then
				printOutput $currentFileName "remove slf4j-log4j12-1.7.16.jar from /src/main/webapp/WEB-INF/lib"
			fi	
		fi
	fi	
	
	#assembly plugin check
	assemblyPlugin=$(grep -i "maven-assembly-plugin" $pomFile | wc -l)
	
	if [[ $assemblyPlugin == $temp ]]
	then
		printOutput $currentFileName "maven-assembly-plugin is missing"
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

CheckAssemblyFile () {
	printHeaderFlag=1
	printFooterFlag=0
	
	directoryTag=$(awk '/<fileSet>/,/<\/fileSet>/' $assemblyFile | grep "<\/directory>" | wc -l)
	directoryCorrectValue=$(awk '/<fileSet>/,/<\/fileSet>/' assembly.xml | grep "<directory>\${project.build.directory}/../package</directory>" | wc -l)
	outputDir=$(awk '/<fileSet>/,/<\/fileSet>/' assembly.xml | grep "</outputDirectory>" | wc -l)
	outputDirCorrectValue=$(awk '/<fileSet>/,/<\/fileSet>/' assembly.xml | grep "<outputDirectory>\/<\/outputDirectory>" | wc -l)
	
	validateTagAndValue 1 $directoryTag $directoryCorrectValue "directory" "\"\${project.build.directory}/../package\"" "ASSEMBLY.xml"
	validateTagAndValue 1 $outputDir $outputDirCorrectValue "outputDirectory" "/" "ASSEMBLY.xml"
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

CheckPreScripts () {
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName=$1
	temp=0
	
	#$fileFormat = $(file $2 | grep "CRLF" | wc -l)
	#file format check
	if file $2 | grep "CRLF"
	then
		printOutput $currentFileName "$1 file is in Windows format. Change it to Unix format"
	else
		#ojdbc6 jar
		ojdbcArtifactId=$(awk '/<artifactId>ojdbc6/,/<\/dependency>/' $pomFile | wc -l)
		ojdbcScopeProvided=$(awk '/<artifactId>ojdbc6/,/<\/dependency>/' $pomFile | grep "<scope>provided</scope>" | wc -l)
		
		ojdbc12c=$(grep "/opt/oracle/product/current11G/jdbc/lib" $2 | wc -l)
		ojdbc11=$(grep "/opt/oracle/product/current/jdbc/lib" $2 | wc -l)
		
		#ojdbcJarFile
		if [[ $ojdbc12c != $temp ]] || [[ $ojdbc11 != $temp ]]
		then
			if [[ $ojdbcScopeProvided != $temp ]]
			then
				if [[ ! -e $ojdbcJarFile ]]
				then
					printOutput $currentFileName "ojdbc6.jar is missing from /src/main/webapp/WEB-INF/lib"
				fi
			else
				if [[ -e $ojdbcJarFile ]]
				then
					printOutput $currentFileName "remove ojdbc6.jar from /src/main/webapp/WEB-INF/lib"
				fi	
			fi					
		fi
		
		#cisco.life
		lifecycleDev=$(grep -i "Dcisco.life=dev" $2 | wc -l)
		lifecycleStg=$(grep -i "Dcisco.life=stage" $2 | wc -l)
		lifecycleProd=$(grep -i "Dcisco.life=prod" $2 | wc -l)
		
		if [[  $lifecycleDev != 2 ]]
		then
			printOutput $currentFileName "set -Dcisco.life=dev in JAVA_OPTS_EXT for dev and default"
		fi	
		if [[  $lifecycleStg == $temp ]]
		then
			printOutput $currentFileName "set -Dcisco.life=stage in JAVA_OPTS_EXT for the stage"
		fi
		if [[  $lifecycleProd == $temp ]]
		then
			printOutput $currentFileName "set -Dcisco.life=prod in JAVA_OPTS_EXT for prod"
		fi	
		
		#LDAP URLs
		nonProdLDAP=$(grep -i "export OPENSHIFT_CISCO_LDAP=\"ldap://dsxstage.cisco.com:389\"" $2 | wc -l)
		prodLDAP=$(grep -i "export OPENSHIFT_CISCO_LDAP=\"ldap://dsx.cisco.com:389\"" $2 | wc -l)
		
		if [[ $nonProdLDAP != 3 ]]
		then
			printOutput $currentFileName "use export OPENSHIFT_CISCO_LDAP=\"ldap://dsxstage.cisco.com:389\" for dev, stage and default"
		fi
		
		if [[ $prodLDAP == $temp ]]
		then
			printOutput $currentFileName "use export OPENSHIFT_CISCO_LDAP=\"ldap://dsx.cisco.com:389\" for prod"
		fi
		
		#REs
		devRE=$(grep -i "\*dev" $2 | wc -l)
		stageRE=$(grep -i "\*stage|\*stg" $2 | wc -l)
		prodRE=$(grep -i "\*prod|\*prd)" $2 | wc -l)
		defaultRE=$(grep -i "\*)" $2 | wc -l)
	
		if [[ $devRE == $temp ]]
		then
			printOutput $currentFileName "use *dev) for the dev environment"
		fi
		
		if [[ $stageRE == $temp ]]
		then
			printOutput $currentFileName "use *stage|*stg) for the stage environment"
		fi
		
		if [[ $prodRE == $temp ]]
		then
			printOutput $currentFileName "use *prod|*prd) for the prod environment"
		fi
		
		if [[ $defaultRE == $temp ]]
		then
			printOutput $currentFileName "use *) for the default environment"
		fi
	fi
			
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

#jboss-web.xml file 
checkJbossWebXMLFile ()
{
	printHeaderFlag=1
	printFooterFlag=0
	currentFileName="JBOSS-WEB.XML"
	temp=0
	
	customOAMAuth=$(grep "com.cisco.jaas.CustomOAMAuthenticator" $jbossWebXMLFile | wc -l)
	customOAMBasicAuth=$(grep "com.cisco.jaas.CustomBasicAuthAuthenticator" $jbossWebXMLFile | wc -l)
	secDomain=$(grep "java:/jaas/trust-user-ldap" $jbossWebXMLFile | wc -l)
	authUser=$(grep "auth_user" $jbossWebXMLFile | wc -l)
	
	if [[ $customOAMAuth == $temp ]]
	then
			printOutput $currentFileName "<class-name>com.cisco.jaas.CustomOAMAuthenticator</class-name> tag is missing"	
	fi
	
	if [[ $customOAMBasicAuth == $temp ]]
	then
			printOutput $currentFileName "<class-name>com.cisco.jaas.CustomBasicAuthAuthenticator</class-name> tag is missing"
	fi
	
	if [[ $secDomain == $temp ]]
	then
			printOutput $currentFileName "<security-domain>java:/jaas/trust-user-ldap</security-domain> tag is missing"	
	fi
	
	if [[ $authUser == $temp ]]
	then
			printOutput $currentFileName "<param-value>auth_user</param-value> tag is missing"	
	fi
	
	if [[ $printFooterFlag == 1 ]]
	then
		echo ""
		printFooterFlag=0
	fi
}

#calling all methods

#if [[ -d $jbosseapDir ]] || [[ -d $jbossewsDir ]]
if [[ -d $packageDir ]]
then
	if [[ -e $logFile ]]
	then
		CheckLoggingPropertiesFile
	fi
	
	if [[ -e $assemblyFile ]]
	then
		CheckAssemblyFile
	fi
	
	if [[ -e $webFile ]]
	then
		checkWebFile
	fi
	
	if [[ -e $applicationWebXMLFile ]]
	then
		checkApplicationWebFile
	fi
	
#for Tomcat applications
	if [[ -e $serverFile ]] || [[ -e $contextFile ]]
	then
	    preStartFile="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_start_jbossews"
	    preRestartFile="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_restart_jbossews"
	    preStartFile_v="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_start_jbossews-2.0"
	    preRestartFile_v="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_restart_jbossews-2.0"
	
	    if [[ -e $preStartFile ]]
	    then
	    	CheckPreScripts "pre_start_jbossews.sh" $preStartFile
	    elif [[ -e $preStartFile_v ]]
	    then
	        CheckPreScripts "pre_start_jbossews.sh" $preStartFile_v
	    elif [[ ! -e $preStartFile ]] && [[ ! -e $preStartFile_v ]]
	    then
	        simpleEcho "pre_start_jbossews file does not exist"
	        echo " " 
	    else
	        echo " " 
	    fi
	
	    if [[ -e $preRestartFile ]]
	    then
	    	CheckPreScripts "pre_restart_jbossews.sh" $preRestartFile
	    elif [[ -e $preRestartFile_v ]]
	    then
	    	CheckPreScripts "pre_restart_jbossews.sh" $preRestartFile_v
		elif [[ ! -e $preRestartFile ]] && [[ ! -e $preRestartFile_v ]]
	    then
	        simpleEcho "pre_restart_jbossews file does not exist" 
	        echo " " 
	    else
	        echo " " 
	    fi
	fi
		
	if [[ -e $pomFile ]]
	then
		checkPomFileForOutputDirectory "\${project.build.directory}/../package/dependencies/jbossews/webapps" "\${project.basedir}/package/dependencies/jbossews/webapps"
	fi
	
	if [[ -e $serverFile ]]
	then
		CheckServerFile
	elif [[ ! -e $serverFile ]] && [[ -e $standaloneFile ]]
	then
		echo " " 
	else
		simpleEcho "server.xml file does not exist"
		echo " " 
	fi
	
	if [[ -e $contextFile ]]
	then
		CheckContextFile
	elif [[ ! -e $contextFile ]] && [[ -e $standaloneFile ]]
	then
		echo " " 
	else
	   simpleEcho "context.xml file does not exist" 
	   echo " " 
	fi
	
	if [[ -e $standaloneFile ]]
	then
		#pre_start and pre_restart files
		preStartFile="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_start_jbosseap"
		preRestartFile="${WORKSPACE}/package/repo/.openshift/action_hooks/pre_restart_jbosseap"
		
	    if [[ -e $preStartFile ]]
	    then
	        CheckPreScripts "pre_start_jbosseap.sh" $preStartFile
	    else
	        simpleEcho "pre_start_jbosseap file does not exist" 
	        echo " " 
	    fi
	
	    if [[ -e $preRestartFile ]]
	    then
	        CheckPreScripts "pre_restart_jbosseap.sh" $preRestartFile
	    else
	        simpleEcho "pre_restart_jbosseap file does not exist" 
	        echo " " 
	    fi
		
		if [[ -e $pomFile ]]
		then
			checkPomFileForOutputDirectory "\${project.build.directory}/../package/dependencies/jbosseap/deployments" "\${project.basedir}/package/dependencies/jbosseap/deployments"
		fi
		
		CheckStandaloneFile
		
		if [[ -e $jbossWebXMLFile ]]
		then
			checkJbossWebXMLFile
		else
			simpleEcho "jboss-web.xml file does not exist"
			echo ""
		fi
		
	elif [[ -e $serverFile  ||  -e $contextFile ]] && [[ ! -e $standaloneFile ]]
	then
	    echo " " 
	else
	    simpleEcho "standalone.xml file does not exist" 
	    echo " " 
	fi
	
	if [[ $printTitleFooterFlag == 1 ]]
	then
		simpleEcho "Please refer this wiki for more help : https://wiki.cisco.com/pages/viewpage.action?pageId=14196515"
		simpleEcho "------------------------------------------------------------------------"
		printTitleFooterFlag=0
	fi
fi