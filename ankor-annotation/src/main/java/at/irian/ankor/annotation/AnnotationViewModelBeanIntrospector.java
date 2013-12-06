package at.irian.ankor.annotation;

import at.irian.ankor.ref.TypedRef;
import at.irian.ankor.ref.match.RefMatcherFactory;
import at.irian.ankor.ref.match.pattern.AntlrRefMatcherFactory;
import at.irian.ankor.viewmodel.metadata.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Manfred Geiler
 */
public class AnnotationViewModelBeanIntrospector implements BeanMetadataProvider {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnnotationViewModelBeanIntrospector.class);

    private static Map<Class<?>, BeanMetadata> CACHE = new ConcurrentHashMap<Class<?>, BeanMetadata>();

    private final RefMatcherFactory refMatcherFactory = new AntlrRefMatcherFactory();

    @Override
    public BeanMetadata getMetadata(Object viewModelBean) {
        if (viewModelBean == null) {
            throw new NullPointerException("viewModelBean");
        }

        Class<?> type = viewModelBean.getClass();

        return getMetadata(type);
    }

    @Override
    public BeanMetadata getMetadata(Class<?> type) {
        BeanMetadata beanMetadata = CACHE.get(type);
        if (beanMetadata != null) {
            return beanMetadata;
        }

        beanMetadata = createBeanTypeInfo(type);
        CACHE.put(type, beanMetadata);

        return beanMetadata;
    }

    private BeanMetadata createBeanTypeInfo(Class<?> type) {
        BeanMetadata beanMetadata = new BeanMetadata();
        beanMetadata = scanFields(beanMetadata, type);
        beanMetadata = scanMethods(beanMetadata, type);
        return beanMetadata;
    }

    private BeanMetadata scanFields(BeanMetadata beanMetadata, Class<?> type) {

        Collection<WatchedPropertyMetadata> watchedProperties = new ArrayList<WatchedPropertyMetadata>();
        for (Field field : type.getDeclaredFields()) {
            AnkorWatched watchedAnnotation = field.getAnnotation(AnkorWatched.class);
            if (watchedAnnotation != null) {
                watchedProperties.add(new WatchedPropertyMetadata(field.getName(),
                                                                  watchedAnnotation.diffThreshold(),
                                                                  field));
            }
        }

        beanMetadata = beanMetadata.withWatchedProperties(watchedProperties);

        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            beanMetadata = scanFields(beanMetadata, superclass);
        }

        return beanMetadata;
    }

    private BeanMetadata scanMethods(BeanMetadata beanMetadata, Class<?> type) {
        Collection<ChangeListenerMetadata> changeListeners = new ArrayList<ChangeListenerMetadata>();
        Collection<ActionListenerMetadata> actionListeners = new ArrayList<ActionListenerMetadata>();
        for (Method method : type.getDeclaredMethods()) {

            Collection<TouchedPropertyMetadata> touchedProperties = getTouchedProperties(method);

            ChangeListener changeListenerAnnotation = method.getAnnotation(ChangeListener.class);
            if (changeListenerAnnotation != null) {
                for (String pattern : changeListenerAnnotation.pattern()) {
                    ParameterMetadata[] parameters = getMethodParameters(method);
                    InvocationMetadata invocation = new InvocationMetadata(method,
                                                                           parameters,
                                                                           touchedProperties);
                    changeListeners.add(new ChangeListenerMetadata(refMatcherFactory.getRefMatcher(pattern),
                                                                   invocation));
                }
            }

            ActionListener actionListenerAnnotation = method.getAnnotation(ActionListener.class);
            if (actionListenerAnnotation != null) {
                ParameterMetadata[] parameters = getMethodParameters(method);
                InvocationMetadata invocation = new InvocationMetadata(method, parameters,
                                                                       touchedProperties);
                for (String pattern : actionListenerAnnotation.pattern()) {
                    String name = actionListenerAnnotation.name();
                    actionListeners.add(new ActionListenerMetadata(name.isEmpty() ? method.getName() : name,
                                                                   pattern.isEmpty()
                                                                   ? null
                                                                   : refMatcherFactory.getRefMatcher(pattern),
                                                                   invocation));
                }
            }

        }


        beanMetadata = beanMetadata.withChangeListeners(changeListeners);
        beanMetadata = beanMetadata.withActionListeners(actionListeners);

        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            beanMetadata = scanMethods(beanMetadata, superclass);
        }

        return beanMetadata;
    }

    private Collection<TouchedPropertyMetadata> getTouchedProperties(Method method) {
        Collection<TouchedPropertyMetadata> touchedProperties = null;

        TouchedProperties touchedPropertiesAnnotation = method.getAnnotation(TouchedProperties.class);
        if (touchedPropertiesAnnotation != null) {
            for (TouchedProperty touchedPropertyAnnotation : touchedPropertiesAnnotation.value()) {
                touchedProperties = addTo(touchedProperties,
                                             new TouchedPropertyMetadata(touchedPropertyAnnotation.value(),
                                                                     touchedPropertyAnnotation.diffHandler(),
                                                                     touchedPropertyAnnotation.diffThreshold()));
            }
        }

        TouchedProperty touchedPropertyAnnotation = method.getAnnotation(TouchedProperty.class);
        if (touchedPropertyAnnotation != null) {
            touchedProperties = addTo(touchedProperties,
                                         new TouchedPropertyMetadata(touchedPropertyAnnotation.value(),
                                                                 touchedPropertyAnnotation.diffHandler(),
                                                                 touchedPropertyAnnotation.diffThreshold()));
        }

        if (touchedProperties == null) {
            touchedProperties = Collections.emptyList();
        }

        return touchedProperties;
    }

    private static <E> Collection<E> addTo(Collection<E> coll, E e) {
        if (coll == null) {
            coll = new ArrayList<E>();
        }
        coll.add(e);
        return coll;
    }

    private ParameterMetadata[] getMethodParameters(Method method) {
        ParameterMetadata[] parameters = new ParameterMetadata[method.getParameterAnnotations().length];
        for (int i = 0; i < method.getParameterAnnotations().length; i++) {
            Annotation[] paramAnnotations = method.getParameterAnnotations()[i];
            String paramName = getParameterNameFromAnnotations(paramAnnotations);
            Class<?> type = method.getParameterTypes()[i];
            boolean backReference = TypedRef.class.isAssignableFrom(type);
            parameters[i] = new ParameterMetadata(paramName, backReference);
        }
        return parameters;
    }

    private String getParameterNameFromAnnotations(Annotation[] paramAnnotations) {
        for (Annotation paramAnnotation : paramAnnotations) {
            if (paramAnnotation instanceof Param) {
                return ((Param) paramAnnotation).value();
            }
        }
        return null;
    }


}
