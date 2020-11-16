package com.purbon.kafka.topology.principals;

public class ServiceAccount {

  private int id;
  private String name;
  private String description;

  public ServiceAccount() {
    this(-1, "", "");
  }

  public ServiceAccount(int id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ServiceAccount{");
    sb.append("id=").append(id);
    sb.append(", name='").append(name).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
