package com.personalproject.homepage.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

/**
 * 여러 메소드에서 사용 될 메소드를 모은 클래스
 */
public class AppUtils {

    /**
     * 파라미터로 들어온 String id를 Long으로 변환한다.
     * @param strId String id
     * @return parsed Long
     */
    public static Long parseParamId(String strId) {
        try {
            float floatId = Float.parseFloat(strId);
            checkArgument(floatId == Math.floor(floatId) && floatId > 0 && floatId <= Long.MAX_VALUE,
                ErrorMessage.INVALID_PARAM.getMessage("id"));
            return (long) floatId;
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorMessage.INVALID_PARAM, "id");
        }
    }

    /**
     * String으로 표현된 Boolean을 변환한다.
     * @param strBoolean String Boolean
     * @return Nullable Boolean
     */
    public static Boolean parseBoolean(String strBoolean) {
        if ("true".equals(strBoolean)) {
            return true;
        } else if ("false".equals(strBoolean)) {
            return false;
        } else {
            return null;
        }
    }

}
