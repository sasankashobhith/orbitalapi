package com.bundee.msfw.svcmod;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bundee.msfw.servicefw.fw.ServiceFramework;

@Path("")
public class SvcModResourceHandler {
	public static final String JSON_UTF8 = MediaType.APPLICATION_JSON + "; charset=UTF-8";
	
	@CrossOrigin
    @GET
    @Path("/{s:.*}")
    @Produces(JSON_UTF8)
    public Response handleGETRequest(@Context UriInfo uriInfo, @Context HttpHeaders headers) {
    	return callServerFramework("GET", uriInfo, headers, (String) null);
    }
    
	@CrossOrigin
    @POST
    @Path("/{s:.*}")
    @Consumes(JSON_UTF8)
    @Produces(JSON_UTF8)
    public Response handlePOSTRequest(@Context UriInfo uriInfo, @Context HttpHeaders headers, String body) {
    	return callServerFramework("POST", uriInfo, headers, body);
    }

	@CrossOrigin
    @PUT
    @Path("/{s:.*}")
    @Consumes(JSON_UTF8)
    @Produces(JSON_UTF8)
    public Response handlePUTRequest(@Context UriInfo uriInfo, @Context HttpHeaders headers, String body) {
    	return callServerFramework("PUT", uriInfo, headers, body);
    }

	@CrossOrigin
    @DELETE
    @Path("/{s:.*}")
    @Consumes({"*/*"})
    @Produces(JSON_UTF8)
    public Response handleDELETERequest(@Context UriInfo uriInfo, @Context HttpHeaders headers, String body) {
    	return callServerFramework("DELETE", uriInfo, headers, body);
    }
    
	@CrossOrigin
    @POST
    @Path("/{s:.*}")
    @Consumes({MediaType.MULTIPART_FORM_DATA, "multipart/mixed"})
    @Produces(JSON_UTF8)
    public Response handlePOSTRequest(@Context UriInfo uriInfo, @Context HttpHeaders headers,  MultipartBody body) {
        return callServerFramework("POST", uriInfo, headers, body);
    }
    
	@CrossOrigin
    @GET
    @Path("/{s:.*}/shutdown")
    @Produces(JSON_UTF8)
    public Response handleShutdownRequest(@Context UriInfo uriInfo, @Context HttpHeaders headers) {
    	URI reqURI = uriInfo.getRequestUri();
    	String reqHost = reqURI.getHost();
    	String respMsg = "You are not allowed send this request!";
    	
    	if(reqHost.equalsIgnoreCase("localhost") || reqHost.equalsIgnoreCase("127.0.0.1")) {
    		respMsg = "Shut down initiated!";
        	ServiceFramework.getInstance().stop();
        	Thread t = new Thread(new ShowStopper());
        	t.start();
        	return Response.ok(respMsg).build();
    	} else {
        	return Response.status(403).entity(respMsg).build();
    	}    	
    }
    
    private static Response callServerFramework(String method, UriInfo uriInfo, HttpHeaders headers, String payLoad) {
    	MultivaluedMap<String, String> qparams = uriInfo.getQueryParameters();
    	MultivaluedMap<String, String> hdrs = headers.getRequestHeaders();
    	
    	URI buri = uriInfo.getBaseUri();
    	String host = buri.getHost();
    	
    	return ServiceFramework.getInstance().process(method, host, uriInfo.getPath(), qparams, hdrs, payLoad);
    }

    private Response callServerFramework(String method, UriInfo uriInfo, HttpHeaders headers, MultipartBody body) {
    	MultivaluedMap<String, String> qparams = uriInfo.getQueryParameters();
    	MultivaluedMap<String, String> hdrs = headers.getRequestHeaders();
    	
    	URI buri = uriInfo.getBaseUri();
    	String host = buri.getHost();
    	
    	return ServiceFramework.getInstance().process(method, host, uriInfo.getPath(), qparams, hdrs, body);
    }
    
    class ShowStopper implements Runnable {

		@Override
		public void run() {
        	SvcModApplication.ctx.close();
		}
    }
}
