package com.yfckevin.bingBao.Interceptor;

import com.yfckevin.bingBao.controller.ProductController;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class LoginInterceptor implements HandlerInterceptor{
    Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handle) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
//        logger.info("Request URL: " + requestURI + ", Method: " + method);

        String internalHeader = request.getHeader("Internal-Request");
        if ("true".equals(internalHeader)) {
            return true;
        }

        final HttpSession session = request.getSession();
        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member == null) {
            logger.warn("未登入索取的資源是：{}", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/login.html");
            return false;
        }
        return true;
    }

}
