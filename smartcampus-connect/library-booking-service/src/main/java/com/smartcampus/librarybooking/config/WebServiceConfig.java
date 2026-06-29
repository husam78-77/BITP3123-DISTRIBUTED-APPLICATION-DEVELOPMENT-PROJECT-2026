
package com.smartcampus.librarybooking.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * R8 — SOAP WEB SERVICE CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Configures the Spring Web Services infrastructure for the Library
 * Booking Service, including:
 *
 * <ul>
 * <li><b>MessageDispatcherServlet</b> — routes SOAP requests to
 * {@code @Endpoint} classes (mapped to {@code /ws/*}).</li>
 * <li><b>WSDL generation</b> — auto-generates the WSDL from the XSD
 * schema at {@code http://localhost:8084/ws/library.wsdl}.</li>
 * <li><b>SOAP Fault Resolver</b> — the
 * {@link SoapFaultAnnotationExceptionResolver} detects
 * {@code @SoapFault}-annotated exceptions and converts them into
 * properly formatted {@code <soap:Fault>} responses.</li>
 * </ul>
 * ═══════════════════════════════════════════════════════════════════════════
 */
@EnableWs
@Configuration
public class WebServiceConfig implements WsConfigurer {

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        // No custom interceptors needed; method required by WsConfigurer interface
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "library")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema librarySchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("LibraryPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://smartcampus.com/library");
        definition.setSchema(librarySchema);
        return definition;
    }

    @Bean
    public XsdSchema librarySchema() {
        return new SimpleXsdSchema(new ClassPathResource("library.xsd"));
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> soapCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}