package com.ljd.double12backend.websocket;

import com.ljd.double12backend.websocket.interceptor.MyHandShakeInterceptor;
import com.ljd.double12backend.websocket.interceptor.SocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * @author ljd
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private MyHandShakeInterceptor myHandShakeInterceptor;

    @Autowired
    private SocketChannelInterceptor socketChannelInterceptor;

    @Autowired
    private WebSocketDecoratorFactory webSocketDecoratorFactory;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        // 以 /endpointWechat端点，客户端就可以通过这个端点来进行连接
        //之前是setAllowedOrigin("*")但是报错
        stompEndpointRegistry
                //断点名称，前端通过这个名称来进行连接
                .addEndpoint("/endpointWechat")
                //注册握手拦截器
                .addInterceptors(myHandShakeInterceptor)
                //允许跨域访问
                .setAllowedOriginPatterns("*")
                // 开启SockJS支持
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅服务的前缀
        registry.enableSimpleBroker("/topic","/getResponse", "/user", "/test");
        // 开启一对一发送消息
        registry.setUserDestinationPrefix("/test");
        // 设置客户端往服务端发送消息时需要加上的前缀
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(webSocketDecoratorFactory);
    }

    /**
     * 注册Channel拦截器（对进来的消息）
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketChannelInterceptor);
    }

    /**
     * 注册Channel拦截器（对出去的消息）
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketChannelInterceptor);
    }


}
