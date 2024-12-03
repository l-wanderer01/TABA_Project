//package com.example.demo.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//// 스프링 컨테이너에 Websocket을 사용하겠다고 등록하는 어노테이션
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        // websocket에서 구독 관련 URI는 /topic으로 시작한다고 선언
//        registry.enableSimpleBroker("/topic");
//        // websocket에 클라이언트가 요청을 보내는 URI, 즉 pub 관련 URI의 시작은 /app이라고 선언
//        registry.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // websocket의 엔드포인트로 websocket을 받아오기 위해선 이 URI를 통해서만 받아올 수 있다고 선언
//        // Flutter가 연결할 WebSocket 경로
//        registry.addEndpoint("/image-websocket")
//                // .setAllowedOrigins("*") // 모든 도메인 허용(권장 x)
//                .withSockJS();
//    }
//}


package com.example.taba_project.config;

import com.example.taba_project.handler.ImageWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ImageWebSocketHandler imageWebSocketHandler;

    public WebSocketConfig(ImageWebSocketHandler imageWebSocketHandler) {
        this.imageWebSocketHandler = imageWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(imageWebSocketHandler, "/image-websocket") // ws://주소:포트/image-websocket으로 요청이 들어오면 websocket 통신을 진행
                .setAllowedOrigins("*"); // 모든 ip에서 접속 가능하도록 해줌 => 이후에 특정 도메인만 허용하는 방식으로 변경할 것 (ex, "http://example.com", "https://example.com")
    }
}