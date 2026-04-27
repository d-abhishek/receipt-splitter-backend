resource "azurerm_resource_group" "rg" {
  name     = "receipt-splitter-rg"
  location = var.resource_location
}

