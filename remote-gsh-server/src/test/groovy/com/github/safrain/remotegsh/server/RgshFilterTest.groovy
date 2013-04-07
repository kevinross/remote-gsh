package com.github.safrain.remotegsh.server

import org.easymock.Capture
import org.easymock.IAnswer
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.XmlWebApplicationContext

import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import static org.easymock.EasyMock.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull;
/**
 * RgshFilter Unit Tests
 * <p/>
 * Get welcome screen
 * Install script
 * Bootstrap Script
 * Shell
 */
public class RgshFilterTest {
    StringWriter responseWriter

    def getResponseString() {
        return responseWriter.buffer.toString()
    }

    def createFilter(Map initParameters) {
        RgshFilter filter = new RgshFilter()
        filter.init([getInitParameter: { initParameters[it] }] as FilterConfig)
        return filter
    }

    def HOST_URL = 'http://localhost/admin/rgsh'

    class MockWebApplicationContext extends ClassPathXmlApplicationContext implements WebApplicationContext {
        def servletContext;

        MockWebApplicationContext(String configLocation) {
            super(configLocation)
        }

        @Override
        ServletContext getServletContext() {
            return servletContext
        }
    }

    def mockRequest(String method, Map param, InputStream input = null) {
        MockWebApplicationContext applicationContext = new MockWebApplicationContext('classpath:com/github/safrain/remotegsh/server/testApplicationContext.xml')
        ServletContext servletContext = createNiceMock(ServletContext.class)
        expect(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).andReturn(applicationContext)
        HttpSession session = createNiceMock(HttpSession.class)
        expect(session.getServletContext()).andReturn(servletContext)
        replay(servletContext, session)
        applicationContext.servletContext = servletContext

        HttpServletRequest request = createNiceMock(HttpServletRequest.class)
        expect(request.getSession()).andReturn(session)
        expect(request.getMethod()).andReturn(method).anyTimes()
        Capture<String> paramCapture = new Capture<String>()
        String x = capture(paramCapture)
        expect(request.getParameter(x)).andAnswer({
            return param[paramCapture.getValue()]
        } as IAnswer
        ).anyTimes()

        expect(request.getInputStream()).andReturn(new DelegateServletInputStream(input)
        );
        expect(request.getRequestURL()).andReturn(new StringBuffer(HOST_URL))
        return request
    }

    def mockResponse() {
        responseWriter = new StringWriter()
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        expect(response.getWriter()).andReturn(new PrintWriter(responseWriter)).anyTimes();
        return response
    }

    @Test
    void testWelcome() {
        def request = mockRequest('GET', [:])
        def response = mockResponse()
        response.setStatus(200)
        expectLastCall()
        replay(request)
        replay(response)
        createFilter([:]).doFilter(request, response, null)
        assertEquals(RgshFilter.getResource(RgshFilter.RESOURCE_PATH + 'welcome.txt', RgshFilter.DEFAULT_CHARSET).replaceAll(RgshFilter.PLACEHOLDER_SERVER, HOST_URL) + '\n', getResponseString())
    }

    @Test
    void testInstall() {
        def request = mockRequest('GET', [r: 'install'])
        def response = mockResponse()
        response.setStatus(200)
        expectLastCall()
        replay(request)
        replay(response)
        createFilter([:]).doFilter(request, response, null)
        assertEquals(RgshFilter.getResource(RgshFilter.RESOURCE_PATH + 'install.txt', RgshFilter.DEFAULT_CHARSET).replaceAll(RgshFilter.PLACEHOLDER_SERVER, HOST_URL) + '\n', getResponseString())
    }

    @Test
    void testRgsh() {
        def request = mockRequest('GET', [r: 'rgsh'])
        def response = mockResponse()
        response.setStatus(200)
        expectLastCall()
        replay(request)
        replay(response)
        createFilter([:]).doFilter(request, response, null)
        assertEquals(RgshFilter.getResource(RgshFilter.RESOURCE_PATH + 'rgsh.txt', RgshFilter.DEFAULT_CHARSET)
                .replaceAll(RgshFilter.PLACEHOLDER_SERVER, HOST_URL)
                .replaceAll(RgshFilter.PLACEHOLDER_CHARSET, RgshFilter.DEFAULT_CHARSET) + '\n', getResponseString())
    }

    @Test
    void testStartShell() {
        def request = mockRequest('GET', [r: 'shell'])
        def response = mockResponse()
        response.setStatus(200)
        expectLastCall()
        replay(request)
        replay(response)
        def filter = createFilter([:])
        filter.doFilter(request, response, null)
        def sid = getResponseString()
        assertEquals(32, sid.length())
        def session = filter.shellSessions[sid]
        assertNotNull(session)
        def bis = new ByteArrayInputStream('print "got it"'.getBytes(RgshFilter.DEFAULT_CHARSET))
        request = mockRequest('POST', [sid: sid], bis)
        response = mockResponse()
        replay(request)
        replay(response)
        filter.doFilter(request, response, null)
        assertEquals('{"response":"got it","result":"null"}', getResponseString())
        assertEquals(session, filter.shellSessions[sid])
    }

    @Test
    void testSpringExtenstion() {
        def request = mockRequest('POST', [:], new ByteArrayInputStream('print beans.testBean.getClass().name'.getBytes(RgshFilter.DEFAULT_CHARSET)))
        def response = mockResponse()
        response.setStatus(200)
        expectLastCall()
        replay(request)
        replay(response)
        def filter = createFilter([:])
        filter.doFilter(request, response, null)
        assertEquals('java.util.HashMap', getResponseString())
    }
}
class DelegateServletInputStream extends ServletInputStream {
    DelegateServletInputStream(InputStream inputStream) {
        this.inputStream = inputStream
    }

    InputStream inputStream

    @Override
    int read() throws IOException {
        return inputStream.read()
    }
}