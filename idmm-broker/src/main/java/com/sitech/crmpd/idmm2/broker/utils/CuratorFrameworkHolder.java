package com.sitech.crmpd.idmm2.broker.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.jolokia.jvmagent.spring.SpringJolokiaAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月26日 下午8:39:33
 */
@Configuration
public class CuratorFrameworkHolder {

	/** name="{@link com.sitech.crmpd.idmm2.broker.utils.CuratorFrameworkHolder}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(CuratorFrameworkHolder.class);
	@Value("${zookeeper.curator.address}")
	private String connectString;
	@Value("${zookeeper.curator.sessionTimeoutMs:5000}")
	private int sessionTimeoutMs;
	@Value("${zookeeper.curator.connectionTimeoutMs:5000}")
	private int connectionTimeoutMs;
	@Value("${zookeeper.curator.retryTimes:3600}")
	private int retryTimes;
	@Value("${zookeeper.curator.sleepMsBetweenRetries:3000}")
	private int sleepMsBetweenRetries;
	@Resource
	private Set<String> bindSet;
	@Autowired
	private SpringJolokiaAgent jolokiaAgent;
	@Value("${tag.local:}")
	private String tag;
	private CuratorFramework client;

	/**
	 * Path 与 {@link NodeCache} 的缓存表
	 *
	 * @see LoadingCache
	 * @see CacheBuilder#newBuilder()
	 * @see CacheBuilder#weakKeys()
	 * @see CacheBuilder#weakValues()
	 * @see CacheBuilder#expireAfterAccess(long, TimeUnit)
	 * @see CacheLoader#load(Object)
	 */
	private final LoadingCache<String, NodeCache> nodeCaches = CacheBuilder.newBuilder().build(
			new CacheLoader<String, NodeCache>() {

				@Override
				public NodeCache load(String key) throws Exception {
					if (client != null) {// 避免在重连未启动的时候就获取数据，导致报错
						client.blockUntilConnected();
					}
					final NodeCache cache = new NodeCache(client, key);
					cache.start(true);
					return cache;
				}
			});

	private final LoadingCache<String, PathChildrenCache> pathChildrenCaches = CacheBuilder
			.newBuilder().build(new CacheLoader<String, PathChildrenCache>() {

				@Override
				public PathChildrenCache load(String key) throws Exception {
					if (client != null) {// 避免在重连未启动的时候就获取数据，导致报错
						client.blockUntilConnected();
					}
					final PathChildrenCache cache = new PathChildrenCache(client, key, false);
					cache.start(StartMode.BUILD_INITIAL_CACHE);
					return cache;
				}
			});

	/**
	 * @throws Exception
	 */
	@PostConstruct
	private void build() throws Exception {
		client = CuratorFrameworkFactory.newClient(connectString, sessionTimeoutMs,
				connectionTimeoutMs, new RetryNTimes(retryTimes, sleepMsBetweenRetries));
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			/**
			 * @see org.apache.curator.framework.state.ConnectionStateListener#stateChanged(org.apache.curator.framework.CuratorFramework,
			 *      org.apache.curator.framework.state.ConnectionState)
			 */
			@Override
			public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
				if (newState == ConnectionState.CONNECTED
						|| newState == ConnectionState.RECONNECTED) {
					for (final String brokerPath : bindSet) {
						register(curatorFramework, brokerPath, jolokiaAgent.getUrl());
					}
				}
			}

		});
		client.getCuratorListenable().addListener(new CuratorListener() {

			@Override
			public void eventReceived(CuratorFramework curatorFramework, CuratorEvent event)
					throws Exception {
				if (event.getType() == CuratorEventType.WATCHED) {
					if (event.getWatchedEvent().getState() == KeeperState.Expired) {
						if (client != null) {
							client.close();
						}
						nodeCaches.invalidateAll();
						pathChildrenCaches.invalidateAll();
						//build();
						LOGGER.error("zookeeeper connect Lost!will exit!");
						System.exit(-2);
					}
				}
			}

		});

		client.start();
		client.blockUntilConnected();
	}

	private void register(CuratorFramework client, String path, String data) {
		try {
			if(tag != null && !"".equals(tag))
				path = path + "-" + tag; //双机房部署， 在地址串后添加后缀
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
			client.setData().forPath(path, data.getBytes());
			LOGGER.warn("register to zookeeper [{}] => [{}]", path, data);
		} catch (final NodeExistsException e) {} catch (final Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 获取缓存数据
	 *
	 * @param path
	 * @return 缓存节点存在时，返回缓存的节点值
	 * @throws Exception
	 */
	public byte[] getData(String path) throws Exception {
		final NodeCache cache = nodeCaches.get(path);
		final ChildData data = cache.getCurrentData();
		if (data == null) {
			throw new NoNodeException(path);
		}
		return data.getData();
	}

	/**
	 * @param path
	 * @return 指定节点的所有子节点集合
	 * @throws Exception
	 */
	public List<String> getChildren(String path) throws Exception {
		final PathChildrenCache cache = pathChildrenCaches.get(path);
		final List<ChildData> childDatas = cache.getCurrentData();
		final Iterator<ChildData> iterator = childDatas.iterator();
		final List<String> children = Lists.newArrayListWithCapacity(childDatas.size());
		while (iterator.hasNext()) {
			final ChildData childData = iterator.next();
			final String absolute = childData.getPath();
			children.add(absolute.substring(absolute.lastIndexOf('/') + 1));
		}
		return children;
	}

}
