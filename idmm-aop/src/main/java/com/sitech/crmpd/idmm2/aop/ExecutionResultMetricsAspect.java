package com.sitech.crmpd.idmm2.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年6月5日 上午9:09:25
 */
@Aspect
@Component
public class ExecutionResultMetricsAspect {

	@AfterReturning("@annotation(com.sitech.crmpd.idmm2.aop.ExecutionResultMetrics)")
	public void doAfterReturning() {

	}
}
