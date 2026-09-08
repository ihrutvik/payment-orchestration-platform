package com.hrutvik.payments.infrastructure;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  static final String HEADER="X-Correlation-Id";
  @Override protected void doFilter(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException{
    String supplied=request.getHeader(HEADER);
    String correlationId=supplied!=null && supplied.matches("[A-Za-z0-9._-]{8,64}")?supplied:UUID.randomUUID().toString();
    MDC.put("correlationId",correlationId); response.setHeader(HEADER,correlationId);
    try{chain.doFilter(request,response);}finally{MDC.remove("correlationId");}
  }
}
