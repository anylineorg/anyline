

package org.anyline.config.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.ConditionChain;

public interface ConfigChain extends Config{
	public void addConfig(Config config);
	public Config getConfig(String key);

	/**
	 * 赋值
	 * @param request
	 */
	public void setValue(HttpServletRequest request);
	public List<Config> getConfigs();
	public ConditionChain createAutoConditionChain();
}