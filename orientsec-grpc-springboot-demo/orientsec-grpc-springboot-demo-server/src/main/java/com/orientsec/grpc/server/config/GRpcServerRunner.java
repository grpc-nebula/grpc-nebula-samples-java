package com.orientsec.grpc.server.config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Order(4)
public class GRpcServerRunner implements CommandLineRunner, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(GRpcServerRunner.class);

    @Autowired
    private AbstractApplicationContext applicationContext;

    private Server server;

    private ServerBuilder<?> serverBuilder;

    @Value("${grpc.port}")
    private Integer port;

    @Value("${grpc.executor.num:2}")
    private Integer num;

    public void run(String... args) throws Exception {
        logger.info("Starting gRPC Server ...");
        serverBuilder = ServerBuilder.forPort(port);

        Collection<ServerInterceptor> globalInterceptors = (Collection) this
            .getBeanNamesByTypeWithAnnotation(GRpcGlobalInterceptor.class, ServerInterceptor.class).map((name) -> {
                return (ServerInterceptor) this.applicationContext.getBeanFactory()
                    .getBean(name, ServerInterceptor.class);
            }).collect(Collectors.toList());

        // find and register all GRpcService-enabled beans
        this.getBeanNamesByTypeWithAnnotation(GRpcService.class, BindableService.class).forEach((name) -> {
            BindableService srv = (BindableService) this.applicationContext.getBeanFactory()
                .getBean(name, BindableService.class);
            ServerServiceDefinition serviceDefinition = srv.bindService();
            GRpcService gRpcServiceAnn = (GRpcService) this.applicationContext
                .findAnnotationOnBean(name, GRpcService.class);
            serviceDefinition = this.bindInterceptors(serviceDefinition, gRpcServiceAnn, globalInterceptors);
            this.serverBuilder.executor(Executors.newFixedThreadPool(num));
            this.serverBuilder.addService(serviceDefinition);
            String serviceName = serviceDefinition.getServiceDescriptor().getName();
            logger.info("'{}' service has been registered.", srv.getClass().getName());
        });

        this.server = this.serverBuilder.build().start();
        this.applicationContext.publishEvent(this.server);
        logger.info("gRPC Server started, listening on port {}.", this.server.getPort());
        this.startDaemonAwaitThread();
    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GRpcService gRpcService,
        Collection<ServerInterceptor> globalInterceptors) {
        Stream<? extends ServerInterceptor> privateInterceptors = Stream.of(gRpcService.interceptors())
            .map((interceptorClass) -> {
                try {
                    return 0 < this.applicationContext.getBeanNamesForType(interceptorClass).length
                        ? (ServerInterceptor) this.applicationContext.getBean(interceptorClass)
                        : (ServerInterceptor) interceptorClass.newInstance();
                } catch (Exception var3) {
                    throw new BeanCreationException("Failed to create interceptor instance.", var3);
                }
            });
        List<ServerInterceptor> interceptors = (List) Stream
            .concat(gRpcService.applyGlobalInterceptors() ? globalInterceptors.stream() : Stream.empty(),
                privateInterceptors).distinct().sorted(this.serverInterceptorOrderComparator())
            .collect(Collectors.toList());
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }

    private Comparator<Object> serverInterceptorOrderComparator() {
        Function<Object, Boolean> isOrderAnnotated = (obj) -> {
            Order ann = obj instanceof Method ? (Order) AnnotationUtils.findAnnotation((Method) obj, Order.class)
                : (Order) AnnotationUtils.findAnnotation(obj.getClass(), Order.class);
            return ann != null;
        };
        return AnnotationAwareOrderComparator.INSTANCE.thenComparing((o1, o2) -> {
            boolean p1 = (Boolean) isOrderAnnotated.apply(o1);
            boolean p2 = (Boolean) isOrderAnnotated.apply(o2);
            return p1 && !p2 ? -1 : (p2 && !p1 ? 1 : 0);
        }).reversed();
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(() -> {
            try {
                this.server.awaitTermination();
            } catch (InterruptedException var2) {
                logger.error("gRPC server stopped.", var2);
                Thread.currentThread().interrupt();
            }

        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    public void destroy() throws Exception {
        logger.info("Shutting down gRPC server ...");
        Optional.ofNullable(this.server).ifPresent(Server::shutdown);
        logger.info("gRPC server stopped.");
    }

    private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<? extends Annotation> annotationType,
        Class<T> beanType) throws Exception {
        return Stream.of(this.applicationContext.getBeanNamesForType(beanType)).filter((name) -> {
            BeanDefinition beanDefinition = this.applicationContext.getBeanFactory().getBeanDefinition(name);
            Map<String, Object> beansWithAnnotation = this.applicationContext.getBeansWithAnnotation(annotationType);
            if (beansWithAnnotation.containsKey(name)) {
                return true;
            } else {
                return beanDefinition.getSource() instanceof AnnotatedTypeMetadata
                    ? ((AnnotatedTypeMetadata) AnnotatedTypeMetadata.class.cast(beanDefinition.getSource()))
                    .isAnnotated(annotationType.getName()) : false;
            }
        });
    }
}
