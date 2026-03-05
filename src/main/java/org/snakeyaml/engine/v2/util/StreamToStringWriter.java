package org.snakeyaml.engine.v2.util;

import org.snakeyaml.engine.v2.api.StreamDataWriter;

import java.io.StringWriter;

/**
 * Internal helper class to support emitting to String.
 */
public class StreamToStringWriter extends StringWriter implements StreamDataWriter {

}
