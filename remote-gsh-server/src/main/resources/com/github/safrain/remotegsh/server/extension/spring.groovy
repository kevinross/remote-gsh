/*
 * Remote Groovy Shell    A servlet web application management tool
 * Copyright (c)          2013 Safrain <z.safrain@gmail.com>
 *                        All Rights Reserved
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://www.wtfpl.net/ for more details.
 */

/**
 * Spring framework support
 */
public class Beans {
    org.springframework.context.ApplicationContext context

    def propertyMissing(String name) {
        try {
            return context.getBean(name)
        } catch (e) {
            e.printStackTrace()
        }
    }

    String toString() {
        StringWriter sw = new StringWriter()
        context.getBeanDefinitionNames().each { sw.println "${it}" }
        sw.buffer.toString()
    }
}
_context = org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(_request.session.servletContext)
beans = new Beans()
beans.context = _context