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

