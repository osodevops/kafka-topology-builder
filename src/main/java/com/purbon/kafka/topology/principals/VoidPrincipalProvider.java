package com.purbon.kafka.topology.principals;

import com.purbon.kafka.topology.PrincipalProvider;
import java.io.IOException;
import java.util.List;

public class VoidPrincipalProvider implements PrincipalProvider {

  @Override
  public void configure() throws IOException {
    throw new IOException("Not implemented!!");
  }

  @Override
  public List<ServiceAccount> listServiceAccounts() throws IOException {
    throw new IOException("Not implemented!!");
  }

  @Override
  public ServiceAccount createServiceAccount(String principal, String description)
      throws IOException {
    throw new IOException("Not implemented!!");
  }

  @Override
  public void deleteServiceAccount(String principal) throws IOException {
    throw new IOException("Not implemented!!");
  }
}
