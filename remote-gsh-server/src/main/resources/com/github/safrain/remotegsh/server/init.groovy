_context = null
public class Beans {
    def context
    def propertyMissing(String name) {
        try {
            return context.getBean(name)
        } catch (e) {
            e.printStackTrace()
        }
    }
    //TODO: list beans
}
//Spring environment detection
try {
    _context = Class.forName('org.springframework.web.context.support.WebApplicationContextUtils')
            .getWebApplicationContext(_request.session.servletContext)
    beans = new Beans()
    beans.context=_context
} catch (ClassNotFoundException e) {
}

