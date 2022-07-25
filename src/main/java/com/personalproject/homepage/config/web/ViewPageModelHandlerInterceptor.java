package com.personalproject.homepage.config.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/********************************************************************************
    뷰 템플릿에서 사용할 변수를 모델에 담아줄 interceptor
    WebConfig에서 제외할 경로가 설정 되었다.
********************************************************************************/
public class ViewPageModelHandlerInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView
    ) throws Exception {

        boolean isAdmin = false; // check user is admin

        isAdmin = request.isUserInRole("ADMIN");

        // add to model
        if (modelAndView != null) {
            modelAndView.addObject("isAdmin", isAdmin);
        }
    }

}
