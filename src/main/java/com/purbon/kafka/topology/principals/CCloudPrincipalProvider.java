package com.purbon.kafka.topology.principals;

import com.purbon.kafka.topology.PrincipalProvider;
import com.purbon.kafka.topology.TopologyBuilderConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CCloudPrincipalProvider implements PrincipalProvider {

  private CCloudCLI cCloudCLI;
  private String env;

  public CCloudPrincipalProvider(TopologyBuilderConfig config) {
    this.cCloudCLI = new CCloudCLI();
    this.env = config.getConfluentCloudEnv();
  }

  @Override
  public void configure() throws IOException {
    cCloudCLI.setEnvironment(env);
  }

  @Override
  public List<ServiceAccount> listServiceAccounts() throws IOException {
    return new ArrayList<>(cCloudCLI.serviceAccounts().values());
  }

  @Override
  public ServiceAccount createServiceAccount(String principal, String description)
      throws IOException {
    return cCloudCLI.newServiceAccount(principal, description);
  }

  @Override
  public void deleteServiceAccount(String principal) throws IOException {
    Map<String, ServiceAccount> accounts = cCloudCLI.serviceAccounts();
    ServiceAccount sa = accounts.get(principal);
    cCloudCLI.deleteServiceAccount(sa.getId());
  }
}
