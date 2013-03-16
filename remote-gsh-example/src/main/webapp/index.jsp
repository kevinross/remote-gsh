<%@ page import="com.github.safrain.remotegsh.example.PetStore" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="com.github.safrain.remotegsh.example.Pet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>PetStore</title>
</head>
<body>
<%
    PetStore petStore = (PetStore) WebApplicationContextUtils.getWebApplicationContext(application).getBean("petStore");
%>
<%
    if (petStore.isOpen()) {
%>
<table>
    <thead>
    <tr>
        <th>Name</th>
        <th>Price</th>
        <th></th>
    </tr>
    </thead>
    <tbody>

    <%
        for (Pet pet : petStore.getPets()) {
    %>
    <tr>
        <td><%=pet.getName()%>
        </td>
        <td><%=pet.getPrice()%>
        </td>
        <td><a href="buy?id=<%=pet.getId()%>">Buy</a></td>
    <tr>

            <%
        }
    %>

    </tbody>
</table>
<%
} else {
%>
Pet shop is closed.Come back later.
<%
    }
%>
</body>
</html>