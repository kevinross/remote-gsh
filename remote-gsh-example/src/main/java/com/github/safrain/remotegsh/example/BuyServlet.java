package com.github.safrain.remotegsh.example;

import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author safrain
 */
public class BuyServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PetStore petStore = (PetStore) WebApplicationContextUtils.getWebApplicationContext(req.getSession().getServletContext()).getBean("petStore");
		int id = Integer.valueOf(req.getParameter("id"));
		Pet pet = petStore.getById(id);
		petStore.deliverPet(pet);
		resp.sendRedirect("delivered.jsp");
	}

}
