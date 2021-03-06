package at.irian.ankor.big.modify;

import at.irian.ankor.big.BigListMetadata;

import java.util.*;

/**
 * @author Manfred Geiler
 */
public class ListToBigListDummyConverter {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListToBigListDummyConverter.class);

    public static final String SIZE_KEY = "@size";
    public static final String SUBSTITUTE_KEY = "@subst";
    public static final String INITIAL_LIST_KEY = "@init";
    public static final String CHUNK_SIZE_KEY = "@chunk";

    private final int thresholdSize;
    private final int initialSize;
    private final Object missingElementSubstitute;
    private final int chunkSize;

    public ListToBigListDummyConverter(int thresholdSize,
                                       int initialSize,
                                       Object missingElementSubstitute,
                                       int chunkSize) {
        this.thresholdSize = thresholdSize;
        this.initialSize = initialSize;
        this.missingElementSubstitute = missingElementSubstitute;
        this.chunkSize = chunkSize;
    }

    public static ListToBigListDummyConverter createFromMetadata(BigListMetadata metadata) {
        Class<?> missingElementSubstituteType = metadata.getMissingElementSubstitute();
        Object missingElementSubstitute = null;
        if (missingElementSubstituteType != null) {
            try {
                missingElementSubstitute = missingElementSubstituteType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate " + missingElementSubstituteType);
            }
        }
        return new ListToBigListDummyConverter(metadata.getThreshold(),
                                               metadata.getInitialSize(),
                                               missingElementSubstitute,
                                               metadata.getChunkSize());
    }


    @SuppressWarnings("UnnecessaryLocalVariable")
    public Collection convert(Collection actualCollection) {
        if (actualCollection == null) {
            return null;
        }

        if (actualCollection.size() < thresholdSize) {
            return actualCollection;
        }

        Map<String, Object> bigListAttributes = new LinkedHashMap<String, Object>();
        bigListAttributes.put(SIZE_KEY,  actualCollection.size());
        bigListAttributes.put(SUBSTITUTE_KEY, missingElementSubstitute);
        bigListAttributes.put(CHUNK_SIZE_KEY, chunkSize);

        if (initialSize > 0) {
            List initialList = new ArrayList(initialSize);
            Iterator iterator = actualCollection.iterator();
            for(int i = 0; i < initialSize && iterator.hasNext(); i++) {
                //noinspection unchecked
                initialList.add(iterator.next());
            }
            bigListAttributes.put(INITIAL_LIST_KEY, initialList);
        }

        List<Map<String,Object>> dummyList = Collections.singletonList(bigListAttributes);
        return dummyList;
    }

}
