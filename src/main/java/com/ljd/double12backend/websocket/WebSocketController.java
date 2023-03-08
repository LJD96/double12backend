package com.ljd.double12backend.websocket;

import com.alibaba.fastjson.JSONObject;
import com.ljd.double12backend.websocket.model.ResponseMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ljd
 */
@Controller

public class WebSocketController {
    //声明SimpMessagingTemplate （或者使用@SendTo和@SendToUser注解），SimpMessagingTemplate可以在需要用到推送的地方如Controller，service，Component等地方
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    // 收到消息记数
    private AtomicInteger count = new AtomicInteger(0);


    /**
     * @MessageMapping 指定要接收消息的地址，类似@RequestMapping。除了注解到方法上，也可以注解到类上
     * @MessageMapping("/receive") 对应html中的  stompClient.send("/app/receive", {}, JSON.stringify({ 'name': name }));
     * 多出来的“/app"是WebSocKetConfig中定义的,如不定义，则HTML中对应改为stompClient.send("/receive")
     * @SendTo默认 消息将被发送到与传入消息相同的目的地
     * 消息的返回值是通过{@link org.springframework.messaging.converter.MessageConverter}进行转换
     * @SendTo("/topic/getResponse") 指定订阅路径，对应HTML中的stompClient.subscribe('/topic/getResponse', ……)
     * 意味将信息推送给所有订阅了"/topic/getResponse"的用户
     * @param requestMessage
     * @return
     */
    @MessageMapping("/receive")
    @SendTo("/topic/broadcast")  // topic是广播全局通讯
    public ResponseMessage receive(String requestMessage){
        log.info("receive message = {}" , requestMessage);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResponseMessage("响应消息WebSocketController receive [" + count.incrementAndGet() + "] records："+requestMessage);
        return responseMessage;
    }

    /**
     * 客户端发消息，服务端接收
     *
     * @param requestMessage
     */
    // 相当于RequestMapping
    @MessageMapping("/sendServer")
    public void sendServer(RequestMessage requestMessage) {
        log.info("sendServer 客服端发送的，不需要发回给客户端message:{}", JSONObject.toJSONString(requestMessage));
    }

    @MessageMapping("/sendServer_str")
    public void sendServer_str(String message) {
        log.info("sendServer 客服端发送的，不需要发回给客户端message:{}", message);
    }



    /**
     * 客户端发消息，大家都接收，相当于直播说话
     *
     * @param message
     * @return
     */
    @MessageMapping("/sendAllUser_str")
    @SendTo("/topic/sendTopic_str")
    public String sendAllUser_str(String message) {
        // 也可以采用template方式
        return "服务的处理后的:"+message;
    }

    @MessageMapping("/sendAllUser")
    @SendTo("/topic/sendTopic")
    public ResponseMessage sendAllUser(RequestMessage requestMessage) {
        log.info("sendTopic 请求message = {}" , JSONObject.toJSONString(requestMessage));
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResponseMessage(JSONObject.toJSONString(requestMessage));
        return responseMessage;
    }



    /**
     * 点对点用户聊天，这边需要注意，由于前端传过来json数据，所以使用@RequestBody
     * 这边需要前端开通var socket = new SockJS(host+'/myUrl' + '?token=token8888');   token为指定name
     * @param map
     */
    @MessageMapping("/sendMyUser")
    public void sendMyUser(@RequestBody Map<String, String> map) {
        log.info("sendMyUser 请求 map = {}", map);
        WebSocketSession webSocketSession = SocketManager.get(map.get("name"));
        if (webSocketSession != null) {
            log.info("sendMyUser sessionId = {}", webSocketSession.getId());

            // 生成IJSONResult对象的data数据
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setResponseMessage("响应消息WebSocketController sendMyUser  records："+map.get("message"));
            // simpMessagingTemplate.convertAndSendToUser(map.get("name"), "/queue/sendUser", IJSONResult.ok(responseMessage));

            simpMessagingTemplate.convertAndSendToUser(map.get("name"), "/queue/sendUser", JSONObject.toJSONString(responseMessage));  //ok
        }
    }

    @MessageMapping("/sendMyUser_obj")
    //@SendToUser("/user/queue/sendUser_obj")  //添加看看
    public ResponseMessage sendMyUser_obj(RequestMessage requestMessage) {
        log.info("sendMyUser message = {}" , JSONObject.toJSONString(requestMessage));
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResponseMessage("响应消息WebSocketController sendMyUser [" + count.incrementAndGet() + "] records："+JSONObject.toJSONString(requestMessage));

        return responseMessage;
    }



    // http://localhost:8080//wechatTask/websocket/index 转发到页面
    @RequestMapping(value="/wechatTask/websocket/index")
    public String websocketIndex(HttpServletRequest req){

        log.info("websocketIndex接口的 req.getRemoteHost(){}" , req.getRemoteHost());
        return "websocket/simple/websocket-index";
    }
}
