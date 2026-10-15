package io.elasticore.base.runtime;

/**
 * Single-argument value transformer for the ingest pipeline.
 * Referenced from DSL via @fn(name) / @function(name).
 */
public interface IngestFunction {

    /**
     * Apply transformation to the incoming value.
     *
     * @param value raw input value (may be null)
     * @return transformed value (may be null)
     */
    Object apply(Object value);
}
