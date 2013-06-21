package at.irian.ankor.application;

import at.irian.ankor.el.BeanResolverELContext;
import at.irian.ankor.el.StandardELContext;
import com.typesafe.config.ConfigFactory;

import javax.el.ExpressionFactory;

/**
 * @author MGeiler (Manfred Geiler)
 */
public class DefaultApplication extends ELApplication {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Application.class);

    private final BeanResolver beanResolver;

    public DefaultApplication(Class<?> modelType, BeanResolver beanResolver) {
        super(modelType,
              ConfigFactory.load(),
              ExpressionFactory.newInstance(),
              new BeanResolverELContext(new StandardELContext(), beanResolver));
        this.beanResolver = beanResolver;
    }

    protected BeanResolver getBeanResolver() {
        return beanResolver;
    }


}
