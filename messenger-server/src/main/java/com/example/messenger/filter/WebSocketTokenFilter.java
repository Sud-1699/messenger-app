package com.example.messenger.filter;

import com.example.messenger.service.UserDetailsServiceImpl;
import com.example.messenger.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class WebSocketTokenFilter implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            final String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
            final String jwtToken;
            final String username;

            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return message;
            }

            jwtToken = authHeader.substring(7);
            username = jwtService.extractUsername(jwtToken);
            if (username != null && accessor.getUser() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(jwtService.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                }
            }
        }

        return message;
    }
}
