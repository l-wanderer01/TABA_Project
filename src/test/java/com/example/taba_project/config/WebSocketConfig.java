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
