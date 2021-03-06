
package controllers;

import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelodao.ClienteDAO;
import modelodao.CuentaDAO;
import modelodao.OperacionDAO;
import models.Cliente;
import models.Cuenta;
import models.Operacion;

public class HomeController extends HttpServlet {
    
    private final String INICIO = "vistas/home.jsp";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String accion = "";
        String action =  request.getParameter("accion");
        String  mensajes = "";
        
        if ("deposito".equals(action)) {
            if (depositar(request, response)) {
                request.setAttribute("mensaje", "Se deposito correctamente");
                request.getRequestDispatcher(INICIO).forward(request, response);
                return;
            } 
            mensajes = "Error al depositar";
        } else if ("retiro".equals(action)) {
            if (retiro(request, response)) {
                request.setAttribute("mensaje", "Retiro exitoso");
                request.getRequestDispatcher(INICIO).forward(request, response);
                return;
            }
            mensajes = "Error o saldo insuficiente";
        }
        
        request.setAttribute("mensaje", mensajes);
        request.getRequestDispatcher(INICIO).forward(request, response);
    }

    private boolean depositar(HttpServletRequest req, HttpServletResponse res) {
        String dni = req.getParameter("dni");
        BigDecimal monto = new BigDecimal(req.getParameter("monto"));
        boolean isSave = false;
        boolean isUpdate = false;
        
        if (monto.compareTo(BigDecimal.ZERO) == 1) {
            ClienteDAO clienteDAO = new ClienteDAO();
            Cliente cliente = clienteDAO.getClienteByDNI(dni);
            CuentaDAO cuentaDAO = new CuentaDAO();
            Cuenta cuenta = cuentaDAO.getCuentaByIdCliente(cliente.getId());
            OperacionDAO operacionDAO = new OperacionDAO();
            Operacion operacion = new Operacion('D', monto, null, cuenta.getNumeroCuenta());
            isSave = operacionDAO.agregar(operacion);
            if (isSave){
                isUpdate = cuentaDAO.modificarSaldo(cuenta.getNumeroCuenta(), 
                                                    cuenta.getSaldo().add(monto));
            }
            return isSave && isUpdate;
        }
        
        return false;
    }  

    private boolean retiro(HttpServletRequest req, HttpServletResponse res) {
        String dni = req.getParameter("dni");
        BigDecimal monto = new BigDecimal(req.getParameter("monto"));
        
        ClienteDAO clienteDAO = new ClienteDAO();
        Cliente cliente = clienteDAO.getClienteByDNI(dni);
        
        CuentaDAO cuentaDAO =  new CuentaDAO();
        Cuenta cuenta = cuentaDAO.getCuentaByIdCliente(cliente.getId());
        
        boolean isSave = false;
        boolean isUpdate = false;
        if (cuenta.getSaldo().compareTo(monto) != -1) {
            OperacionDAO operacionDAO = new OperacionDAO();
            Operacion operacion = new Operacion('R', monto, null, cuenta.getNumeroCuenta());
            isSave = operacionDAO.agregar(operacion);
            if (isSave) {
               return cuentaDAO.modificarSaldo(cuenta.getNumeroCuenta(), 
                                                   cuenta.getSaldo().subtract(monto));
            } 
        } 
        return false;
    }
}
