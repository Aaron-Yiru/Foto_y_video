
package pe.edu.uni.demospring.interceptor; // Crear este paquete

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && modelAndView.hasView()) {

            // Obtener la sesión existente
            HttpSession session = request.getSession(false);

            if (session != null) {
                // Obtener los datos guardados en PerfilController
                String nombreUsuario = (String) session.getAttribute("nombreUsuario");
                String emailUsuario = (String) session.getAttribute("emailUsuario");

                // Inyectar en el Modelo de TODAS las vistas (incluyendo /index, /servicios, etc.)
                if (nombreUsuario != null) {
                    modelAndView.addObject("nombreUsuario", nombreUsuario);
                    modelAndView.addObject("emailUsuario", emailUsuario);
                }
            }
        }
    }
}