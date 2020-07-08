package com.yangjie.bitoperator.annotations;


import com.yangjie.bitoperator.codec.Codec;
import com.yangjie.bitoperator.codec.CommonCodec;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthType;
import com.yangjie.bitoperator.enums.LengthUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BitsProperty {

    int index() default -1;

    /**
     * <p>
     * when data format is a String or is a List or is a Bean, the length is calculated based on the data size，length set is invalid
     * </p>
     *
     * @return data length
     */
    int length() default 0;

    LengthType lengthType() default LengthType.SELF;

    DataFormat dataFormat() default DataFormat.NONE;

    long equals() default -1;

    LengthUnit unit() default LengthUnit.BYTE;

    Class<? extends Codec> codec() default CommonCodec.class;

    String desc() default "";


}
