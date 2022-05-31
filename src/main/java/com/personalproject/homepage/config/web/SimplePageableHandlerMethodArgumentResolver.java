package com.personalproject.homepage.config.web;

import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class SimplePageableHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String DEFAULT_PAGE_PARAMETER = "page";
    // private static final String DEFAULT_SIZE_PARAMETER = "size";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 8;
    private static final String DEFAULT_SORT_PROPS = "createAt";
    private static final Direction DEFAULT_SORT_DIRECTION = Direction.DESC;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        int page = DEFAULT_PAGE;
        int size = DEFAULT_SIZE;
        Sort sort = Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPS);

        String pageStr = webRequest.getParameter(DEFAULT_PAGE_PARAMETER);

        if (pageStr == null) {
            return PageRequest.of(page, size, sort);
        }

        boolean isThrown = false;
        try {
            float pageFloat = Float.parseFloat(pageStr);
            if (pageFloat != Math.floor(pageFloat) || pageFloat < 1 || pageFloat > Integer.MAX_VALUE) {
                isThrown = true;
            }
            page = (int) pageFloat;
        } catch (NumberFormatException e) {
            isThrown = true;
        }

        if (isThrown) {
            throw new ApiException(ErrorMessage.INVALID_QUERY_STRING, "page", "1이상의 정수");
        }

        --page; // page는 0부터 시작
        return PageRequest.of(page, size, sort);
    }
}
