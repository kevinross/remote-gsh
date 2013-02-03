//Spring environment detection
try {
	Class.forName('org.springframework.web.context.support.WebApplicationContextUtils')
	_context=org.springframework.web.context.support.WebApplicationContextUtils
			.getWebApplicationContext(_request.session.servletContext)
	getbean= { _context.getBean(it) }
	//TODO: list beans
} catch (ClassNotFoundException e) {
}

