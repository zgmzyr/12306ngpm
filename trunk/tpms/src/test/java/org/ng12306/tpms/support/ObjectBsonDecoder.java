package org.ng12306.tpms.support;

import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ClassResolver;

public class ObjectBsonDecoder extends ObjectDecoder {
    public ObjectBsonDecoder(ClassResolver classResolver) {
	super(classResolver);
    }
}
