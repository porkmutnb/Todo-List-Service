package com.chermew.todolist.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RepositoryAspect {

    @Before("execution(* org.springframework.data.repository.CrudRepository+.save*(..)) || execution(* org.springframework.data.repository.CrudRepository+.delete*(..))")
    public void captureRepositoryWrite(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length == 0) return;

            Object entity = args[0];
            if (entity == null) return;

            String methodName = joinPoint.getSignature().getName();
            String action = methodName.startsWith("delete") ? "DELETE" : "SAVE";

            // Deduce table name from entity class using @Table annotation or class name
            String tableName = entity.getClass().getSimpleName().toLowerCase() + "s";
            if (entity.getClass().isAnnotationPresent(jakarta.persistence.Table.class)) {
                tableName = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
            }

            // Extract primary key ID dynamically
            String entityId = null;
            try {
                for (Method method : entity.getClass().getMethods()) {
                    if (method.getName().equals("getId") && method.getParameterCount() == 0) {
                        Object idVal = method.invoke(entity);
                        if (idVal != null) {
                            entityId = idVal.toString();
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore
            }

            ActivityLogContext.addActivity(tableName, action, entityId);
        } catch (Exception e) {
            // Fail-safe
        }
    }
}
