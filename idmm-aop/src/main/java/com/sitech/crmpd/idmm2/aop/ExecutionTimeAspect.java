package com.sitech.crmpd.idmm2.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年6月3日 下午10:00:46
 */
public class ExecutionTimeAspect {

	/** name="{@link com.sitech.crmpd.idmm2.aop.ExecutionTimeAspect}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeAspect.class);

	/**
	 * @param point
	 * @return 方法返回值
	 * @throws Throwable
	 */
	public Object doAround(ProceedingJoinPoint point) throws Throwable {
		// 调用方法的参数
		final Object[] args = point.getArgs();
		// 调用的方法名
		final String method = point.getSignature().getName();
		final StopWatch watch = new StopWatch();
		watch.start();
		final Object result = point.proceed();
		watch.stop();  // 计时结束

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("方法[{}]参数[{}]耗时[{}]", method, Arrays.toString(args),
					watch.getLastTaskTimeMillis());
		}
		return result;
	}

}
