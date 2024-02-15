package com.bundee.msfw.servicefw.security;


//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodFilter { 
	/*extends OncePerRequestFilter { 

	private static final Set<String> allowedMethods = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	static {
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.PUT);
		allowedMethods.add(HttpMethod.DELETE);
		allowedMethods.add(HttpMethod.OPTIONS);
	};
	
    @Override 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                    throws ServletException, IOException {

    	if(allowedMethods.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
    	}
    	else {
    		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
    */
} 
