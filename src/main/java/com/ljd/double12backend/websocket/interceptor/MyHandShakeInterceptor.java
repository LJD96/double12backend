package com.ljd.double12backend.websocket.interceptor;

import com.ljd.double12backend.websocket.SocketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

// 初始化对象，拦截握手，发生在链接之前
@Component
public class MyHandShakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MyHandShakeInterceptor.class);
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {


        // http://localhost:8080/endpointWechat/948/dtdzvrrs/websocket?token=token8888
        // js调用：
        //        var host="http://localhost:8080";
        //        var socket = new SockJS(host+'/endpointWechat' + '?token=token8888');

        log.info("this.getClass().getCanonicalName() = {},在这里决定是否允许链接,http协议转换websoket协议进行前, 握手前Url = {}",this.getClass().getCanonicalName(),request.getURI());


        // System.out.println(this.getClass().getCanonicalName() + " 在这里决定是否允许链接,http协议转换websoket协议进行前, 握手前"+request.getURI() );


        // http协议转换websoket协议进行前，可以在这里通过session信息判断用户登录是否合法
        // request.getURI().getPath(); //   /endpointWechat/896/mdoqjqia/websocket
        // request.getURI().getHost();//localhost
        // request.getURI().string  ; //  http://localhost:8080/endpointWechat/896/mdoqjqia/websocket?token=token8888
        // request.getURI().getQuery() ;  // token=token8888
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletServerHttpRequest.getServletRequest();

            String myToken = httpRequest.getParameter("token");
            if (null != myToken && !StringUtils.isEmpty(myToken)){
                WebSocketSession webSocketSession = SocketManager.get(myToken);
                if (webSocketSession != null){
                    log.info("token = {},已经在建立链接列表，不允许重复链接",myToken);
                }else {
                    return true;

                }
            }
        }

        return false;  // 不允许建立链接

        // return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        // 握手成功后,
        System.out.println(this.getClass().getCanonicalName() + "握手成功后...");
    }
}