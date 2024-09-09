package com.cisco.convergence.obs.model;

public enum AccessLevel {
	UNKNOWN("0"),
	GUEST("1"),
	CUSTOMER("2"),
	CBR("3"),
	EMPLOYEE("4");
	
	private String level;
	AccessLevel(String level){
		this.level = level;
	}
	public String getLevel() {
		return level;
	}
	public static AccessLevel getByName(String level){
	    for(AccessLevel prop : values()){
	      if(prop.getLevel().equals(level)){
	        return prop;
	      }
	    }

	    throw new IllegalArgumentException(level + " is not a valid PropName");
	  }
}
