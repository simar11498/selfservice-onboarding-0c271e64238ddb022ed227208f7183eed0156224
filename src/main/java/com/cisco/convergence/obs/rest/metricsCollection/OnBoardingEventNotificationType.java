package com.cisco.convergence.obs.rest.metricsCollection;

public enum OnBoardingEventNotificationType {
	
	SSO_Go_Proactive_Link_Clicked(101),
	SSO_Go_Mobile_Link_Clicked(102),
	SSO_User_Is_Already_Onboarded_To_SNTC(103),
	Request_To_Be_Delegated_Admin_Clicked(104),
	SSO_Successfully_Verified_Contract_And_Serial_Number(105),
	SSO_Address_Verification_Email_Sent(106),
	SSO_Failed_To_Send_Address_Verification_Email(107),
	SSO_User_Clicked_On_Link_In_Verfication_Email(108),	
	SSO_Error_Occured_While_Sending_Email_To_EF_Team(109),
	SSO_Missing_Contract_Number(110),
	SSO_Missing_Serial_Number(111),
	SSO_Invalid_Contract_Number(112),
	SSO_Invalid_Serial_Number(113),
	SSO_User_Email_Domain_Blacklisted(114),	
	SSO_Link_In_Verfication_Email_Has_Expired(115),	
	SSO_DA_Nomination_Rejected_By_EF(116),
	SSO_DA_Nomination_Successful(117),
	SSO_Failed_To_Send_DA_Confirmation_Email(118),
	SSO_Successfully_Sent_DA_Confirmation_Email(119),
	SSO_Nominate_DA_Clicked(120),
	SSO_Missing_EF_Validated_Party_ID(121),	
	SSO_Request_Portal_Access_Clicked(122),
	SSO_Inactive_Contract_Number(123),
	SSO_Contract_Is_Not_Smart_Entitled(124),
	SSO_Back_Button_Clicked_On_Request_DA_Form(125),
	SSO_Submit_Button_Clicked_To_Become_DA(126),	
	SSO_User_Company_Has_DA(127),
	SSO_Email_Successfully_Sent_To_Registering_User(128),
	SSO_Email_Successfully_Sent_To_Existing_DA(129),	
	SSO_Error_In_Nominate_DA_API(130),
	SSO_Error_In_User_To_Party_Association_API(131),
	SSO_Error_In_User_To_Role_Association_API(132),
	SSO_Error_In_Enabling_Party_BSSLP_API(133),
	SSO_Error_In_User_CR_Party_Lookup(134),		
	SSO_Request_Forwarded_For_Manual_Onboarding(135),
	SSO_Error_In_Sending_Email_For_Manual_Onboarding(136),	
	SSO_User_Clicked_On_Complete_Registration(137),
	SSO_User_Registration_Successful(138),
	SSO_User_Clicked_On_Proceed_To_Portal_Link(139),
	SSO_Clicked_Support_On_Request_To_Be_Delegated_Admin_Form(140),
	SSO_Valid_Contract_Number_NotActive(141),
	SSO_GUID_MISTMATCH(142),
	SSO_ERROR_FECTHING_GUID(143),
	SSO_User_Clicked_On_Register(144),
	SSO_User_Clicked_On_SignIn(145),
	SSO_User_Clicked_On_Community_Link(146),
	SSO_User_Clicked_On_CSAM_Link(147),
	SSO_Welcome_Email_Sent_Successfully(148),
	SSO_Error_In_Sending_Welcome_Email(149),
	SSO_GUID_MATCH_SUCCESS(150),
	SSO_Valid_RetCode_In_User_To_Party_Association_API(151),
	SSO_Valid_RetCode_In_User_To_Role_Association_API(152),
	SSO_Valid_RetCode_In_Enabling_Party_BSSLP_API(153),
	SSO_Valid_RetCode_User_CR_Party_Lookup(154),
	SSO_User_Clicked_Continue_Registration_On_Overview_Page(155),
	SSO_User_Clicked_Continue_After_Email_Verified(156),
	SSO_GUID_Mismatch(157),
	SSO_Error_Fetching_GUID(158),
	SSO_GUID_Match_Success(159),
	SSO_DPL_Check_Failed(160),
	SSO_Agent_Onboarding_Successful(161),
	SSO_Agent_Onboarding_Shared_Account(162),
	SSO_Agent_Onboarding_Other(163),
	SSO_Agent_Onboarding_Need_Additional_Information(164),
	SSO_Agent_Onboarding_User_Not_From_Customer_Company(165),
	SSO_Agent_Onboarding_AAA_Cache_Sync(166),
	SSO_User_Associated_To_Another_Party(167),
	SSO_Error_In_Onboard_User(168),
	SSO_User_Installsite_PartyID(169),
	SSO_Partner_Details_Captured(170),
	SSO_Partner_Details_Not_Captured(171);
	
	
	private int value;
	
	private OnBoardingEventNotificationType(int val) {
		this.value = val;
	}
	
	public int getValue() {
		return this.value;
	}

}
