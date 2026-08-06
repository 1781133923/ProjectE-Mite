package cpw.mods.fml.common.eventhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal stand-in for the Forge event bus. Dispatches to methods annotated with
 * @SubscribeEvent whose single parameter type matches the posted event class.
 */
public class EventBus {
    private final List<Object> listeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<HandlerMethod>> cache = new HashMap<>();

    public void register(Object listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void unregister(Object listener) {
        this.listeners.remove(listener);
    }

    public boolean post(Event event) {
        List<HandlerMethod> handlers = this.cache.computeIfAbsent(event.getClass(), this::collectHandlers);
        for (HandlerMethod handler : handlers) {
            try {
                handler.method.invoke(handler.target, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new RuntimeException("Exception thrown while handling event " + event.getClass().getSimpleName()
                        + " in " + handler.target.getClass().getName() + "." + handler.method.getName(), cause);
            }
            if (event.isCancelable() && event.isCanceled()) {
                break;
            }
        }
        return event.isCancelable() && event.isCanceled();
    }

    private List<HandlerMethod> collectHandlers(Class<?> eventClass) {
        List<HandlerMethod> result = new ArrayList<>();
        for (Object listener : this.listeners) {
            for (Method method : listener.getClass().getMethods()) {
                if (method.getAnnotation(SubscribeEvent.class) == null
                        || method.getParameterCount() != 1
                        || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                Class<?> param = method.getParameterTypes()[0];
                if (param.isAssignableFrom(eventClass)) {
                    result.add(new HandlerMethod(listener, method));
                }
            }
        }
        return result;
    }

    private record HandlerMethod(Object target, Method method) {
    }
}
