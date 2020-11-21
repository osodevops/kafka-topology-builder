package com.purbon.kafka.topology.utils;

import com.purbon.kafka.topology.TopologyBuilderConfig;
import com.purbon.kafka.topology.principals.CCloudCLI;
import com.purbon.kafka.topology.principals.ServiceAccount;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CCloudUtils {

  private final CCloudCLI cli;
  private String env;
  private Map<String, ServiceAccount> serviceAccounts;

  public CCloudUtils(CCloudCLI cli, TopologyBuilderConfig config) {
    this.cli = cli;
    this.env = config.useConfuentCloud() ? config.getConfluentCloudEnv() : "";
    this.serviceAccounts = new HashMap<>();
  }

  public void warmup() throws IOException {
    if (serviceAccounts.isEmpty()) {
      if (env.isEmpty()) {
        throw new IOException("Environment can't be empty");
      }
      cli.setEnvironment(env);
      this.serviceAccounts = cli.serviceAccounts();
    }
  }

  public int translate(String name) {
    return serviceAccounts.containsKey(name) ? serviceAccounts.get(name).getId() : -1;
  }
}
