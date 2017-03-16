package com.sitech.crmpd.idmm2.transport;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2014年7月14日 下午11:00:35
 */
@Configuration
public class BootstrapBuilder implements ApplicationContextAware {

	@Autowired(required = false)
	@Qualifier("channelHandler")
	private ChannelHandler channelHandler;
	@Autowired(required = false)
	@Qualifier("channelOptions")
	private Map<ChannelOption<?>, ?> channelOptions;
	@Autowired(required = false)
	@Qualifier("loggerFactory")
	private InternalLoggerFactory loggerFactory;
	@Resource
	private NioEventLoopGroup bossEventLoopGroup;
	@Resource
	private NioEventLoopGroup workerEventLoopGroup;
	@Autowired(required = false)
	@Qualifier("eventExecutorGroup")
	private EventExecutorGroup eventExecutorGroup;
	@Resource
	private Map<String, List<String>> bindChildHandlers;
	@Resource
	private Map<String, List<ChannelHandler>> bindLogicHandlers;
	@Resource
	private Map<String, InetSocketAddress> bindSocketAddresses;
	@Autowired
	private ListeningExecutorService executorService;
	private ApplicationContext applicationContext;
	private final List<ListenableFuture<?>> futures = Lists.newArrayList();

	/**
	 * 根据指定的配置完成 {@link ServerBootstrap} 的创建
	 *
	 * @return {@link ServerBootstrap}对象实例
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	@Scope(SCOPE_PROTOTYPE)
	public ServerBootstrap initServerBootstrap() {
		InternalLoggerFactory.setDefaultFactory(loggerFactory == null ? new Slf4JLoggerFactory()
				: loggerFactory);

		final ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossEventLoopGroup, workerEventLoopGroup); // (2)
		bootstrap.channel(NioServerSocketChannel.class); // (3)
		if (channelOptions != null) {
			final Set<ChannelOption<?>> keySet = channelOptions.keySet();
			for (final ChannelOption option : keySet) {
				bootstrap.option(option, channelOptions.get(option));
				bootstrap.childOption(option, channelOptions.get(option));
			}
		}
		if (channelHandler != null) {
			bootstrap.handler(channelHandler);
		}
		return bootstrap;
	}

	/**
	 * @return 需要注册的键值
	 * @throws InterruptedException
	 */
	@Bean
	public Set<String> bindSet() throws InterruptedException {
		if (bindChildHandlers.isEmpty()) {
			throw new IllegalStateException("bindChildHandlers  must be not null");
		}
		final Set<String> bindSet = Sets.newHashSet();
		for (final Entry<String, InetSocketAddress> entry : bindSocketAddresses.entrySet()) {
			final String name = entry.getKey();
			final InetSocketAddress socketAddress = entry.getValue();
			final List<String> childHandlers = bindChildHandlers.get(name);

			if (childHandlers == null || childHandlers.isEmpty()) {
				throw new IllegalStateException("childHandlers which with be bind with " + name
						+ " must be not null");
			}

			final List<ChannelHandler> executorHandlers = bindLogicHandlers.get(name);
			if (executorHandlers == null || executorHandlers.isEmpty()) {
				throw new IllegalStateException("executorHandlers which with be bind with " + name
						+ " must be not null");
			}

			final ServerBootstrap bootstrap = applicationContext.getBean("initServerBootstrap",
					ServerBootstrap.class);

			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					final ChannelPipeline pipeline = ch.pipeline();
					for (final String handler : childHandlers) {
						pipeline.addLast(applicationContext.getBean(handler, ChannelHandler.class));
					}
					if (executorHandlers != null && !executorHandlers.isEmpty()) {
						if (eventExecutorGroup == null) {
							for (final ChannelHandler handler : executorHandlers) {
								pipeline.addLast(handler);
							}
						} else {
							for (final ChannelHandler handler : executorHandlers) {
								pipeline.addLast(eventExecutorGroup, handler);
							}
						}
					}
				}

			});
			final Channel channel = bootstrap.bind(socketAddress).sync().channel();
			futures.add(executorService.submit(new Runnable() {

				@Override
				public void run() {
					try {
						channel.closeFuture().sync();
					} catch (final InterruptedException e) {
						throw new IllegalStateException(e);
					}
				}

			}));

			final InetSocketAddress address = (InetSocketAddress) channel.localAddress();
			bindSet.add(name + "/" + address.getHostString() + ":" + address.getPort());
		}
		return bindSet;
	}

	/**
	 * 获取{@link #futures}属性的值
	 *
	 * @return {@link #futures}属性的值
	 */
	@Bean
	public List<ListenableFuture<?>> futures() {
		return futures;
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
