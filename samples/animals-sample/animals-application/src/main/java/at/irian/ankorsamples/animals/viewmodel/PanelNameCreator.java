package at.irian.ankorsamples.animals.viewmodel;

import at.irian.ankor.base.ObjectUtils;

/**
 * @author Thomas Spiegl
 */
public class PanelNameCreator {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PanelNameCreator.class);

    private static final int MAX_LEN = 15;
    public String createName(String name, String value) {
        if (ObjectUtils.isEmpty(value)) {
            return name;
        } else {
            if (value.length() > MAX_LEN) {
                value = value.substring(0, MAX_LEN);
            }
            return String.format("%s (%s)", name, value);
        }
    }

}
