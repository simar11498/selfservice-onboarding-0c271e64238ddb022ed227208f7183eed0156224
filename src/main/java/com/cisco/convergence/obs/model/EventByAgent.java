package com.cisco.convergence.obs.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class EventByAgent {
  String user_ccoid;
  String event_name;
  String case_number;
  String comments;
  String party_id;
  
  public void setParty_id(String party_id){
	  this.party_id=party_id;	  
  }
  
  public String getParty_id() {
		return party_id;
	}
  
  public void setUser_ccoid(String user_ccoid){
	  this.user_ccoid=user_ccoid;	  
  }
  
  public String getUser_ccoid() {
		return user_ccoid;
	}
  
  public void setEvent_name(String event_name){
	  this.event_name=event_name;
	  
  }
  
  public String getEvent_name() {
		return event_name;
	}
  
  public void setCase_number(String case_number){
	  this.case_number=case_number;
	  
  }
  
  public String getCase_number() {
		return case_number;
	}
  
  public void setComments(String comments){
	  this.comments=comments;
	  
  } 

  public String getComments() {
		return comments;
	}
  
}
