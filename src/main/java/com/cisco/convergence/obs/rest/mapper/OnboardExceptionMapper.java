package com.cisco.convergence.obs.rest.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.cisco.convergence.obs.exception.OnBoardingException;

@Provider
public class OnboardExceptionMapper implements ExceptionMapper<OnBoardingException> {

	public Response toResponse(OnBoardingException ex) {
		return Response.status(500).
		entity(ex.getMessage()).
		type(MediaType.APPLICATION_JSON).build();
	}

}
