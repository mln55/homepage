package com.personalproject.homepage.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.personalproject.homepage.config.web.ViewName;

@ControllerAdvice(annotations = Controller.class)
public class ViewExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, PageNotFoundException.class})
    public ModelAndView handleNotFound(Exception e) {
        ModelAndView mv = new ModelAndView(ViewName.ERROR_404);
        mv.setStatus(HttpStatus.NOT_FOUND);
        return mv;
    }

    @ExceptionHandler(ApiException.class)
    public ModelAndView handleApiException(ApiException ae) {
        ModelAndView mv = new ModelAndView();
        if (ae.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            mv.setViewName(ViewName.ERROR_500);
        } else {
            mv.setStatus(HttpStatus.NOT_FOUND);
            mv.setViewName(ViewName.ERROR_404);
        }
        return mv;
    }
}
