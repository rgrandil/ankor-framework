package at.irian.ankorsamples.fxrates.server;

import at.irian.ankor.annotation.AnkorWatched;
import at.irian.ankor.serialization.AnkorIgnore;
import at.irian.ankor.pattern.AnkorPatterns;
import at.irian.ankor.ref.Ref;
import at.irian.ankor.viewmodel.ViewModelBase;
import at.irian.ankor.viewmodel.watch.ExtendedList;
import at.irian.ankor.viewmodel.watch.ExtendedListWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Spiegl
 */
@SuppressWarnings("UnusedDeclaration")
public class RatesViewModel extends ViewModelBase {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RatesViewModel.class);

    @AnkorIgnore
    private final RatesRepository repository;

    @AnkorIgnore
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @AnkorWatched(diffThreshold = 30)
    private ExtendedList<Rate> rates;

    public RatesViewModel(Ref viewModelRef, RatesRepository repository) {
        super(viewModelRef);
        this.repository = repository;
        this.rates = new ExtendedListWrapper<>(new ArrayList<Rate>());
        AnkorPatterns.initViewModel(this);
    }

    public void startRatesUpdate(long updateRateSeconds) {
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // update rates
                    final List<Rate> newRates = repository.getRates();
                    LOG.info("setting new rates");
                    // Run later is necessary because we are outside of Ankor dispatching:
                    AnkorPatterns.runLater(getRef(), new Runnable() {
                        @Override
                        public void run() {
                            rates.setAll(newRates);
                        }
                    });

                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, 0, updateRateSeconds, TimeUnit.SECONDS);
    }

    public void stopRatesUpdate() {
        executorService.shutdown();
    }

    public ExtendedList<Rate> getRates() {
        return rates;
    }
}
