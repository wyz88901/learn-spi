package com.ywei.spi.dubbo;

import com.ywei.spi.annotations.SPI;
import com.ywei.spi.utils.Holder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author yingzhou.wei
 * @Description  模拟dubbo spi的简单实现,剥离了dubbo的部分代码. 源于dubbo spi的源码
 *
 * @date 2019/6/20
 */
public class ExtensionLoader<T> {

    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    // 接口class
    private   Class<?> type;

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

   // 分隔符
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    // 拓展类加载器的缓存
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    // 拓展类实例的缓存
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();
    // 默认接口SPI的实现类名称
    private String cachedDefaultName;

     // 异常记录
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();



    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }


    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {//1.拓展类类型非空判断
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {//2.拓展类类型只能是接口
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {//3.需要添加spi注解,否则抛异常
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" +  SPI.class.getSimpleName() + " Annotation!");
        }
        //从缓存EXTENSION_LOADERS中获取,如果不存在则新建后加入缓存
        //对于每一个拓展,都会有且只有一个ExtensionLoader与其对应
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }


    // 加载解析spi配置文件,将结果放入缓存
    private void loadFile(Map<String, Class<?>> extensionClasses ,String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                if(ci >= 0){
                                    line = line.substring(0, ci);
                                }
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        String name = null;
                                        int i = line.indexOf('=');
                                        if (i > 0) {
                                            name = line.substring(0, i).trim();  // spi key
                                            line = line.substring(i + 1).trim(); // spi value
                                        }
                                        if (line.length() > 0) {
                                            // 此处若不用javasssit，可以不指定类加载器
                                            Class<?> clazz = Class.forName(line, true, classLoader);
                                            //判断type接口是clazz类的接口
                                            if (! type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " +
                                                        type + ", class line: " + clazz.getName() + "), class "
                                                        + clazz.getName() + "is not subtype of interface.");
                                            }
                                            extensionClasses.put(name, clazz);
                                            //由于仅仅简单实现dubbo spi, 其它逻辑暂不做实现

                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            } // end of while read lines
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        System.out.println("failed to load spi file " + t.getMessage());
                    }
                } // end of while urls
            }
        } catch (Throwable t) {

        }
    }


    //获取默认拓展类
    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }


    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }


    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    // synchronized in getExtensionClasses
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1){
                    cachedDefaultName = names[0];
                }
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        return extensionClasses;
    }

    //异常提示
    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(entry.getValue().toString());
        }
        return new IllegalStateException(buf.toString());
    }
}
