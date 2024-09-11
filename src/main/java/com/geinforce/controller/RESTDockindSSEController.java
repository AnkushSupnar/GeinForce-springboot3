package com.geinforce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.geinforce.util.JwtUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RESTDockindSSEController {
	
	@Autowired
	JwtUtil jwtUtil;
	
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/stream-updates")
    public SseEmitter streamUpdates(@RequestParam String jobName) {
    	// String username = jwtUtil.extractUsername(token);
//    	 if (username == null || !jwtUtil.validateToken(token,username)) {
//    	        throw new RuntimeException("Invalid token");
//    	    }
    	 
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new RuntimeException("User not authenticated");
//        }
       // String username = authentication.getName();
    	String username = jobName;
        System.out.println("Creating SSE emitter for user: " + username);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        this.emitters.put(username, emitter);
        
        emitter.onCompletion(() -> {
            System.out.println("SSE completed for user: " + username);
            this.emitters.remove(username);
        });
        emitter.onTimeout(() -> {
            System.out.println("SSE timed out for user: " + username);
            this.emitters.remove(username);
        });
        
        // Send an initial event to establish the connection
        try {
            emitter.send(SseEmitter.event().data("Connection established"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return emitter;
    }
    public void sendUpdateToClients1(String update, String username) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(update));
            } catch (IOException e) {
                emitters.remove(username);
            }
        }
    }

    public void sendUpdateToClient(String update, String username) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                System.out.println("Sending update to " + username + ": " + update);
                emitter.send(SseEmitter.event().data(update));
            } catch (IOException e) {
                System.err.println("Error sending update to " + username + ": " + e.getMessage());
                emitters.remove(username);
            }
        } else {
            System.out.println("No emitter found for user: " + username);
        }
    }
    public void closeConnection(String jobName) {
        SseEmitter emitter = emitters.get(jobName);
        if (emitter != null) {
            try {
                System.out.println("Closing SSE connection for job: " + jobName);
                emitter.send(SseEmitter.event().data("Task completed. Closing connection."));
                emitter.complete();
            } catch (IOException e) {
                System.err.println("Error closing SSE connection for job " + jobName + ": " + e.getMessage());
            } finally {
                emitters.remove(jobName);
            }
        } else {
            System.out.println("No emitter found to close for job: " + jobName);
        }
    }

}