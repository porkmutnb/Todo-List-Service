package com.chermew.todolist.aspect;

import com.chermew.todolist.annotation.LogActivity;
import com.chermew.todolist.dto.ApiResponse;
import com.chermew.todolist.dto.AuthResponse;
import com.chermew.todolist.dto.UserProfileResponse;
import com.chermew.todolist.entity.Logger;
import com.chermew.todolist.repository.LoggerRepository;
import com.chermew.todolist.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final LoggerRepository loggerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Around("@annotation(logActivity)")
    public Object logActivityAround(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        // 1. Initialize Log Context (Clear ThreadLocal first to be safe, then let RepositoryAspect collect writes)
        ActivityLogContext.clear();

        // 2. Pre-execution extraction (Path, IP, Request Body JSON)
        String path = "";
        String ipAddress = "unknown";
        String requestJson = null;

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            path = request.getRequestURI();
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
        }

        // Serialize the request body argument from Controller parameters
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) continue;
            String className = arg.getClass().getName();
            if (className.startsWith("jakarta.servlet") ||
                    className.startsWith("org.springframework.security") ||
                    className.startsWith("org.springframework.validation") ||
                    className.contains("UserPrincipal")) {
                continue;
            }
            try {
                requestJson = objectMapper.writeValueAsString(arg);
                break;
            } catch (Exception e) {
                // ignore
            }
        }

        Object result = null;
        Throwable exceptionThrown = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exceptionThrown = t;
            throw t;
        } finally {
            try {
                // 3. Post-execution extraction (Response Body JSON, HTTP Status, User ID, Repository operations)
                UUID userId = null;
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal principal) {
                    userId = principal.getId();
                }

                int httpStatus = exceptionThrown != null ? 500 : 200;
                Object body = null;
                String responseJson = null;

                if (exceptionThrown != null) {
                    responseJson = String.format("{\"success\":false,\"message\":\"%s\",\"data\":null}", exceptionThrown.getMessage());
                } else if (result != null) {
                    if (result instanceof ResponseEntity<?> responseEntity) {
                        httpStatus = responseEntity.getStatusCode().value();
                        body = responseEntity.getBody();
                    } else {
                        body = result;
                    }

                    if (body != null) {
                        try {
                            responseJson = objectMapper.writeValueAsString(body);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }

                UUID entityId = null;
                String detailsMessage = exceptionThrown != null ? exceptionThrown.getMessage() : "Success";

                if (body instanceof ApiResponse<?> apiResponse) {
                    detailsMessage = apiResponse.getMessage();
                    Object data = apiResponse.getData();
                    if (data instanceof AuthResponse authResponse) {
                        if (userId == null) {
                            userId = authResponse.getUserId();
                        }
                        entityId = authResponse.getUserId();
                    } else if (data instanceof UserProfileResponse profileResponse) {
                        if (userId == null) {
                            userId = profileResponse.getId();
                        }
                        entityId = profileResponse.getId();
                    }
                }

                if ("users".equalsIgnoreCase(logActivity.entityType()) && entityId == null) {
                    entityId = userId;
                }

                // 4. Retrieve repository activities from ThreadLocal context
                List<RepositoryActivity> activities = ActivityLogContext.getActivities();
                String affectedTablesJson = null;
                if (activities != null && !activities.isEmpty()) {
                    try {
                        affectedTablesJson = objectMapper.writeValueAsString(activities);
                    } catch (Exception e) {
                        // ignore
                    }
                }

                String details = String.format("Action: %s. Details: %s.", logActivity.action(), detailsMessage);

                // 5. Save Logger record
                Logger logger = Logger.builder()
                        .userId(userId)
                        .action(logActivity.action())
                        .entityType(logActivity.entityType())
                        .entityId(entityId)
                        .path(path)
                        .requestBody(requestJson)
                        .responseBody(responseJson)
                        .httpStatus(httpStatus)
                        .affectedTables(affectedTablesJson)
                        .details(details)
                        .ipAddress(ipAddress)
                        .build();

                loggerRepository.save(logger);
                log.info("Advanced activity log saved: Path={}, User={}, Action={}, Status={}, WritesCount={}",
                        path, userId, logActivity.action(), httpStatus, activities.size());

            } catch (Exception ex) {
                log.error("Failed to save activity log in Aspect", ex);
            } finally {
                // Always clear context to prevent memory leak
                ActivityLogContext.clear();
            }
        }
    }
}
