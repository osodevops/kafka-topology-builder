package com.purbon.kafka.topology.actions;

import com.purbon.kafka.topology.PrincipalProvider;
import com.purbon.kafka.topology.principals.ServiceAccount;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseAccountsAction extends BaseAction {

  protected PrincipalProvider provider;
  protected Collection<ServiceAccount> accounts;

  public BaseAccountsAction(PrincipalProvider provider, Collection<ServiceAccount> accounts) {
    this.provider = provider;
    this.accounts = accounts;
  }

  public Collection<ServiceAccount> getPrincipals() {
    return accounts;
  }

  @Override
  protected Map<String, Object> props() {
    Map<String, Object> map = new HashMap<>();
    map.put("Operation", getClass().getName());
    map.put("Principals", accounts);
    return map;
  }
}
