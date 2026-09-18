package com.badminton.controller;

import com.badminton.exception.AttendanceException;
import com.badminton.exception.EventNotFoundException;
import com.badminton.exception.LineAuthenticationException;
import com.badminton.exception.MemberDisabledException;
import com.badminton.exception.MemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler({MemberNotFoundException.class, MemberDisabledException.class, LineAuthenticationException.class, AttendanceException.class})
    public ModelAndView handleBusinessException(RuntimeException ex, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ModelAndView handleEventNotFound(EventNotFoundException ex, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}", request.getRequestURI(), ex);
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("message", "システムエラーが発生しました。時間をおいて再度お試しください。");
        modelAndView.addObject("path", request.getRequestURI());
        return modelAndView;
    }
}
