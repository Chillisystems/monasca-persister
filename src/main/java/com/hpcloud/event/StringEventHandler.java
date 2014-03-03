package com.hpcloud.event;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.hpcloud.repository.VerticaMetricRepository;
import com.lmax.disruptor.EventHandler;

public class StringEventHandler implements EventHandler<StringEvent> {

    private final int ordinal;
    private final int numProcessors;
    private final int batchSize;

    VerticaMetricRepository verticaMetricRepository;

    @Inject
    public StringEventHandler(VerticaMetricRepository verticaMetricRepository,
                              @Assisted("ordinal") int ordinal,
                              @Assisted("numProcessors") int numProcessors,
                              @Assisted("batchSize") int batchSize) {

        this.verticaMetricRepository = verticaMetricRepository;
        this.ordinal = ordinal;
        this.numProcessors = numProcessors;
        this.batchSize = batchSize;

    }

    @Override
    public void onEvent(StringEvent stringEvent, long sequence, boolean b) throws Exception {

        System.out.println("Sequence number: " + sequence +
                " Ordinal: " + ordinal +
                " Event: " + stringEvent.getValue());

        if (((sequence / batchSize) % this.numProcessors) != this.ordinal) {
            return;
        }

        verticaMetricRepository.addToBatch(stringEvent.getValue());

        if (sequence % batchSize == (batchSize - 1)) {
            verticaMetricRepository.commitBatch();
        }


    }
}
