package org.openecomp.sdc.fe.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PluginStatusBL {

	private static Logger log = LoggerFactory.getLogger(PluginStatusBL.class.getName());
	private static  Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private CloseableHttpClient client = null;
	private PluginsConfiguration pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
	private Integer connectionTimeout;

	public PluginStatusBL() {
		this.client = HttpClients.createDefault();
	}

	public PluginStatusBL(CloseableHttpClient client) {
		this.client = client;
				
	}

	public String checkPluginsListAvailability() {
		String result = null;

		if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
			log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
		} else {
			log.debug("The value returned from getConfig is {}", pluginsConfiguration);
			connectionTimeout = pluginsConfiguration.getConnectionTimeout();

			List<Plugin> availablePluginsList = new ArrayList<>();

			pluginsConfiguration.getPluginsList().forEach(plugin -> {
				plugin.setOnline(checkPluginAvailability(plugin));

				availablePluginsList.add(plugin);
			});
			result = gson.toJson(availablePluginsList);
		}
		return result;
	}

	private boolean checkPluginAvailability(Plugin plugin) {
		boolean result = false;

		HttpHead head = new HttpHead(plugin.getPluginDiscoveryUrl());
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(connectionTimeout)
				.setConnectTimeout(connectionTimeout)
				.setConnectionRequestTimeout(connectionTimeout).build();

		head.setConfig(requestConfig);

		try (CloseableHttpResponse response = this.client.execute(head)) {
			result = response != null && response.getStatusLine().getStatusCode() == 200;
		} catch (IOException e) {
			log.debug("The plugin {} is offline", plugin.getPluginId());
		}

		return result;
	}

}
