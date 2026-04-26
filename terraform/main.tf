resource "azurerm_resource_group" "rg" {
  name     = "receipt-splitter-rg"
  location = var.resource_location
}

resource "azurerm_container_registry" "acr" {
  name                = "receiptSplitterACR"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  sku                 = "Basic"
  admin_enabled       = true
}

resource "azurerm_kubernetes_cluster" "aks" {
  name                = "receipt-splitter-aks"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = "receipt-splitter"

  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_D2as_v4"
  }

  identity {
    type = "SystemAssigned"
  }
}

resource "helm_release" "ingress_nginx" {
  name       = "ingress-nginx"
  repository = "https://kubernetes.github.io/ingress-nginx"
  chart      = "ingress-nginx"
  namespace  = "ingress-nginx"

  create_namespace = true

  set = [{
    name  = "controller.service.type"
    value = "LoadBalancer"
    },
    {
      name  = "controller.service.externalTrafficPolicy"
      value = "Cluster"
    }
  ]

  depends_on = [azurerm_kubernetes_cluster.aks]
}

data "azurerm_kubernetes_cluster" "aks" {
  name                = azurerm_kubernetes_cluster.aks.name
  resource_group_name = azurerm_resource_group.rg.name
}

resource "azurerm_role_assignment" "example" {
  principal_id         = data.azurerm_kubernetes_cluster.aks.kubelet_identity[0].object_id
  role_definition_name = "AcrPull"
  scope                = azurerm_container_registry.acr.id
}

resource "azurerm_postgresql_flexible_server" "db" {
  name                   = "receipt-splitter-db"
  resource_group_name    = azurerm_resource_group.rg.name
  location               = azurerm_resource_group.rg.location
  version                = "18"
  administrator_login    = "rs_user"
  administrator_password = "RSPass1234"
  sku_name               = "B_Standard_B1ms"

  storage_mb = 32768

  backup_retention_days = 7

  public_network_access_enabled = true

  lifecycle {
    ignore_changes = [
      zone
    ]
  }
}

resource "azurerm_postgresql_flexible_server_database" "db" {
  name      = "receipt_splitter"
  server_id = azurerm_postgresql_flexible_server.db.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_all" {
  name             = "allow-aks"
  server_id        = azurerm_postgresql_flexible_server.db.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}