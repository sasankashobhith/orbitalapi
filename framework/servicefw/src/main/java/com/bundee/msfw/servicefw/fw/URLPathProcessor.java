package com.bundee.msfw.servicefw.fw;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.BLModule;
import com.bundee.msfw.interfaces.blmodi.FormDataInput;
import com.bundee.msfw.interfaces.blmodi.PageHandler;
import com.bundee.msfw.interfaces.blmodi.RequestAuditor;
import com.bundee.msfw.interfaces.endpoint.BEndpoint;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class URLPathProcessor {
	public class EndpointData {
		public String uri;
		public String httpMethod;
		public String permission;
		public Class<?> reqType;
		public Method method;
		public Class<?> auditType;
		public Class<?> pageHandlerType;

		@Override
		public String toString() {
			return httpMethod + ":" + uri + ":" + method.getName() + ":"
					+ (reqType != null ? reqType.getSimpleName() : "NO reqDTO") + ":"
					+ (auditType != null ? auditType.getSimpleName() : "NO auditType") + ":" + permission;
		}
	}

	private class MethodNode {
		String httpMethod;
		Map<String, URLNode> urlNodes;

		public MethodNode(String httpMethod) {
			this.httpMethod = httpMethod;
			urlNodes = new TreeMap<String, URLNode>(String.CASE_INSENSITIVE_ORDER);
		}

		public URLNode search(String segment) {
			return urlNodes.get(segment);
		}

		URLNode addURL(String staticSegment) {
			URLNode urlNode = urlNodes.get(staticSegment);
			if (urlNode == null) {
				urlNode = new URLNode(staticSegment);
				urlNodes.put(staticSegment, urlNode);
			}
			return urlNode;
		}

		private void getAllURLs(List<String> uris) {
			uris.add(httpMethod);
			for (Map.Entry<String, URLNode> pair : urlNodes.entrySet()) {
				List<String> segments = new ArrayList<String>();
				pair.getValue().getAllURLs(segments, uris);
			}
		}
	}

	public class VariableH {
		public EndpointData epd;
		public BLModule blModule;
		public List<String> variables;
	}

	private class URLNode {
		String segment;
		Map<Integer, VariableH> count2Variables;
		VariableH leafFields;
		boolean bLeaf = false;
		Map<String, URLNode> childNodes;

		public URLNode(String segment) {
			this.segment = segment;
			this.count2Variables = new HashMap<Integer, VariableH>();
			this.childNodes = new TreeMap<String, URLNode>(String.CASE_INSENSITIVE_ORDER);
		}

		public URLNode searchStatic(String segment) {
			return childNodes.get(segment);
		}

		public VariableH matchVars(String segment, String[] segments, int idx) {
			int numVars = segments.length - idx;
			return count2Variables.get(numVars);
		}

		public boolean isLeaf() {
			return this.bLeaf;
		}

		public VariableH getVarH() {
			return leafFields;
		}

		public void setLeaf(BLModule blModule, EndpointData epd) {
			this.bLeaf = true;
			this.leafFields = new VariableH();
			this.leafFields.epd = epd;
			this.leafFields.blModule = blModule;
		}

		@Override
		public String toString() {
			return segment;
		}

		URLNode addStaticChild(String segment) {
			URLNode cnode = childNodes.get(segment);
			if (cnode == null) {
				cnode = new URLNode(segment);
				childNodes.put(segment, cnode);
			}
			return cnode;
		}

		String addAllVariables(String url, int idx, String[] segments, BLModule blModule, EndpointData epd,
				BExceptions exceptions) {
			List<String> trimmedVarList = new ArrayList<String>();
			int numVars = numVariables(idx, segments, trimmedVarList);
			if (numVars < (segments.length - idx)) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE, url
						+ " once there's a variable segment, all subsequent segemnts should be variable! " + segment);
			} else {
				if (count2Variables == null) {
					count2Variables = new HashMap<Integer, VariableH>();
				}
				if (count2Variables.containsKey(numVars)) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE,
							url + " has same number of variable segments as " + count2Variables.get(numVars));
				} else {
					VariableH var = new VariableH();
					var.epd = epd;
					var.blModule = blModule;
					var.variables = trimmedVarList;
					count2Variables.put(numVars, var);
				}
			}

			return trimmedVarList.toString();
		}

		private void getAllURLs(List<String> segments, List<String> uris) {
			segments.add(segment);
			String uri = makeURI(segments);
			if (bLeaf) {
				uris.add(uri);
			}
			for (Map.Entry<Integer, VariableH> pair : count2Variables.entrySet()) {
				VariableH var = pair.getValue();
				uris.add(uri + var.variables);
			}
			for (Map.Entry<String, URLNode> pair : childNodes.entrySet()) {
				URLNode cnode = pair.getValue();
				cnode.getAllURLs(segments, uris);
			}
		}

		private String makeURI(List<String> segments) {
			String uri = "/";
			for (String seg : segments) {
				uri += seg + "/";
			}

			return uri;
		}
	}

	private Map<String, MethodNode> methodNodes;
	private Set<Class<?>> methodParamClasses = new HashSet<Class<?>>();

	public void init(BLogger logger) {
		methodParamClasses.add(BLogger.class);
		methodParamClasses.add(BLModServices.class);
		methodParamClasses.add(RequestContext.class);
		
		methodNodes = new TreeMap<String, MethodNode>(String.CASE_INSENSITIVE_ORDER);
	}

	public void log(BLogger logger) {
		for (Map.Entry<String, MethodNode> pair : methodNodes.entrySet()) {
			List<String> uris = new ArrayList<String>();
			pair.getValue().getAllURLs(uris);
			for (String uri : uris) {
				logger.info(uri);
			}
		}
	}

	public class SearchResult {
		public VariableH varH;
		public String[] segments;
		public Integer idx;
	}

	public SearchResult search(String method, String url) throws BExceptions {
		BExceptions exceptions = new BExceptions();
		MethodNode methodNode = methodNodes.get(method);
		if (methodNode == null) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, method + " has no APIs supporting it!");
			throw exceptions;
		}

		String[] segments = url.split("/");
		URLNode urlNode = null;
		URLNode childNode = null;
		VariableH resVarH = null;
		int idx = 0;
		for (; idx < segments.length; idx++) {
			String segment = segments[idx];
			if (urlNode == null) {
				urlNode = methodNode.search(segment);
			} else {
				childNode = urlNode.searchStatic(segment);
				if (childNode == null) {
					resVarH = urlNode.matchVars(segment, segments, idx);
					urlNode = null;
					break;
				}
				urlNode = childNode;
			}
		}

		if (resVarH == null && urlNode != null && urlNode.isLeaf()) {
			resVarH = urlNode.getVarH();
		}

		SearchResult sr = new SearchResult();
		sr.varH = resVarH;
		sr.segments = segments;
		sr.idx = idx;
		return sr;
	}

	private void registerAPIURLs(BLModule blMod, Set<EndpointData> endpointData, BExceptions exceptions) {

		for (EndpointData epData : endpointData) {
			MethodNode methodNode = methodNodes.get(epData.httpMethod);
			if (methodNode == null) {
				methodNode = new MethodNode(epData.httpMethod);
				methodNodes.put(epData.httpMethod, methodNode);
			}

			String url = epData.uri;

			if (url.isBlank())
				continue;
			if (url.startsWith("/")) {
				url = url.substring(1);
			}

			String[] segments = url.split("/");
			String segment1 = segments[0];
			boolean bVariable = isVariableSegment(segment1, exceptions);
			if (bVariable) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE, url + " first segment should be static! " + segment1);
				continue;
			}

			URLNode urlNode = null;
			URLNode childNode = null;
			for (int idx = 0; idx < segments.length; idx++) {
				String segment = segments[idx];
				if (segment.isBlank()) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE, url + " segment can not be blank! " + segment);
					break;
				}
				bVariable = isVariableSegment(segment, exceptions);

				if (bVariable) {
					urlNode.addAllVariables(url, idx, segments, blMod, epData, exceptions);
					break;
				} else {
					if (urlNode == null) {
						childNode = methodNode.addURL(segment);
					} else {
						childNode = urlNode.addStaticChild(segment);
					}
					urlNode = childNode;
				}
			}

			if (!bVariable) {
				if (urlNode.isLeaf()) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE, url + " duplicate!");
				} else {
					urlNode.setLeaf(blMod, epData);
				}
			}
		}
	}

	private int numVariables(int idx, String[] segments, List<String> trimmedVarList) {
		int numVars = 0;
		for (int i = idx; i < segments.length; i++) {
			String var = segments[i];
			if (isVariableSegment(var, null)) {
				numVars++;
				var = var.substring(1);
				var = var.substring(0, var.length() - 1);
				trimmedVarList.add(var);
			}
		}
		return numVars;
	}

	private boolean isVariableSegment(String segment, BExceptions exceptions) {
		boolean bVariable = false;
		if (segment == null || segment.isBlank())
			return bVariable;
		if (segment.charAt(0) == '{') {
			if (segment.charAt(segment.length() - 1) == '}') {
				bVariable = true;
			} else {
				if (exceptions != null) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE, segment);
				}
			}
		}

		return bVariable;
	}

	public Map<String, Set<EndpointData>> registerEndpoints(BLogger logger, Set<BLModule> blMods,
			BExceptions exceptions) {
		Map<String, Set<EndpointData>> blMod2EndpointDetails = new HashMap<String, Set<EndpointData>>();
		for (BLModule blMod : blMods) {
			try {
				Set<EndpointData> endpointDataSet = new HashSet<EndpointData>();
				extractEndpointDetails(logger, blMod, endpointDataSet, exceptions);
				registerAPIURLs(blMod, endpointDataSet, exceptions);
				blMod2EndpointDetails.put(blMod.getClass().getSimpleName(), endpointDataSet);
			} catch (Exception e) {
				exceptions.add(e, FwConstants.PCodes.INTERNAL_ERROR);
			}
		}

		return blMod2EndpointDetails;
	}

	private void extractEndpointDetails(BLogger logger, BLModule blMod, Set<EndpointData> endpointData,
			BExceptions exceptions) {
		Class<?> endpointParent = blMod.getClass();
		for (Method method : endpointParent.getDeclaredMethods()) {
			if (method.isAnnotationPresent(BEndpoint.class)) {
				String epName = endpointParent.getSimpleName() + "." + method.getName();
				if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE, epName + " is not accessibe!");
				} else {
					logger.info("Registering Endpoint : " + epName);
					BEndpoint endpoint = (BEndpoint) method.getAnnotation(BEndpoint.class);
					validateMethod(epName, endpoint, method, exceptions);

					EndpointData epData = new EndpointData();
					epData.uri = endpoint.uri();
					epData.permission = endpoint.permission();
					epData.httpMethod = endpoint.httpMethod();
					epData.reqType = endpoint.reqDTOClass();
					epData.auditType = endpoint.auditorClass();
					epData.pageHandlerType = endpoint.pageHandlerClass();
					epData.method = method;
					endpointData.add(epData);
				}
			}
		}
	}

	private void validateMethod(String epName, BEndpoint endpoint, Method method, BExceptions exceptions) {
		Set<Class<?>> otherClasses = new HashSet<Class<?>>();
		validateReqType(epName, endpoint.reqDTOClass(), otherClasses, exceptions);
		validateReturnType(epName, method, exceptions);
		validateAuditType(epName, endpoint.auditorClass(), otherClasses, exceptions);
		validatePageHandlerType(epName, endpoint, otherClasses, exceptions);
		validateArguments(epName, otherClasses, method, exceptions);
		validateExceptions(epName, method, exceptions);
	}

	private void validateReqType(String epName, Class<?> reqType, Set<Class<?>> otherClasses, BExceptions exceptions) {
		if (reqType != Object.class && reqType != FormDataInput.class) {
			try {
				reqType.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						epName + " request type " + reqType.getSimpleName() + " must have default constructor");
			}
			otherClasses.add(reqType);
		}
	}

	private void validateReturnType(String epName, Method method, BExceptions exceptions) {
		Type returnType = method.getReturnType();
		if (returnType != BaseResponse.class) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE,
					epName + " return type must be " + BaseResponse.class.getSimpleName());
		}
	}

	private void validateAuditType(String epName, Class<?> auditorClass, Set<Class<?>> otherClasses, BExceptions exceptions) {
		if (auditorClass != null && auditorClass != Object.class) {
			try {
				Object auditorObj = auditorClass.getConstructor().newInstance();
				if (!(auditorObj instanceof RequestAuditor)) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE,
							epName + " auditorClass " + auditorClass.getSimpleName() + " must implement "
									+ RequestAuditor.class.getSimpleName() + " interface");
				}				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						epName + " auditorClass " + auditorClass.getSimpleName() + " must have default constructor");
			}
			otherClasses.add(auditorClass);
		}
	}

	private void validatePageHandlerType(String epName, BEndpoint endpoint, Set<Class<?>> otherClasses, BExceptions exceptions) {
		Class<?> pageHandlerClass = endpoint.pageHandlerClass();
		
		if (pageHandlerClass != null && pageHandlerClass != Object.class) {
			try {
				Object pageHandlerObj = pageHandlerClass.getConstructor().newInstance();
				if (!(pageHandlerObj instanceof PageHandler)) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE,
							epName + " pageHandlerObj " + pageHandlerClass.getSimpleName() + " must implement "
									+ PageHandler.class.getSimpleName() + " interface");
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						epName + " pageHandlerObj " + pageHandlerClass.getSimpleName() + " must have default constructor");
			}
			otherClasses.add(pageHandlerClass);
		}
	}
	
	private void validateArguments(String epName, Set<Class<?>> otherClasses, Method method, BExceptions exceptions) {
		Parameter[] parameters = method.getParameters();
		Set<Class<?>> methodParams1 = new HashSet<Class<?>>();
		Set<Class<?>> methodParams2 = new HashSet<Class<?>>();
		
		otherClasses.addAll(methodParamClasses);
		
		for (Parameter param : parameters) {
			Class<?> argClass = param.getType();
			methodParams1.add(argClass);
			methodParams2.add(argClass);
		}
		
		methodParams1.removeAll(otherClasses);
		
		if(!methodParams1.isEmpty()) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE,
					epName + " has unwanted meothd params " +  methodParams1.toString());
		}
		
		otherClasses.removeAll(methodParams2);
		
		if(!otherClasses.isEmpty()) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE,
					epName + " some of the classes are missing from meothd params " +  otherClasses.toString());
		}
	}

	private void validateExceptions(String epName, Method method, BExceptions exceptions) {
		Class<?>[] thrownExceptions = method.getExceptionTypes();
		if (thrownExceptions == null || thrownExceptions.length == 0)
			return;

		Set<String> exceptionsSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		exceptionsSet.add(BExceptions.class.getSimpleName());

		int numExcept = 0;
		for (Class<?> cls : thrownExceptions) {
			if (exceptionsSet.contains(cls.getSimpleName())) {
				numExcept++;
			}
		}
		if (thrownExceptions.length > numExcept) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, epName + " should throw one or more of "
					+ exceptionsSet.toString() + " only. Its throwing " + exceptions.toString());
		}
	}
}
