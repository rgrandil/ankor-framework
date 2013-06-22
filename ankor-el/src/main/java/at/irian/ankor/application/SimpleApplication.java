package at.irian.ankor.application;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MGeiler (Manfred Geiler)
 */
public class SimpleApplication extends DefaultApplication {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SimpleApplication.class);

    protected SimpleApplication(Class<?> modelType, BeanResolver beanResolver) {
        super(modelType, beanResolver);
    }

    public static SimpleApplication create(Class<?> modelType) {
        return new SimpleApplication(modelType, new MyBeanResolver());
    }

    public SimpleApplication withBean(String beanName, Object bean) {
        ((MyBeanResolver)getBeanResolver()).beans.put(beanName, bean);
        return this;
    }

    public SimpleApplication withBeanResolver(BeanResolver beanResolver) {
        return new SimpleApplication(getModelHolder().getModelType(), beanResolver);
    }

    private static class MyBeanResolver implements BeanResolver {

        private final Map<String, Object> beans = new HashMap<String, Object>();

        @Override
        public Object resolveByName(String beanName) {
            return beans.get(beanName);
        }
    }
}
