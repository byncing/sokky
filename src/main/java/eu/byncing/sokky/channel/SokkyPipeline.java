package eu.byncing.sokky.channel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SokkyPipeline implements ISokkyChannel.Pipeline {

    private final List<Object> objects = new ArrayList<>();

    @Override
    public void add(Object... objects) {
        this.objects.addAll(Arrays.asList(objects));
    }

    @Override
    public void remove(Class<?>... classes) {
        for (Class<?> aClass : classes) objects.removeIf(object -> aClass.equals(object.getClass()));
    }

    protected boolean invoke(Class<?> clazz, String name, Object... objects) {
        for (Object object : this.objects) {
            Class<?> aClass = object.getClass().getInterfaces()[0];
            if (aClass == null) break;
            if (clazz.equals(aClass)) {
                for (Method method : aClass.getMethods()) {
                    if (method.getName().equals(name)) {
                        try {
                            method.invoke(object, objects);
                            return true;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public boolean sync(Class<?> clazz, String name, Object... objects) {
        return invoke(clazz, name, objects);
    }

    @Override
    public void async(Class<?> clazz, String name, Object... objects) {
        new Thread(() -> invoke(clazz, name, objects)).start();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        for (Object object : objects) {
            Class<?> aClass = object.getClass().getInterfaces()[0];
            if (aClass.equals(clazz)) return (T) object;
        }
        return null;
    }
}